package info.bcrc.mc.bingo.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigHandler {

    public ConfigHandler(FileConfiguration config) {
        config.getStringList("item-list").forEach(item -> itemList.add(new ItemStack(Material.valueOf(item))));
    }

    public List<ItemStack> returnItemList() {
        return itemList;
    }

    private List<ItemStack> itemList = new ArrayList<>();

}
