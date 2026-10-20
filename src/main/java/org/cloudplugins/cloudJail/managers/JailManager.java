package org.cloudplugins.cloudJail.managers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.objects.Jail;

import java.util.HashMap;
import java.util.UUID;

public class JailManager {
    private static JailManager instance;
    private final CloudJail plugin;

    private HashMap<String, Jail> jailHashMap = new HashMap<>();

    private JailManager(CloudJail plugin) {
        this.plugin = plugin;
    }

    public static void init(CloudJail plugin) {
        if (instance != null) {
            throw new IllegalStateException("JailManager already initialized");
        }
        instance = new JailManager(plugin);
        instance.reload();
    }

    public static JailManager getInstance() {
        return instance;
    }

    private void addJail(String name, Jail jail){
        jailHashMap.put(name.toLowerCase(), jail);
    }

    private void removeJail(String name){
        jailHashMap.remove(name.toLowerCase());
    }

    public Jail getJailByName(String name){
        return jailHashMap.get(name.toLowerCase());
    }

    public boolean hasJail(String name){
        return jailHashMap.containsKey(name.toLowerCase());
    }

    public void reload(){
        jailHashMap.clear();

        ConfigurationSection jailsSection = Config.getOrCreateSection("Jails");
        if (!isSectionEmpty(jailsSection)) {
            jailsSection = Config.getSection("Jails");
            loadJails(jailsSection);
        }

        Config.save();
    }

    private boolean isSectionEmpty(ConfigurationSection section) {
        return section == null || section.getKeys(false).isEmpty();
    }

    private void loadJails(ConfigurationSection jailsSection) {
        for(String jailName : jailsSection.getKeys(false)){
            ConfigurationSection jSection = jailsSection.getConfigurationSection(jailName);
            if (jSection == null || jSection.getKeys(false).isEmpty()) {
                continue;
            }

            try {
                addJail(jailName, parseJail(jSection));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load jail '" + jailName + "': " + e.getMessage());
            }
        }
    }

    private Jail parseJail(ConfigurationSection section) {
        String uuidString = section.getString("world", "");
        UUID worldUUID = uuidString.isEmpty() ? null : UUID.fromString(uuidString);
        String worldName = section.getString("world-name", "");

        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 0);
        double z = section.getDouble("z", 0);
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);

        return new Jail(worldUUID, worldName, x, y, z, yaw, pitch);
    }

    public boolean teleportToJail(String name, Player player){
        Jail jail = jailHashMap.get(name);
        if(jail == null){
            return false;
        }

        Location jailLoc = jail.getLocation();
        player.teleport(jailLoc);
        return true;
    }

    // force = true если в конец команды -n
    public boolean createJail(String name, Location playerLoc, boolean force){
        if(hasJail(name) && !force){
            return false;
        }

        Jail newJail = new Jail(playerLoc);
        addJail(name, newJail);
        Config.setJail(name.toLowerCase(), newJail);
        return true;
    }

    public boolean deleteJail(String name){
        if(!hasJail(name)){
            return false;
        }

        jailHashMap.remove(name.toLowerCase());
        Config.removeJail(name.toLowerCase());
        return true;
    }
}
