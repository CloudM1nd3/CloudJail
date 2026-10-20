package org.cloudplugins.cloudJail;

import org.bukkit.plugin.java.JavaPlugin;
import org.cloudplugins.cloudJail.commands.Commands;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;

public final class CloudJail extends JavaPlugin {
    public static CloudJail getInstance(){
        return getPlugin(CloudJail.class);
    }


    @Override
    public void onEnable() {
        Config.init(this);
        JailManager.init(this);
        try {
            PunishmentManager.init(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Commands chatCommand = new Commands();
        getCommand("jail").setExecutor(chatCommand);
    }

    @Override
    public void onDisable() {

    }
}
