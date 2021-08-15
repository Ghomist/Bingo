package info.bcrc.mc.bingo.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RandomTpPlayer {

    public static void randomTpPlayer(Player player) {
        World world = player.getWorld();
        int x = (int) (Math.random() * range - range / 2);
        int z = (int) (Math.random() * range - range / 2);
        int y = world.getHighestBlockYAt(x, z);

        if (world.getBlockAt(x, y, z).getType().equals(Material.WATER))
            world.getBlockAt(x, y, z).setType(Material.DIRT);

        // +1 to make players stand on the ground 233
        player.teleport(new Location(world, x, y + 1, z));

    }

    private static int range = 2000000;
}
