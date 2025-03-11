package me.memeweft.sharppvp.practice;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.memeweft.sharppvp.practice.command.Command;
import me.memeweft.sharppvp.practice.command.CommandArgs;
import me.memeweft.sharppvp.practice.command.CommandFramework;
import me.memeweft.sharppvp.practice.game.arena.ArenaManager;
import me.memeweft.sharppvp.practice.game.gametype.GameTypeManager;
import me.memeweft.sharppvp.practice.game.match.DuelManager;
import me.memeweft.sharppvp.practice.game.match.MatchManager;
import me.memeweft.sharppvp.practice.player.InventoryManager;
import me.memeweft.sharppvp.practice.player.KitEditManager;
import me.memeweft.sharppvp.practice.player.PlayerDataManager;
import me.memeweft.sharppvp.practice.player.PlayerEvents;
import me.memeweft.sharppvp.practice.util.EntityHider;
import me.memeweft.sharppvp.practice.util.LocationUtil;
import me.memeweft.sharppvp.practice.util.PlayerTeleportFixListener;

public class SharpPractice 
{
	
    private CommandFramework cmdFramework;
    private InventoryManager inventoryManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private GameTypeManager gameTypeManager;
    private PlayerDataManager playerDataManager;
    private Location spawn;
    private EntityHider entityHider;
    private KitEditManager kitEditManager;
    private DuelManager duelManager;
    private PlayerEvents playerEvents;
    
    public void onEnable() {
        this.saveDefaultConfig();
        this.cmdFramework = new CommandFramework((Plugin)this);
        this.arenaManager = new ArenaManager(this);
        this.gameTypeManager = new GameTypeManager(this);
        this.matchManager = new MatchManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.kitEditManager = new KitEditManager(this);
        this.entityHider = new EntityHider((Plugin)this, EntityHider.Policy.BLACKLIST);
        this.duelManager = new DuelManager(this);
        this.playerEvents = new PlayerEvents(this);
        Bukkit.getPluginManager().registerEvents((Listener)new PlayerTeleportFixListener(), (Plugin)this);
        if (this.getConfig().contains("spawn")) {
            this.spawn = LocationUtil.getLocation(this.getConfig().getString("spawn"));
        }
        this.cmdFramework.registerCommands(this);
        for (final Player ply : Bukkit.getOnlinePlayers()) {
            this.inventoryManager.setDefaultInventory(ply);
            ply.teleport(this.getSpawn());
            ply.setFoodLevel(20);
            for (final Player see : Bukkit.getOnlinePlayers()) {
                if (!ply.canSee(see)) {
                    ply.showPlayer(see);
                }
            }
        }
    }
    
    @Command(name = "practice.setspawn", permission = "practice.setspawn", inGameOnly = true)
    public void onSetSpawn(final CommandArgs args) {
        this.spawn = args.getPlayer().getLocation();
        this.getConfig().set("spawn", (Object)LocationUtil.getString(this.spawn));
        this.saveConfig();
        args.getPlayer().sendMessage(ChatColor.GREEN + "Spawn Set!");
    }
    
    @Command(name = "practice.seteditor", permission = "practice.seteditor", inGameOnly = true)
    public void onSetEditor(final CommandArgs args) {
        final Location editor = args.getPlayer().getLocation();
        this.kitEditManager.setEditor(editor);
        this.getConfig().set("editor", (Object)LocationUtil.getString(editor));
        this.saveConfig();
        args.getPlayer().sendMessage(ChatColor.GREEN + "Edit Location Set!");
    }
    
    public CommandFramework getCmdFramework() {
        return this.cmdFramework;
    }
    
    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }
    
    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }
    
    public MatchManager getMatchManager() {
        return this.matchManager;
    }
    
    public GameTypeManager getGameTypeManager() {
        return this.gameTypeManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }
    
    public Location getSpawn() {
        return this.spawn;
    }
    
    public EntityHider getEntityHider() {
        return this.entityHider;
    }
    
    public KitEditManager getKitEditManager() {
        return this.kitEditManager;
    }
    
    public DuelManager getDuelManager() {
        return this.duelManager;
    }
    
    public PlayerEvents getPlayerEvents() {
        return this.playerEvents;
    }

}
