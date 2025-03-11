package me.memeweft.sharppvp.practice.game.gametype;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.memeweft.sharppvp.practice.game.arena.Arena;
import me.memeweft.sharppvp.practice.player.Kit;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class GameType 
{
    private String name;
    private String displayName;
    private Kit startingKit;
    private List<Arena> possibleArenas;
    private Material display;
    private boolean editable;
    private Inventory possibleGear;
    private List<Location> signs;
    
    public GameType(final String name) {
        this.name = name;
        this.displayName = name;
        this.startingKit = new Kit(this.displayName + "Default Kit", new PlyInv(new ItemStack[36], new ItemStack[4]));
        this.possibleArenas = new ArrayList<Arena>();
        this.display = Material.ANVIL;
        this.editable = false;
        this.possibleGear = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.stripColor(this.displayName));
        this.signs = new ArrayList<Location>();
    }
    
    public GameType(final String name, final String displayName, final Kit startingKit, final List<Arena> possibleArenas, final Material display, final boolean editable, final Inventory possibleGear, final List<Location> signs) {
        this.name = name;
        this.displayName = displayName;
        this.startingKit = startingKit;
        this.possibleArenas = possibleArenas;
        this.display = display;
        this.editable = editable;
        this.possibleGear = possibleGear;
        this.signs = signs;
    }
    
    public boolean isSetup() {
        return this.displayName != null && this.startingKit != null && this.possibleArenas != null && this.display != null;
    }
    
    public void setPossibleGear(final Inventory inv) {
        (this.possibleGear = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.displayName)))).setContents(inv.getContents());
    }
    
    public Inventory getPossibleGear() {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, this.possibleGear.getSize(), this.possibleGear.getName());
        inv.setContents(this.possibleGear.getContents());
        return inv;
    }
    
    public void setDisplayName(final String string) {
        this.displayName = string;
        this.setPossibleGear(this.getPossibleGear());
    }
    
    public String getDisplayNameColorless() {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.getDisplayName()));
    }
    
    public void addSign(final Location location) {
        this.signs.add(location);
    }
    
    public void removeSign(final Location location) {
        this.signs.remove(location);
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public Kit getStartingKit() {
        return this.startingKit;
    }
    
    public void setStartingKit(final Kit startingKit) {
        this.startingKit = startingKit;
    }
    
    public List<Arena> getPossibleArenas() {
        return this.possibleArenas;
    }
    
    public void setPossibleArenas(final List<Arena> possibleArenas) {
        this.possibleArenas = possibleArenas;
    }
    
    public Material getDisplay() {
        return this.display;
    }
    
    public void setDisplay(final Material display) {
        this.display = display;
    }
    
    public boolean isEditable() {
        return this.editable;
    }
    
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }
    
    public List<Location> getSigns() {
        return this.signs;
    }
    
    public void setSigns(final List<Location> signs) {
        this.signs = signs;
    }
}
