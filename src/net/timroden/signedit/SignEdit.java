package net.timroden.signedit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.timroden.signedit.commands.CommandSignEdit;
import net.timroden.signedit.data.SignEditDataPackage;
import net.timroden.signedit.utils.SignEditLogger;
import net.timroden.signedit.utils.SignEditUtils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class SignEdit extends JavaPlugin {	
	public String chatPrefix = ChatColor.RESET + "[" + ChatColor.AQUA + "SignEdit" + ChatColor.WHITE + "] ";
	public PluginManager pluginMan;

	public Map<String, SignEditDataPackage> playerData = new HashMap<String, SignEditDataPackage>();
	public Map<String, Integer> pasteAmounts = new HashMap<String, Integer>();

	private SignEditPlayerListener listener;
	public SignEditLogger log;
	public SignEditUtils utils;
	public VersionChecker version;
	public Config config;

	@Override
	public void onEnable() {		
		utils = new SignEditUtils(this);
		log = new SignEditLogger(this);

		listener = new SignEditPlayerListener(this);
		version = new VersionChecker(this);

		version.start();

		config = new Config(this);

		try {			
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			log.severe("Error enabling metrics");
		}	

		pluginMan = getServer().getPluginManager();

		pluginMan.registerEvents(listener, this);

		getCommand("signedit").setExecutor(new CommandSignEdit(this));
	}

	@Override
	public void onDisable() {}
}