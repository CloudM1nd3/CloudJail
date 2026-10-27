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

    public static boolean register(JavaPlugin plugin) {
        BukkitVoicechatService service =
                plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) return false;

        service.registerPlugin(new SVCBlocker());
        return true;
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
        if (!VoiceIntegration.getBlockVoiceChat()) return;

        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null) return;

        PunishmentManager manager = PunishmentManager.getInstance();
        if (manager != null && manager.isJailed(sender.getPlayer().getUuid())) {
            event.cancel();
        }
    }
}