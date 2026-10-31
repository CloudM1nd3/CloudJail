package org.cloudplugins.cloudJail.integrations.essentials;

import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsIntegration {
    private IEssentials essentials;

    public boolean hook() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin instanceof IEssentials ess && plugin.isEnabled()) {
            this.essentials = ess;
            plugin.getLogger().info("[CloudJail] Essentials-API подключено ^_^");
            return true;
        }
        plugin.getLogger().info("[CloudJail] Essentials-API не воркает ;-;");
        return false;
    }

    public boolean isAfk(Player player) {
        if (essentials == null) return false;
        User user = essentials.getUser(player);
        return user != null && user.isAfk();
    }
}
