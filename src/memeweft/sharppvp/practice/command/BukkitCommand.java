package me.memeweft.sharppvp.practice.command;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

public class BukkitCommand extends Command 
{
	
    private final Plugin owningPlugin;
    private CommandExecutor executor;
    protected BukkitCompleter completer;
    
    protected BukkitCommand(final String label, final CommandExecutor executor, final Plugin owner) {
        super(label);
        this.executor = executor;
        this.owningPlugin = owner;
        this.usageMessage = "";
    }
    
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        boolean success = false;
        if (!this.owningPlugin.isEnabled()) {
            return false;
        }
        if (!this.testPermission(sender)) {
            return true;
        }
        try {
            success = this.executor.onCommand(sender, (Command)this, commandLabel, args);
        }
        catch (Throwable ex) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + this.owningPlugin.getDescription().getFullName(), ex);
        }
        if (!success && this.usageMessage.length() > 0) {
            for (final String line : this.usageMessage.replace("<command>", commandLabel).split("\n")) {
                sender.sendMessage(line);
            }
        }
        return success;
    }
    
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws CommandException, IllegalArgumentException {
        Validate.notNull((Object)sender, "Sender cannot be null");
        Validate.notNull((Object)args, "Arguments cannot be null");
        Validate.notNull((Object)alias, "Alias cannot be null");
        List<String> completions = null;
        try {
            if (this.completer != null) {
                completions = this.completer.onTabComplete(sender, this, alias, args);
            }
            if (completions == null && this.executor instanceof TabCompleter) {
                completions = (List<String>)((TabCompleter)this.executor).onTabComplete(sender, (Command)this, alias, args);
            }
        }
        catch (Throwable ex) {
            final StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (final String arg : args) {
                message.append(arg).append(' ');
            }
            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(this.owningPlugin.getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }
        if (completions == null) {
            return (List<String>)super.tabComplete(sender, alias, args);
        }
        return completions;
    }
}
