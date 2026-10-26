package org.cloudplugins.cloudJail.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.cloudplugins.cloudJail.configs.Config;
import org.cloudplugins.cloudJail.configs.Messages;
import org.cloudplugins.cloudJail.managers.JailManager;
import org.cloudplugins.cloudJail.managers.PunishmentManager;
import org.cloudplugins.cloudJail.objects.Jail;
import org.cloudplugins.cloudJail.objects.Punishment;
import org.cloudplugins.cloudJail.utils.ColorService;
import org.cloudplugins.cloudJail.voice.VoiceIntegration;

import java.util.List;

public class PlayerListener implements Listener {
    private boolean blockMovement;
    private boolean blockChat;
    private boolean blockCommands;
    private boolean blockConsume;
    private List<String> allowedCommands;

    public PlayerListener() {
        loadSettings();
    }

    public void loadSettings() {
        blockChat = Config.getBoolean("deny.chat", true);
        blockMovement = Config.getBoolean("deny.movement", false);
        blockConsume = Config.getBoolean("deny.consume", false);
        blockCommands = Config.getBoolean("deny.commands", true);
        allowedCommands = Config.getStringList("deny.allowed-commands")
                .stream().map(String::toLowerCase).toList();
        VoiceIntegration.setBlockVoiceChat(Config.getBoolean("deny.voice-chat", true));
    }

    private boolean isJailed(Player player) {
        return PunishmentManager.getInstance().isJailed(player.getUniqueId());
    }

    private void sendAB(Player player, Messages message) {
        player.sendActionBar(ColorService.processLegacy(message.get()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_BREAK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_PLACE);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PunishmentManager.getInstance().handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PunishmentManager.getInstance().handleQuit(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_INTERACT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_INTERACT);
    }


    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null || !isJailed(player)) return;

        if (e.getAction() == Action.PHYSICAL) {
            e.setCancelled(true);
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        boolean interactable = block.getType().isInteractable();
        boolean tool = item != null && isTool(item.getType());

        if (interactable || tool) {
            e.setCancelled(true);
            sendAB(player, Messages.DENY_INTERACT);
        }
    }

    private boolean isTool(Material m) {
        String n = m.name();
        return n.endsWith("_HOE") || n.endsWith("_AXE") || n.endsWith("_SHOVEL")
                || m == Material.FLINT_AND_STEEL || m == Material.BONE_MEAL;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_INTERACT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player player && isJailed(player)) {
            e.setCancelled(true);
            sendAB(player, Messages.DENY_INTERACT);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player player && isJailed(player)) {
            e.setCancelled(true);
            sendAB(player, Messages.DENY_INTERACT);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (!isJailed(e.getPlayer()) || !blockConsume) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_CONSUME);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!isJailed(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && isJailed(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Player attacker = null;

        if (e.getDamager() instanceof Player direct) {
            attacker = direct;
        } else if (e.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker == null || !isJailed(attacker)) return;

        e.setCancelled(true);
        sendAB(attacker, Messages.DENY_DAMAGE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player player && isJailed(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!isJailed(e.getPlayer()) || !blockChat) return;
        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_CHAT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!isJailed(e.getPlayer()) || !blockCommands) return;

        String msg = e.getMessage().toLowerCase();

        for (String allowed : allowedCommands) {
            if (msg.equals(allowed)) return;
        }

        e.setCancelled(true);
        sendAB(e.getPlayer(), Messages.DENY_COMMAND);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!blockMovement || !isJailed(e.getPlayer())) return;

        Location pos1 = e.getFrom();
        Location pos2 = e.getTo();

        if (pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ()) {
            return;
        }

        Location newTo = pos1.clone();
        newTo.setYaw(pos2.getYaw());
        newTo.setPitch(pos2.getPitch());
        e.setTo(newTo);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!isJailed(e.getPlayer())) return;

        PlayerTeleportEvent.TeleportCause cause = e.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || cause == PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent e) {
        Punishment p = PunishmentManager.getInstance().getActivePunishment(e.getPlayer().getUniqueId());
        if (p == null) return;

        Jail jail = JailManager.getInstance().getJailByName(p.getJailName());
        if (jail != null && jail.getLocation() != null) {
            e.setRespawnLocation(jail.getLocation());
        }
    }
}
