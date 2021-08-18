package info.bcrc.mc.bingo;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import info.bcrc.mc.bingo.util.BingoMapCreator;
import info.bcrc.mc.bingo.util.TpPlayer;

public class BingoGame {

    protected enum BingoGameState {
        SETUP, START, END
    }

    protected class BingoPlayer {
        UUID uuid;
        String team;
        BingoMap bingoMap;
        Score score;

        protected BingoPlayer(UUID uuid, BingoMap bingoMap, String team, Score score) {
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
        Objective objective = scoreboard.registerNewObjective("bingo", "", "");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Bingo Score");

        // info
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage("[Bingo] A bingo game has been set up"));
        plugin.getServer().getOnlinePlayers()
                .forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f));

        gameState = BingoGameState.SETUP;

    }

    protected void playerJoin(Player player, String team) {
        if (!gameState.equals(BingoGameState.SETUP))
            return;

        if (isBingoPlayer(player)) {
            // handle player rejoin
            getBingoPlayer(player.getUniqueId()).team = team;
        } else {
            // create new player data
            Inventory newInventory = Bukkit.createInventory(player, 45, player.getName() + "'s Bingo Map");
            ItemStack[] itemList = bingoMapCreator.returnDefaultList();
            for (int i = 0; i < 45; i++) {
                newInventory.setItem(i, itemList[i]);
            }
            players.add(new BingoPlayer(player.getUniqueId(), new BingoMap(player, newInventory), team,
                    scoreboard.getObjective("bingo").getScore(player.getName())));

            // potion effects
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 255, false, false));
        }

        // info
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1f);
        messageAll(ChatColor.valueOf(team.toUpperCase()) + "[Bingo] " + player.getName() + " have joined as " + team
                + " team");
        printPlayerList(player);
        player.setScoreboard(scoreboard);
    }

    protected void startGame(Player sponsor) {
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

            p.getInventory().clear();
            // give players nether start
            ItemStack item = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Bingo card");
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            p.getInventory().setItem(8, item);

            // give players boots with depth strider
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 5);
            p.getInventory().setBoots(boots);

            // random teleport
            TpPlayer.randomTpPlayer(p);
            p.setBedSpawnLocation(p.getLocation(), true);

            // set gamemode to survival
            p.setGameMode(GameMode.SURVIVAL);

            // set spawnpoint
            p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);

            // info
            p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1f, 1f);
            p.sendMessage("[Bingo] The game has been started");
        });
        sponsor.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        sponsor.getWorld().setTime(1000);
        // remove all the advancements
        // p.performCommand("/advancement revoke @s everything");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "/advancement revoke @a everything");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke @a everything");

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

        players.forEach(bp -> {
            Player p = Bukkit.getPlayer(bp.uuid);
            if (p != null) {
                p.sendMessage(ChatColor.valueOf(bPlayer.team.toUpperCase()) + "[Bingo] " + player.getName()
                        + " has achieved [" + item.getType().getKey().getKey() + "]");
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
            }
        });

        // hand in one item in need
        item.setAmount(item.getAmount() - 1);

        if (collectAll && !shareInventory && bPlayer.bingoMap.testAllCollected(25)) {
            playerFinishBingo(player);
            gameState = BingoGameState.END;
            return;
        }

        if (collectAll && shareInventory && bPlayer.bingoMap.testAllCollected(25 / players.size())) {
            playerFinishBingo(player);
            gameState = BingoGameState.END;
        }

        if (!collectAll && bPlayer.bingoMap.testCross(index)) {
            playerFinishBingo(player);
            gameState = BingoGameState.END;
        }

    }

    protected void playerFinishBingo(Player player) {
        if (gameState.equals(BingoGameState.END))
            return;

        if (player == null) {
            messageAll(ChatColor.RED + "[Bingo] The game had been shut up forcibly");
            gameState = BingoGameState.END;
        } else {
            players.forEach(bp -> {
                Player p = Bukkit.getPlayer(bp.uuid);
                if (p != null) {
                    p.sendMessage(ChatColor.YELLOW + "[Bingo] " + player.getName()
                            + " has finished the bingo first with collecting " + bp.score + " items !");
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
            });
        }
    }

    protected BingoMap getBingoMap(Player player) {
        return getBingoPlayer(player.getUniqueId()).bingoMap;
    }

    protected void printPlayerList(Player player) {
        player.sendMessage("[Bingo] Player list:");
        for (BingoPlayer p : players) {
            if (Bukkit.getPlayer(p.uuid) != null)
                player.sendMessage(
                        ChatColor.valueOf(p.team.toUpperCase()) + " - " + Bukkit.getPlayer(p.uuid).getName());
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

}
