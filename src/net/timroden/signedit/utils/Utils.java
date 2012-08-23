package net.timroden.signedit.utils;

import net.timroden.signedit.SignEdit;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class Utils {
	public SignEdit plugin;

	public Utils(SignEdit plugin) {
		this.plugin = plugin;
	}

	public boolean isSign(Block b) {
		return (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN));
	} 
	public String stripColourCodes(String string) {
		return string.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");        
	}    
	public void throwSignChange(final Block theBlock, final Player thePlayer, final String[] theLines) {
		SignChangeEvent event = new SignChangeEvent(theBlock, thePlayer, theLines);
		plugin.pm.callEvent(event);    	
	}
	public String implode(String[] inputArray, String glueString, int start, int end) {
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
}