package info.bcrc.mc.bingo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class BingoMap {

    protected BingoMap(Player owner, Inventory inventory) {
        this.owner = owner;
        this.inventory = inventory;
        // make a be inventory
        this.beInventory = Bukkit.createInventory(owner, 36, owner.getName() + "'s Bingo Map");
        updateBeInventory();
    }

    protected void playerGetItem(Player player, ItemStack item, String team) {
        ItemStack replacement = new ItemStack(Material.valueOf(team.toUpperCase() + "_STAINED_GLASS_PANE"));
        ItemMeta meta = replacement.getItemMeta();
        meta.setDisplayName(ChatColor.of(team.toUpperCase()) + player.getName() + " obtained");
        replacement.setItemMeta(meta);

        inventory.setItem(getIndex(item), replacement);
        collectedCount++;

        updateBeInventory();
    }

    protected boolean testCross(int index) {
        Material current;
        try {
            current = inventory.getItem(index).getType();
        } catch (NullPointerException e) {
            current = Material.AIR;
        }

        boolean line = true;
        boolean column = true;
        boolean leftCaterCorner = true;
        boolean rightCaterCorner = true;
        int x = index / 9 * 9 + 2;
        int y = index % 9;
        int left = 2;
        int right = 6;

        for (int i = 0; i < 5; i++) {
            if (line && !inventory.getItem(x).getType().equals(current))
                line = false;
            if (column && !inventory.getItem(y).getType().equals(current))
                column = false;
            if (leftCaterCorner && !inventory.getItem(left).getType().equals(current))
                leftCaterCorner = false;
            if (rightCaterCorner && !inventory.getItem(right).getType().equals(current))
                rightCaterCorner = false;

            x += 1;
            y += 9;
            left += 10;
            right += 8;
        }

        return line || column || leftCaterCorner || rightCaterCorner;
    }

    protected boolean testAllCollected(int teamCount) {
        int limit = 25 / teamCount;
        if (25 % teamCount != 0)
            limit += 1;

        if (collectedCount == limit)
            return true;
        return false;
    }

    protected int getIndex(ItemStack item) {
        for (ItemStack i : inventory.getContents()) {
            if (i == null)
                continue;
            if (i.getType().equals(item.getType()))
                return inventory.first(i);
        }
        return -1;
    }

    protected Inventory getInventory(boolean beview) {
        if (beview) {
            updateBeInventory();
            return beInventory;
        } else
            return inventory;
    }

    private void updateBeInventory() {
        for (int i = 0; i < 30; i++) {
            if (i % 6 == 5) {
                beInventory.setItem(i, new ItemStack(Material.AIR));
                continue;
            }
            beInventory.setItem(i, this.inventory.getItem(i / 6 * 9 + i % 6 + 2));
        }
    }

    private Inventory inventory;
    private Inventory beInventory;
    private int collectedCount = 0;
    private Player owner;
}
