package net.timroden.signedit;

import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.Action;

public class Config {
	private SignEdit plugin;
	private static Configuration config;
	private static boolean ignoreCreative;
	private static boolean invertMouse;
	private static boolean notifyOnVersion;
	private static boolean commandsLogConsole;
	private static boolean commandsLogFile;
	private static boolean colorsOnPlace;
	private static boolean useCOPPerm;
	private static boolean metrics;
	private static Action clickAction;
	private static String logName;
	private static String locale;
	private static boolean fireBlockBreakPlace;
	private static List<String> blockedSigns;

	public Config(SignEdit plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().options().configuration();
		config.options().copyDefaults(true);
		plugin.saveConfig();

		getOpts();
	}

	public void reload() {
		this.plugin.reloadConfig();
		config = this.plugin.getConfig().options().configuration();
		config.options().copyDefaults(true);
		this.plugin.saveConfig();

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

		metrics = config.getBoolean("signedit.metrics");
		fireBlockBreakPlace = config.getBoolean("signedit.fireBlockBreakPlace");
		blockedSigns = config.getStringList("signedit.blockIfFirstLineEquals");

		if (invertMouse)
			clickAction = Action.RIGHT_CLICK_BLOCK;
		else
			clickAction = Action.LEFT_CLICK_BLOCK;
	}

	public static boolean fireBlockBreakPlace() {
		return fireBlockBreakPlace;
	}

	public static boolean ignoreCreative() {
		return ignoreCreative;
	}

	public static Action clickAction() {
		return clickAction;
	}

	public static String logName() {
		return logName;
	}

	public static boolean notifyVersionUpdate() {
		return notifyOnVersion;
	}

	public static boolean commandsLogConsole() {
		return commandsLogConsole;
	}

	public static boolean commandsLogFile() {
		return commandsLogFile;
	}

	public boolean colorsOnPlace() {
		return colorsOnPlace;
	}

	public boolean useCOPPermission() {
		return useCOPPerm;
	}

	public boolean invertMouse() {
		return invertMouse;
	}

	public static String getLocale() {
		return locale;
	}

	public boolean useMetrics() {
		return metrics;
	}

	public String clickActionStr() {
		return invertMouse ? this.plugin.localization.get("clickRight")
				: this.plugin.localization.get("clickLeft");
	}

	public static List<String> getBlockedSigns() {
		return blockedSigns;
	}
}