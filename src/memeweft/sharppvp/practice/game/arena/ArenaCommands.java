package me.memeweft.sharppvp.practice.game.arena;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.memeweft.sharppvp.practice.command.Command;
import me.memeweft.sharppvp.practice.command.CommandArgs;
import me.memeweft.sharppvp.practice.util.LocationUtil;
import me.memeweft.sharppvp.practice.util.MiscUtil;

public class ArenaCommands 
{
    private ArenaManager am;
    
    public ArenaCommands(final ArenaManager am) {
        this.am = am;
    }
    
    @Command(name = "arena.create", permission = "sharppractice.arena.create", aliases = { "arena.c" }, description = "Create a new Practice Arena", usage = "/arena create <name>")
    public void onArenaCreate(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (this.am.doesArenaExist(name)) {
            sender.sendMessage(ChatColor.RED + "That arena already exists!");
            return;
        }
        this.am.createArena(name);
        sender.sendMessage(ChatColor.GOLD + "Arena succesfully created!");
    }
    
    @Command(name = "arena.delete", permission = "sharppractice.arena.delete", aliases = { "arena.remove", "arena.d", "arena.r", "arena.rmv", "arena.del" }, description = "Delete a Practice Arena", usage = "/arena delete <name>")
    public void onDeleteArena(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;	
        }
        final String name = args.getArgs(0);
        if (!this.am.doesArenaExist(name)) {
            sender.sendMessage(ChatColor.RED + "That arena doesn't exist!");
            return;
        }
        this.am.removeArena(name);
        sender.sendMessage(ChatColor.GOLD + "Arena succesfully removed!");
    }
    
    @Command(name = "arena.setspawn", permission = "sharppractice.arena.modify", aliases = { "arena.ss", "arena.addspawn", "arena.modify", "arena.as", "arena.m" }, description = "Set a spawnpoint for a Practice Arena", usage = "/arena setspawn <1 or 2> <arena name>", inGameOnly = true)
    public void onModifyArena(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 2 || !MiscUtil.isInt(args.getArgs(0))) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final int spawnPoint = Integer.parseInt(args.getArgs(0));
        final String name = args.getArgs(1);
        if (!this.am.doesArenaExist(name)) {
            sender.sendMessage(ChatColor.RED + "That Arena doesn't exist!");
            return;
        }
        final Arena a = this.am.getArena(name);
        switch (spawnPoint) {
            case 1: {
                a.setSpawn1(args.getPlayer().getLocation());
                break;
            }
            case 2: {
                a.setSpawn2(args.getPlayer().getLocation());
                break;
            }
        }
        sender.sendMessage(ChatColor.GOLD + "Arena succesfully modified!");
        this.am.saveArenas();
    }
    
    @Command(name = "arena.list", permission = "sharppractice.arena.list", aliases = { "arena.ls", "arena.l" }, description = "List all setup arenas", usage = "/arena list")
    public void onList(final CommandArgs args) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD).append(ChatColor.BOLD).append("Arenas: \n");
        for (final Arena a : this.am.getArenas()) {
            if (a.isSetup()) {
                builder.append(ChatColor.BLUE + " - " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD).append(a.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append(": ").append(ChatColor.DARK_AQUA).append(LocationUtil.getString(a.getSpawn1())).append(" ").append(LocationUtil.getString(a.getSpawn2())).append("\n");
            }
            else {
                builder.append(ChatColor.BLUE + " - " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD).append(a.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append(":").append(ChatColor.DARK_AQUA + " Not Setup");
            }
        }
        args.getSender().sendMessage(builder.toString());
    }
}
