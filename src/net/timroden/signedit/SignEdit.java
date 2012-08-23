package net.timroden.signedit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import net.timroden.signedit.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SignEdit extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");
	public File logFile = null;
	public FileWriter fstream = null;
	public BufferedWriter fileOutput = null;
	public PluginManager pm = null;

	public String chatPrefix = ChatColor.RESET + "[" + ChatColor.AQUA + "SignEdit" + ChatColor.WHITE + "] ";

	public HashMap<Player, Object[]> playerLines = new HashMap<Player, Object[]>();
	public HashMap<Player, String[]> clipboard = new HashMap<Player, String[]>();
	public HashMap<Player, SignFunction> playerFunction = new HashMap<Player, SignFunction>();
	public HashMap<String, Integer> pasteAmount = new HashMap<String, Integer>();	
	public SignEditPlayerListener pl = null;

	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	Config config;	
	VersionChecker version; 
	Utils utils;

	@Override
	public void onEnable() {
		Long st = System.currentTimeMillis();

		config = new Config(this);
		version = new VersionChecker(this);				
		pl = new SignEditPlayerListener(this);
		utils = new Utils(this);

		version.start();
		
		this.pm = Bukkit.getPluginManager();
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			log.severe("[SignEdit] Error enabling metrics");
		}
		getServer().getPluginManager().registerEvents(this.pl, this);
		log.info("[SignEdit] Enabled successfully! (" + (((System.currentTimeMillis() - st) / 1000D) % 60) + "s)");
	}
	@Override
	public void onDisable() {	
		log.info("[SignEdit] Disabled successfully.");
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String line = "";
		Object[] toPut = new Object[3];
		if(cmd.getName().equalsIgnoreCase("signedit")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("reload")) {
					logAll("[PLAYER_COMMAND] " + sender.getName() + ": /signedit reload");
					if(sender.hasPermission("signedit.admin")) {
						sender.sendMessage(chatPrefix + ChatColor.GRAY + "Reloading config...");
						config.reload();
						sender.sendMessage(chatPrefix + ChatColor.GRAY + "Config reloaded.");
						return true;
					} else {
						sender.sendMessage(chatPrefix + ChatColor.RED + "You don't have permission to reload the SignEdit config! Missing permission: " + ChatColor.GRAY + "signedit.admin");
						return true;
					}
				}				
				if(args[0].equalsIgnoreCase("help")) {
					logAll("[PLAYER_COMMAND] " + sender.getName() + ": /signedit help");
					showHelp(sender);
					return true;
				}
				if(sender instanceof Player) {
					player = (Player) sender;
				}	
				if(player != null) {					
					if(args[0].equalsIgnoreCase("reload")) {
						logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit reload");
						if(player.hasPermission("signedit.admin")) {
							player.sendMessage(chatPrefix + ChatColor.GRAY + "Reloading config...");
							config.reload();
							player.sendMessage(chatPrefix + ChatColor.GRAY + "Config reloaded.");
							return true;
						} else {
							player.sendMessage(chatPrefix + ChatColor.RED + "You don't have permission to reload the SignEdit config! Missing permission: " + ChatColor.GRAY + "signedit.admin");
							return true;
						}
					}
					if(player.hasPermission("signedit.edit")) {
						if(args[0].equalsIgnoreCase("cancel")) {
							logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit cancel");
							if(clipboard.containsKey(player)) {
								clipboard.remove(player);
								playerFunction.remove(player);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Copy request cancelled.");
								return true;
							} else if(playerLines.containsKey(player)) {
								playerLines.remove(player);
								playerFunction.remove(player);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Sign change cancelled.");
								return true;
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "You don't have any requests pending!");
								return true;
							}
						}
						if(args[0].equalsIgnoreCase("copy")) {
							if(!pasteAmount.containsKey(player)) {
								pasteAmount.put(player.getName(), 1);
							}
							if(args.length>1){
								if(args[1].equalsIgnoreCase("persist")) {
									logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit copy persist");
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Persistent copying enabled. " + config.clickActionStr + " the sign you wish to add to clipboard.");
									toPut[0] = "persist";
									toPut[1] = null;
									playerLines.put(player, toPut);
									playerFunction.put(player, SignFunction.COPY);
									return true;
								} else if(args[1].equalsIgnoreCase("default")) {
									try {
										Integer.parseInt(args[2]);
									} catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" is not a valid number. Please specify a valid integer.");
										return true;
									}
									logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit default " + args[2]);
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Changed the default paste amount to " + args[2]);
									pasteAmount.put(player.getName(), Integer.parseInt(args[2]));
									return true;
								} else {
									logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit copy " + args[1]);
									try {
										Integer.parseInt(args[1]);
									} catch(NumberFormatException ex) {
										player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[1] + "\" is not a valid number.");
										player.sendMessage(chatPrefix + ChatColor.GREEN + config.clickActionStr + " your sign to copy. Making default amount of " + pasteAmount.get(player) + " copies");
										toPut[0] = null;
										toPut[1] = pasteAmount.get(player);
										playerFunction.put(player, SignFunction.COPY);
										playerLines.put(player, toPut);
										return true;
									}
									player.sendMessage(chatPrefix + ChatColor.GREEN + config.clickActionStr + " your sign to Copy. Making " + args[1] + " copies");
									toPut[0] = null;
									toPut[1] = args[1];
									playerFunction.put(player, SignFunction.COPY);
									playerLines.put(player, toPut);
									return true;
								}
							} else {
								logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit copy");
								toPut[0] = null;
								toPut[1] = pasteAmount.get(player.getName());
								playerFunction.put(player, SignFunction.COPY);
								playerLines.put(player, toPut);
								player.sendMessage(chatPrefix + ChatColor.GREEN + config.clickActionStr + " your sign to copy. Making default amount of " + pasteAmount.get(player.getName()) + " copies.");
								return true;
							}
						}
						if(args.length >= 1) {
							try {
								Integer.parseInt(args[0]);
							} catch(NumberFormatException ex) {
								player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" isn't a number. Please enter a valid number.");
								return true;
							}
							if(Integer.parseInt(args[0]) > 4) {
								player.sendMessage(chatPrefix + ChatColor.RED + "\"" + args[0] + "\" isn't a valid line number! Please enter a valid line number.");
								return true;	
							}
							if(args.length >= 2) {
								line = utils.implode(args, " ", 1, args.length);
							}
							logAll("[PLAYER_COMMAND] " + player.getName() + ": /signedit " + args[0] + " " + line);
							if(line.length() <= 15) {
								toPut[0] = args[0];
								toPut[1] = line;
								playerFunction.put(player, SignFunction.EDIT);
								playerLines.put(player, toPut);
								player.sendMessage(chatPrefix + ChatColor.GREEN + "Text saved. " + config.clickActionStr + " a sign to complete your changes.");
								return true;
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "The most characters a line can hold is 15. Your text was " + line.length() + " characters long.");
								return true;
							}	
						} else {
							showHelp(sender);
							return true;
						}
					} else {
						player.sendMessage(chatPrefix + ChatColor.RED + "You don't have permission to use SignEdit! Missing permission: " + ChatColor.GRAY + "signedit.edit");
						return true;
					}
				} else {
					sender.sendMessage(chatPrefix + ChatColor.RED + "This command can only be initiated by a player.");
					return true;
				}
			} else {
				showHelp(sender);
				return true;
			}
		}
		return false;	
	}

	public void showHelp(CommandSender sender) {
		sender.sendMessage(chatPrefix + ChatColor.GREEN + "Available commands:");
		if(sender instanceof Player) {
			sender.sendMessage(chatPrefix + ChatColor.GRAY + "When altering your signs, " + config.clickActionStr + " to apply changes.");
			sender.sendMessage(ChatColor.GRAY + " - /signedit cancel - Cancel any pending SignEdit requests");
			sender.sendMessage(ChatColor.GRAY + " - /signedit <line> <text> - Changes the text on the specified line");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy [amount] - Copy a sign - amount optional");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy persist - Copy a sign infinitely");
			sender.sendMessage(ChatColor.GRAY + " - /signedit copy default <amount> - Define your default paste amount");
		}
		if(sender.hasPermission("signedit.admin")) {
			sender.sendMessage(ChatColor.GRAY + " - /signedit reload - Reload SignEdit configuration");
		}
		sender.sendMessage(ChatColor.GRAY + " - /signedit help - Display this help dialogue");
	}
	public void logAll(String message) {
		if(config.commandsLogFile)
			logToFile("[" + dateFormat.format(new Date()) + "] " + message);

		if(config.commandsLogConsole)
			log.info(message);
	}
	public void logToFile(String message) {
		try {
			openFileOutput();
			fileOutput.write(message);
			fileOutput.newLine();
			fileOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void openFileOutput() {
		try	{
			logFile = new File(getDataFolder(), config.logName);
			if(!logFile.exists()){
				logFile.createNewFile();
			}	
			fstream = new FileWriter(getDataFolder() + System.getProperty("file.separator") + config.logName, true);
			fileOutput = new BufferedWriter(fstream);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}