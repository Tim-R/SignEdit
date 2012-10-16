package net.timroden.signedit;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.Action;

public class Config {	
	private SignEdit plugin;
	private static Configuration config;

	private boolean ignoreCreative, invertMouse, notifyOnVersion, commandsLogConsole, commandsLogFile, colorsOnPlace, useCOPPerm;
	private Action clickAction;
	private String logName, clickActionStr, locale;

	public Config(SignEdit plugin) {
		this.plugin = plugin;		
		config = plugin.getConfig().options().configuration();		
		config.options().copyDefaults(true);		
		plugin.saveConfig();

		getOpts();
	}

	public void reload() {
		plugin.reloadConfig();
		config = plugin.getConfig().options().configuration();		
		config.options().copyDefaults(true);		
		plugin.saveConfig();

		getOpts();
	}

	public void getOpts() {
		ignoreCreative = config.getBoolean("signedit.ignorecreative");
		logName = config.getString("signedit.log.filename");
		invertMouse = config.getBoolean("signedit.invertmouse");
		notifyOnVersion = config.getBoolean("signedit.notifyversion");
		commandsLogConsole = config.getBoolean("signedit.commands.logtoconsole");
		commandsLogFile = config.getBoolean("signedit.commands.logtofile");
		colorsOnPlace = config.getBoolean("signedit.colorsonplace.enabled");
		useCOPPerm = config.getBoolean("signedit.colorsonplace.usepermission");
		locale = config.getString("signedit.locale");

		if(invertMouse) {
			clickAction = Action.RIGHT_CLICK_BLOCK;
			clickActionStr = "right click";
		} else {
			clickAction = Action.LEFT_CLICK_BLOCK;
			clickActionStr = "left click";
		}
	}

	public boolean ignoreCreative() {
		return this.ignoreCreative;
	}

	public Action clickAction() {
		return this.clickAction;
	}

	public String logName() {
		return this.logName;
	}

	public boolean notifyVersionUpdate() {
		return this.notifyOnVersion;
	}

	public boolean commandsLogConsole() {
		return this.commandsLogConsole;
	}

	public boolean commandsLogFile() {
		return this.commandsLogFile;
	}

	public boolean colorsOnPlace() {
		return this.colorsOnPlace;
	}

	public boolean useCOPPermission() {
		return this.useCOPPerm;
	}

	public String clickActionStr() {
		return this.clickActionStr;
	}

	public boolean invertMouse() {
		return this.invertMouse;
	}
	
	public String getLocale() {
		return this.locale;
	}
}