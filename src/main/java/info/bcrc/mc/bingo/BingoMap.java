package info.bcrc.mc.bingo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BingoMap {

    protected BingoMap(Inventory inventory) {
        this.inventory = inventory;
    }

    protected void playerGetItem(Player player, ItemStack item, String team) {
        inventory.setItem(inventory.first(item), new ItemStack(Material.valueOf(team.toUpperCase() + "_WOOL")));
        player.sendMessage("[Bingo] You have finished " + item.getType().getKey().getKey());
        collectedCount++;
    }

    protected boolean testCross(int index) {
        Material current = inventory.getItem(index).getType();

        boolean line = true;
        int x = index / 9 * 9 + 2;
        for (int i = 0; i < 5; i++) {
            if (!inventory.getItem(x).getType().equals(current))
                line = false;
            x += 1;
        }

        boolean column = true;
        int y = index % 9;
        for (int i = 0; i < 5; i++) {
            if (!inventory.getItem(y).getType().equals(current))
                column = false;
            y += 9;
        }

        return line || column;
    }

    protected boolean testAllCollected() {
        if (collectedCount == 25)
            return true;
        return false;
    }

    protected int getIndex(ItemStack item) {
        return inventory.first(item);
    }

    protected Inventory getInventory() {
        return inventory;
    }

    private Inventory inventory;
    private int collectedCount = 0;

}
