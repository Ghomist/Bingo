package info.bcrc.mc.bingo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.Bingo;

public class BingoMapCreator {

    public BingoMapCreator(Bingo plugin) {
        this.configHandler = plugin.getConfigHandler();

        List<List<ItemStack>> itemList = configHandler.returnItemList();
        ItemStack emptyStack = new ItemStack(Material.AIR);

        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++)
            index.add(i);
        Collections.shuffle(index);
        Iterator<Integer> ind = index.iterator();

        // generate default map
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++)
                defaultList[9 * i + j] = emptyStack;

            for (int j = 2; j < 7; j++) {
                List<ItemStack> chooseItem = itemList.get(ind.next());
                defaultList[9 * i + j] = chooseItem.get(random.nextInt(chooseItem.size()));
            }

            for (int j = 7; j < 9; j++)
                defaultList[9 * i + j] = emptyStack;
        }
    }

    public ItemStack[] returnDefaultList() {
        return defaultList;
    }

    private ItemStack[] defaultList = new ItemStack[45];
    private ConfigHandler configHandler;
}