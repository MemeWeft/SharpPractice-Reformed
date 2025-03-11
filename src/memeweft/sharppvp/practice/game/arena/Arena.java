package me.memeweft.sharppvp.practice.game.arena;

import org.bukkit.Location;

public class Arena 
{
    private String name;
    private Location spawn1;
    private Location spawn2;
    
    public Arena(final String name) {
        this.name = name;
    }
    
    public Arena(final String name, final Location spawn1, final Location spawn2) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
    }
    
    public boolean isSetup() {
        return this.spawn1 != null && this.spawn2 != null;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Location getSpawn1() {
        return this.spawn1;
    }
    
    public void setSpawn1(final Location spawn1) {
        this.spawn1 = spawn1;
    }
    
    public Location getSpawn2() {
        return this.spawn2;
    }
    
    public void setSpawn2(final Location spawn2) {
        this.spawn2 = spawn2;
    }
}
