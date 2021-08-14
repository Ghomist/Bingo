package info.bcrc.mc.bingo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BingoMapCreator {

    protected Bingo plugin;

    private List<ItemStack> allAllowedItems = new ArrayList<>();

    protected BingoMapCreator(Bingo plugin) {
        this.plugin = plugin;
        for (String itemName : plugin.getConfig().getStringList("item-list")) {
            allAllowedItems.add(new ItemStack(Material.getMaterial(itemName)));
        }
    }
}