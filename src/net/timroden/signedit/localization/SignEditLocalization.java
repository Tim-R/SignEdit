package net.timroden.signedit.localization;

import java.util.HashMap;
import java.util.Map;

import net.timroden.signedit.SignEdit;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;


public class SignEditLocalization {
	private SignEdit plugin;

	public static Map<String, String> Strings = new HashMap<String, String>();

	public SignEditLocalization(SignEdit plugin) {
		this.plugin = plugin;
		loadLocales();		
	}

	public void loadLocales() {
		Strings.clear();
		
		FileConfiguration locales = plugin.yml.getYMLConfig(plugin.config.getLocale(), true); 

		if (locales != null) {
			String value;
			for (String key : locales.getKeys(false)) {
				value = locales.getString(key);
				Strings.put(key, ChatColor.translateAlternateColorCodes('&', value));
			}
		}
				
		locales = plugin.yml.getYMLConfig("enUS.yml", true); //Get any keys that don't exist in the user specified file, and load them
		
		if (locales != null) {
			String value;
			for (String key : locales.getKeys(false)) {
				if (!Strings.containsKey(key)){
					value = locales.getString(key);
					Strings.put(key, ChatColor.translateAlternateColorCodes('&', value));
				}
			}
		}
	}
	
	public String get(String key) {
		return Strings.get(key);
	}
	
	public String get(String key, Object...args) {
		String value = Strings.get(key).toString();
		try {
			if (value != null || args != null)
				value = String.format(value, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
}