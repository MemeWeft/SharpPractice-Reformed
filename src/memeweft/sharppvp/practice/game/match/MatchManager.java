package me.memeweft.sharppvp.practice.game.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.game.gametype.GameType;

public class MatchManager implements Listener
{
	  private SharpPractice plugin;
	  
	  private List<Match> matches;
	  
	  private List<Queue> queues;
	  
	  public MatchManager(final SharpPractice plugin) {
	    this.plugin = plugin;
	    this.matches = new ArrayList<>();
	    this.queues = new ArrayList<>();
	    for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
	      this.queues.add(new Queue(gt, true));
	      this.queues.add(new Queue(gt, false));
	    } 
	    for (Queue queue : this.queues)
	      Bukkit.getPluginManager().registerEvents(queue, (Plugin)this.plugin); 
	    Bukkit.getPluginManager().registerEvents(this, (Plugin)this.plugin);
	    (new BukkitRunnable() {
	        public void run() {
	          MatchManager.this.queues.stream().filter(Queue::hasMatch).forEach(queue -> {
	                for (Player ply : queue.getAwaitingMatch().keySet()) {
	                  GameType gt = queue.getGame();
	                  Match match = new Match(gt.getPossibleArenas().isEmpty() ? plugin.getArenaManager().getArenas().get((new Random()).nextInt(plugin.getArenaManager().getArenas().size())) : gt.getPossibleArenas().get((new Random()).nextInt(gt.getPossibleArenas().size())), gt, ply, queue.getAwaitingMatch().get(ply), queue.isRanked());
	                  match.startMatch();
	                  MatchManager.this.matches.add(match);
	                  queue.startMatch(ply);
	                  plugin.getInventoryManager().updateMenus();
	                } 
	              });
	        }
	      }).runTaskTimer((Plugin)this.plugin, 0L, 5L);
	  }
	  
	  public void startMatch(Player ply, Player ply2, GameType gt, boolean ranked) {
	    Match match = new Match(gt.getPossibleArenas().isEmpty() ? this.plugin.getArenaManager().getArenas().get((new Random()).nextInt(this.plugin.getArenaManager().getArenas().size())) : gt.getPossibleArenas().get((new Random()).nextInt(gt.getPossibleArenas().size())), gt, ply, ply2, ranked);
	    match.startMatch();
	    this.matches.add(match);
	  }
	  
	  private Queue getQueue(Predicate<Queue> test) {
	    for (Queue queue : this.queues) {
	      if (test.test(queue))
	        return queue; 
	    } 
	    return null;
	  }
	  
	  private Queue getQueue(GameType gt, boolean ranked) {
	    return getQueue(queue -> (queue.isRanked() == ranked && queue.getGame() == gt));
	  }
	  
	  public void addToQueue(Player ply, GameType gt, boolean ranked) {
	    getQueue(gt, ranked).addToQueue(ply, this.plugin.getPlayerDataManager().getRating(ply, gt));
	  }
	  
	  public int getAmountInQueue(GameType gt, boolean ranked) {
	    return getQueue(gt, ranked).getQueue().keySet().size();
	  }
	  
	  public int getAmountInMatch(GameType gt, boolean ranked) {
	    return getMatches(match -> (match.getGameType() == gt && match.isRanked() == ranked)).size() * 2;
	  }
	  
	  public Match getMatch(Predicate<Match> test) {
	    for (Match match : this.matches) {
	      if (test.test(match))
	        return match; 
	    } 
	    return null;
	  }
	  
	  public List<Match> getMatches(Predicate<Match> test) {
	    List<Match> matches = new ArrayList<>();
	    for (Match match : this.matches) {
	      if (test.test(match))
	        matches.add(match); 
	    } 
	    return matches;
	  }
	  
	  public Match getMatch(Player ply) {
	    return getMatch(match -> match.hasPlayer(ply));
	  }
	  
	  public GameType getGameType(Player ply) {
	    return getMatch(ply).getGameType();
	  }
	  
	  public boolean isInMatch(Player ply) {
	    return (getMatch(ply) != null);
	  }
	  
	  @EventHandler
	  public void onDropItem(PlayerDropItemEvent event) {
	    if (!isInMatch(event.getPlayer()))
	      event.setCancelled(true); 
	  }
	  
	  @EventHandler
	  public void onPlayerDamage(EntityDamageEvent event) {
	    if (event.getEntity() instanceof Player && 
	      !isInMatch((Player)event.getEntity()))
	      event.setCancelled(true); 
	  }
	  
	  @EventHandler
	  public void onFoodChange(FoodLevelChangeEvent event) {
	    if (!isInMatch((Player)event.getEntity()))
	      event.setCancelled(true); 
	  }
	  
	  public void endMatch(Match match) {
	    this.matches.remove(match);
	    this.plugin.getInventoryManager().updateMenus();
	  }
}
