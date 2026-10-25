package org.cloudplugins.cloudJail.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.database.Database;
import org.cloudplugins.cloudJail.objects.HistoryPage;
import org.cloudplugins.cloudJail.objects.Jail;
import org.cloudplugins.cloudJail.objects.Punishment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PunishmentManager {
    private static PunishmentManager instance;
    private final CloudJail plugin;
    private final Database db;
    private boolean offlineCounting;

    private final Map<UUID, Location> pendingReturns = new HashMap<>();
    private final Map<UUID, Punishment> activePunishments = new ConcurrentHashMap<>();

    private PunishmentManager(CloudJail plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    public static void init(CloudJail plugin, Database db) {
        if (instance != null) {
            throw new IllegalStateException("PunishmentManager already initialized");
        }
        instance = new PunishmentManager(plugin, db);
        instance.loadSettings();
        instance.load();
        instance.startTimer();
    }

    public static PunishmentManager getInstance() {
        return instance;
    }

    public void loadSettings() {
        boolean old = offlineCounting;
        offlineCounting = Config.getBoolean("offline-counting", false);

        if (old == offlineCounting) return;

        for (Punishment p : activePunishments.values()) {
            if (Bukkit.getPlayer(p.getPlayerUUID()) != null) continue;

            if (offlineCounting) {
                p.updateExpectedEnd();
            } else {
                p.updateRemainingTime();
                db.updateRemaining(p.getPlayerUUID(), p.getRemainingTime());
            }
        }
    }

    private void load() {
        pendingReturns.putAll(db.loadPendingReturns().join());

        for (Punishment p : db.loadActive().join()) {
            activePunishments.put(p.getPlayerUUID(), p);
        }
    }

    public void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();

        if (pendingReturns.containsKey(uuid)) {
            teleportBack(player, pendingReturns.remove(uuid));
            Messages.SET_RELEASED.send(player);
            db.removePendingReturn(uuid);
        }

        Punishment p = activePunishments.get(uuid);
        if (p == null) return;

        if (!offlineCounting) p.updateExpectedEnd();

        if (p.isExpired(System.currentTimeMillis())) {
            release(p, Punishment.Status.EXPIRED);
            return;
        }

        if (p.getPendingTeleport()) {
            Messages.SET_JAILED.send(player);
            teleportToJail(player, p.getJailName());
            p.setPendingTeleport(false);
            db.updatePendingTeleport(uuid, false);
        }
    }

    public void handleQuit(Player player) {
        Punishment p = activePunishments.get(player.getUniqueId());
        if (p == null) return;

        if (!offlineCounting) {
            p.updateRemainingTime();
            db.updateRemaining(p.getPlayerUUID(), p.getRemainingTime());
        }
    }

    public void saveAll() {
        for (Punishment p : activePunishments.values()) {
            boolean online = Bukkit.getPlayer(p.getPlayerUUID()) != null;
            if (online || offlineCounting) {
                p.updateRemainingTime();
            }
            db.updateRemaining(p.getPlayerUUID(), p.getRemainingTime());
        }
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Punishment p : activePunishments.values()) {
                boolean online = Bukkit.getPlayer(p.getPlayerUUID()) != null;
                if (!online && !offlineCounting) continue;

                if (p.isExpired(now)) {
                    release(p, Punishment.Status.EXPIRED);
                }
            }
        }, 20L, 20L);
    }

    public boolean isJailed(UUID uuid) {
        return activePunishments.containsKey(uuid);
    }

    public Punishment getActivePunishment(UUID uuid) {
        return activePunishments.get(uuid);
    }

    public boolean jail(OfflinePlayer player, String jailName, String punisher,
                        long durationMillis, String reason) {
        UUID uuid = player.getUniqueId();
        if (activePunishments.containsKey(uuid)) {
            return false;
        }

        if (pendingReturns.containsKey(uuid)) {
            pendingReturns.remove(uuid);
            db.removePendingReturn(uuid);
        }

        long now = System.currentTimeMillis();
        Punishment p = new Punishment(uuid, player.getName(), jailName, punisher, reason,
                now, durationMillis, Punishment.Status.ACTIVE, player.getLocation());

        Player online = player.getPlayer();
        if (online == null) {
            p.setPendingTeleport(true);
        }

        activePunishments.put(uuid, p);
        db.insertPunishment(p);

        if (online != null) {
            Messages.SET_JAILED.send(online);
            teleportToJail(online, jailName);
        }
        return true;
    }

    public void release(Punishment punishment, Punishment.Status status) {
        Punishment p = activePunishments.remove(punishment.getPlayerUUID());
        if (p == null) return;

        p.setStatus(status);
        Player player = Bukkit.getPlayer(p.getPlayerUUID());

        if (player != null) {
            teleportBack(player, p.getLastLocation());
            if (status == Punishment.Status.EXPIRED) {
                Messages.TERM_ENDED.send(player);
            } else {
                Messages.SET_RELEASED.send(player);
            }
        } else {
            pendingReturns.put(p.getPlayerUUID(), p.getLastLocation());
        }

        db.releasePunishment(p, player != null);
    }

    private void teleportToJail(Player player, String jailName) {
        Jail jail = JailManager.getInstance().getJailByName(jailName);
        if (jail == null) return;

        Location jailLocation = jail.getLocation();
        if (jailLocation != null) {
            player.teleport(jailLocation);
        }
    }

    private void teleportBack(Player player, Location location) {
        player.teleport(location != null
                ? location
                : Bukkit.getWorlds().getFirst().getSpawnLocation());
    }

    public void getHistory(UUID uuid, int page, Consumer<HistoryPage> callback) {
        db.loadHistory(uuid, page).thenAccept(result ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result)));
    }
}