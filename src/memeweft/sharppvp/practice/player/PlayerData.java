package me.memeweft.sharppvp.practice.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.gametype.GameType;
import me.memeweft.sharppvp.practice.util.MiscUtil;

public class PlayerData 
{
	  private HashMap<GameType, Integer> ratings;
	  
	  private HashMap<GameType, Kit[]> kits;
	  
	  private FileConfiguration config;
	  
	  private Player player;
	  
	  private File file;
	  
	  public HashMap<GameType, Integer> getRatings() {
	    return this.ratings;
	  }
	  
	  public HashMap<GameType, Kit[]> getKits() {
	    return this.kits;
	  }
	  
	  public FileConfiguration getConfig() {
	    return this.config;
	  }
	  
	  public Player getPlayer() {
	    return this.player;
	  }
	  
	  public PlayerData(Player player) {
	    this.player = player;
	    this.ratings = new HashMap<>();
	    this.kits = (HashMap)new HashMap<>();
	    this.file = new File(((SharpPractice)JavaPlugin.getPlugin(SharpPractice.class)).getDataFolder().getAbsolutePath() + File.separator + "playerdata");
	    if (!this.file.exists())
	      this.file.mkdir(); 
	    this.file = new File(((SharpPractice)JavaPlugin.getPlugin(SharpPractice.class)).getDataFolder().getAbsolutePath() + File.separator + "playerdata" + File.separator + player.getUniqueId().toString() + ".yml");
	    if (!this.file.exists())
	      try {
	        this.file.createNewFile();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }  
	    this.config = (FileConfiguration)new YamlConfiguration();
	    try {
	      this.config.load(this.file);
	    } catch (IOException|org.bukkit.configuration.InvalidConfigurationException e) {
	      e.printStackTrace();
	    } 
	  }
	  
	  public void setRating(GameType gt, int rating, boolean save) {
	    this.ratings.put(gt, Integer.valueOf(rating));
	    if (save)
	      save(); 
	  }
	  
	  public void removeKit(GameType gt, int postion, boolean save) {
	    Kit[] kits = this.kits.get(gt);
	    kits[postion - 1] = null;
	    this.kits.put(gt, kits);
	    if (save)
	      save(); 
	  }
	  
	  public void removeKit(GameType gt, Kit kit, boolean save) {
	    Kit[] kits = this.kits.get(gt);
	    for (int i = 0; i < 5; i++) {
	      if (kits[i] == kit)
	        kits[i] = null; 
	    } 
	    this.kits.put(gt, kits);
	    if (save)
	      save(); 
	  }
	  
	  public void setKit(GameType gt, Kit kit, int postion, boolean save) {
	    if (!this.kits.containsKey(gt))
	      this.kits.put(gt, new Kit[5]); 
	    Kit[] kits = this.kits.get(gt);
	    kits[postion - 1] = kit;
	    this.kits.put(gt, kits);
	    if (save)
	      save(); 
	  }
	  
	  protected void save() {
	    for (GameType gt : this.ratings.keySet())
	      this.config.set("ratings." + gt.getName(), this.ratings.get(gt)); 
	    for (GameType gt : this.kits.keySet()) {
	      this.config.set("kits." + gt.getName(), new ArrayList());
	      for (int i = 1; i < 5; i++) {
	        Kit kit = ((Kit[])this.kits.get(gt))[i - 1];
	        if (kit == null) {
	          this.config.set("kits." + gt.getName() + "." + i, null);
	        } else {
	          this.config.set("kits." + gt.getName() + "." + i, kit.getName() + "|" + MiscUtil.playerInventoryToString(kit.getInv()));
	        } 
	      } 
	    } 
	    try {
	      this.config.save(this.file);
	    } catch (IOException e) {
	      e.printStackTrace();
	    } 
	  }
	  
	  public int getRating(GameType gt) {
	    return ((Integer)this.ratings.get(gt)).intValue();
	  }
	  
	  public void setupKit(GameType gt) {
	    this.kits.put(gt, new Kit[5]);
	  }
}
