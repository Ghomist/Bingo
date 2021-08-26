package info.bcrc.mc.bingo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import info.bcrc.mc.bingo.util.BingoMapCreator;
import info.bcrc.mc.bingo.util.TpPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BingoGame {

    public static String announcer = "" + ChatColor.BOLD + ChatColor.GOLD + "[Bingo] " + ChatColor.RESET;

    protected enum BingoGameState {
        SETUP, START, END
    }

    protected class BingoTime {
        private int timeValue;

        protected BingoTime() {
            timeValue = 0;
        }

        protected void timeCount(int count) {
            if (timeValue < 3600)
                timeValue += count;
        }

        public String toString() {
            if (timeValue >= 3600) {
                return "Out of 1 hour!";
            } else {
                int m = timeValue / 60;
                String min;
                if (m < 10)
                    min = "0" + m;
                else
                    min = "" + m;

                int s = timeValue % 60;
                String second;
                if (s < 10)
                    second = "0" + s;
                else
                    second = "" + s;

                return min + ":" + second;
            }
        }
    }

    public class BingoPlayer {
        public UUID uuid;

        String team;
        BingoMap bingoMap;
        Score score;
        boolean hasFinishedBingo = false;

        public BingoPlayer(UUID uuid, BingoMap bingoMap, String team, Score score) {
            this.uuid = uuid;
            this.team = team;
            this.bingoMap = bingoMap;
            this.score = score;
            score.setScore(0);
        }

        boolean is(UUID uuid) {
            return this.uuid.equals(uuid);
        }

        boolean isInTeam(String team) {
            return this.team.equals(team);
        }

    }

    protected BingoGame(Bingo plugin, boolean collectAll, boolean shareInventory) {
        // handle gamemode
        this.collectAll = collectAll;
        this.shareInventory = shareInventory;

        bingoMapCreator = new BingoMapCreator(plugin);

        // make scoreboard
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("bingo", "", "Bingo Score");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // info
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            p.sendMessage(announcer + "A bingo game has been set up");
            p.sendMessage(announcer + "Mode setting: ");
            p.sendMessage("Collect All: " + collectAll);
            p.sendMessage("Share Inventory: " + shareInventory);
        });
        plugin.getServer().getOnlinePlayers()
                .forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f));

        System.out.println(announcer + "A bingo game has been set up");

        gameState = BingoGameState.SETUP;

    }

    protected void playerJoin(Player player, String team) {
        if (!gameState.equals(BingoGameState.SETUP))
            return;

        if (isBingoPlayer(player)) {
            // handle player rejoin
            String formerTeam = getBingoPlayer(player.getUniqueId()).team;
            players.remove(getBingoPlayer(player.getUniqueId()));
            scoreboard.resetScores(ChatColor.valueOf(formerTeam.toUpperCase()) + player.getName());
        }
        // create new player data
        Inventory newInventory = Bukkit.createInventory(player, 45, player.getName() + "'s Bingo Map");
        ItemStack[] itemList = bingoMapCreator.returnDefaultList();
        for (int i = 0; i < 45; i++) {
            newInventory.setItem(i, itemList[i]);
        }
        players.add(new BingoPlayer(player.getUniqueId(), new BingoMap(player, newInventory), team,
                scoreboard.getObjective("bingo").getScore(ChatColor.valueOf(team.toUpperCase()) + player.getName())));

        // potion effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 255, false, false));

        // info
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1f);
        String msg = announcer + formatPlayerName(player) + "has joined as " + team + " team";
        messageAll(msg);
        System.out.println(msg);
        printPlayerList(player);
        player.setScoreboard(scoreboard);
    }

    protected void startGame(Bingo plugin, Player sponsor) {
        BingoPlayer bSponsor = getBingoPlayer(sponsor.getUniqueId());
        if (bSponsor == null || !players.contains(bSponsor))
            return;

        if (!gameState.equals(BingoGameState.SETUP))
            return;

        players.forEach(bp -> {
            // get player
            if (!Bukkit.getOfflinePlayer(bp.uuid).isOnline())
                return;
            Player p = Bukkit.getOfflinePlayer(bp.uuid).getPlayer();

            // manage players' potion effects
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999, 255, false, false));

            p.getInventory().clear();
            // give players nether start
            ItemStack item = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Bingo card");
            meta.setLore(Arrays.asList(ChatColor.GREEN + "Click with this to open bingo map", ""));
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            p.getInventory().setItem(8, item);
            // give players boots with depth strider
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
            boots.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
            p.getInventory().setBoots(boots);

            // random teleport
            TpPlayer.randomTpPlayer(p);

            // set spawnpoint
            p.setBedSpawnLocation(p.getLocation(), true);

            // set gamemode to survival
            p.setGameMode(GameMode.SURVIVAL);

            // info
            p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
            p.sendTitle(formatTitle("Game Start"), "Go and seek items on the map!", 10, 100, 20);
            p.sendMessage(announcer + "The game has been started");
        });
        sponsor.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        sponsor.getWorld().setTime(0);
        // remove all the advancements
        sponsor.getServer().dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke @a everything");

        System.out.println(announcer + "The game has been started by " + formatPlayerName(sponsor));

        timeCounter = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                players.forEach(bp -> {
                    Player p = Bukkit.getPlayer(bp.uuid);
                    if (p != null) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(gameTime.toString()));
                    }
                });
                if (gameTime.timeValue > 3600) {
                    playerFinishBingo(null, true);
                } else {
                    gameTime.timeCount(1);
                }
            }
        }, 0, 20);

        gameState = BingoGameState.START;

    }

    protected void playerGetItem(Player player, ItemStack item) {
        if (!gameState.equals(BingoGameState.START) || item == null)
            return;

        BingoPlayer bPlayer = getBingoPlayer(player.getUniqueId());

        // get item index in advance
        if (bPlayer.bingoMap.getIndex(item) == -1)
            return;
        int index = bPlayer.bingoMap.getIndex(item);

        if (shareInventory) {
            for (BingoPlayer p : players) {
                p.bingoMap.playerGetItem(player, item, bPlayer.team);
                if (p.isInTeam(bPlayer.team))
                    p.score.setScore(p.score.getScore() + 1);
            }
        } else {
            for (BingoPlayer p : players) {
                if (p.isInTeam(bPlayer.team)) {
                    p.bingoMap.playerGetItem(player, item, bPlayer.team);
                    p.score.setScore(p.score.getScore() + 1);
                }
            }
        }

        // info
        String msg = announcer + formatPlayerName(player) + "has achieved" + formatItemName(item) + "at " + gameTime;
        players.forEach(bp -> {
            Player p = Bukkit.getPlayer(bp.uuid);
            if (p != null) {
                p.sendMessage(msg);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
            }
        });
        System.out.println(msg);

        // hand in one item in need
        item.setAmount(item.getAmount() - 1);
        if (!bPlayer.hasFinishedBingo) {
            if (collectAll && !shareInventory && bPlayer.bingoMap.testAllCollected(25)) {
                playerFinishBingo(player, false);
                gameState = BingoGameState.END;
                return;
            }

            if (collectAll && shareInventory && bPlayer.bingoMap.testAllCollected(players.size()))
                playerFinishBingo(player, false);

            if (!collectAll && bPlayer.bingoMap.testCross(index))
                playerFinishBingo(player, false);
        }
    }

    protected void playerFinishBingo(Player player, boolean isForcibly) {
        if (gameState.equals(BingoGameState.END))
            return;

        if (isForcibly) {
            String msg = "";
            if (player == null) {
                // shutdown by timer
                msg = announcer + ChatColor.RED + "Time is up!";
                players.forEach(bp -> {
                    Player p = Bukkit.getPlayer(bp.uuid);
                    if (p != null)
                        printPlayerList(p);
                });
            } else {
                // shutdown by player
                msg = announcer + ChatColor.RED + "The game had been shut up forcibly by " + formatPlayerName(player);
            }
            if (gameState.equals(BingoGameState.START))
                Bukkit.getScheduler().cancelTask(timeCounter.getTaskId());
            // info
            messageAll(msg);
            System.out.println(msg);

            // stop game
            gameState = BingoGameState.END;
            return;
        } else {
            if (collectAll) {
                String msg = announcer + "Game over! " + formatPlayerName(player) + " was the winner!";
                players.forEach(bp -> {
                    Player p = Bukkit.getPlayer(bp.uuid);
                    if (p != null) {
                        // close inventory to show info
                        p.closeInventory();
                        // info
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        p.sendMessage(msg);
                        printPlayerList(p);
                        p.sendTitle(formatTitle("Bingo!"), formatPlayerName(player) + " has been the first!", 10, 100,
                                20);

                        // handel inventory
                        if (p.getInventory().getItem(8).getType().equals(Material.NETHER_STAR))
                            p.getInventory().clear(8);
                        if (p.getInventory().getBoots().getType().equals(Material.LEATHER_BOOTS))
                            p.getInventory().getBoots().setType(Material.AIR);
                        // remove scoreboard
                        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }
                });
                // stop game
                Bukkit.getScheduler().cancelTask(timeCounter.getTaskId());
                gameState = BingoGameState.END;
            } else {
                finishedPlayerCount++;
                String msg = announcer + formatPlayerName(player) + "has finished the bingo with collecting "
                        + getBingoPlayer(player.getUniqueId()).score.getScore() + " items in " + gameTime;
                players.forEach(bp -> {
                    Player p = Bukkit.getPlayer(bp.uuid);
                    if (p != null) {
                        // close inventory to show info
                        p.closeInventory();
                        // info
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        p.sendMessage(msg);

                        if (finishedPlayerCount < players.size()) {
                            // keep gaming and info
                            p.sendTitle(formatTitle("No." + finishedPlayerCount + " bingo!"),
                                    formatPlayerName(player) + "has finished the bingo!", 10, 60, 20);
                        } else {
                            // info
                            p.sendTitle(formatTitle("Game Over"), "All player has finished bingo!", 10, 100, 20);
                            printPlayerList(p);
                            // handel inventory
                            if (p.getInventory().getItem(8).getType().equals(Material.NETHER_STAR))
                                p.getInventory().clear(8);
                            if (p.getInventory().getBoots().getType().equals(Material.LEATHER_BOOTS))
                                p.getInventory().getBoots().setType(Material.AIR);
                            // remove scoreboard
                            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                        }
                    }
                    if (finishedPlayerCount == players.size()) {
                        // stop game
                        Bukkit.getScheduler().cancelTask(timeCounter.getTaskId());
                        gameState = BingoGameState.END;
                    }
                });
                System.out.println(msg);
            }
        }
    }

    protected BingoMap getBingoMap(Player player) {
        return getBingoPlayer(player.getUniqueId()).bingoMap;
    }

    protected void printPlayerList(CommandSender sender) {
        sender.sendMessage(announcer + "Player list:");
        System.out.println(announcer + "Player list:");
        for (BingoPlayer p : players) {
            if (Bukkit.getPlayer(p.uuid) != null) {
                sender.sendMessage(ChatColor.valueOf(p.team.toUpperCase()) + " - " + Bukkit.getPlayer(p.uuid).getName()
                        + " (" + p.score.getScore() + ")");
                System.out.println(p.team.toUpperCase() + " - " + Bukkit.getPlayer(p.uuid).getName() + " ("
                        + p.score.getScore() + ")");
            }
        }
    }

    protected BingoGameState getGameState() {
        return gameState;
    }

    protected void setGameState(BingoGameState gameState) {
        this.gameState = gameState;
    }

    protected boolean isBingoMap(Inventory inventory) {
        for (BingoPlayer p : players) {
            if (p.bingoMap.getInventory().equals(inventory)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isBingoPlayer(Player player) {
        for (BingoPlayer p : players) {
            if (player.getUniqueId().equals(p.uuid))
                return true;
        }
        return false;
    }

    protected Scoreboard getScoreboard() {
        return scoreboard;
    }

    private boolean collectAll;
    private boolean shareInventory;

    private BingoGameState gameState;
    private BingoMapCreator bingoMapCreator;

    private HashSet<BingoPlayer> players = new HashSet<>();

    private Scoreboard scoreboard;

    private int finishedPlayerCount = 0;

    private BukkitTask timeCounter;

    private BingoTime gameTime = new BingoTime();

    private BingoPlayer getBingoPlayer(UUID uuid) {
        for (BingoPlayer p : players) {
            if (p.is(uuid)) {
                return p;
            }
        }
        return null;
    }

    private void messageAll(String msg) {
        for (BingoPlayer p : players) {
            if (Bukkit.getPlayer(p.uuid) != null) {
                Bukkit.getPlayer(p.uuid).sendMessage(msg);
            }
        }
    }

    private String formatItemName(ItemStack item) {
        StringBuffer str = new StringBuffer(ChatColor.GREEN.toString());
        str.append(" [").append(ChatColor.BOLD).append(item.getType().getKey().getKey()).append(ChatColor.RESET)
                .append(ChatColor.GREEN).append("] ").append(ChatColor.RESET);
        return str.toString();
    }

    private String formatPlayerName(Player player) {
        StringBuffer str = new StringBuffer("");
        BingoPlayer bp = getBingoPlayer(player.getUniqueId());
        if (bp != null)
            str.append(ChatColor.valueOf(bp.team.toUpperCase())).append(player.getName()).append(" ")
                    .append(ChatColor.RESET);
        return str.toString();
    }

    private String formatTitle(String title) {
        StringBuffer str = new StringBuffer("");
        str.append(ChatColor.MAGIC).append("aaa").append(ChatColor.RESET).append(ChatColor.GOLD).append(" ")
                .append(title).append(" ").append(ChatColor.RESET).append(ChatColor.MAGIC).append("aaa")
                .append(ChatColor.RESET);
        return str.toString();
    }
}
