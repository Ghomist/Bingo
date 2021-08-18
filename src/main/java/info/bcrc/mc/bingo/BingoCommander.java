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
            if (command.getName().equalsIgnoreCase("bingo")) {
                if (args.length >= 1) {
                    switch (args[0]) {
                        case "setup":
                            if (args.length >= 1 && args.length <= 3) {
                                boolean allcollect = false;
                                if (Arrays.asList(args).contains("allcollect"))
                                    allcollect = true;

                                boolean shareInventory = false;
                                if (Arrays.asList(args).contains("shareinventory"))
                                    shareInventory = true;
                                if (shareInventory) {
                                    plugin.logger.info("allcollect");
                                }

                                plugin.bingoGame = new BingoGame(plugin, allcollect, shareInventory);
                            } else {
                                badInput(sender);
                            }
                            break;

                        case "join":
                            if (args.length == 2)
                                plugin.bingoGame.playerJoin((Player) sender, args[1]);
                            else
                                badInput(sender);
                            break;

                        case "start":
                            plugin.bingoGame.startGame((Player) sender);
                            break;

                        case "shutdown":
                            plugin.bingoGame.playerFinishBingo(null);
                            break;

                        case "playerlist":
                            plugin.bingoGame.printPlayerList((Player) sender);
                            break;

                        case "help":
                            sender.sendMessage(plugin.configHandler.returnBingoCommandUsage());
                            break;

                        case "check":
                            if (args.length == 2) {
                                if (plugin.bingoGame.getGameState().equals(BingoGameState.START)) {
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
    private List<String> allColours = Arrays.asList("white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red" /* ,"black" */);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("bingo") && args.length > 0) {
            if (args.length > 1) {
                switch (args[0]) {
                    case "setup":
                        if (args.length < 3)
                            return Arrays.asList("allcollect", "shareinventory");
                    case "join":
                        if (args.length == 2)
                            return allColours;
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
        sender.sendMessage(ChatColor.RED + "[Bingo]: Bad Input");
    }

    private Bingo plugin;

}
