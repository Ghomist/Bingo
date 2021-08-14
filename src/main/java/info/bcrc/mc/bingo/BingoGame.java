package info.bcrc.mc.bingo;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import info.bcrc.mc.bingo.util.BingoMapCreator;

public class BingoGame {

    protected enum BingoGameMode {
        ALLCOLLECT, NORMAL, RACE
    }

    protected enum BingoGameState {
        INITIAL, SETUP, START, END
    }

    protected BingoGame(Bingo plugin, String mode) {
        setGameMode(mode);
        setup();
        bingoMapCreator = new BingoMapCreator(plugin);
    }

    protected void setup() {
        gameState = BingoGameState.SETUP;

    }

    protected void join(Player player) {
        players_Maps.put(player, bingoMapCreator.returnDefaultMap());
    }

    protected void setGameMode(String mode) {
        switch (mode) {
            case "allcollect":
                this.gameMode = BingoGameMode.ALLCOLLECT;
                break;
            case "normal":
                this.gameMode = BingoGameMode.NORMAL;
                break;
            case "race":
                this.gameMode = BingoGameMode.RACE;
                break;
            default:
                throw new IllegalArgumentException("There is no such game mode.");
        }
    }

    private HashMap<Player, Inventory> players_Maps = new HashMap<>();

    private BingoGameMode gameMode;
    private BingoGameState gameState = BingoGameState.INITIAL;
    private BingoMapCreator bingoMapCreator;

}
