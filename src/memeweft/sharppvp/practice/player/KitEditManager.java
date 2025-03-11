package me.memeweft.sharppvp.practice.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.gametype.GameType;
import me.memeweft.sharppvp.practice.util.EntityHider;
import me.memeweft.sharppvp.practice.util.IconMenu;
import me.memeweft.sharppvp.practice.util.LocationUtil;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class KitEditManager implements Listener
{
	  private SharpPractice plugin;
	  
	  private HashMap<Player, GameType> editing;
	  
	  private Location editLocation;
	  
	  private HashMap<Player, Kit> renaming;
	  
	  private PlayerDataManager playerDataManager;
	  
	  private HashMap<Player, IconMenu> menus;
	  
	  private List<Player> clickCooldown;
	  
	  public KitEditManager(SharpPractice plugin) {
	    this.plugin = plugin;
	    this.editing = new HashMap<>();
	    if (this.plugin.getConfig().contains("editor"))
	      this.editLocation = LocationUtil.getLocation(this.plugin.getConfig().getString("editor")); 
	    Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin);
	    this.renaming = new HashMap<>();
	    this.menus = new HashMap<>();
	    this.playerDataManager = this.plugin.getPlayerDataManager();
	    this.clickCooldown = new ArrayList<>();
	    Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin);
	  }
	  
	  public void beginEditing(final Player ply, GameType gt) {
	    ply.teleport(this.editLocation);
	    ply.getInventory().clear();
	    (new BukkitRunnable() {
	        public void run() {
	          for (Player eply : KitEditManager.this.editing.keySet()) {
	            eply.hidePlayer(ply);
	            ply.hidePlayer(eply);
	          } 
	        }
	      }).runTaskLater((Plugin)this.plugin, 2L);
	    this.editing.put(ply, gt);
	    ply.sendMessage(ChatColor.GREEN + "Now editing kits for " + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())));
	  }
	  
	  public void setEditor(Location editLocation) {
	    this.editLocation = editLocation;
	  }
	  
	  @EventHandler
	  public void onRightClickSign(PlayerInteractEvent event) {
	    Player ply = event.getPlayer();
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) && this.editing.containsKey(ply)) {
	      this.editing.remove(ply);
	      ply.teleport(this.plugin.getSpawn());
	      this.plugin.getInventoryManager().setDefaultInventory(ply);
	      for (Player eply : Bukkit.getOnlinePlayers()) {
	        if (ply != eply) {
	          ply.showPlayer(eply);
	          eply.showPlayer(ply);
	        } 
	      } 
	    } 
	  }
	  
	  @EventHandler
	  public void onItemDrop(final PlayerDropItemEvent event) {
	    Player ply = event.getPlayer();
	    if (this.editing.containsKey(ply)) {
	      EntityHider hider = this.plugin.getEntityHider();
	      for (Player p : Bukkit.getOnlinePlayers()) {
	        if (p != ply)
	          hider.hideEntity(ply, (Entity)event.getItemDrop()); 
	      } 
	      (new BukkitRunnable() {
	          public void run() {
	            event.getItemDrop().remove();
	          }
	        }).runTaskLater((Plugin)this.plugin, 200L);
	    } 
	  }
	  
	  @EventHandler
	  public void onOpenChest(PlayerInteractEvent event) {
	    Player ply = event.getPlayer();
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST && this.editing.containsKey(ply)) {
	      event.setCancelled(true);
	      GameType gt = this.editing.get(ply);
	      ply.openInventory(gt.getPossibleGear());
	    } 
	  }
	  
	  @EventHandler
	  public void onClickAnvil(PlayerInteractEvent event) {
	    Player ply = event.getPlayer();
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ANVIL && this.editing.containsKey(ply)) {
	      IconMenu menu = getKitMenu(ply, this.editing.get(ply));
	      this.menus.put(ply, menu);
	      menu.open(ply);
	      event.setCancelled(true);
	    } 
	  }
	  
	  private IconMenu getKitMenu(Player ply, final GameType gt) {
	    IconMenu menu = new IconMenu("Manage " + gt.getDisplayNameColorless() + " kits", 36, event -> {
	          int kitNumber;
	          Kit kit1;
	          Kit kit2;
	          Kit kit3;
	          Kit kit4;
	          Player ply1 = event.getPlayer();
	          String itemName = ChatColor.stripColor(event.getName());
	          if (itemName.equals(""))
	            return; 
	          if (this.clickCooldown.contains(ply1))
	            return; 
	          this.clickCooldown.add(ply1);
	          (new BukkitRunnable() {
	              public void run() {
	                KitEditManager.this.clickCooldown.remove(ply1);
	              }
	            },  ).runTaskLater((Plugin)this.plugin, 5L);
	          String[] itemNameA = itemName.split(" ");
	          String option = itemNameA[0] + " " + itemNameA[1] + " ";
	          String kitName = itemName.replaceFirst(option, "");
	          switch (option) {
	            case "Save kit ":
	              kitNumber = Integer.parseInt(kitName);
	              kit1 = new Kit("Custom " + gt.getDisplayNameColorless() + " kit " + kitNumber, PlyInv.fromPlayerInventory(ply1.getInventory()));
	              this.playerDataManager.setKit(ply1, gt, kitNumber, kit1);
	              ply1.sendMessage(ChatColor.GREEN + "Saved kit: " + ChatColor.GOLD + kitName);
	              (new BukkitRunnable() {
	                  public void run() {
	                    KitEditManager.this.updateMenu(ply1, gt);
	                  }
	                }).runTaskLater((Plugin)this.plugin, 5L);
	              break;
	            case "Save kit: ":
	              kit2 = this.playerDataManager.getKit(ply1, gt, getPostion(event.getPosition()));
	              kit2.setInv(PlyInv.fromPlayerInventory(ply1.getInventory()));
	              ply1.sendMessage(ChatColor.GREEN + "Saved kit: " + ChatColor.GOLD + kitName);
	              break;
	            case "Load kit: ":
	              kit3 = this.playerDataManager.getKit(ply1, gt, getPostion(event.getPosition()));
	              if (kit3.getInv().getArmorContents() != null)
	                ply1.getInventory().setArmorContents(kit3.getInv().getArmorContents()); 
	              ply1.getInventory().setContents(kit3.getInv().getContents());
	              ply1.sendMessage(ChatColor.GREEN + "Loaded kit: " + ChatColor.GOLD + kit3.getName());
	              break;
	            case "Rename kit: ":
	              kit4 = this.playerDataManager.getKit(ply1, gt, getPostion(event.getPosition()));
	              if (this.renaming.containsKey(ply1)) {
	                ply1.sendMessage(ChatColor.RED + "Cancelling renaming of " + ((Kit)this.renaming.get(ply1)).getName());
	                this.renaming.remove(ply1);
	              } 
	              this.renaming.put(ply1, kit4);
	              ply1.sendMessage(ChatColor.GREEN + "Type a new name for " + ChatColor.GOLD + kit4.getName());
	              (new BukkitRunnable() {
	                  public void run() {
	                    if (KitEditManager.this.renaming.containsKey(ply1) && KitEditManager.this.renaming.get(ply1) == kit4) {
	                      KitEditManager.this.renaming.remove(ply1);
	                      ply1.sendMessage(ChatColor.RED + "Renaming cancelled");
	                    } 
	                  }
	                }).runTaskLater((Plugin)this.plugin, 300L);
	              break;
	            case "Delete kit: ":
	              this.playerDataManager.removeKit(ply1, gt, getPostion(event.getPosition()));
	              ply1.sendMessage(ChatColor.RED + "Deleted kit: " + ChatColor.GOLD + kitName);
	              break;
	          } 
	          ply1.closeInventory();
	        }(Plugin)this.plugin);
	    for (int i = 1; i <= 5; i++) {
	      Kit kit = this.playerDataManager.getKit(ply, gt, i);
	      int slot = (i - 1) * 2;
	      if (kit == null) {
	        menu.setOption(slot, Material.CHEST, ChatColor.GREEN + "Save kit " + ChatColor.GOLD + i, new String[0]);
	      } else {
	        String kitName = kit.getName();
	        menu.setOption(slot, Material.CHEST, ChatColor.GREEN + "Save kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 9, Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Load kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 18, Material.NAME_TAG, ChatColor.YELLOW + "Rename kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 27, Material.FIRE, ChatColor.RED + "Delete kit: " + ChatColor.GOLD + kitName, new String[0]);
	      } 
	    } 
	    return menu;
	  }
	  
	  @EventHandler
	  public void onRenameKit(AsyncPlayerChatEvent event) {
	    Player ply = event.getPlayer();
	    if (this.renaming.containsKey(ply)) {
	      Kit kit = this.renaming.get(ply);
	      String newName = event.getMessage();
	      ply.sendMessage(ChatColor.GOLD + kit.getName() + ChatColor.GREEN + " renamed to: " + ChatColor.GOLD + newName);
	      ((Kit)this.renaming.get(ply)).setName(newName);
	      this.renaming.remove(ply);
	      this.playerDataManager.saveKits(ply);
	      event.setCancelled(true);
	    } 
	  }
	  
	  @EventHandler
	  public void onInventoryClose(InventoryCloseEvent event) {
	    if (this.menus.containsKey(event.getPlayer()))
	      this.menus.remove(event.getPlayer()); 
	  }
	  
	  public void updateMenu(Player ply, GameType gt) {
	    if (!this.menus.containsKey(ply))
	      return; 
	    IconMenu menu = this.menus.get(ply);
	    for (int i = 1; i <= 5; i++) {
	      Kit kit = this.playerDataManager.getKit(ply, gt, i);
	      int slot = (i - 1) * 2;
	      if (kit == null) {
	        menu.setOption(slot, Material.CHEST, ChatColor.GREEN + "Save kit " + ChatColor.GOLD + i, new String[0]);
	      } else {
	        String kitName = kit.getName();
	        menu.setOption(slot, Material.CHEST, ChatColor.GREEN + "Save kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 9, Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Load kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 18, Material.NAME_TAG, ChatColor.YELLOW + "Rename kit: " + ChatColor.GOLD + kitName, new String[0]);
	        menu.setOption(slot + 27, Material.FIRE, ChatColor.RED + "Delete kit: " + ChatColor.GOLD + kitName, new String[0]);
	      } 
	    } 
	  }
	  
	  private int getPostion(int slot) {
	    if (slot > 26) {
	      slot -= 27;
	    } else if (slot > 17) {
	      slot -= 18;
	    } else if (slot > 8) {
	      slot -= 9;
	    } 
	    return slot / 2 + 1;
	  }
	  
	  public boolean isEditing(Player player) {
	    return this.editing.containsKey(player);
	  }
}
