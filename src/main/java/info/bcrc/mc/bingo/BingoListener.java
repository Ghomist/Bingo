package info.bcrc.mc.bingo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import info.bcrc.mc.bingo.BingoGame.BingoGameState;
import net.md_5.bungee.api.ChatColor;

public class BingoListener implements Listener {

    protected Bingo plugin;

    public BingoListener(Bingo plugin) {
        this.plugin = plugin;
    }

    @EventHandler // (priority = EventPriority.HIGH)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null)
            if (plugin.bingoGame.isBingoMap((Player) event.getWhoClicked(), event.getInventory())
                    || (event.getCurrentItem().getType().equals(Material.NETHER_STAR)
                            && plugin.bingoGame.getGameState().equals(BingoGameState.START)))
                event.setCancelled(true);

    }

    @EventHandler // (priority = EventPriority.HIGH)
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        if (plugin.bingoGame.getGameState().equals(BingoGameState.START)
                && event.getItem().getItemStack().getType().equals(Material.NETHER_STAR))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        try {
            if (plugin.bingoGame.isBingoMap((Player) event.getDestination().getHolder(), event.getDestination()))
                event.setCancelled(true);
        } catch (Exception e) {
            plugin.logger.info(e.toString());
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && plugin.bingoGame.getGameState().equals(BingoGameState.START)
                && item.getType().equals(Material.NETHER_STAR)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.openInventory(
                    plugin.bingoGame.getBingoMap(player).getInventory(plugin.bingoGame.isBePlayer(player)));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
        if (event.getOffHandItem().getType().equals(Material.NETHER_STAR))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        Player player = event.getPlayer();
        if (item.getType().equals(Material.NETHER_STAR)) {
            player.openInventory(
                    plugin.bingoGame.getBingoMap(player).getInventory(plugin.bingoGame.isBePlayer(player)));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            event.setCancelled(true);
            return;
        }
        // try to achieve bingo
        plugin.bingoGame.playerGetItem(player, item);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
        Location location = event.getEntity().getLocation();
        player.sendMessage(BingoGame.announcer + ChatColor.GRAY + "Death position: " + ChatColor.GREEN + "("
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.bingoGame.isBingoPlayer(player)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999, 255, false, false));
            });
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.bingoGame.isBingoPlayer(player))
            player.setScoreboard(plugin.bingoGame.getScoreboard());
    }

    @EventHandler
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item.getType().equals(Material.LEATHER_BOOTS) && item.containsEnchantment(Enchantment.DEPTH_STRIDER))
            event.setCancelled(true);
    }
}