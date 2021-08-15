package info.bcrc.mc.bingo.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.Bingo;
import net.md_5.bungee.api.ChatColor;

public class ConfigHandler {

    public ConfigHandler(Bingo plugin) {
        plugin.getConfig().getStringList("item-list").forEach(item -> {
            itemList.add(new ItemStack(Material.valueOf(item)));
        });

        bingoInfo = plugin.getDescription().getName() + "\n" + plugin.getDescription().getAuthors();

        commandUsage = ChatColor.GREEN + "[Bingo] Command usages:\n" + plugin.getCommand("bingo").getDescription();
    }

    public List<ItemStack> returnItemList() {
        return itemList;
    }

    public String returnBingoInfo() {
        return bingoInfo;
    }

    public String returnBingoCommandUsage() {
        return commandUsage;
    }

    private List<ItemStack> itemList = new ArrayList<>();
    private String bingoInfo = "";
    private String commandUsage = "";

}
