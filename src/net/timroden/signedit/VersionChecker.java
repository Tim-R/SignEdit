package net.timroden.signedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

public class VersionChecker extends Thread {
	public Plugin plugin;
	
	public String address = "http://dev.bukkit.org/server-mods/signedit/files/";

	public boolean isLatestVersion = true;
	public String versionMessage;
	public String pName;
	public String latestVersion = null;
	
	private Logger log;
	

	public VersionChecker(Plugin plugin) {
		this.plugin = plugin;
		log = plugin.getLogger();
		pName = plugin.getDescription().getName();	
	}	
	
	public void run() {
		String uA = plugin.getDescription().getName() + " " + plugin.getDescription().getVersion();
		final URL url;
		URLConnection connection = null;
		BufferedReader bufferedReader = null;
		try {
			url = new URL(address.replace(" ", "%20"));
			connection = url.openConnection();
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(15000);
			connection.setRequestProperty("User-agent", uA);
			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Pattern titleFinder = Pattern.compile("<td[^>]*><a[^>]*>(.*?)</a></td>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher regexMatcher;
			String str;
			while ((str = bufferedReader.readLine()) != null) {
				str = str.trim();
				regexMatcher = titleFinder.matcher(str);
				if (regexMatcher.find()) {
					latestVersion = regexMatcher.group(1).toLowerCase().replace("signedit ", "");
					break;
				}
			}
			bufferedReader.close();
			connection.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String msg = null;
		if (latestVersion != null) {
			int compare = plugin.getDescription().getVersion().compareTo(latestVersion);
			if (compare < 0) {
				msg = "The version of " + pName + " this server is running is out of date. Latest version: " + latestVersion;
				isLatestVersion = false;
				versionMessage = msg + " You can download the latest version at " + address;
				log.warning(msg);
			} else if (compare == 0) { 
				msg = plugin.getDescription().getName() + " is up to date!";
				log.info(msg);
			} else {
				msg = "This server is running a Development version of " + pName + ". Expect bugs!";
				isLatestVersion = false;
				versionMessage = msg;
				log.warning(msg);
			}
		} else {
			msg = "Error retrieving latest version from BukkitDev.";
			log.warning(msg);
		}
	}
}