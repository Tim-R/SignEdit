package net.timroden.signedit.commands;

import net.timroden.signedit.SignEdit;
import net.timroden.signedit.data.LogType;
import net.timroden.signedit.data.SignEditDataPackage;
import net.timroden.signedit.data.SignFunction;
import net.timroden.signedit.utils.SignEditUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSignEdit implements CommandExecutor {
	private SignEdit plugin;
	private SignEditUtils utils;

	public CommandSignEdit(SignEdit plugin) {
		this.plugin = plugin;
		this.utils = plugin.utils;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Syntax error");
			help(sender);
			return true;
		}

		if(args[0].equalsIgnoreCase("help")){ 
			help(sender);
			return true;
		}

		if(args[0].equalsIgnoreCase("reload")) {
			if(!sender.hasPermission("signedit.admin")) {
				sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have permission to reload SignEdit");
				return true;
			}
			plugin.config.reload();
			sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Reloaded config");
		}

		if(!(sender instanceof Player)) {
			sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You must be a player to use this command");
			return true;
		}

		Player player = (Player) sender;
		String line = null;

		if(!player.hasPermission("signedit.edit")) {
			player.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have permission to use SignEdit!");
			return true;
		}

		if(args[0].equalsIgnoreCase("cancel")) {
			plugin.log.logAll(player.getName(), "cancel", LogType.PLAYERCOMMAND);
			if(!plugin.playerData.containsKey(player.getName())) {
				player.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have any requests pending!");
				return true;
			} 

			plugin.playerData.remove(player.getName());			
			player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Request cancelled");
			return true;
		} else if(args[0].equalsIgnoreCase("copy")) {
			if(!plugin.pasteAmounts.containsKey(player.getName())) {
				plugin.pasteAmounts.put(player.getName(), 1);
			}
			if(args.length > 1){
				if(args[1].equalsIgnoreCase("persist")) {
					plugin.log.logAll(player.getName(), "copy persist", LogType.PLAYERCOMMAND);				
					SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.COPYPERSIST);					
					plugin.playerData.put(player.getName(), tmp);
					player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Persistent copying enabled. " + utils.capitalize(plugin.config.clickActionStr()) + " the sign you wish to add to clipboard.");
					return true;
				} else if(args[1].equalsIgnoreCase("default")) {
					if(!utils.isInt(args[2])) {
						player.sendMessage(plugin.chatPrefix + ChatColor.RED + "\"" + args[2] + "\" is not a valid number. Please specify a valid number.");
						return true;
					}
					plugin.log.logAll(player.getName(), "default " + args[2], LogType.PLAYERCOMMAND);
					plugin.pasteAmounts.put(player.getName(), Integer.parseInt(args[2]));
					player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Changed your default paste amount to " + args[2]);
					return true;
				} else {
					plugin.log.logAll(player.getName(), "copy " + args[1], LogType.PLAYERCOMMAND);
					if(!utils.isInt(args[1])) {
						SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.COPY, plugin.pasteAmounts.get(player.getName()));						
						plugin.playerData.put(player.getName(), tmp);
						player.sendMessage(plugin.chatPrefix + ChatColor.RED + "\"" + args[1] + "\" is not a valid number.");
						player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + utils.capitalize(plugin.config.clickActionStr()) + " your sign to copy. Making default amount of " + plugin.pasteAmounts.get(player) + " copies");
						return true;
					}
					SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.COPY, Integer.parseInt(args[1]));					
					plugin.playerData.put(player.getName(), tmp);
					player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + utils.capitalize(plugin.config.clickActionStr()) + " your sign to Copy. Making " + args[1] + " " + (Integer.parseInt(args[1]) == 1 ? "copy" : "copies"));
					return true;
				}
			} else {
				plugin.log.logAll(player.getName(), "copy", LogType.PLAYERCOMMAND);
				int amt = plugin.pasteAmounts.get(player.getName());
				SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.COPY, amt);			
				plugin.playerData.put(player.getName(), tmp);
				player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + utils.capitalize(plugin.config.clickActionStr()) + " your sign to copy. Making default amount of " + amt + " " + (amt == 1 ? "copy" : "copies"));
				return true;
			}
		}
		if(args.length >= 1) {			
			if(!utils.isInt(args[0])) {
				player.sendMessage(plugin.chatPrefix + ChatColor.RED + "\"" + args[0] + "\" isn't a number. Please enter a valid number.");
				return true;
			}

			if(Integer.parseInt(args[0]) > 4 || Integer.parseInt(args[0]) < 1) {
				player.sendMessage(plugin.chatPrefix + ChatColor.RED + "\"" + args[0] + "\" isn't a valid line number! Please enter a valid line number (1-4).");
				return true;
			}

			line = utils.implode(args, " ", 1, args.length);

			if(line == null) {
				plugin.log.logAll(player.getName(), args[0], LogType.PLAYERCOMMAND);
				SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.EDIT, "", Integer.parseInt(args[0]) - 1);
				plugin.playerData.put(player.getName(), tmp);		
				player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Text saved. " + utils.capitalize(plugin.config.clickActionStr()) + " a sign to complete your changes.");
				return true;
			}			

			if(line.length() > 15) {
				player.sendMessage(plugin.chatPrefix + ChatColor.RED + "Truncating line to be 15 characters");
				line = line.substring(0, 15);
			}			
			plugin.log.logAll(player.getName(), utils.implode(args, " ", 0, args.length), LogType.PLAYERCOMMAND);
			SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.EDIT, line, Integer.parseInt(args[0]) - 1);
			plugin.playerData.put(player.getName(), tmp);		
			player.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Text saved. " + utils.capitalize(plugin.config.clickActionStr()) + " a sign to complete your changes.");
			return true;
		}
		sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Syntax error");
		help(sender);		
		return true;
	}

	public void help(CommandSender sender) {
		sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Available commands:");
		if(sender instanceof Player) {
			sender.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "When altering your signs, " + plugin.config.clickActionStr() + " to apply changes.");
			sender.sendMessage(ChatColor.GRAY + " - /signedit <line> <text> - Changes the text on the specified line");
			sender.sendMessage(ChatColor.GRAY + " - /signedit cancel - Cancel any pending SignEdit requests");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy [amount] - Copy a sign - amount optional");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy persist - Copy a sign infinitely");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy default <amount> - Define your default paste amount");
		}
		if(sender.hasPermission("signedit.admin")) {
			sender.sendMessage(ChatColor.GRAY + " - /signedit reload - Reload SignEdit configuration");
		}
		sender.sendMessage(ChatColor.GRAY + " - /signedit help - Display this help dialogue");
	}
}