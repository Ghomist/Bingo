package info.bcrc.mc.bingo;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import info.bcrc.mc.bingo.util.BingoMapCreator;
import info.bcrc.mc.bingo.util.RandomTpPlayer;

public class BingoGame {

    protected enum BingoGameState {
        SETUP, START, END
    }

    protected BingoGame(Bingo plugin, boolean collectAll, boolean shareInventory) {
        this.collectAll = collectAll;
        this.shareInventory = shareInventory;

        bingoMapCreator = new BingoMapCreator(plugin);

        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage("[Bingo] A bingo game has been set up"));
        gameState = BingoGameState.SETUP;

        plugin.getServer().getOnlinePlayers()
                .forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f));
    }

    protected void join(Player player, String team) {
        if (gameState.equals(BingoGameState.START) && !getPlayers().contains(player)) {
            rejoin(player);
        }

        if (!gameState.equals(BingoGameState.SETUP))
            return;

        if (player_team.keySet().contains(player)) {
            player_team.replace(player, team);
            players_maps.replace(player, new BingoMap(player, bingoMapCreator.returnDefaultMap()));
        } else {
            player_team.put(player, team);
            players_maps.put(player, new BingoMap(player, bingoMapCreator.returnDefaultMap()));
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 255, false, false));

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
        sendMessageToAll(ChatColor.valueOf(team.toUpperCase()) + "[Bingo] " + player.getName() + " have joined as "
                + team + " team");
    }

    protected void playerQuit(Player player) {
        if (getPlayers().contains(player) && gameState.equals(BingoGameState.START)) {
            UUID uuid = player.getUniqueId();
            player_savedMaps.put(uuid, players_maps.get(player));
            player_savedTeam.put(uuid, player_team.get(player));

            player_team.remove(player);
            players_maps.remove(player);
        }
    }

    protected void rejoin(Player player) {
        UUID uuid = player.getUniqueId();
        if (player_savedMaps.keySet().contains(uuid) && gameState.equals(BingoGameState.START)) {
            player_team.put(player, player_savedTeam.get(uuid));
            players_maps.put(player, player_savedMaps.get(uuid));

            player_savedMaps.remove(uuid);
            player_savedTeam.remove(uuid);
        }
    }

    protected void printPlayerList(Player player) {
        if (gameState.equals(BingoGameState.END))
            return;

        player.sendMessage("[Bingo] Player lists: (Color stands for team)");
        getPlayers().forEach(p -> {
            player.sendMessage(" - " + ChatColor.valueOf(player_team.get(p).toUpperCase()) + p.getName());
        });
        player.sendMessage(getPlayers().size() + " players joined the game in total");
    }

    protected void start(Player sponsor) {
        getPlayers().forEach(p -> {
            // manage players' potion effects
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));

            p.getInventory().clear();
            // give players nether start
            ItemStack item = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Bingo card");
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            p.getInventory().setItem(8, item);

            // give players boots with depth strider
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 5);
            p.getInventory().setBoots(boots);

            // random teleport
            RandomTpPlayer.randomTpPlayer(p);
            p.setBedSpawnLocation(p.getLocation(), true);

            // set gamemode to survival
            p.setGameMode(GameMode.SURVIVAL);

            // set spawnpoint
            p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);

            // info
            p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
            p.sendMessage("[Bingo] The game has been started");
        });
        sponsor.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        sponsor.getWorld().setTime(1000);

        gameState = BingoGameState.START;
    }

    protected void playerGetItem(Player player, ItemStack item) {
        if (!gameState.equals(BingoGameState.START))
            return;

        BingoMap map = players_maps.get(player);
        String team = player_team.get(player);
        int index = map.getIndex(item); // get the index of tested slot

        if (shareInventory)
            players_maps.values().forEach(m -> m.playerGetItem(player, item, team));
        else {
            player_team.forEach((p, t) -> {
                if (t.equalsIgnoreCase(team)) {
                    players_maps.get(p).playerGetItem(player, item, team);
                }
            });
        }

        getPlayers().forEach(p -> {
            p.sendMessage(ChatColor.valueOf(team.toUpperCase()) + "[Bingo] " + player.getName() + " has achieved ["
                    + item.getType().getKey().getKey() + "]");
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
        });

        if (collectAll && map.testAllCollected()) {
            playerFinishBingo(player);
            gameState = BingoGameState.END;
        }

        if (!collectAll && map.testCross(index)) {
            playerFinishBingo(player);
            gameState = BingoGameState.END;
        }

    }

    protected void playerFinishBingo(Player player) {
        if (gameState.equals(BingoGameState.END))
            return;

        if (player == null) {
            sendMessageToAll(ChatColor.RED + "[Bingo] The game had been shut up forcibly");
            gameState = BingoGameState.END;
        } else {
            getPlayers().forEach(p -> {
                p.sendMessage(ChatColor.YELLOW + "[Bingo] " + player.getName() + " has finished the bingo");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            });
        }
    }

    protected boolean isInventoryBingoMap(Inventory inventory) {
        for (BingoMap m : players_maps.values())
            if (m.getInventory().equals(inventory))
                return true;

        return false;
    }

    protected boolean isItemTarget(Player player, ItemStack item) {
        return players_maps.get(player).getInventory().contains(item);
    }

    protected boolean isStarted() {
        return gameState.equals(BingoGameState.START);
    }

    protected Set<Player> getPlayers() {
        return players_maps.keySet();
    }

    protected void replacePlayer(Player formerPlayer, Player currentPlayer) {
        player_team.put(currentPlayer, player_team.get(formerPlayer));
        players_maps.put(currentPlayer, players_maps.get(formerPlayer));

        player_team.remove(formerPlayer);
        players_maps.remove(formerPlayer);
    }

    protected void sendMessageToAll(String msg) {
        getPlayers().forEach(p -> p.sendMessage(msg));
    }

    protected void openBingoMap(Player player) {
        if (getPlayers().contains(player)) {
            players_maps.get(player).openBingoMap();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    protected BingoGameState getGameState() {
        return gameState;
    }

    private boolean collectAll;
    private boolean shareInventory;

    private BingoGameState gameState;
    private BingoMapCreator bingoMapCreator;

    private HashMap<Player, BingoMap> players_maps = new HashMap<>();
    private HashMap<Player, String> player_team = new HashMap<>();
    private HashMap<UUID, BingoMap> player_savedMaps = new HashMap<>();
    private HashMap<UUID, String> player_savedTeam = new HashMap<>();

}
