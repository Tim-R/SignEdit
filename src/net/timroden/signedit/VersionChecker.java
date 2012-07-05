package net.timroden.signedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker {
	public SignEdit plugin;
	
	public VersionChecker(SignEdit plugin) {
		this.plugin = plugin;
	}	
	
	public String getLatestVersion() {
		String latestVersion = null;
		String uA = plugin.getDescription().getName() + " " + plugin.getDescription().getVersion();
		final String address = "http://dev.bukkit.org/server-mods/signedit/files/";
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
					latestVersion = regexMatcher.group(1);
					break;
				}
			}
			bufferedReader.close();
			connection.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return latestVersion;
	}

	String latestVersion = null;
	
	public void versionCheck() {
		String curVersion = plugin.getDescription().getVersion();
		if (latestVersion == null) {
			latestVersion = getLatestVersion().toLowerCase().replace("signedit ", "");
		}
		String msg = null;
		if (latestVersion != null) {
			int compare = curVersion.compareTo(latestVersion);
			if (compare < 0) {
				msg = "The version of " + plugin.getDescription().getName() + " this server is running is out of date. Latest version: " + latestVersion;
				plugin.log.warning("[SignEdit] " + msg);
			} else if (compare == 0) {
				msg = plugin.getDescription().getName() + " is up to date!";
				plugin.log.info("[SignEdit] " + msg);
			} else {
				msg = "This server is running a Development version of " + plugin.getDescription().getName() + ". Expect bugs!";
				plugin.log.warning("[SignEdit] " + msg);
			}
		} else {
			msg = "Error retrieving latest version from server.";
			plugin.log.warning("[SignEdit] " + msg);
		}
	}
	
	public static double getUnixTime() {
		return System.currentTimeMillis() / 1000D;
	}
}