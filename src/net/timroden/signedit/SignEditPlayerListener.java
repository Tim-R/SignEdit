package net.timroden.signedit;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class SignEditPlayerListener implements Listener {
	public SignEdit plugin;
	Object[] data;
	int line;
	String changetext;
	
	public SignEditPlayerListener(SignEdit parent) {
		this.plugin = parent;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(p.isPermissionSet("signedit.notify")) 
			if(!plugin.version.isLatestVersion) 
				p.sendMessage(plugin.chatPrefix + ChatColor.DARK_PURPLE + plugin.version.versionMessage);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Block b = event.getClickedBlock();
		data = plugin.playerLines.get(p);
		if((event.getClickedBlock() != null) && isSign(b)) {
			BlockState gs = event.getClickedBlock().getState();
			
			Sign sign = (Sign) gs;
			
			boolean canAccess;
			if(plugin.config.useLWC) {
				canAccess = plugin.performLWCCheck(p, plugin.lwc.findProtection(b));
			} else {
				canAccess = true;
			}
				
			
			if(event.getAction().equals(plugin.config.clickAction)) {
				if(plugin.playerLines.containsKey(p)) {
					if(data[2] == SignFunction.COPY) {
						if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
							event.setCancelled(true);
							sign.update();
						}
						if(canAccess || p.hasPermission("signedit.override")) {
							plugin.clipboard.put(p, sign.getLines());
							data[2] = SignFunction.PASTE;
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign added to clipboard, " + plugin.config.clickActionStr + " a sign to paste.");						
						} else {
							p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have permission to copy that sign!");
							sign.update();
						}
					} else if(data[2] == SignFunction.PASTE) {
						if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
							event.setCancelled(true);
							sign.update();
						}
						if(canAccess || p.hasPermission("signedit.override")) {
							String[] cplines = plugin.clipboard.get(p);
							sign.setLine(0, cplines[0]);
							sign.setLine(1, cplines[1]);
							sign.setLine(2, cplines[2]);
							sign.setLine(3, cplines[3]);
							sign.update();
							if(data[0] == null && data[0] != "persist") {
								plugin.log.info("[SE] Got past persist");
								if(data[1] instanceof Integer) {
									data[1] = ((Integer) data[1] - 1);
								} else if(data[1] instanceof String) {
									data[1] = (Integer.parseInt((String) data[1]) - 1);
								}								
								if((Integer) data[1] == 0) {
									p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted." + ChatColor.RED + " You are out of Copies!");
									plugin.clipboard.remove(p);
									plugin.playerLines.remove(p);
								} else {
									p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted. You have " + data[1] + " copies left.");
								}
							} else {
								p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted.");
							}
						} else {
							p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have permission to paste on that sign!");
							sign.update();
						}
					} else if(data[2] == SignFunction.EDIT) {
						if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
							event.setCancelled(true);
							sign.update();
						}
						if(canAccess == true || p.hasPermission("signedit.override")) {
							String originalLine = sign.getLine(line);
							line = (Integer.parseInt((String) data[0]) - 1);
							changetext = (String) data[1];
						
							if(changetext == "") {
								sign.setLine(line, "");
								changetext = stripColourCodes(changetext);
								p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line deleted.");
							} else {
								sign.setLine(line, ChatColor.translateAlternateColorCodes('&', changetext));
								changetext = stripColourCodes(changetext);
								p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line changed.");
							}
							if(plugin.config.logEnabled) {							
								logToFile("[" + dateFormat.format(new Date()) + "] " + p.getName() + ": (x:" + sign.getLocation().getBlockX() + ", y:" + sign.getLocation().getBlockY() + ", z:" + sign.getLocation().getBlockZ() + ", world " + p.getWorld().getName() + ") " + originalLine + " changed to \"" + changetext + "\"");
							}
							sign.update();
							throwSignChange(b, p, sign.getLines());
							plugin.playerLines.remove(p); 
						} else {
							plugin.playerLines.remove(p);
							sign.update();
							p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have permission to edit that sign!");
						}
					}
				}
			}
		}
	}
	public void logToFile(String message) {
		try {
			plugin.openFileOutput();
			plugin.fileOutput.write(message);
			plugin.fileOutput.newLine();
			plugin.fileOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isSign(Block b) {
		return (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN));
	} 
    public static String stripColourCodes(String string) {
    	return string.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");        
    }    
    public void throwSignChange(final Block theBlock, final Player thePlayer, final String[] theLines) {
    	SignChangeEvent event = new SignChangeEvent(theBlock, thePlayer, theLines);
    	this.plugin.pm.callEvent(event);    	
    }
}