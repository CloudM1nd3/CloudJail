package org.cloudplugins.cloudJail;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudplugins.cloudJail.commands.*;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.database.Database;
import org.cloudplugins.cloudJail.integrations.essentials.EssentialsIntegration;
import org.cloudplugins.cloudJail.integrations.voice.mods.SVCBlocker;
import org.cloudplugins.cloudJail.listeners.PlayerListener;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.integrations.voice.VoiceIntegration;
import org.cloudplugins.cloudJail.tabcompleter.JailTabCompleter;

import java.io.File;
import java.util.List;

public final class CloudJail extends JavaPlugin {
    private static CloudJail instance;
    private Database db;
    private PlayerListener playerListener;
    private VoiceIntegration voiceIntegration;
    private EssentialsIntegration essentialsIntegration;
    private JailTabCompleter tabCompleter;

    @Override
    public void onLoad() {
        voiceIntegration = new VoiceIntegration(this);
        voiceIntegration.load(); //pv
    }

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

        getCommand("jailadmin").setExecutor(new JailAdminCommand());
        getCommand("jail").setExecutor(new JailCommand());
        getCommand("unjail").setExecutor(new UnjailCommand());
        getCommand("jailhistory").setExecutor(new JailHistoryCommand());
        getCommand("jailinfo").setExecutor(new JailInfoCommand());

        tabCompleter = new JailTabCompleter();
        for (String name : List.of("jail", "unjail", "jailhistory", "jailinfo", "jailadmin")) {
            getCommand(name).setTabCompleter(tabCompleter);
        }

        playerListener = new PlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);

        voiceIntegration.enable(); //SVC nah

        if (getServer().getPluginManager().isPluginEnabled("Essentials")) { //essentials nah
            EssentialsIntegration ess = new EssentialsIntegration();
            if (ess.hook()) essentialsIntegration = ess;
        }

    }

    @Override
    public void onDisable() {
        if (PunishmentManager.getInstance() != null) {
            PunishmentManager.getInstance().saveAll();
        }
        if (db != null) {
            db.close();
        }
        instance = null;
    }

    public EssentialsIntegration getEssentials() {
        return essentialsIntegration;
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