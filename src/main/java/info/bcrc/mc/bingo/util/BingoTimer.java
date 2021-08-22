package info.bcrc.mc.bingo.util;

import java.sql.Time;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import info.bcrc.mc.bingo.BingoGame.BingoPlayer;
import net.md_5.bungee.api.ChatMessageType;

public class BingoTimer extends BukkitRunnable {

    private HashSet<BingoPlayer> players;

    public BingoTimer(HashSet<BingoPlayer> players) {
        this.players = players;
        this.time = new Time(0);
    }

    @Override
    public void run() {
        players.forEach(bp -> {
            Player p = Bukkit.getPlayer(bp.uuid);
            if (p != null) {
                p.sendMessage(ChatMessageType.ACTION_BAR + time.toString());
                time.setTime(time.getTime() + 1);
            }
        });
    }

    private Time time;

}
