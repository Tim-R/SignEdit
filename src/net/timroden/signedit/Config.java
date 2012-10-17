package net.timroden.signedit;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.Action;

public class Config {	
	private SignEdit plugin;
	private static Configuration config;

	private static boolean ignoreCreative, invertMouse, notifyOnVersion, commandsLogConsole, commandsLogFile, colorsOnPlace, useCOPPerm;
	private static Action clickAction;
	private static String logName, locale;

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
		} else {
			clickAction = Action.LEFT_CLICK_BLOCK;
		}
	}

	public static boolean ignoreCreative() {
		return Config.ignoreCreative;
	}

	public static Action clickAction() {
		return Config.clickAction;
	}

	public static String logName() {
		return Config.logName;
	}

	public static boolean notifyVersionUpdate() {
		return Config.notifyOnVersion;
	}

	public static boolean commandsLogConsole() {
		return Config.commandsLogConsole;
	}

	public static boolean commandsLogFile() {
		return Config.commandsLogFile;
	}

	public boolean colorsOnPlace() {
		return Config.colorsOnPlace;
	}

	public boolean useCOPPermission() {
		return Config.useCOPPerm;
	}

	public boolean invertMouse() {
		return Config.invertMouse;
	}
	
	public static String getLocale() {
		return Config.locale;
	}

	public String clickActionStr() {
		return (invertMouse ? plugin.localization.get("clickRight") : plugin.localization.get("clickLeft"));
	}
}