package org.cloudplugins.cloudJail.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.HistoryPage;
import org.cloudplugins.cloudJail.objects.Punishment;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Database {
    private final Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Database(File file) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS punishments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "player_uuid TEXT NOT NULL, " +
                    "player_name TEXT NOT NULL, " +
                    "jail_name TEXT NOT NULL, " +
                    "punisher TEXT NOT NULL, " +
                    "reason TEXT, " +
                    "start_time INTEGER NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "remainingMillis INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "pending_teleport INTEGER NOT NULL DEFAULT 0)");

            st.execute("CREATE TABLE IF NOT EXISTS last_locations (" +
                    "player_uuid TEXT PRIMARY KEY, " +
                    "world_uuid TEXT, world_name TEXT, " +
                    "x REAL, y REAL, z REAL, yaw REAL, pitch REAL, " +
                    "pending_return INTEGER NOT NULL DEFAULT 0)");

            st.execute("CREATE INDEX IF NOT EXISTS idx_player_status " +
                    "ON punishments(player_uuid, status, id)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active " +
                    "ON punishments(player_uuid) WHERE status = 'ACTIVE'");
        }
    }

    public void insertPunishment(Punishment p) {
        executor.execute(() -> {
            insert(p);
            saveLastLocation(p.getPlayerUUID(), p.getLastLocation());
        });
    }

    public void releasePunishment(Punishment p, boolean playerOnline) {
        executor.execute(() -> {
            updateStatus(p);
            if (playerOnline) {
                deleteLastLocation(p.getPlayerUUID());
            } else {
                markPendingReturn(p.getPlayerUUID());
            }
        });
    }

    public void removePendingReturn(UUID uuid) {
        executor.execute(() -> deleteLastLocation(uuid));
    }

    public void updatePendingTeleport(UUID uuid, boolean pending) {
        executor.execute(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE punishments SET pending_teleport = ? WHERE player_uuid = ? AND status = 'ACTIVE'")) {
                ps.setInt(1, pending ? 1 : 0);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateRemaining(UUID uuid, long remainingMillis) {
        executor.execute(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE punishments SET remainingMillis = ? WHERE player_uuid = ? AND status = 'ACTIVE'")) {
                ps.setLong(1, remainingMillis);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<Punishment>> loadActive() {
        return CompletableFuture.supplyAsync(() -> {
            List<Punishment> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM punishments WHERE status = 'ACTIVE'")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(readPunishment(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }, executor);
    }

    public CompletableFuture<Map<UUID, Location>> loadPendingReturns() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Location> map = new HashMap<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM last_locations WHERE pending_return = 1")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(UUID.fromString(rs.getString("player_uuid")), readLocation(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return map;
        }, executor);
    }

    public CompletableFuture<HistoryPage> loadHistory(UUID uuid, int page) {
        return CompletableFuture.supplyAsync(() -> {
            int total = 0;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM punishments WHERE player_uuid = ? AND status IN ('EXPIRED', 'RELEASED')")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total = rs.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            List<Punishment> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM punishments WHERE player_uuid = ? AND status IN ('EXPIRED', 'RELEASED') " +
                            "ORDER BY id DESC LIMIT ? OFFSET ?")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, 5);
                ps.setInt(3, (page - 1) * 5);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(readPunishment(rs));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Punishment active = PunishmentManager.getInstance().getActivePunishment(uuid);
            int totalPages = Math.max(1, (total + 5 - 1) / 5);
            return new HistoryPage(active, list, page, totalPages, total);
        }, executor);
    }

    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        try { connection.close(); } catch (SQLException ignored) {}
    }

    private void insert(Punishment p) {
        String sql = "INSERT INTO punishments (player_uuid, player_name, jail_name, punisher, reason, " +
                "start_time, duration, remainingMillis, status, pending_teleport) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getPlayerUUID().toString());
            ps.setString(2, p.getPlayerName());
            ps.setString(3, p.getJailName());
            ps.setString(4, p.getPunisher());
            ps.setString(5, p.getReason());
            ps.setLong(6, p.getStartTime());
            ps.setLong(7, p.getDuration());
            ps.setLong(8, p.getRemainingMillis());
            ps.setString(9, p.getStatus().name());
            ps.setInt(10, p.getPendingTeleport() ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveLastLocation(UUID uuid, Location location) {
        if (location == null || location.getWorld() == null) return;

        String sql = "INSERT OR REPLACE INTO last_locations " +
                "(player_uuid, world_uuid, world_name, x, y, z, yaw, pitch, pending_return) " +
                "VALUES (?,?,?,?,?,?,?,?,0)";
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
    }

    private void updateStatus(Punishment p) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE punishments SET status = ? WHERE player_uuid = ? AND status = 'ACTIVE'")) {
            ps.setString(1, p.getStatus().name());
            ps.setString(2, p.getPlayerUUID().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteLastLocation(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM last_locations WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markPendingReturn(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE last_locations SET pending_return = 1 WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Punishment readPunishment(ResultSet rs) throws SQLException {
        UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
        Punishment.Status status = Punishment.Status.valueOf(rs.getString("status"));
        Location last = status == Punishment.Status.ACTIVE ? loadLastLocation(playerUUID) : null;

        Punishment p = new Punishment(
                playerUUID,
                rs.getString("player_name"),
                rs.getString("jail_name"),
                rs.getString("punisher"),
                rs.getString("reason"),
                rs.getLong("start_time"),
                rs.getLong("duration"),
                rs.getLong("remainingMillis"),
                status,
                last);
        p.setId(rs.getInt("id"));
        p.setPendingTeleport(rs.getInt("pending_teleport") == 1);
        return p;
    }

    private Location loadLastLocation(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM last_locations WHERE player_uuid = ? AND pending_return = 0")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? readLocation(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Location readLocation(ResultSet rs) throws SQLException {
        World world = Bukkit.getWorld(UUID.fromString(rs.getString("world_uuid")));
        if (world == null) {
            world = Bukkit.getWorld(rs.getString("world_name"));
        }
        if (world == null) return null;

        return new Location(world,
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                (float) rs.getDouble("yaw"),
                (float) rs.getDouble("pitch"));
    }
}