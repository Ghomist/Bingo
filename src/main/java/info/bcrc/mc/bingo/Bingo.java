package info.bcrc.mc.bingo;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import info.bcrc.mc.bingo.BingoGame.BingoGameState;
import info.bcrc.mc.bingo.util.ConfigHandler;

public class Bingo extends JavaPlugin {

    protected ConfigHandler configHandler;

    protected Server server;
    protected Logger logger;

    protected BingoListener lsn = new BingoListener(this);
    protected BingoCommander cmd = new BingoCommander(this);

    protected BingoGame bingoGame;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configHandler = new ConfigHandler(this);

        server = getServer();
        logger = getLogger();

        server.getPluginManager().registerEvents(lsn, this);

        getCommand("bingo").setExecutor(cmd);

        getConfig();

        bingoGame = new BingoGame(this, false, false);
        bingoGame.setGameState(BingoGameState.END);
    }

    @Override
    public void onDisable() {
        this.bingoGame.playerFinishBingo(null, true);
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

}
