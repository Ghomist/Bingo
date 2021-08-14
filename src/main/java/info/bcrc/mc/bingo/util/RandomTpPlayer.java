package info.bcrc.mc.bingo.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RandomTpPlayer {

    public static void randomTpPlayer(Player player) {
        int x = (int) Math.random() * range - range / 2;
        int z = (int) Math.random() * range - range / 2;

        World world = player.getWorld();

        player.teleport(new Location(world, x, world.getHighestBlockYAt(x, z), z));

    }

    private static int range = 2000000;
}
