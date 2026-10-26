package org.cloudplugins.cloudJail.voice;

import org.bukkit.Bukkit;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.voice.mods.PlasmoVoiceBlocker;
import org.cloudplugins.cloudJail.voice.mods.SVCBlocker;

public class VoiceIntegration {
    private final CloudJail plugin;

    private boolean svcActive;
    private boolean plasmoActive;
    private static volatile boolean blockVoiceChat;

    public static boolean getBlockVoiceChat() {
        return blockVoiceChat;
    }

    public static void setBlockVoiceChat(boolean value) {
        blockVoiceChat = value;
    }

    public VoiceIntegration(CloudJail plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (Bukkit.getPluginManager().getPlugin("PlasmoVoice") == null) return;

        try {
            PlasmoVoiceBlocker.register();
            plasmoActive = true;
            plugin.getLogger().info("[CloudJail] PlasmoVoice-API подключено^_^");
        } catch (Throwable t) {
            plugin.getLogger().warning("[CloudJail] PlasmoVoice-API не работает :с - " + t);
        }
    }

    public void enable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("voicechat")) return;

        try {
            svcActive = SVCBlocker.register(plugin);
            if (svcActive) {
                plugin.getLogger().info("[CloudJail] SimpleVoiceChat-API подключено ^_^");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[CloudJail]  SimpleVoiceChat-API не работает: " + t);
        }
    }

    public boolean isSVCActive()    { return svcActive; }
    public boolean isPlasmoVoiceActive() { return plasmoActive; }
}