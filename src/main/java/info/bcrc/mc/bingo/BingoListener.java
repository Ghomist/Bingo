package info.bcrc.mc.bingo;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BingoListener implements Listener {

    protected Bingo plugin;

    public BingoListener(Bingo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (plugin.bingoGame == null)
            return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        plugin.getLogger().info("onPlayerInteractEvent");
        if (item != null && plugin.bingoGame.isStarted() && item.getType().equals(Material.NETHER_STAR)) {
            event.setCancelled(true);
            plugin.getLogger().info("openBingoCard");
            plugin.bingoGame.openBingoMap(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            if (event.getCurrentItem().getType().equals(Material.NETHER_STAR)
                    || plugin.bingoGame.isInventoryBingoMap(event.getInventory())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.NETHER_STAR)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        if (plugin.bingoGame.isInventoryBingoMap(event.getDestination())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
        if (event.getOffHandItem().getType().equals(Material.NETHER_STAR)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType().equals(Material.NETHER_STAR)) {
            event.setCancelled(true);
            return;
        }

        Player player = event.getPlayer();
        if (plugin.bingoGame.isItemTarget(player, item)) {
            plugin.bingoGame.playerGetItem(player, item);
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.getEntity().playSound(event.getEntity().getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.bingoGame.getPlayers().contains(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!plugin.bingoGame.isStarted())
            return;

        Player player = event.getPlayer();
        plugin.bingoGame.getPlayers().forEach(p -> {
            if (p.getName().equals(player.getName())) {
                plugin.bingoGame.replacePlayer(p, player);
            }
        });

    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        if (plugin.bingoGame.isStarted())
            plugin.bingoGame.playerQuit(event.getPlayer());
    }

}