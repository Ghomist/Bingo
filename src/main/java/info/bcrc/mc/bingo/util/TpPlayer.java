package info.bcrc.mc.bingo.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TpPlayer {

    public static void randomTpPlayer(Player player) {
        World world = player.getWorld();

        int x;
        int y;
        int z;
        Location destination = player.getLocation();
        do {
            x = (int) (Math.random() * range - range / 2);
            z = (int) (Math.random() * range - range / 2);
            y = world.getHighestBlockYAt((int) x, (int) z);
            destination.setX(x);
            destination.setY(y);
            destination.setZ(z);
        } while (destination.getBlock().getBiome().name().contains("OCEAN") || y < world.getSeaLevel() - 5);

        if (world.getBlockAt(x, y, z).isLiquid())
            world.getBlockAt(x, y, z).setType(Material.DIRT);

        // +1 to make players stand on the ground 233
        destination.setY(destination.getY() + 1);
        player.teleport(destination);

        // info
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

    }

    public static void tpPlayerToGround(Player player) {
        World world = player.getWorld();
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();
        int y = world.getHighestBlockYAt(x, z);

        if (world.getBlockAt(x, y, z).isLiquid())
            world.getBlockAt(x, y, z).setType(Material.DIRT);

        // +1 to make players stand on the ground 233
        player.teleport(new Location(world, x, y + 1, z));

        // info
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private static int range = 200000;
}
