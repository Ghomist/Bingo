package info.bcrc.mc.bingo;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        gameState = BingoGameState.SETUP;
    }

    protected void join(Player player, String team) {
        if (!gameState.equals(BingoGameState.SETUP))
            return;

        player_team.put(player, team);
        players_maps.put(player, new BingoMap(bingoMapCreator.returnDefaultMap()));

        player.sendMessage(ChatColor.valueOf(team) + "[Bingo] You have joined as " + team + " team");
    }

    protected void start(Player sponsor) {
        for (Player p : players_maps.keySet()) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 255, false, false));

            p.getInventory().setItem(8, new ItemStack(Material.NETHER_STAR));

            RandomTpPlayer.randomTpPlayer(p);

            p.sendMessage("[Bingo] The game has been started");
        }
    }

    protected void playerGetItem(Player player, ItemStack item) {
        if (!gameState.equals(BingoGameState.START))
            return;

        BingoMap map = players_maps.get(player);
        int index = map.getIndex(item); // get the index of tested slot

        if (shareInventory) {
            for (BingoMap m : players_maps.values()) {
                m.playerGetItem(player, item, player_team.get(player));
            }
        } else {
            map.playerGetItem(player, item, player_team.get(player));
        }

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
            for (Player p : players_maps.keySet()) {
                p.sendMessage(ChatColor.RED + "[Bingo] The game had been shut up forcibly");
            }
            gameState = BingoGameState.END;
        }
    }

    private HashMap<Player, BingoMap> players_maps = new HashMap<>();

    private boolean collectAll;
    private boolean shareInventory;

    private BingoGameState gameState;
    private BingoMapCreator bingoMapCreator;

    private HashMap<Player, String> player_team = new HashMap<>();
}
