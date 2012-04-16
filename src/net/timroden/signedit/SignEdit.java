package net.timroden.signedit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
	
	/* Main data HashMap, stores all pending SignEdit "jobs" for a specific player */
	public HashMap<Player, String[]> playerLines = new HashMap<Player, String[]>();

	/* Initialize our listener */
	public SignEditPlayerListener pl = new SignEditPlayerListener(this);
	
	/* Variables for dealing with Plugin Configuration files */
	File configFile;
	FileConfiguration config;
	
	@Override
	public void onEnable() {
		Long st = System.currentTimeMillis();
		configFile = new File(getDataFolder(), "config.yml");
		try {
	        firstRun();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		config = new YamlConfiguration();
		loadCfg();
		if(config.getBoolean("signedit.uselwc") == true) {
			findLWC();
		}
		if(config.getBoolean("signedit.log.enabled") == true) {
			openFileOutput();
		}
		
		getServer().getPluginManager().registerEvents(this.pl, this);

		log.info("[SignEdit] SignEdit enabled successfully! (" + (System.currentTimeMillis() - st) + " ms)");
	}

	@Override
	public void onDisable() {
		if(config.getBoolean("signedit.log.enabled") == true) {
			try {
				fileOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("[SignEdit] SignEdit disabled successfully.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String line = "";
		String[] toPut = new String[2];
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
						if(args[0].equalsIgnoreCase("help") && args.length == 1) {
							player.sendMessage(chatPrefix + ChatColor.GREEN + "Available commands:");
							player.sendMessage(chatPrefix + ChatColor.GRAY + "When altering your signs, left click to apply changes.");
							player.sendMessage(ChatColor.GRAY + "    - /signedit cancel - Cancels any pending SignEdit requests");
							player.sendMessage(ChatColor.GRAY + "    - /signedit <line> <text> - Changes the text on the specified line to <text> (The line must be 1,2,3, or 4)");
							player.sendMessage(ChatColor.GRAY + "    - /signedit help - Display this help dialogue");
							return true;
						}
						if(!playerLines.containsKey(player)) {
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
								if(line.length() <= 15) {
									toPut[0] = args[0];
									toPut[1] = line;
									playerLines.put(player, toPut);
									player.sendMessage(chatPrefix + ChatColor.GREEN + "Text saved. Left click a sign to complete your changes.");
									return true;
								} else {
									player.sendMessage(chatPrefix + ChatColor.RED + "The most characters a line can hold is 15. Your text was " + line.length() + " characters.");
									return true;
								}	
							} else {
								player.sendMessage(chatPrefix + ChatColor.RED + "For usage information on this command, type /signedit help");
								return true;
							}
						} else {
							player.sendMessage(chatPrefix + ChatColor.RED + "You already have a request pending. You can remove it by typing \"/signedit cancel\"");
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
			logFile = new File(getDataFolder(), config.getString("signedit.log.filename"));
			if(!logFile.exists()){
				logFile.createNewFile();
			}		
			fstream = new FileWriter(getDataFolder() + System.getProperty("file.separator") + config.getString("signedit.log.filename"), true);
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
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	}
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void saveCfg() {
	    try {
	        config.save(configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public void loadCfg() {
	    try {
	        config.load(configFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}