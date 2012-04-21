package net.timroden.signedit;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;

import com.griefcraft.model.Protection;

public class SignEditPlayerListener implements Listener {
	/* The parent plugin, we use this for config options, chatprefix, etc */
	public SignEdit plugin;
	
	/* Declare the protection object for LWC */
	Protection protection;
	
	/* The 3 main data handlers for changing lines
	 * 
	 * @param playerLinesArray holds the line number and text to change said line to 
	 * @param line holds the line to be changed
	 * @param changetext holds the text to put on the line
	 * 
	 */
	String[] playerLinesArray;
	int line;
	String changetext;
	
	private static final Pattern[] patterns = {
        Pattern.compile("^$|^\\w.+$"),
        Pattern.compile("[0-9]+"),
        Pattern.compile(".+"),
        Pattern.compile("[\\w : -]+")
	};
	
	public SignEditPlayerListener(SignEdit parent) {
		this.plugin = parent;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		boolean canAccess = true;
		boolean csAccess = true;
		boolean fbAccess = true;
		
		playerLinesArray = plugin.playerLines.get(p);
		if((event.getClickedBlock() != null) && (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN) )) {
			BlockState gs = event.getClickedBlock().getState();
			Sign sign = (Sign) gs;
			String[] lines = sign.getLines();
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				/* Copy command */
				if(plugin.playerLines.containsKey(p) && playerLinesArray[2] == "copy") {
					if(!plugin.clipboard.containsKey(p)) {
						if(plugin.config.getBoolean("signedit.uselwc") == true) {
							canAccess = plugin.performLWCCheck(p, plugin.lwc.findProtection(event.getClickedBlock()));
						}
						if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.getBoolean("signedit.ignorecreative") == true) {
							event.setCancelled(true);
							sign.update();
						}
						if(canAccess == true || p.hasPermission("signedit.override")) {
							plugin.clipboard.put(p, sign.getLines());
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign added to clipboard. To disable persistent copying, type /signedit copy");
							
						} else {
							plugin.playerLines.remove(p);
							sign.update();
							p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You do not have permission to copy that sign!");
						}
					}
				
				}				
				/* Edit command */
				if(plugin.playerLines.containsKey(p) && playerLinesArray[2] == "edit") {
					if(plugin.config.getBoolean("signedit.uselwc") == true) {
						canAccess = plugin.performLWCCheck(p, plugin.lwc.findProtection(event.getClickedBlock()));
					}
					if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.getBoolean("signedit.ignorecreative") == true) {
						event.setCancelled(true);
					}
					if(canAccess == true || p.hasPermission("signedit.override")) {
						line = (Integer.parseInt(playerLinesArray[0]) - 1);
						changetext = playerLinesArray[1];
						if(futureValid(sign, changetext, line)) {
							if (line == 0 && !changetext.equals(p.getName())){
								csAccess = false;		
							}else{
								if (formatFirstLine(lines[0], p)) sign.setLine(0, p.getName());								
							}
						}
						if(line == 1 && changetext.contains("[MC")) {
							fbAccess = false;
						}
						if(csAccess) {
							if(fbAccess) {
								if(changetext == "") {
									sign.setLine(line, "");
									p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line deleted.");
									changetext = stripColourCodes(changetext);
								} else {
									sign.setLine(line, ChatColor.translateAlternateColorCodes('&', changetext));
									if (becomeValid(sign, changetext,line)){
										p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Shop Created");
									} else if (isValid(sign)){
										p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Shop Updated");
									} else {
										p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line changed.");										
									}
									changetext = stripColourCodes(changetext);
								}
								if(plugin.config.getBoolean("signedit.log.enabled") == false) {
									plugin.log.info("[SignEdit] Sign Change: " + p.getName() + " changed sign at x:" + sign.getLocation().getBlockX() + " y:" + sign.getLocation().getBlockY() + " z:" + sign.getLocation().getBlockZ() + " in world " + p.getWorld().getName() + "; Line " + playerLinesArray[0] + " changed to \"" + changetext + "\"");
								} else {
									DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
									try {
										plugin.openFileOutput();
										plugin.fileOutput.write("[" + dateFormat.format(new Date()) + "] " + p.getName() + " changed sign at x:" + sign.getLocation().getBlockX() + " y:" + sign.getLocation().getBlockY() + " z:" + sign.getLocation().getBlockZ() + " in world " + p.getWorld().getName() + "; Line " + playerLinesArray[0] + " changed to \"" + changetext + "\"");
										plugin.fileOutput.newLine();
										plugin.fileOutput.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								notify(plugin.chatPrefix + "Sign Change: " + p.getName() + " changed \"" + lines[Integer.parseInt(playerLinesArray[0]) - 1] + "\" to \"" + changetext + "\"");
								sign.update();
								plugin.playerLines.remove(p);
							} else {
								plugin.playerLines.remove(p);
								p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You cannot change IC's. Access denied!");
							}
						} else {
							if (line!=0){
							}
							plugin.playerLines.remove(p);
							sign.update();
							p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You cannot modify shop owners. Access denied!");
						}
					} else {
						plugin.playerLines.remove(p);
						sign.update();
						p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You do not have permission to edit that sign!");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlockPlaced();
		// if((event.getClickedBlock() != null) && (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN) )) {
		if((b != null) && (b.getType().equals(Material.SIGN)) || b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign) b.getState();
			if(plugin.clipboard.containsKey(p)) {
				String[] lines = plugin.clipboard.get(p);
				sign.setLine(0, lines[0]);
				sign.setLine(1, lines[1]);
				sign.setLine(2, lines[2]);
				sign.setLine(3, lines[3]);
				sign.update();
			}
		}
	}
	
	public void notify(String message) {
	    for(Player player: Bukkit.getServer().getOnlinePlayers()) {	        
	        if(player.hasPermission("signedit.notify")) {
	            player.sendMessage(message);
	        }	     
	    }
	}
	/* ChestShop Security Measures */
	
	public static boolean futureValid(Sign sign, String changetext, Integer line){
		String [] newsign = sign.getLines();
		newsign[line]=changetext;
		return isValid(newsign);
	}
	
	public static boolean becomeValid(Sign sign, String changetext, Integer line){
		return (!isValid(sign) && futureValid(sign, changetext, line));
	}
	public static boolean isValid(Sign sign) {
        return isValid(sign.getLines());
    }

    public static boolean isValid(String[] line) {
        return isValidPreparedSign(line) && (line[2].contains("B") || line[2].contains("S")) && !line[0].isEmpty();
    }
    public static boolean isValidPreparedSign(String[] lines) {
        boolean toReturn = true;
        for (int i = 0; i < 4 && toReturn; i++) toReturn = patterns[i].matcher(lines[i]).matches();
        return toReturn && lines[2].indexOf(':') == lines[2].lastIndexOf(':');
    }
    
    private static boolean formatFirstLine(String line1, Player player) {
        return line1.isEmpty() || (!line1.equals(player.getName()));
    }
    
    /* Colour formatting */ 
    public static String stripColourCodes(String string) {
    	return string.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");        
    }
}