package me.memeweft.sharppvp.practice.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlyInv 
{
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    
    public PlyInv() {
    }
    
    public PlyInv(final ItemStack[] contents, final ItemStack[] armorContents) {
        this.contents = contents;
        this.armorContents = armorContents;
    }
    
    public static PlyInv fromPlayerInventory(final PlayerInventory inv) {
        return new PlyInv(inv.getContents(), inv.getArmorContents());
    }
    
    public ItemStack[] getContents() {
        return this.contents;
    }
    
    public void setContents(final ItemStack[] contents) {
        this.contents = contents;
    }
    
    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }
    
    public void setArmorContents(final ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }
}
