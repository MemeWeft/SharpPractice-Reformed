package me.memeweft.sharppvp.practice.game.gametype;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.arena.Arena;
import me.memeweft.sharppvp.practice.player.Kit;
import me.memeweft.sharppvp.practice.util.LocationUtil;
import me.memeweft.sharppvp.practice.util.MiscUtil;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class GameTypeManager implements Listener
{
    private SharpPractice plugin;
    private FileConfiguration config;
    private List<GameType> gameTypes;
    private List<Player> editing;
    
    public GameTypeManager(final SharpPractice plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        this.editing = new ArrayList<Player>();
        this.gameTypes = new ArrayList<GameType>();
        final GameType gt;
        String in;
        String kitName;
        int startIndex;
        PlyInv inv;
        Inventory inv2;
        this.config.getConfigurationSection("gametype").getKeys(false).forEach(s -> {
            gt = new GameType(s);
            if (this.config.getString("gametype." + s + ".items") != null) {
                in = this.config.getString("gametype." + s + ".items");
                kitName = in.split("\\|")[0];
                startIndex = in.indexOf("|");
                inv = MiscUtil.playerInventoryFromString(in.substring(startIndex + 1, in.length() - 1));
                gt.setStartingKit(new Kit(kitName, inv));
            }
            if (this.config.getStringList("gametype." + s + ".arenas") != null) {
                gt.setPossibleArenas((List<Arena>)this.config.getStringList("gametype." + s + ".arenas").stream().map(an -> this.plugin.getArenaManager().getArena(an)).collect(Collectors.toList()));
            }
            if (this.config.getStringList("gametype." + s + ".display") != null) {
                gt.setDisplay(Material.getMaterial(this.config.getString("gametype." + s + ".display")));
            }
            if (this.config.getString("gametype." + s + ".display-name") != null) {
                gt.setDisplayName(this.config.getString("gametype." + s + ".display-name"));
            }
            if (this.config.getString("gametype." + s + ".editable") != null) {
                gt.setEditable(this.config.getBoolean("gametype." + s + ".editable"));
            }
            if (this.config.getString("gametype." + s + ".possible-gear") != null) {
                inv2 = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.stripColor(gt.getDisplayName()));
                inv2.setContents(MiscUtil.inventoryFromString(this.config.getString("gametype." + s + ".possible-gear")).getContents());
                gt.setPossibleGear(inv2);
            }
            if (this.config.getStringList("gametype." + s + ".signs") != null) {
                gt.setSigns((List<Location>)this.config.getStringList("gametype." + s + ".signs").stream().map(LocationUtil::getLocation).collect(Collectors.toList()));
            }
            this.gameTypes.add(gt);
            return;
        });
        this.plugin.getCmdFramework().registerCommands(new GameTypeCommands(this));
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        for (final GameType gt2 : this.getGameTypes()) {
            this.updateSigns(gt2);
        }
    }
    
    public GameType getGameType(final Predicate<GameType> test) {
        for (final GameType gt : this.gameTypes) {
            if (test.test(gt)) {
                return gt;
            }
        }
        return null;
    }
    
    public GameType getGameType(final String name) {
        return this.getGameType(gt -> name.equals(gt.getName()));
    }
    
    public GameType getGameTypeFromDisplayName(final String displayName) {
        return this.getGameType(gt -> displayName.equals(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())));
    }
    
    public GameType getGameTypeFromDisplayNameColorless(final String displayName) {
        return this.getGameType(gt -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())).equals(displayName));
    }
    
    public boolean doesGameTypeExist(final String name) {
        for (final GameType gt : this.getGameTypes()) {
            if (gt.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public void createGameType(final String name) {
        final GameType gt = new GameType(name);
        this.gameTypes.add(gt);
        this.saveGameTypes();
        this.plugin.getDuelManager().setupMenu();
        this.plugin.getPlayerDataManager().setupNewGameType(gt);
    }
    
    public void removeGameType(final String name) {
        final GameType gt = this.getGameType(name);
        this.gameTypes.remove(gt);
        this.saveGameTypes();
        this.plugin.getDuelManager().setupMenu();
    }
    
    public void addSign(final Location loc, final GameType gt) {
        gt.addSign(loc);
        this.saveGameTypes();
    }
    
    public void removeSign(final Location loc, final GameType gt) {
        gt.removeSign(loc);
        this.saveGameTypes();
    }
    
    public void saveGameTypes() {
        this.config.set("gametype", (Object)null);
        for (final GameType gt : this.gameTypes) {
            this.config.set("gametype." + gt.getName() + ".items", (Object)gt.getStartingKit().toString());
            this.config.set("gametype." + gt.getName() + ".arenas", (Object)gt.getPossibleArenas().stream().map((Function<? super Object, ?>)Arena::getName).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
            this.config.set("gametype." + gt.getName() + ".signs", (Object)gt.getSigns().stream().map((Function<? super Object, ?>)LocationUtil::getString).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
            if (gt.getDisplay() != null) {
                this.config.set("gametype." + gt.getName() + ".display", (Object)gt.getDisplay().name());
            }
            this.config.set("gametype." + gt.getName() + ".display-name", (Object)gt.getDisplayName());
            this.config.set("gametype." + gt.getName() + ".editable", (Object)gt.isEditable());
            if (gt.getPossibleGear() != null && gt.getPossibleGear().getContents() != null) {
                this.config.set("gametype." + gt.getName() + ".possible-gear", (Object)MiscUtil.inventoryToString(gt.getPossibleGear()));
            }
        }
        this.plugin.saveConfig();
    }
    
    @EventHandler
    public void onInvClose(final InventoryCloseEvent event) {
        if (this.editing.contains(event.getPlayer()) && this.getGameTypeFromDisplayNameColorless(event.getInventory().getName()) != null) {
            final GameType gt = this.getGameTypeFromDisplayNameColorless(event.getInventory().getName());
            gt.setPossibleGear(event.getInventory());
            this.saveGameTypes();
            this.editing.remove(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            this.getGameTypes().stream().filter(gt -> gt.getSigns().contains(event.getBlock().getLocation())).forEach(gt -> this.removeSign(event.getBlock().getLocation(), gt));
        }
    }
    
    public void addEditing(final Player ply) {
        this.editing.add(ply);
    }
    
    public void updateSigns(final GameType gt) {
        final List<Location> signs = new ArrayList<Location>();
        final Sign sign;
        final List<Location> list;
        gt.getSigns().stream().filter(loc -> loc.getBlock().getType() == Material.SIGN || loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN_POST).forEach(loc -> {
            sign = (Sign)loc.getBlock().getState();
            sign.setLine(0, gt.getDisplayName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.update();
            list.add(loc);
            return;
        });
        gt.setSigns(signs);
    }
    
    public List<GameType> getGameTypes() {
        return this.gameTypes;
    }
}
