package org.cloudplugins.cloudJail;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudplugins.cloudJail.commands.Commands;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.database.Database;
import org.cloudplugins.cloudJail.listeners.PlayerListener;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;

import java.io.File;
import java.sql.SQLException;

public final class CloudJail extends JavaPlugin {
    private static CloudJail instance;
    private Database db;
    private PlayerListener playerListener;


    @Override
    public void onEnable() {
        instance = this;
        Config.init(instance);
        Messages.init(instance);

        try {
            db = new Database(new File(getDataFolder(), "punishments.db"));
            JailManager.init(this);
            PunishmentManager.init(this, db);
        } catch (Exception e) {
            getLogger().severe("Error starting plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("jail").setExecutor(new Commands());
        playerListener = new PlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.close();
        }
        instance = null;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public static CloudJail getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return db;
    }
}