package net.timroden.signedit.utils;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import net.timroden.signedit.Config;
import net.timroden.signedit.SignEdit;

public class SignEditUtils {
	private SignEdit plugin;

	public SignEditUtils(SignEdit plugin) {
		this.plugin = plugin;
	}

	public boolean isInt(String check) {
		try { 
			Integer.parseInt(check);
		} catch(NumberFormatException ignored) {
			return false;
		}
		return true;
	}

	public Boolean throwSignChange(final Block theBlock, final Player thePlayer, final String[] theLines) {
		SignChangeEvent event = new SignChangeEvent(theBlock, thePlayer, theLines);
		plugin.pluginMan.callEvent(event);
		return event.isCancelled();
	}

	public boolean isSign(Block b) {
		return (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN));
	}

	public boolean shouldCancel(Player player) {
		boolean ret = (Config.ignoreCreative() && !plugin.config.invertMouse() && player.getGameMode().equals(GameMode.CREATIVE)); 
		return ret;
	}

	public String implode(String[] inputArray, String glue, int start, int end) {
		if(inputArray.length - 1 == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		if (inputArray.length > 0) {
			for (int i = start; i<inputArray.length; i++) {
				sb.append(inputArray[i]);
				sb.append(glue);
			}
		}
		return sb.toString().trim();		
	}

	public String capitalize(String toCaps) {
		return toCaps.substring(0,1).toUpperCase() + toCaps.substring(1);  
	}
}