package org.cloudplugins.cloudJail.integrations.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.cloudplugins.cloudJail.CloudJail;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.TimeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceHolderApiHook extends PlaceholderExpansion {
    private final CloudJail plugin;
    private final PunishmentManager punishmentManager;

    public PlaceHolderApiHook(CloudJail plugin, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cloudjail";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Punishment punishment = punishmentManager.getActivePunishment(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "jailed": {
                if (punishment == null) {
                    return Messages.PLACEHOLDER_NOT_JAILED.get();
                }
                return Messages.PLACEHOLDER_JAILED.get();
            }
            case "remaining": {
                if (punishment == null) {
                    return TimeParser.format(0);
                }

                long remainingMillis = punishment.getRemainingMillis();

                return Messages.PLACEHOLDER_REMAINING.get("%time%", TimeParser.format(remainingMillis));
            }
        }

        return null;
    }
}


