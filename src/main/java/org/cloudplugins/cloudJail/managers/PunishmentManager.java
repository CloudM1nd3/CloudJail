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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PunishmentManager {
    private static PunishmentManager instance;
    private final CloudJail plugin;
    private final Database db;
    private boolean offlineCounting;

    private final Map<UUID, Punishment> activePunishments = new ConcurrentHashMap<>();

    private PunishmentManager(CloudJail plugin, Database db) throws Exception {
        this.plugin = plugin;
        this.db = db;
    }

    public static void init(CloudJail plugin, Database db) throws Exception {
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
            boolean online = Bukkit.getPlayer(p.getPlayerUUID()) != null;
            if (online) continue;

            if (offlineCounting) {
                p.updateExpectedEnd();
            } else {
                p.updateRemainingTime();
                db.updateRemaining(p.getPlayerUUID(), p.getRemainingTime());
            }
        }
    }

    private void load() {
        for (Punishment p : db.loadActive().join()) {
            activePunishments.put(p.getPlayerUUID(), p);
            if (Bukkit.getPlayer(p.getPlayerUUID()) != null) {
                p.updateExpectedEnd();
            }
        }
    }

    public void handleJoin(Player player) {
        Punishment p = activePunishments.get(player.getUniqueId());
        if (p == null) return;

        if (!offlineCounting) p.updateExpectedEnd();

        if (p.isExpired(System.currentTimeMillis())) {
            release(p, Punishment.Status.EXPIRED);
        } else if (p.getPendingTeleport()) {
            Messages.SET_JAILED.send(player);
            teleportToJail(player, p.getJailName());
            p.setPendingTeleport(false);
            db.updatePendingTeleport(p.getPlayerUUID(), false);
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
                if (Bukkit.getPlayer(p.getPlayerUUID()) == null) continue;
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
        if (activePunishments.containsKey(player.getUniqueId())) {
            return false;
        }

        long now = System.currentTimeMillis();
        Punishment p = new Punishment(player.getUniqueId(), player.getName(), jailName, punisher, reason,
                now, durationMillis, Punishment.Status.ACTIVE, player.getLocation());

        Player online = player.getPlayer();
        if (online != null) {
            Messages.SET_JAILED.send(online);
            teleportToJail(online, jailName);
        } else {
            p.setPendingTeleport(true);
        }

        activePunishments.put(player.getUniqueId(), p);
        db.insertPunishment(p);
        return true;
    }

    public void release(Punishment punishment, Punishment.Status status) {
        UUID playerUUID = punishment.getPlayerUUID();
        if (activePunishments.containsKey(playerUUID)) {
            Player player = Bukkit.getPlayer(playerUUID);
            Punishment p = activePunishments.remove(playerUUID);
            teleportToLastLocation(p);
            p.setStatus(status);
            if (player != null && player.isOnline()) {
                if (status.equals(Punishment.Status.EXPIRED)) {
                    Messages.TERM_ENDED.send(player);
                } else {
                    Messages.SET_RELEASED.send(player);
                }
            }
            db.releasePunishment(p);
        }
    }

    private void teleportToJail(Player player, String jailName) {
        Jail jail = JailManager.getInstance().getJailByName(jailName);
        if (player == null || jail == null) return;

        Location jailLocation = jail.getLocation();
        if (jailLocation != null) {
            player.teleport(jailLocation);
        }
    }

    private void teleportToLastLocation(Punishment punishment) {
        Player player = Bukkit.getPlayer(punishment.getPlayerUUID());
        if (player == null) {
            return;
        }
        Location lastLocation = punishment.getLastLocation();
        if (lastLocation != null) {
            player.teleport(lastLocation);
        } else {
            player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        }
    }

    public void getHistory(UUID uuid, int page, Consumer<HistoryPage> callback) {
        db.loadHistory(uuid, page).thenAccept(result ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result)));
    }
}