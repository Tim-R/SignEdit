package net.timroden.signedit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	/* Variables for logging to our log file */
	public File logFile = null;
	public FileWriter fstream = null;
	public BufferedWriter fileOutput = null;

	/* Variables for LWC integration */
	Plugin lwcPlugin;
	LWC lwc;

	/* Prefix to show users in chat when they perform any SignEdit commands */
	public String chatPrefix = ChatColor.RESET + "[" + ChatColor.AQUA + "SignEdit" + ChatColor.WHITE + "] ";
	
	/* Main data HashMaps, stores all pending SignEdit "jobs" for a specific player */
	public HashMap<Player, String[]> playerLines = new HashMap<Player, String[]>();
	public HashMap<Player, String[]> clipboard = new HashMap<Player, String[]>();
	public HashMap<Player, String> pasteAmount = new HashMap<Player, String>();	

	/* Initialize our listener */
	public SignEditPlayerListener pl = new SignEditPlayerListener(this);

	/* Variables for dealing with Plugin Configuration files */
	Config config;
	
	/* Version Checker */
	VersionChecker version; 
	
	@Override
	public void onEnable() {
		Long st = System.currentTimeMillis();
		config = new Config(this);
		version = new VersionChecker(this);
			
		version.versionCheck();
		if(config.useLWC){
			findLWC();
		}
		
		getServer().getPluginManager().registerEvents(this.pl, this);
		log.info("[SignEdit] Enabled successfully! (" + (((System.currentTimeMillis() - st) / 1000D) % 60) + " s)");
	}

	@Override
	public void onDisable() {
		
		if(config.logEnabled) {
			try {
				fileOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("[SignEdit] Disabled successfully.");
	}
	
	//Main Function
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String line = "";
		String[] toPut = new String[3];
		if(sender instanceof Player) {
			player = (Player) sender;
		}	
		if(player != null) {
			if(cmd.getName().equalsIgnoreCase("signedit")) {
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("help")) {
						player.sendMessage(chatPrefix + ChatColor.GREEN + "Available commands:");
						player.sendMessage(chatPrefix + ChatColor.GRAY + "When altering your signs, left click to apply changes.");
						player.sendMessage(ChatColor.GRAY + " - /signedit cancel - Cancels any pending SignEdit requests");
						player.sendMessage(ChatColor.GRAY + " - /signedit <line> <text> - Changes the text on the specified line to <text> (The line must be 1,2,3, or 4)");
						player.sendMessage(ChatColor.GRAY + " - /signedit [copy]/[paste] <#>- Copy or paste a sign - amount optional for copying");
						player.sendMessage(ChatColor.GRAY + " - /signedit [copy]/[paste] <persist> - Copy or paste unlimited times");
						player.sendMessage(ChatColor.GRAY + " - /signedit [copy]/[paste] <default> <#> - Reassign the default amount you want to copy and paste");
						if(player.hasPermission("signedit.admin")) {
							player.sendMessage(ChatColor.GRAY + " - /signedit reload - Reloads SignEdit configuration");
						}
						player.sendMessage(ChatColor.GRAY + " - /signedit help - Display this help dialogue");
						return true;
					}
					if(args[0].equalsIgnoreCase("reload")) {
						if(player.hasPermission("signedit.admin")) {
							player.sendMessage(chatPrefix + ChatColor.AQUA + "Reloading config...");
							config.reload();
							player.sendMessage(chatPrefix + ChatColor.AQUA + "Config reloaded.");
							return true;
						} else {
							player.sendMessage(chatPrefix + ChatColor.RED + "You do not have permission to reload the SignEdit config!");
							return true;
						}
					}
					if(player.hasPermission("signedit.edit")) {
						if(args[0].equalsIgnoreCase("cancel")) {
							if(playerLines.containsKey(player)) {
								playerLines.remove(player);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Sign change cancelled.");
								return true;
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "You don't have any requests pending!");
								return true;
							}
						}
						if(args[0].equalsIgnoreCase("copy")) {
							if(!pasteAmount.containsKey(player)){
								pasteAmount.put(player, "1");
							}
							if(args.length>1){
								if(args[1].equalsIgnoreCase("persist")){
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Persistent copying enabled. Punch the sign you wish to add to clipboard.");
									toPut[0] = "persist";
									toPut[1] = null;
									toPut[2] = "copy";
									playerLines.put(player, toPut);
									return true;
								}else if(args[1].equalsIgnoreCase("default")){
									try{
										Integer.parseInt(args[2]);
									}catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" is not a valid number. Please enter an integer to set as your default copying amount.");
										return true;
									}
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Changed the default paste amount to "+args[2]);
									pasteAmount.put(player, args[2]);
									return true;
								}else{
									try{
										Integer.parseInt(args[1]);
									}catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[1] + "\" is not a valid number.");
										player.sendMessage(chatPrefix + ChatColor.GREEN + " Punch your sign to copy. Making default amount of "+pasteAmount.get(player)+" copies");
										toPut[0] = null;
										toPut[1] = pasteAmount.get(player);
										toPut[2] = "copy";
										playerLines.put(player, toPut);
										return true;
									}
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Punch your Sign to Copy. Making "+args[1]+" copies, /signedit paste # to extend");
									toPut[0] = null;
									toPut[1] = args[1];
									toPut[2] = "copy";
									playerLines.put(player, toPut);
									return true;
								}
							}else{
								toPut[0] = null;
								toPut[1] = pasteAmount.get(player);
								toPut[2] = "copy";
								playerLines.put(player, toPut);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Punch your sign to copy. Making default amount of "+pasteAmount.get(player)+" copies.");
								return true;
							}
						}
						if(args[0].equalsIgnoreCase("paste")){
							if (!clipboard.containsKey(player)){
								player.sendMessage(chatPrefix + ChatColor.GREEN + "You don't have anything copied!");
								return true;
							}
							if(!pasteAmount.containsKey(player)){
								pasteAmount.put(player, "1");
							}
							if(args.length>1){
								if(args[1].equalsIgnoreCase("persist")){
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Pasting indefinetly");
									toPut[0] = "persist";
									toPut[1] = null;
									toPut[2] = "paste";
									playerLines.put(player, toPut);
									return true;
								}else if(args[1].equalsIgnoreCase("default")){
									try{
										Integer.parseInt(args[2]);
									}catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" is not a valid number. Please enter an integer to set as your default copying amount.");
										return true;
									}
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Changed the default paste amount to "+args[2]);
									pasteAmount.put(player, args[2]);
									return true;
								}else{
									try{
										Integer.parseInt(args[1]);
									}catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" is not a valid number. Reloading clipboard for "+pasteAmount.get(player)+" pastes.");
										toPut[0] = null;
										toPut[1] = pasteAmount.get(player);
										toPut[2] = "paste";
										playerLines.put(player, toPut);
										return true;
									}
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Reloading clipboard for "+args[1]+" pastes.");
									toPut[0] = null;
									toPut[1] = args[1];
									toPut[2] = "paste";
									playerLines.put(player, toPut);
									return true;
								}
							}else{
								toPut[0] = null;
								toPut[1] = pasteAmount.get(player);
								toPut[2] = "paste";
								playerLines.put(player, toPut);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Making default amount of:  "+pasteAmount.get(player)+" pastes.");
								return true;
							}
							
						}
						if(args.length >= 1) {
							try {
								Integer.parseInt(args[0]);
							} catch(NumberFormatException ex) {
								player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" is not a number. Please enter a valid line number. (1,2,3 or 4)");
								return true;
							}
							if(Integer.parseInt(args[0]) > 4) {
								player.sendMessage(chatPrefix + "\"" + args[0] + "\" isn't a valid line number! Please enter a valid line number (1,2,3 or 4)");
								return true;	
							}
							if(args.length >= 2) {
								line = implodeArray(args, " ", 1, args.length);
							}
							if(stripColourCodes(line).length() <= 15) {
								toPut[0] = args[0];
								toPut[1] = line;
								toPut[2] = "edit";
								playerLines.put(player, toPut);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Text saved. Punch a sign to complete your changes.");
								return true;
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "The most characters a line can hold is 15. Your text was " + line.length() + " characters long.");
								return true;
							}	
						} else {
							player.sendMessage(chatPrefix + ChatColor.RED + "For usage information on this command, type /signedit help");
							return true;
						}
					} else {
						player.sendMessage(chatPrefix + ChatColor.RED + "You do not have permission to use SignEdit!");
						return true;
					}
				} else {
					player.sendMessage(chatPrefix + ChatColor.RED + "For usage information on this command, type /signedit help");
					return true;
				}
			}
		} else {
			sender.sendMessage(chatPrefix + ChatColor.RED + "This command can only be initiated by a player.");
			return true;
		}
		return false;	
	}
	public static String implodeArray(String[] inputArray, String glueString, int start, int end) {
		StringBuilder sb = new StringBuilder();
		if (inputArray.length > 0) {
			sb.append(inputArray[start]);
			for (int i = (start + 1); i<inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}
		}
		return sb.toString();
	}
	public void openFileOutput() {
		try	{
			logFile = new File(getDataFolder(), config.logFilename);
			if(!logFile.exists()){
				logFile.createNewFile();
			}	
			fstream = new FileWriter(getDataFolder() + System.getProperty("file.separator") + config.logFilename, true);
			fileOutput = new BufferedWriter(fstream);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public void findLWC() {
		lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
		if(lwcPlugin != null) {
			lwc = ((LWCPlugin) lwcPlugin).getLWC();
			log.info("[SignEdit] LWC found. Will check for protections");
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
    public static String stripColourCodes(String string) {
    	return string.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");        
    }
}