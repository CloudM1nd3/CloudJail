package org.cloudplugins.cloudJail.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.database.Database;
import org.cloudplugins.cloudJail.objects.Jail;
import org.cloudplugins.cloudJail.objects.Punishment;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PunishmentManager {
    private static PunishmentManager instance;
    private final CloudJail plugin;
    private final Database db;

    private final Map<UUID, Punishment> activePunishments = new ConcurrentHashMap<>();

    private PunishmentManager(CloudJail plugin) throws Exception {
        this.plugin = plugin;
        this.db = new Database(new File(plugin.getDataFolder(), "punishments.db"));
    }

    public static void init(CloudJail plugin) throws Exception {
        if (instance != null) {
            throw new IllegalStateException("PunishmentManager already initialized");
        }
        instance = new PunishmentManager(plugin);
        instance.load();
        instance.startTimer();
    }

    public static PunishmentManager getInstance() {
        return instance;
    }

    private void load() {
        for (Punishment p : db.loadActive().join()) {
            activePunishments.put(p.getPlayerUUID(), p);
        }
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Punishment p : activePunishments.values()) {
                if (p.isExpired()) {
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

    public boolean jail(Player player, String jailName, String punisher,
                        String reason, long durationMillis) {
        if (activePunishments.containsKey(player.getUniqueId())) {
            return false;
        }

        long now = System.currentTimeMillis();
        Punishment p = new Punishment(player.getUniqueId(), player.getName(), jailName, punisher, reason,
                now, now + durationMillis, Punishment.Status.ACTIVE, player.getLocation());

        teleportToJail(player, jailName);
        activePunishments.put(player.getUniqueId(), p);
        db.insertPunishment(p);
        return true;
    }

    public boolean release(Punishment punishment, Punishment.Status status) {
        UUID playerUUID = punishment.getPlayerUUID();
        if (activePunishments.containsKey(playerUUID)) {
            Punishment p = activePunishments.remove(playerUUID);
            teleportToLastLocation(p);
            p.setStatus(status);
            db.releasePunishment(p);
            return true;
        } else {
            return false;
        }
    }

    private void teleportToJail(Player player, String jailName){
        Jail jail = JailManager.getInstance().getJailByName(jailName);
        if (player == null || jail == null) return;

        Location jailLocation = jail.getLocation();
        if (jailLocation != null) {
            player.teleport(jailLocation);
        }
    }

    private void teleportToLastLocation(Punishment punishment){
        Player player = Bukkit.getPlayer(punishment.getPlayerUUID());
        if(player == null){
            return;
        }
        Location lastLocation = punishment.getLastLocation();
        if (lastLocation != null) {
            player.teleport(lastLocation);
        } else {
            player.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        }
    }

    public void getHistory(UUID uuid, Consumer<List<Punishment>> callback) {
        db.loadHistory(uuid).thenAccept(list ->
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(list)));
    }

    public void shutdown() {
        db.close();
        instance = null;
    }
}
