package info.bcrc.mc.bingo.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RandomTpPlayer {

    public static void randomTpPlayer(Player player) {
        int x = (int) (Math.random() * range - range / 2);
        int z = (int) (Math.random() * range - range / 2);

        World world = player.getWorld();

        // +1 to make players stand on the ground 233
        player.teleport(new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z));

    }

    private static int range = 2000000;
}
