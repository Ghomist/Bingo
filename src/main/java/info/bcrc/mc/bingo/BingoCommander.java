package info.bcrc.mc.bingo;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class BingoCommander implements CommandExecutor, TabCompleter {

    public BingoCommander(Bingo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equals("bingo")) {
                if (args.length >= 1) {
                    switch (args[0]) {
                        case "setup":
                            // if (args.length == 2)
                            // plugin.bingoGame = new BingoGame(plugin, args[2]);
                            // else
                            // badInput(sender);
                            break;
                        case "start":
                            break;
                        default:
                            badInput(sender);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            badInput(sender);
        }
        return false;
    }

    private List<String> baseCommands = Arrays.asList("setup", "start");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (command.getName().equals("bingo")) {
            if (args.length < 1) {
                return baseCommands;
            } else {
                if (args.length == 1) {
                    if (args[1].equals("setup")) {
                        return Arrays.asList("allcollect", "normal");
                    } else {
                        return baseCommands;
                    }
                } else if (args.length == 2) {
                    return null;
                }
            }
        }
        return null;
    }

    private void badInput(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "[Bingo]: Bad Input");
    }

    private Bingo plugin;

}
