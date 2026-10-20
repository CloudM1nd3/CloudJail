package org.cloudplugins.cloudJail.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.objects.Jail;

public class Config {
    private static CloudJail plugin;
    private static FileConfiguration config;

    public static void init(CloudJail pl){
        plugin = pl;
        plugin.saveDefaultConfig();
        reload();
    }

    public static void reload(){
        plugin.reloadConfig();
        config = plugin.getConfig();

        boolean changed = addMissingDefaults();
        if (changed) {
            save();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }
    }

    public static ConfigurationSection getOrCreateSection(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            section = config.createSection(path);
        }
        return section;
    }

    public static ConfigurationSection getSection(String path){
        return config.getConfigurationSection(path);
    }

    private static boolean addMissingDefaults() {
        boolean changed = false;

        if (!config.isSet("lang")) {
            config.set("lang", "ru");
            changed = true;
        }

        return changed;
    }

    public static void setJail(String name, Jail jail){
        ConfigurationSection jailSec = getOrCreateSection("Jails." + name);

        jailSec.set("world", jail.getWorldUUID() == null ? null : jail.getWorldUUID().toString());
        jailSec.set("world-name", jail.getWorldName());
        jailSec.set("x", jail.getX());
        jailSec.set("y", jail.getY());
        jailSec.set("z", jail.getZ());
        jailSec.set("yaw", jail.getYaw());
        jailSec.set("pitch", jail.getPitch());
        save();
    }

    public static void removeJail(String name){
        config.set("Jails." + name, null);
        save();
    }

    public static void setValue(String path, Object value) {
        config.set(path, value);
        save();
    }

    public static void save(){
        plugin.saveConfig();
    }

}
