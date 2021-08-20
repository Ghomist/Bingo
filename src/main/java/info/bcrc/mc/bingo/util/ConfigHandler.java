package info.bcrc.mc.bingo.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.Bingo;

public class ConfigHandler {

    public ConfigHandler(Bingo plugin) {
        int length = plugin.getConfig().getInt("item-list.length");
        for (int i = 1; i <= length; i++) {
            List<ItemStack> items = new ArrayList<>();
            plugin.getConfig().getStringList("item-list." + i)
                    .forEach(str -> items.add(new ItemStack(Material.valueOf(str))));
            itemList.add(items);
        }

        bingoInfo = plugin.getDescription().getName() + "\n" + plugin.getDescription().getAuthors();

        commandUsage = ChatColor.GREEN + "[Bingo] Command usages:\n" + plugin.getCommand("bingo").getUsage();
    }

    // Material a = Material.WEEPING_VINES;

    public List<List<ItemStack>> returnItemList() {
        return itemList;
    }

    public String returnBingoInfo() {
        return bingoInfo;
    }

    public String returnBingoCommandUsage() {
        return commandUsage;
    }

    public List<Biome> returnBiomes() {
        return biomes;
    }

    private List<List<ItemStack>> itemList = new ArrayList<>();
    private List<Biome> biomes = new ArrayList<>();

    private String bingoInfo = "";
    private String commandUsage = "";

}
