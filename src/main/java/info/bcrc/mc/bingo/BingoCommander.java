package info.bcrc.mc.bingo;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class BingoCommander implements CommandExecutor, TabCompleter {

    protected Bingo plugin;

    protected BingoCommander(Bingo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("bingo")) {
            if (args[0].equals("setup")) {
                if (args.length == 2) {
                    if (args[1].equals("normal")) {
                        plugin.inGame = new BingoInGame(plugin);
                    } else if (args[1].equals("allcollect")) {

                    }

                }

            } else if (args[0].equals("start")) {

            }
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
                        return Arrays.asList("allcollect", "normal", "race");
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

}
