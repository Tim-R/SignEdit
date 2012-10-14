package net.timroden.signedit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.timroden.signedit.Config;
import net.timroden.signedit.SignEdit;
import net.timroden.signedit.data.LogType;

public class SignEditLogger {
	private SignEdit plugin; 
	private Config config;
	private Logger log;
	
	private File logFile;
	private BufferedWriter fileOut;
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public SignEditLogger(SignEdit plugin) {
		this.plugin = plugin;
		this.config = plugin.config;
		this.log = plugin.getLogger();
	}
	
	public void logAll(String thePlayer, String theCommand, LogType theType) {
		String theMessage = thePlayer + ": /signedit " + theCommand;
		
		if(theType.equals(LogType.PLAYERCOMMAND))
			theMessage = "[PLAYER_COMMAND] " + thePlayer + ": /signedit " + theCommand;
		else if(theType.equals(LogType.SIGNCHANGE)) 
			theMessage = "[SIGN_CHANGE] " + thePlayer + theCommand; 
		
		if(config.commandsLogFile())
			logFile("[" + dateFormat.format(new Date()) + "] " + theMessage);

		if(config.commandsLogConsole())
			log.info(theMessage);
	}
	public void logFile(String data) {
		try {
			openFileOutput();
			fileOut.write(data);
			fileOut.newLine();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void openFileOutput() {
		try	{
			logFile = new File(plugin.getDataFolder(), config.logName());
			if(!logFile.exists()){
				logFile.createNewFile();
			}	
			fileOut = new BufferedWriter(new FileWriter(plugin.getDataFolder() + System.getProperty("file.separator") + config.logName(), true));
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void info(String msg) {
		log.info(msg);
	}
	
	public void warning(String msg) {
		log.warning(msg);
	}
	
	public void severe(String msg) {
		log.severe(msg);
	}	
}