package me.memeweft.sharppvp.practice.util;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ItemBuilder 
{
    private Material type;
    private String name;
    private String lore;
    private int amount;
    private MaterialData data;
    
    public ItemBuilder(final Material type, final String name, final String lore) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = 1;
        this.data = new MaterialData(type);
    }
    
    public ItemBuilder(final Material type, final String name, final String lore, final int amount) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.data = new MaterialData(type);
    }
    
    public ItemBuilder(final Material type, final String name, final String lore, final int amount, final MaterialData data) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.data = data;
    }
    
    public ItemStack getItem() {
        final ItemStack item = new ItemStack(this.type);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.name));
        meta.setLore((List)Collections.singletonList(ChatColor.translateAlternateColorCodes('&', this.lore)));
        item.setItemMeta(meta);
        item.setAmount(this.amount);
        item.setData(this.data);
        return item;
    }
}
