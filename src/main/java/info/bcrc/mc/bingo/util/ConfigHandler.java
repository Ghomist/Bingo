package info.bcrc.mc.bingo.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import info.bcrc.mc.bingo.Bingo;
import net.md_5.bungee.api.ChatColor;

public class ConfigHandler {

    public ConfigHandler(Bingo plugin) {
        plugin.getConfig().getStringList("item-list.easy")
                .forEach(item -> easyList.add(new ItemStack(Material.valueOf(item))));
        plugin.getConfig().getStringList("item-list.normal")
                .forEach(item -> normalList.add(new ItemStack(Material.valueOf(item))));
        plugin.getConfig().getStringList("item-list.hard")
                .forEach(item -> hardList.add(new ItemStack(Material.valueOf(item))));
        plugin.getConfig().getStringList("item-list.impossible")
                .forEach(item -> impossibleList.add(new ItemStack(Material.valueOf(item))));

        impossibleList.addAll(hardList);
        hardList.addAll(normalList);
        normalList.addAll(easyList);
        bingoInfo = plugin.getDescription().getName() + "\n" + plugin.getDescription().getAuthors();

        commandUsage = ChatColor.GREEN + "[Bingo] Command usages:\n" + plugin.getCommand("bingo").getDescription();
    }

    public List<ItemStack> returnItemList(String difficulty) {
        switch (difficulty) {
            case "easy":
                return easyList;
            case "normal":
                return normalList;
            case "hard":
                return hardList;
            case "impossible":
                return impossibleList;
            default:
                return normalList;
        }
    }

    public String returnBingoInfo() {
        return bingoInfo;
    }

    public String returnBingoCommandUsage() {
        return commandUsage;
    }

    private List<ItemStack> easyList = new ArrayList<>();
    private List<ItemStack> normalList = new ArrayList<>();
    private List<ItemStack> hardList = new ArrayList<>();
    private List<ItemStack> impossibleList = new ArrayList<>();
    private String bingoInfo = "";
    private String commandUsage = "";

}
