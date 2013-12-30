package net.timroden.signedit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.timroden.signedit.Config;
import net.timroden.signedit.SignEdit;
import net.timroden.signedit.data.LogType;

public class SignEditLogger {
	private SignEdit plugin;
	private Logger log;
	private File logFile;
	private BufferedWriter fileOut;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public SignEditLogger(SignEdit plugin) {
		this.plugin = plugin;
		this.log = plugin.getLogger();
	}

	public void logAll(String thePlayer, String theCommand, LogType theType,
			Level level) {
		String theMessage = thePlayer + ": /signedit " + theCommand;

		if (theType.equals(LogType.PLAYERCOMMAND))
			theMessage = this.plugin.localization.get("playerCommand") + " "
					+ thePlayer + ": /signedit " + theCommand;
		else if (theType.equals(LogType.SIGNCHANGE)) {
			theMessage = this.plugin.localization.get("signChange") + " "
					+ thePlayer + theCommand;
		}
		if (Config.commandsLogFile()) {
			logFile("[" + this.dateFormat.format(new Date()) + "] "
					+ theMessage);
		}
		if (Config.commandsLogConsole())
			log(level, theMessage);
	}

	private void logFile(String data) {
		try {
			openFileOutput();
			this.fileOut.write(data);
			this.fileOut.newLine();
			this.fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openFileOutput() {
		try {
			this.logFile = new File(this.plugin.getDataFolder(),
					Config.logName());
			if (!this.logFile.exists()) {
				this.logFile.createNewFile();
			}
			this.fileOut = new BufferedWriter(new FileWriter(
					this.plugin.getDataFolder()
							+ System.getProperty("file.separator")
							+ Config.logName(), true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void info(String msg) {
		this.log.info(msg);
	}

	public void warning(String msg) {
		this.log.warning(msg);
	}

	public void severe(String msg) {
		this.log.severe(msg);
	}

	public void log(Level level, String msg) {
		this.log.log(level, msg);
	}
}