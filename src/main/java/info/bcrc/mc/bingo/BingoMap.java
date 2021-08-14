package info.bcrc.mc.bingo;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class BingoMap {

    protected BingoMap(Inventory inventory) {
        this.inventory = inventory;
    }

    protected abstract void playerGetItem(Player player, ItemStack item);

    protected Inventory getInventory() {
        return inventory;
    }

    private Inventory inventory;

}
