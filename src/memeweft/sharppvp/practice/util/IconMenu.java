package me.memeweft.sharppvp.practice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class IconMenu implements Listener 
{	
    private String name;
    private int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;
    private String[] optionNames;
    private ItemStack[] optionIcons;
    List<Player> open;
    
    public IconMenu(final String name, final int size, final OptionClickEventHandler handler, final Plugin plugin) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.open = new ArrayList<Player>();
        plugin.getServer().getPluginManager().registerEvents((Listener)this, plugin);
    }
    
    public IconMenu setOption(final int position, final Material icon, final String name, final String... info) {
        final ItemStack item = this.setItemNameAndLore(icon, name, info);
        if (this.optionIcons[position] != null && this.optionIcons[position].getType() == icon) {
            this.updateItem(position, item.getItemMeta());
        }
        this.optionIcons[position] = item;
        this.optionNames[position] = name;
        return this;
    }
    
    public void open(final Player player) {
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)player, this.size, this.name);
        for (int i = 0; i < this.optionIcons.length; ++i) {
            if (this.optionIcons[i] != null) {
                inventory.setItem(i, this.optionIcons[i]);
            }
        }
        this.open.add(player);
        player.openInventory(inventory);
    }
    
    public void destroy() {
        HandlerList.unregisterAll((Listener)this);
        this.handler = null;
        this.plugin = null;
        this.optionNames = null;
        this.optionIcons = null;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals(this.name)) {
            event.setCancelled(true);
            final int slot = event.getRawSlot();
            if (slot >= 0 && slot < this.size && this.optionNames[slot] != null) {
                final Plugin plugin = this.plugin;
                final OptionClickEvent e = new OptionClickEvent((Player)event.getWhoClicked(), slot, this.optionNames[slot]);
                this.handler.onOptionClick(e);
                if (e.willClose()) {
                    final Player p = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new Runnable() {
                        @Override
                        public void run() {
                            p.closeInventory();
                        }
                    }, 1L);
                }
                if (e.willDestroy()) {
                    this.destroy();
                }
            }
        }
    }
    
    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        if (this.open.contains(event.getPlayer())) {
            this.open.remove(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onLeave(final PlayerQuitEvent event) {
        if (this.open.contains(event.getPlayer())) {
            this.open.remove(event.getPlayer());
        }
    }
    
    private ItemStack setItemNameAndLore(final Material material, final String name, final String[] lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.setLore((List)Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }
    
    public void updateItem(final int slot, final ItemMeta imm) {
        for (final Player p : this.open) {
            p.getOpenInventory().getItem(slot).setItemMeta(imm);
            p.updateInventory();
        }
    }
    
    public ItemStack getItem(final int slot) {
        return this.optionIcons[slot];
    }
    
    public class OptionClickEvent
    {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;
        
        public OptionClickEvent(final Player player, final int position, final String name) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = false;
        }
        
        public Player getPlayer() {
            return this.player;
        }
        
        public int getPosition() {
            return this.position;
        }
        
        public String getName() {
            return this.name;
        }
        
        public boolean willClose() {
            return this.close;
        }
        
        public boolean willDestroy() {
            return this.destroy;
        }
        
        public void setWillClose(final boolean close) {
            this.close = close;
        }
        
        public void setWillDestroy(final boolean destroy) {
            this.destroy = destroy;
        }
    }
    
    public interface OptionClickEventHandler
    {
        void onOptionClick(final OptionClickEvent p0);
    }
}
