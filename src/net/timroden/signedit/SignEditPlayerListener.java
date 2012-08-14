package net.timroden.signedit;

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
		if(p.isPermissionSet("signedit.notify") && plugin.config.notifyOnVersion) 
			if(!plugin.version.isLatestVersion) 
				p.sendMessage(plugin.chatPrefix + ChatColor.DARK_PURPLE + plugin.version.versionMessage);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		data = plugin.playerLines.get(p);
		SignFunction f;
		if(plugin.playerFunction.containsKey(p)) {
			f = plugin.playerFunction.get(p);
		} else {
			return;
		}
		if((b != null) && isSign(b)) {
			BlockState gs = event.getClickedBlock().getState();
		
			Sign sign = (Sign) gs;
		
			if(event.getAction().equals(plugin.config.clickAction)) {
				if(f.equals(SignFunction.COPY)) {
					if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
						event.setCancelled(true);
						sign.update();
					}	
					plugin.clipboard.put(p, sign.getLines());
					plugin.playerFunction.put(p, SignFunction.PASTE);
					p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign added to clipboard, " + plugin.config.clickActionStr + " a sign to paste.");						
				} else if(f.equals(SignFunction.PASTE)) {
					if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
						event.setCancelled(true);
						sign.update();
					}
					String[] cplines = plugin.clipboard.get(p);
					sign.setLine(0, cplines[0]);
					sign.setLine(1, cplines[1]);
					sign.setLine(2, cplines[2]);
					sign.setLine(3, cplines[3]);
					sign.update();
					if(data[0] == null && data[0] != "persist") {
						if(data[1] instanceof Integer) {
							data[1] = ((Integer) data[1] - 1);
						} else if(data[1] instanceof String) {
							data[1] = (Integer.parseInt((String) data[1]) - 1);
						}								
						if((Integer) data[1] == 0) {
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted." + ChatColor.RED + " You are out of Copies!");
							plugin.clipboard.remove(p);
							plugin.playerLines.remove(p);
							plugin.playerFunction.remove(p);
						} else {
							String cStr = "copies";
							if((Integer) data[1] == 1) {
								cStr = "copy";
							} 
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted. You have " + data[1] + " " + cStr + " left.");
						}
					} else {
						p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Sign pasted. \u221E copies left.");
					}
				} else if(f.equals(SignFunction.EDIT)) {
					if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.ignoreCreative) {
						event.setCancelled(true);
						sign.update();
					}
					String originalLine = stripColourCodes(sign.getLine(line));
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
					plugin.logAll(p.getName() + ": (x:" + sign.getLocation().getBlockX() + ", y:" + sign.getLocation().getBlockY() + ", z:" + sign.getLocation().getBlockZ() + ", " + p.getWorld().getName() + ") \"" + originalLine + "\" changed to \"" + changetext + "\"");
					sign.update();
					throwSignChange(b, p, sign.getLines());
					plugin.playerLines.remove(p); 
					plugin.playerFunction.remove(p);
				}
			} 
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