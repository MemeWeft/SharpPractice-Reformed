package me.memeweft.sharppvp.practice.game.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.configuration.file.FileConfiguration;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.util.LocationUtil;

public class ArenaManager 
{
    private SharpPractice plugin;
    private FileConfiguration config;
    private List<Arena> arenas;
    
    public ArenaManager(final SharpPractice plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        this.arenas = new ArrayList<Arena>();
        final Arena a;
        this.config.getConfigurationSection("arena").getKeys(false).forEach(s -> {
            a = new Arena(s);
            if (this.config.getString("arena." + s + ".spawn1") != null) {
                a.setSpawn1(LocationUtil.getLocation(this.config.getString("arena." + s + ".spawn1")));
            }
            if (this.config.getString("arena." + s + ".spawn2") != null) {
                a.setSpawn2(LocationUtil.getLocation(this.config.getString("arena." + s + ".spawn2")));
            }
            this.arenas.add(a);
            return;
        });
        this.plugin.getCmdFramework().registerCommands(new ArenaCommands(this));
    }
    
    public List<Arena> getArenas() {
        return this.arenas;
    }
    
    public Arena getArena(final Predicate<Arena> test) {
        for (final Arena arena : this.arenas) {
            if (test.test(arena)) {
                return arena;
            }
        }
        return null;
    }
    
    public Arena getArena(final String name) {
        return this.getArena(new Predicate<Arena>() {
            @Override
            public boolean test(final Arena arena) {
                return name.equals(arena.getName());
            }
        });
    }
    
    public boolean doesArenaExist(final String name) {
        for (final Arena a : this.arenas) {
            if (a.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public void createArena(final String name) {
        final Arena a = new Arena(name);
        this.arenas.add(a);
        this.saveArenas();
    }
    
    public void removeArena(final String name) {
        final Arena a = this.getArena(name);
        this.arenas.remove(a);
        this.saveArenas();
    }
    
    public void saveArenas() {
        this.config.set("arena", (Object)null);
        for (final Arena a : this.arenas) {
            this.config.set("arena." + a.getName() + ".spawn1", (Object)LocationUtil.getString(a.getSpawn1()));
            this.config.set("arena." + a.getName() + ".spawn2", (Object)LocationUtil.getString(a.getSpawn2()));
        }
        this.plugin.saveConfig();
    }
}
