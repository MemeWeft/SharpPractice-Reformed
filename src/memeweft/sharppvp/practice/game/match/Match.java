package me.memeweft.sharppvp.practice.game.match;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.arena.Arena;
import me.memeweft.sharppvp.practice.game.gametype.GameType;
import me.memeweft.sharppvp.practice.util.EntityHider;
import me.memeweft.sharppvp.practice.util.JsonBuilder;

public class Match implements Listener 
{
    private Arena arena;
    private GameType gameType;
    private Player player1;
    private Player player2;
    private SharpPractice plugin;
    private boolean started;
    private boolean ranked;
    private HashMap<Player, PearlCounter> counters;
    
    public Match(final Arena arena, final GameType gameType, final Player p1, final Player p2, final boolean ranked) {
        this.arena = arena;
        this.gameType = gameType;
        this.player1 = p1;
        this.player2 = p2;
        this.started = false;
        this.ranked = ranked;
        this.plugin = (SharpPractice)JavaPlugin.getPlugin((Class)SharpPractice.class);
        this.counters = new HashMap<Player, PearlCounter>();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
    }
    
    public void startMatch() {
        this.player1.teleport(this.arena.getSpawn1());
        this.player2.teleport(this.arena.getSpawn2());
        for (final Player ply : Bukkit.getOnlinePlayers()) {
            if (ply != this.player1 && ply != this.player2) {
                ply.hidePlayer(this.player1);
                ply.hidePlayer(this.player2);
                this.player1.hidePlayer(ply);
                this.player2.hidePlayer(ply);
            }
        }
        new BukkitRunnable() {
            public void run() {
                Match.this.player1.showPlayer(Match.this.player2);
                Match.this.player2.showPlayer(Match.this.player1);
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
        this.player1.sendMessage(ChatColor.YELLOW + "Starting match against " + ChatColor.GREEN + this.player2.getName());
        this.player2.sendMessage(ChatColor.YELLOW + "Starting match against " + ChatColor.GREEN + this.player1.getName());
        this.player1.getInventory().clear();
        this.player2.getInventory().clear();
        this.plugin.getInventoryManager().showKits(this.player1, this.gameType);
        this.plugin.getInventoryManager().showKits(this.player2, this.gameType);
        new BukkitRunnable() {
            private int i = 5;
            
            public void run() {
                if (this.i == 0) {
                    this.cancel();
                    Match.this.started = true;
                    for (final Player ply : new Player[] { Match.this.player1, Match.this.player2 }) {
                        if (ply == null) {
                            this.cancel();
                            return;
                        }
                        ply.sendMessage(ChatColor.GREEN + "Duel starting now!");
                        ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                    }
                    return;
                }
                for (final Player ply : new Player[] { Match.this.player1, Match.this.player2 }) {
                    if (ply == null) {
                        this.cancel();
                        return;
                    }
                    ply.sendMessage(ChatColor.YELLOW + "Match starting in " + ChatColor.GREEN + this.i + ChatColor.YELLOW + " seconds.");
                    ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                }
                --this.i;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }
    
    public void endMatch(final Player winner) {
        for (final Player ply : new Player[] { this.player1, this.player2 }) {
            this.plugin.getInventoryManager().setDefaultInventory(ply);
            ply.teleport(this.plugin.getSpawn());
            ply.setHealth(20);
            ply.setFoodLevel(20);
            ply.setLevel(0);
            for (final PotionEffectType type : PotionEffectType.values()) {
                if (type != null && ply.hasPotionEffect(type)) {
                    ply.removePotionEffect(type);
                }
            }
            ply.getActivePotionEffects().clear();
        }
        new BukkitRunnable() {
            public void run() {
                for (final Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply != null) {
                        if (ply != Match.this.player1 && Match.this.player1 != null) {
                            ply.showPlayer(Match.this.player1);
                            Match.this.player1.showPlayer(ply);
                        }
                        if (ply != Match.this.player2 && Match.this.player2 != null) {
                            ply.showPlayer(Match.this.player2);
                            Match.this.player2.showPlayer(ply);
                        }
                    }
                }
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
        if (this.ranked) {
            final double p1 = this.plugin.getPlayerDataManager().getRating(this.player1, this.gameType);
            final double p2 = this.plugin.getPlayerDataManager().getRating(this.player2, this.gameType);
            int scoreChange = 0;
            final double expectedp1 = 1.0 / (1.0 + Math.pow(10.0, (p1 - p2) / 400.0));
            final double expectedp2 = 1.0 / (1.0 + Math.pow(10.0, (p2 - p1) / 400.0));
            Player loser;
            if (winner == this.player1) {
                scoreChange = (int)(expectedp1 * 32.0);
                loser = this.player2;
            }
            else {
                scoreChange = (int)(expectedp2 * 32.0);
                loser = this.player1;
            }
            scoreChange = ((scoreChange > 25) ? 25 : scoreChange);
            this.plugin.getPlayerDataManager().updateElo(winner, this.gameType, scoreChange, true);
            this.plugin.getPlayerDataManager().updateElo(loser, this.gameType, scoreChange, false);
            for (final Player ply2 : new Player[] { this.player1, this.player2 }) {
                ply2.sendMessage(ChatColor.YELLOW + "Winner: " + winner.getName());
                final JsonBuilder message = new JsonBuilder(new String[0]).withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(this.player1.getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + this.player1.getName()).withText(this.player2.getName()).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + this.player2.getName());
                message.sendJson(ply2);
                ply2.sendMessage(ChatColor.YELLOW + "Elo Changes: " + ChatColor.GREEN + winner.getName() + " +" + scoreChange + " (" + this.plugin.getPlayerDataManager().getRating(winner, this.gameType) + ") " + ChatColor.RED + loser.getName() + " -" + scoreChange + " (" + this.plugin.getPlayerDataManager().getRating(loser, this.gameType) + ")");
            }
        }
        else {
            for (final Player ply : new Player[] { this.player1, this.player2 }) {
                ply.sendMessage(ChatColor.YELLOW + "Winner: " + winner.getName());
                final JsonBuilder message2 = new JsonBuilder(new String[0]).withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(this.player1.getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + this.player1.getName()).withText(this.player2.getName()).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + this.player2.getName());
                message2.sendJson(ply);
            }
        }
        this.plugin.getMatchManager().endMatch(this);
        this.player1 = null;
        this.player2 = null;
        this.arena = null;
        this.gameType = null;
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        if (event.getPlayer() == this.player1) {
            this.endMatch(this.player2);
        }
        else if (event.getPlayer() == this.player2) {
            this.endMatch(this.player1);
        }
    }
    
    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        if (this.player1 == event.getEntity() || this.player2 == event.getEntity()) {
            event.setDeathMessage((String)null);
            event.getDrops().clear();
            new BukkitRunnable() {
                public void run() {
                    event.getEntity().spigot().respawn();
                    if (event.getEntity() == Match.this.player1) {
                        Match.this.endMatch(Match.this.player2);
                    }
                    else {
                        Match.this.endMatch(Match.this.player1);
                    }
                    Match.this.started = false;
                }
            }.runTaskLater((Plugin)this.plugin, 2L);
        }
    }
    
    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && (this.player1 == event.getEntity() || this.player2 == event.getEntity())) {
            if (!this.started) {
                event.setCancelled(true);
            }
            else if (event.getDamage() >= ((Player)event.getEntity()).getHealth()) {
                this.plugin.getInventoryManager().storeInv(this.player1, event.getEntity() == this.player1);
                this.plugin.getInventoryManager().storeInv(this.player2, event.getEntity() == this.player2);
            }
        }
    }
    
    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (event.getPlayer() == this.player1 || event.getPlayer() == this.player2) {
            final EntityHider hider = this.plugin.getEntityHider();
            for (final Player ply : Bukkit.getOnlinePlayers()) {
                if (ply != this.player1 && ply != this.player2) {
                    hider.hideEntity(ply, (Entity)event.getItemDrop());
                }
            }
            new BukkitRunnable() {
                public void run() {
                    event.getItemDrop().remove();
                }
            }.runTaskLater((Plugin)this.plugin, 60L);
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == this.player1 || event.getPlayer() == this.player2)) {
            final Player shooter = event.getPlayer();
            if (!this.started) {
                event.setCancelled(true);
                shooter.sendMessage(ChatColor.RED + "You must wait for the match to start!");
            }
            else if (this.counters.containsKey(shooter)) {
                shooter.sendMessage(ChatColor.RED + "You are on cooldown for " + this.counters.get(shooter).getCooldown() + " more seconds.");
                event.setCancelled(true);
            }
            else {
                final PearlCounter counter = new PearlCounter(shooter, this);
                counter.runTaskTimer((Plugin)this.plugin, 0L, 20L);
                this.counters.put(shooter, counter);
            }
        }
    }
    
    @EventHandler
    public void onThrowItem(final ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.SPLASH_POTION) {
            final EntityHider hider = this.plugin.getEntityHider();
            if (event.getEntity().getShooter() == this.player1 || event.getEntity().getShooter() == this.player2) {
                for (final Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply != this.player1 && ply != this.player2) {
                        hider.hideEntity(ply, (Entity)event.getEntity());
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPotionSplashEvent(final PotionSplashEvent event) {
        if (event.getEntity().getShooter() == this.player1 || event.getEntity().getShooter() == this.player2) {
            event.getAffectedEntities().stream().filter(entity -> entity != this.player1 && entity != this.player2).forEach(entity -> event.getAffectedEntities().remove(entity));
            event.setCancelled(true);
            event.getAffectedEntities().stream().filter(entity -> entity == this.player1 || entity == this.player2).forEach(entity -> entity.addPotionEffects(event.getEntity().getEffects()));
        }
    }
    
    public void removeCounter(final PearlCounter counter) {
        this.counters.remove(counter.ply);
    }
    
    public boolean hasPlayer(final Player ply) {
        return this.player1 == ply || this.player2 == ply;
    }
    
    public GameType getGameType() {
        return this.gameType;
    }
    
    public boolean isRanked() {
        return this.ranked;
    }
    
    private class PearlCounter extends BukkitRunnable
    {
        private int counter;
        private Player ply;
        private Match match;
        
        public PearlCounter(final Player ply, final Match match) {
            this.ply = ply;
            this.counter = 16;
            this.match = match;
        }
        
        public void run() {
            this.ply.setLevel(this.counter);
            --this.counter;
            if (this.counter < 0) {
                this.cancel();
                this.match.removeCounter(this);
            }
        }
        
        public int getCooldown() {
            return this.counter + 1;
        }
    }
}
