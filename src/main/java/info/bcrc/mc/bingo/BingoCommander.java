package info.bcrc.mc.bingo;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import info.bcrc.mc.bingo.BingoGame.BingoGameState;
import info.bcrc.mc.bingo.util.TpPlayer;

public class BingoCommander implements CommandExecutor, TabCompleter {

    public BingoCommander(Bingo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("bingo") && sender.hasPermission("bingo.default")) {
                if (args.length >= 1) {
                    switch (args[0]) {
                        case "setup":
                            if (args.length >= 1 && args.length <= 3
                                    && !plugin.bingoGame.getGameState().equals(BingoGameState.START)) {
                                boolean allcollect = false;
                                if (Arrays.asList(args).contains("allcollect"))
                                    allcollect = true;

                                boolean shareInventory = false;
                                if (Arrays.asList(args).contains("shareinventory"))
                                    shareInventory = true;

                                plugin.bingoGame = new BingoGame(plugin, allcollect, shareInventory);
                            } else {
                                badInput(sender);
                            }
                            break;

                        case "join":
                            if (args.length == 2 && allTeams.contains(args[1]))
                                plugin.bingoGame.playerJoin((Player) sender, args[1]);
                            else
                                badInput(sender);
                            break;

                        case "start":
                            plugin.bingoGame.startGame(plugin, (Player) sender);
                            break;

                        case "shutdown":
                            plugin.bingoGame.playerFinishBingo((Player) sender, true);
                            break;

                        case "playerlist":
                            plugin.bingoGame.printPlayerList(sender);
                            break;

                        case "help":
                            sender.sendMessage(plugin.configHandler.returnBingoCommandUsage());
                            break;

                        case "check":
                            if (args.length == 2) {
                                if (plugin.bingoGame.getGameState().equals(BingoGameState.START)
                                        || plugin.bingoGame.getGameState().equals(BingoGameState.END)) {
                                    Player sponsor = (Player) sender;
                                    sponsor.openInventory(plugin.bingoGame.getBingoMap(plugin.server.getPlayer(args[1]))
                                            .getInventory());
                                }
                            } else {
                                badInput(sender);
                            }
                            break;

                        case "up":
                            TpPlayer.tpPlayerToGround((Player) sender);
                            break;

                        default:
                            badInput(sender);
                            break;
                    }
                } else {
                    sender.sendMessage(plugin.configHandler.returnBingoInfo());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            badInput(sender);
            return false;
        }
    }

    private List<String> baseCommands = Arrays.asList("setup", "join", "start", "playerlist", "shutdown", "help",
            "check", "up");
    private List<String> allTeams = Arrays.asList("red", "yellow", "blue", "green"/* ,gray */);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("bingo") && args.length > 0) {
            if (args.length > 1) {
                switch (args[0]) {
                    case "setup":
                        if (args.length <= 3)
                            return Arrays.asList("allcollect", "shareinventory");
                    case "join":
                        if (args.length == 2)
                            return allTeams;
                    case "start":
                        return null;
                    default:
                        return null;
                }
            } else {
                return baseCommands;
            }
        }

        return null;
    }

    private void badInput(CommandSender sender) {
        sender.sendMessage(BingoGame.announcer + ChatColor.RED + "Bad Input");
        sender.sendMessage(BingoGame.announcer + ChatColor.YELLOW
                + "Warning only. Please check server terminal for further information.");
        plugin.logger.info(BingoGame.announcer + ChatColor.RED + "Bad Input");
    }

    private Bingo plugin;

}
