package net.timroden.signedit;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;

public class SignEdit extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	Plugin lwcPlugin;
	LWC lwc;
	
	public String chatPrefix = "[" + ChatColor.GREEN + "SignEdit" + ChatColor.WHITE + "] ";
	public HashMap<Player, String[]> playerLines = new HashMap<Player, String[]>();

	public SignEditPlayerListener pl = new SignEditPlayerListener(this);
	
	@Override
	public void onEnable() {
		Long st = System.currentTimeMillis();
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		if(getConfig().getBoolean("signedit.uselwc") == true) {
			findLWC();
		}
		getServer().getPluginManager().registerEvents(this.pl, this);

		log.info("[SignEdit] SignEdit enabled successfully! (" + (System.currentTimeMillis() - st) + " ms)");
	}
	
	@Override
	public void onDisable() {
		saveConfig();
		log.info("[SignEdit] SignEdit disabled successfully.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String line = null;
		if(sender instanceof Player) {
			player = (Player) sender;
		}		
		if(player != null) {
			if(cmd.getName().equalsIgnoreCase("signedit")) {
				if(player.hasPermission("signedit.edit")) {
					if(args.length > 0) {
						if(args[0].equalsIgnoreCase("cancel") && args.length == 1) {
							if(playerLines.containsKey(player)) {
								playerLines.remove(player);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Sign change cancelled.");
								return true;
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "You don't have any requests pending!");
								return true;
							}
						}
						if(!playerLines.containsKey(player)) {
							if(args.length >= 2) {
								if(args[1].equalsIgnoreCase("delete") && args.length == 2) {
									String[] toPut = new String[2];
									toPut[0] = args[0];
									toPut[1] = "delete";									
									playerLines.put(player, toPut);
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Left click a sign to complete your changes.");
									return true;
								}
								line = implodeArray(args, " ", 1, args.length);
								if(line.length() <= 15) {
									String[] toPut = new String[2];
									toPut[0] = args[0];
									toPut[1] = line;
									playerLines.put(player, toPut);
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Text saved. Left click a sign to complete your changes.");
									return true;
								} else {
									player.sendMessage(chatPrefix + ChatColor.RED + "The most characters a line can hold is 15. Your text was " + line.length() + " characters.");
								}
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "Usage: /signedit <line|cancel> [delete|text]");
								return true;
							}
						} else {
							player.sendMessage(chatPrefix + ChatColor.RED + "You already have a request pending. You can remove it by typing \"/signedit cancel\"");
							return true;
						}
					} else {
						player.sendMessage(chatPrefix + ChatColor.RED + "Usage: /signedit <line|cancel> [delete|text]");
						return true;
					}
				} else {
					player.sendMessage(chatPrefix + ChatColor.RED + "You do not have permission to edit signs!");
					return true;
				}
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "This command can only be initiated by a player.");
		}
		return false;		
	}
	
	public static String implodeArray(String[] inputArray, String glueString, int start, int end) {
		String output = "";
		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[start]);
			for (int i = (start + 1); i<inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}
			output = sb.toString();
		}
		return output;
	}
	
	public void findLWC() {
		lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
		if(lwcPlugin != null) {
			lwc = ((LWCPlugin) lwcPlugin).getLWC();
		} else {
			log.severe("[SignEdit] LWC not found, disabling SignEdit");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	public boolean performLWCCheck(Player player, Protection protection) {
		if(lwc.canAccessProtection(player, protection)) {
			return true;
		}		
		return false;
	}
}