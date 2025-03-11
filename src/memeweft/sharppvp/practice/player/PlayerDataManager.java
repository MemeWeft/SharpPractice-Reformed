package me.memeweft.sharppvp.practice.player;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.gametype.GameType;
import me.memeweft.sharppvp.practice.util.MiscUtil;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class PlayerDataManager implements Listener 
{
	  private SharpPractice plugin;
	  
	  private HashMap<Player, PlayerData> playerData;
	  
	  public PlayerDataManager(SharpPractice plugin) {
	    this.plugin = plugin;
	    this.playerData = new HashMap<>();
	    Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin);
	    for (Player ply : Bukkit.getOnlinePlayers()) {
	      this.playerData.put(ply, new PlayerData(ply));
	      loadPlayerInfo(ply);
	    } 
	  }
	  
	  @EventHandler
	  public void onJoin(PlayerJoinEvent event) {
	    this.playerData.put(event.getPlayer(), new PlayerData(event.getPlayer()));
	    loadPlayerInfo(event.getPlayer());
	  }
	  
	  @EventHandler
	  public void onQuit(final PlayerQuitEvent event) {
	    if (this.playerData.containsKey(event.getPlayer()))
	      (new BukkitRunnable() {
	          public void run() {
	            ((PlayerData)PlayerDataManager.this.playerData.get(event.getPlayer())).save();
	            PlayerDataManager.this.playerData.remove(event.getPlayer());
	          }
	        }).runTaskLater((Plugin)this.plugin, 5L); 
	  }
	  
	  public void loadPlayerInfo(Player ply) {
	    PlayerData data = this.playerData.get(ply);
	    FileConfiguration config = data.getConfig();
	    if (config.contains("ratings")) {
	      for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
	        if (config.contains("ratings." + gt.getName())) {
	          data.setRating(gt, config.getInt("ratings." + gt.getName()), false);
	          continue;
	        } 
	        data.setRating(gt, 1000, false);
	      } 
	    } else {
	      for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
	        config.set("ratings." + gt.getName(), Integer.valueOf(1000));
	        data.setRating(gt, 1000, false);
	      } 
	    } 
	    if (config.contains("kits")) {
	      for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
	        if (!config.contains("kits." + gt.getName()))
	          for (int j = 1; j <= 5; j++)
	            config.set("kits." + gt.getName() + "." + j, "");  
	        for (int i = 1; i <= 5; i++) {
	          String in = config.getString("kits." + gt.getName() + "." + i);
	          if (in == null) {
	            data.setKit(gt, null, i, false);
	            config.set("kits." + gt.getName() + "." + i, "");
	          } else if (in.equals("")) {
	            data.setKit(gt, null, i, false);
	          } else {
	            String kitName = in.split("\\|")[0];
	            int startIndex = in.indexOf("|");
	            PlyInv inv = MiscUtil.playerInventoryFromString(in.substring(startIndex + 1, in.length() - 1));
	            data.setKit(gt, new Kit(kitName, inv), i, false);
	          } 
	        } 
	      } 
	    } else {
	      for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
	        for (int i = 1; i <= 5; i++)
	          config.set("kits." + gt.getName() + "." + i, ""); 
	        data.setupKit(gt);
	      } 
	    } 
	    data.save();
	  }
	  
	  public void setKit(Player ply, GameType gt, int postion, Kit kit) {
	    ((PlayerData)this.playerData.get(ply)).setKit(gt, kit, postion, true);
	  }
	  
	  public int getRating(Player ply, GameType gt) {
	    return ((Integer)((PlayerData)this.playerData.get(ply)).getRatings().get(gt)).intValue();
	  }
	  
	  public Kit[] getKits(Player ply, GameType gt) {
	    return ((PlayerData)this.playerData.get(ply)).getKits().get(gt);
	  }
	  
	  public Kit getKit(Player ply, GameType gt, int postion) {
	    return getKits(ply, gt)[postion - 1];
	  }
	  
	  public void removeKit(Player ply, GameType gt, int postion) {
	    ((PlayerData)this.playerData.get(ply)).removeKit(gt, postion, true);
	  }
	  
	  public void updateElo(Player ply, GameType gt, int scoreChange, boolean add) {
	    PlayerData data = this.playerData.get(ply);
	    int rating = data.getRating(gt);
	    if (add) {
	      ((PlayerData)this.playerData.get(ply)).setRating(gt, rating + scoreChange, true);
	    } else {
	      ((PlayerData)this.playerData.get(ply)).setRating(gt, rating - scoreChange, true);
	    } 
	  }
	  
	  public void saveKits(Player ply) {
	    ((PlayerData)this.playerData.get(ply)).save();
	  }
	  
	  private PlayerData getPlayerData(String player) {
	    for (Player ply : this.playerData.keySet()) {
	      if (ply.getName().equals(player))
	        return this.playerData.get(ply); 
	    } 
	    return null;
	  }
	  
	  public void setupNewGameType(GameType gt) {
	    for (PlayerData data : this.playerData.values())
	      data.setRating(gt, 1000, true); 
	  }
}
