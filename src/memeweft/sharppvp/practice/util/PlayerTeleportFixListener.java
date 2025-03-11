package me.memeweft.sharppvp.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportFixListener implements Listener
{
    static int visibleDistance;
    static int minVisible;
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        refreshPlayer(event.getPlayer());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        refreshPlayer(event.getPlayer());
    }
    
    public static void refreshPlayer(final Player player) {
        for (final Player ply : Bukkit.getOnlinePlayers()) {
            player.showPlayer(ply);
            ply.showPlayer(player);
        }
    }
    
    static {
        PlayerTeleportFixListener.visibleDistance = Bukkit.getServer().getViewDistance() * 16;
        PlayerTeleportFixListener.minVisible = 4096;
    }
}
