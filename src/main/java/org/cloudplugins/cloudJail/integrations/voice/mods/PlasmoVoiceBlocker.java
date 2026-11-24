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
    private static volatile PlasmoVoiceBlocker instance;
    private volatile boolean active = true;

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    public static synchronized boolean register() {
        if (instance != null) return false;
        PlasmoVoiceBlocker addon = new PlasmoVoiceBlocker();
        PlasmoVoiceServer.getAddonsLoader().load(addon);
        instance = addon;
        return true;
    }

    public static synchronized void unregister() {
        PlasmoVoiceBlocker addon = instance;
        instance = null;
        if (addon == null) return;
        addon.active = false;
        try {
            PlasmoVoiceServer.getAddonsLoader().unload(addon);
        } catch (Throwable ignored) { }
    }

    @Override
    public void onAddonInitialize() { }

    @Override
    public void onAddonShutdown() {
        active = false;
    }

    @EventSubscribe
    public void onSpeak(PlayerServerActivationEvent event) {
        if (shouldBlock(event.getPlayer().getInstance().getUuid()) && VoiceIntegration.getBlockVoiceChat()) {
            event.setResult(ServerActivation.Result.HANDLED);
        }
    }

    @EventSubscribe
    public void onSpeakEnd(PlayerServerActivationEndEvent event) {
        if (shouldBlock(event.getPlayer().getInstance().getUuid()) && VoiceIntegration.getBlockVoiceChat()) {
            event.setResult(ServerActivation.Result.HANDLED);
        }
    }

    private boolean shouldBlock(UUID uuid) {
        if (!active) return false;
        try {
            if (!VoiceIntegration.getBlockVoiceChat()) return false;
            PunishmentManager punishmentManager = PunishmentManager.getInstance();
            return punishmentManager != null && punishmentManager.isJailed(uuid);
        } catch (Throwable t) {
            active = false;
            return false;
        }
    }
}