package me.memeweft.sharppvp.practice.player;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;

public class PlayerEvents implements Listener
{
	  private SharpPractice plugin;
	  
	  public PlayerEvents(SharpPractice plugin) {
	    this.plugin = plugin;
	    Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin);
	  }
	  
	  @EventHandler
	  public void onDrop(final PlayerDropItemEvent event) {
	    if (!this.plugin.getMatchManager().isInMatch(event.getPlayer()) && !this.plugin.getKitEditManager().isEditing(event.getPlayer())) {
	      event.getItemDrop().remove();
	      (new BukkitRunnable() {
	          public void run() {
	            PlayerEvents.this.plugin.getInventoryManager().setDefaultInventory(event.getPlayer());
	          }
	        }).runTaskLater((Plugin)this.plugin, 2L);
	    } 
	  }
	  
	  @EventHandler
	  public void onJoin(final PlayerJoinEvent event) {
	    (new BukkitRunnable() {
	        public void run() {
	          event.getPlayer().teleport(PlayerEvents.this.plugin.getSpawn());
	        }
	      }).runTaskLater((Plugin)this.plugin, 5L);
	  }
}
