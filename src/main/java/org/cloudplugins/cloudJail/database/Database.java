package org.cloudplugins.cloudJail.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.objects.Punishment;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Database(File file) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS punishments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, player_uuid TEXT NOT NULL, player_name TEXT NOT NULL, " +
                    "jail_name TEXT NOT NULL, punisher TEXT NOT NULL, reason TEXT, " +
                    "start_time INTEGER NOT NULL, end_time INTEGER NOT NULL, status TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS last_locations (" +
                    "player_uuid TEXT PRIMARY KEY, world_uuid TEXT, world_name TEXT, " +
                    "x REAL, y REAL, z REAL, yaw REAL, pitch REAL)");

            st.execute("CREATE INDEX IF NOT EXISTS idx_player ON punishments(player_uuid)");
        }
    }

    public void insertPunishment(Punishment p) {
        executor.execute(() -> {
            insert(p);
            saveLastLocation(p.getPlayerUUID(), p.getLastLocation());
        });
    }

    public void releasePunishment(Punishment p) {
        executor.execute(() -> {
            updateStatus(p);
            deleteLastLocation(p.getPlayerUUID());
        });
    }


    private void insert(Punishment p) {
        executor.execute(() -> {
            String sql = "INSERT INTO punishments (player_uuid, player_name, jail_name, punisher, reason, " +
                    "start_time, end_time, status) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, p.getPlayerUUID().toString());
                ps.setString(2, p.getPlayerName());
                ps.setString(3, p.getJailName());
                ps.setString(4, p.getPunisher());
                ps.setString(5, p.getReason());
                ps.setLong(6, p.getStartTime());
                ps.setLong(7, p.getEndTime());
                ps.setString(8, p.getStatus().name());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveLastLocation(UUID uuid, Location location) {
        executor.execute(() -> {
            String sql = "INSERT OR REPLACE INTO last_locations VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, location.getWorld().getUID().toString());
                ps.setString(3, location.getWorld().getName());
                ps.setDouble(4, location.x());
                ps.setDouble(5, location.y());
                ps.setDouble(6, location.z());
                ps.setDouble(7, location.getYaw());
                ps.setDouble(8, location.getPitch());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateStatus(Punishment p) {
        executor.execute(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE punishments SET status = ? WHERE player_uuid = ? AND status = 'ACTIVE'")) {
                ps.setString(1, p.getStatus().name());
                ps.setString(2, p.getPlayerUUID().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteLastLocation(UUID uuid) {
        executor.execute(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM last_locations WHERE player_uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<Punishment>> loadActive() {
        return query("SELECT * FROM punishments WHERE status = 'ACTIVE'", null);
    }

    public CompletableFuture<List<Punishment>> loadHistory(UUID uuid) {
        return query("SELECT * FROM punishments WHERE player_uuid = ? ORDER BY start_time DESC LIMIT 50", uuid);
    }

    private CompletableFuture<List<Punishment>> query(String sql, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Punishment> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                if (uuid != null) ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    list.add(new Punishment(
                            playerUUID,
                            rs.getString("player_name"),
                            rs.getString("jail_name"),
                            rs.getString("punisher"),
                            rs.getString("reason"),
                            rs.getLong("start_time"),
                            rs.getLong("end_time"),
                            Punishment.Status.valueOf(rs.getString("status")),
                            loadLastLocation(playerUUID)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }, executor);
    }

    private Location loadLastLocation(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM last_locations WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            World world = Bukkit.getWorld(UUID.fromString(rs.getString("world_uuid")));
            if (world == null) {
                world = Bukkit.getWorld(rs.getString("world_name"));
            }
            if (world == null) {
                return null;
            }

            return new Location(world,
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    (float) rs.getDouble("yaw"),
                    (float) rs.getDouble("pitch"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        executor.shutdown();
        try { connection.close(); } catch (SQLException ignored) {}
    }
}
