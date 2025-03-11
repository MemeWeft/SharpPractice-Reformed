package me.memeweft.sharppvp.practice.game.gametype;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.memeweft.sharppvp.practice.command.Command;
import me.memeweft.sharppvp.practice.command.CommandArgs;
import me.memeweft.sharppvp.practice.player.Kit;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class GameTypeCommands 
{
    private GameTypeManager gtm;
    
    public GameTypeCommands(final GameTypeManager gtm) {
        this.gtm = gtm;
    }
    
    @Command(name = "gametype.create", permission = "sharppractice.gametype.create", aliases = { "gt.create", "gt.c", "gametype.c" }, description = "Create a new Practice GameType", usage = "/gametype create <name>")
    public void onGameTypeCreate(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType already exists!");
            return;
        }
        this.gtm.createGameType(name);
        sender.sendMessage(ChatColor.GOLD + "GameType succesfully created!");
    }
    
    @Command(name = "gametype.delete", permission = "sharppractice.gametype.delete", aliases = { "gt.delete", "gt.remove", "gt.d", "gt.r", "gt.rmv", "gt.del", "gametype.remove", "gametype.d", "gametype.r", "gametype.rmv", "gametype.del" }, description = "Delete a Practice GameType", usage = "/gametype delete <name>")
    public void onDeleteGameType(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        this.gtm.removeGameType(name);
        sender.sendMessage(ChatColor.GOLD + "GameType succesfully removed!");
    }
    
    @Command(name = "gametype.setdisplay", permission = "sharppractice.gametype.setdisplay", aliases = { "gt.setdisplay", "gt.setd", "gametype.setd" }, description = "Set the display for a GameType", usage = "/gametype setdisplay <GameType> <MATERIAL")
    public void onSetDisplay(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 2) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        final Material display = Material.getMaterial(args.getArgs(1));
        if (display == null) {
            sender.sendMessage(ChatColor.RED + "That Material doesn't exist!");
            return;
        }
        gt.setDisplay(display);
        sender.sendMessage(ChatColor.GOLD + "GameType succesfully updated!");
        this.gtm.saveGameTypes();
    }
    
    @Command(name = "gametype.setinventory", permission = "sharppractice.gametype.setdefaultinventory", aliases = { "gt.setinventory", "gt.setinv", "gametype.setinv" }, description = "Set the default inventory for a GameType", usage = "/gametype setinventory <GameType>", inGameOnly = true)
    public void onSetInventory(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        gt.setStartingKit(new Kit(gt.getDisplayName() + "Default Kit", PlyInv.fromPlayerInventory(args.getPlayer().getInventory())));
        sender.sendMessage(ChatColor.GOLD + "GameType succesfully updated!");
        this.gtm.saveGameTypes();
    }
    
    @Command(name = "gametype.loadinventory", permission = "sharppractice.gametype.loadinventory", aliases = { "gt.loadinventory", "gt.loadinv", "gametype.loadinv" }, description = "Load the default inventory for a GameType", usage = "/gametype loadinventory <GameType>", inGameOnly = true)
    public void onLoadInvetory(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        args.getPlayer().getInventory().setContents(gt.getStartingKit().getInv().getContents());
        args.getPlayer().getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
        args.getPlayer().updateInventory();
        sender.sendMessage(ChatColor.GOLD + "Default Inventory Loaded!");
    }
    
    @Command(name = "gametype.list", permission = "sharppractice.gametype.list", aliases = { "gt.list", "gt.ls", "gt.l", "gametype.ls", "gametype.l" }, description = "List all setup GameTypes", usage = "/gametype list")
    public void onList(final CommandArgs args) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD).append(ChatColor.BOLD).append("GameTypes: \n");
        for (final GameType a : this.gtm.getGameTypes()) {
            builder.append(ChatColor.BLUE + " - " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD).append(a.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append("\n");
        }
        args.getSender().sendMessage(builder.toString());
    }
    
    @Command(name = "gametype.seteditable", permission = "sharppractice.gametype.seteditable", aliases = { "gt.se" })
    public void onSetEdit(final CommandArgs args) {
        final CommandSender sender = args.getSender();
        if (args.length() < 1) {
            sender.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            sender.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        gt.setEditable(!gt.isEditable());
        args.getPlayer().sendMessage(ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())) + " is " + (gt.isEditable() ? "now " : "no longer ") + "editable!");
        this.gtm.saveGameTypes();
    }
    
    @Command(name = "gametype.editinv", permission = "sharppractice.gametype.editinv", aliases = { "gt.editinv" }, inGameOnly = true)
    public void onEditInv(final CommandArgs args) {
        final Player ply = args.getPlayer();
        if (args.length() < 1) {
            ply.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        if (gt.isEditable()) {
            ply.openInventory(gt.getPossibleGear());
            this.gtm.addEditing(ply);
        }
        else {
            ply.sendMessage(ChatColor.RED + "That gametype isn't editable!");
        }
    }
    
    @Command(name = "gametype.addsign", permission = "sharppractice.gametype.addsign", aliases = { "gt.addsign", "gt.adds", "gametype.adds" }, inGameOnly = true)
    public void onAddSign(final CommandArgs args) {
        final Player ply = args.getPlayer();
        if (args.length() < 1) {
            ply.sendMessage(args.getCommand().getUsage());
            return;
        }
        final String name = args.getArgs(0);
        if (!this.gtm.doesGameTypeExist(name)) {
            ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
            return;
        }
        final GameType gt = this.gtm.getGameType(name);
        final Block targetBlock = ply.getTargetBlock((HashSet)null, 5);
        if (targetBlock != null && (targetBlock.getType() == Material.SIGN || targetBlock.getType() == Material.WALL_SIGN || targetBlock.getType() == Material.SIGN_POST)) {
            this.gtm.addSign(targetBlock.getLocation(), gt);
        }
    }
}
