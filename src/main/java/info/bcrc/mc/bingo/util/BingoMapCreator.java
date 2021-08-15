package info.bcrc.mc.bingo.util;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.Bingo;

public class BingoMapCreator {

    public BingoMapCreator(Bingo plugin) {
        this.configHandler = plugin.getConfigHandler();

        defaultMap = plugin.getServer().createInventory(null, 45);

        List<ItemStack> itemList = configHandler.returnItemList();
        ItemStack air = new ItemStack(Material.AIR);

        // generate default map
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++)
                defaultMap.setItem(9 * i + j, air);

            for (int j = 2; j < 7; j++)
                defaultMap.setItem(9 * i + j, itemList.get(random.nextInt(itemList.size())));

            for (int j = 7; j < 9; j++)
                defaultMap.setItem(9 * i + j, air);
        }
    }

    public Inventory returnDefaultMap() {
        return defaultMap;
    }

    private Inventory defaultMap;
    private ConfigHandler configHandler;
}