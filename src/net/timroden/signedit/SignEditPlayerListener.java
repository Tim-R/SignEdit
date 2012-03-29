package net.timroden.signedit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import com.griefcraft.model.Protection;

public class SignEditPlayerListener implements Listener {
	public SignEdit plugin;
	Protection protection;
	String[] playerLinesArray;
	int line;
	String changetext;
	
	public SignEditPlayerListener(SignEdit parent) {
		this.plugin = parent;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		boolean canAccess = true;
		playerLinesArray = plugin.playerLines.get(event.getPlayer());
		if((event.getClickedBlock() != null) && (event.getClickedBlock().getType().equals(Material.SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN) )) {
			BlockState gs = event.getClickedBlock().getState();
			Sign sign = (Sign) gs;
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				if(plugin.playerLines.containsKey(event.getPlayer())) {
					if(plugin.getConfig().getBoolean("signedit.uselwc") == true) {
						canAccess = plugin.performLWCCheck(event.getPlayer(), plugin.lwc.findProtection(event.getClickedBlock()));
					}
					if(canAccess == true) {
						line = (Integer.parseInt(playerLinesArray[0]) - 1);
						changetext = playerLinesArray[1];
						if(changetext == "delete") {
							sign.setLine(line, "");
							sign.update();
							plugin.playerLines.remove(event.getPlayer());
							event.getPlayer().sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line deleted.");
						} else {
							sign.setLine(line, changetext);
							sign.update();
							plugin.playerLines.remove(event.getPlayer());
							event.getPlayer().sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Line changed.");
						}
					} else {
						event.getPlayer().sendMessage(plugin.chatPrefix + ChatColor.RED + "You do not have permission to edit that sign!");
					}
				}
			}
		}
	}
}