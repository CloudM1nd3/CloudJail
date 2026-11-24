package org.cloudplugins.cloudJail.integrations.voice.mods;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.integrations.voice.VoiceIntegration;

public class SVCBlocker implements VoicechatPlugin {
    private static volatile SVCBlocker instance;
    private volatile boolean active = true;

    public static synchronized boolean register(JavaPlugin plugin) {
        if (instance != null) return false;

        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) return false;

        SVCBlocker blocker = new SVCBlocker();
        service.registerPlugin(blocker);
        instance = blocker;
        return true;
    }

    public static synchronized void unregister() {
        SVCBlocker blocker = instance;
        if (blocker == null) {
            return;
        }
        instance = null;
        blocker.active = false;
    }

    @Override
    public String getPluginId() {
        return "cloudjail";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone, 100);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (!active) return;

        try {
            if (!VoiceIntegration.getBlockVoiceChat()) return;

            VoicechatConnection sender = event.getSenderConnection();
            if (sender == null) return;

            PunishmentManager manager = PunishmentManager.getInstance();
            if (manager != null && manager.isJailed(sender.getPlayer().getUuid())) {
                event.cancel();
            }
        } catch (Throwable t) {
            active = false;
        }
    }
}