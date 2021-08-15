package info.bcrc.mc.bingo;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));

            p.getInventory().clear();
            p.getInventory().setItem(8, new ItemStack(Material.NETHER_STAR));

            RandomTpPlayer.randomTpPlayer(p);
            p.setBedSpawnLocation(p.getLocation(), true);

            p.setGameMode(GameMode.SURVIVAL);

            p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);

            p.sendMessage("[Bingo] The game has been started");
        });
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

    protected void sendMessageToAll(String msg) {
        getPlayers().forEach(p -> p.sendMessage(msg));
    }

    protected void openBingoMap(Player player) {
        players_maps.get(player).openBingoMap();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    protected BingoGameState getGameState() {
        return gameState;
    }

    private HashMap<Player, BingoMap> players_maps = new HashMap<>();

    private boolean collectAll;
    private boolean shareInventory;

    private BingoGameState gameState;
    private BingoMapCreator bingoMapCreator;

    private HashMap<Player, String> player_team = new HashMap<>();
}
