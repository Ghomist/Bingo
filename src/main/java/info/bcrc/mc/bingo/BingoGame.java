package info.bcrc.mc.bingo;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.util.BingoMapCreator;

public class BingoGame {

    protected enum BingoGameState {
        INITIAL, SETUP, START, END
    }

    protected BingoGame(Bingo plugin, boolean collectAll, boolean shareInventory) {
        this.collectAll = collectAll;
        this.shareInventory = shareInventory;

        setup();
        bingoMapCreator = new BingoMapCreator(plugin);

        gameState = BingoGameState.INITIAL;
    }

    protected void setup() {
        if (shareInventory) {
            players = new ArrayList<>();
            publicMap = new BingoMap(bingoMapCreator.returnDefaultMap());
        } else {
            players_maps = new HashMap<>();
        }

        gameState = BingoGameState.SETUP;
    }

    protected void join(Player player, String team) {
        if (!gameState.equals(BingoGameState.SETUP))
            return;

        if (shareInventory) {
            players.add(player);
        } else {
            players_maps.put(player, new BingoMap(bingoMapCreator.returnDefaultMap()));
        }

        player_team.put(player, team);
        players_maps.put(player, new BingoMap(bingoMapCreator.returnDefaultMap()));
    }

    protected void start() {

    }

    protected void playerGetItem(Player player, ItemStack item) {
        if (!gameState.equals(BingoGameState.START))
            return;

        BingoMap map;
        if (shareInventory)
            map = publicMap;
        else
            map = players_maps.get(player);

        int index = map.getIndex(item);
        map.playerGetItem(player, item, player_team.get(player));

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

    }

    private ArrayList<Player> players;
    private BingoMap publicMap;

    private HashMap<Player, BingoMap> players_maps;

    private boolean collectAll;
    private boolean shareInventory;

    private BingoGameState gameState;
    private BingoMapCreator bingoMapCreator;

    private HashMap<Player, String> player_team = new HashMap<>();
}
