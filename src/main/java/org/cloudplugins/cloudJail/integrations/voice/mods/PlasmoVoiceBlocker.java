package org.cloudplugins.cloudJail.integrations.voice.mods;

import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.integrations.voice.VoiceIntegration;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEndEvent;
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEvent;

import java.util.UUID;

@Addon(
        id = "pv-addon-cloudjail",
        name = "CloudJail",
        version = "1.0.0",
        authors = {"CloudM1nde"}
)
public final class PlasmoVoiceBlocker implements AddonInitializer {

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    public static void register() {
        PlasmoVoiceServer.getAddonsLoader().load(new PlasmoVoiceBlocker());
    }

    @Override
    public void onAddonInitialize() { }

    @Override
    public void onAddonShutdown() { }

    @EventSubscribe
    public void onSpeak(PlayerServerActivationEvent event) {
        if (isJailed(event.getPlayer().getInstance().getUuid()) && VoiceIntegration.getBlockVoiceChat()) {
            event.setResult(ServerActivation.Result.HANDLED);
        }
    }

    @EventSubscribe
    public void onSpeakEnd(PlayerServerActivationEndEvent event) {
        if (isJailed(event.getPlayer().getInstance().getUuid()) && VoiceIntegration.getBlockVoiceChat()) {
            event.setResult(ServerActivation.Result.HANDLED);
        }
    }

    private boolean isJailed(UUID uuid) {
        PunishmentManager manager = PunishmentManager.getInstance();
        return manager != null && manager.isJailed(uuid);
    }
}