package info.bcrc.mc.bingo;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class Bingo extends JavaPlugin {

    protected Server server;
    protected Logger logger;

    protected BingoListener lsn = new BingoListener(this);
    protected BingoCommander cmd = new BingoCommander(this);

    protected BingoInGame inGame;

    @Override
    public void onEnable() {
        server = getServer();
        logger = getLogger();

        server.getPluginManager().registerEvents(lsn, this);

        getCommand("bingo").setExecutor(cmd);

    }

    @Override
    public void onDisable() {

    }
}
