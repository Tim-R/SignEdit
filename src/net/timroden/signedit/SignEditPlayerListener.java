package net.timroden.signedit;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
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
	
	public SignEditPlayerListener(SignEdit parent) {
		this.plugin = parent;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		boolean canAccess = true;
		playerLinesArray = plugin.playerLines.get(p);
		if((event.getClickedBlock() != null) && (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN) )) {
			BlockState gs = event.getClickedBlock().getState();
			Sign sign = (Sign) gs;
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				if(plugin.playerLines.containsKey(p)) {
					if(plugin.config.getBoolean("signedit.uselwc") == true) {
						canAccess = plugin.performLWCCheck(p, plugin.lwc.findProtection(event.getClickedBlock()));
					}
					if(p.getGameMode().equals(GameMode.CREATIVE) && plugin.config.getBoolean("signedit.ignorecreative") == true) {
						event.setCancelled(true);
					}
					if(canAccess == true || p.hasPermission("signedit.override")) {
						line = (Integer.parseInt(playerLinesArray[0]) - 1);
						changetext = playerLinesArray[1];
						if(changetext == "") {
							sign.setLine(line, "");
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line deleted.");
						} else {
							sign.setLine(line, changetext);
							p.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line changed.");
						}
						if(plugin.config.getBoolean("signedit.log.enabled") == false) {
							plugin.log.info("[SignEdit] Sign Change: " + p.getName() + " changed sign at x:" + p.getLocation().getX() + " y:" + p.getLocation().getY() + " z:" + p.getLocation().getZ() + " in world " + p.getWorld().getName() + "; Line " + line + " changed to \"" + changetext + "\"");
						} else {
							DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
							try {
								plugin.openFileOutput();
								plugin.fileOutput.write("[" + dateFormat.format(new Date()) + "] " + p.getName() + " changed sign at x:" + p.getLocation().getX() + " y:" + p.getLocation().getY() + " z:" + p.getLocation().getZ() + " in world " + p.getWorld().getName() + "; Line " + line + " changed to \"" + changetext + "\"");
								plugin.fileOutput.newLine();
								plugin.fileOutput.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						sign.update();
						plugin.playerLines.remove(p);
					} else {
						plugin.playerLines.remove(p);
						p.sendMessage(plugin.chatPrefix + ChatColor.RED + "You do not have permission to edit that sign!");
					}
				}
			}
		}
	}
}