package net.timroden.signedit;

import org.bukkit.configuration.Configuration;

public class Config {
	private SignEdit plugin;
	private static Configuration config;
	
	public boolean useLWC, ignoreCreative, logEnabled;
	public String logFilename;
	
	public Config(SignEdit plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		
		getOpts();
	}
	
	public void reload() {
		plugin.reloadConfig();
		config = plugin.getConfig().getRoot();
		
		getOpts();
	}
	
	public void getOpts() {
		useLWC = config.getBoolean("signedit.uselwc");
		ignoreCreative = config.getBoolean("signedit.ignorecreative");
		logEnabled = config.getBoolean("signedit.log.filename");
	}
}
