package net.timroden.signedit;

import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionChecker extends Thread {
	private String rssURL = "http://dev.bukkit.org/server-mods/signedit/files.rss";
	private String currentVersion = null;	
	private String versionMessage;	
	private PluginDescriptionFile pdfile;	
	private Logger log;	
	private boolean isLatestVersion = true;
	
	public VersionChecker(SignEdit plugin) {
		this.pdfile = plugin.getDescription();
		this.log = plugin.getLogger();
	}

	@Override
	public void run() {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			URL url = new URL(rssURL);  
			URLConnection con = url.openConnection();
			
			con.setConnectTimeout(8000);
			con.setReadTimeout(15000);
			con.setRequestProperty("User-agent", pdfile.getName() + " " + pdfile.getVersion());
			
			Document doc = docBuilder.parse(con.getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("item");

			for(int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					currentVersion = getTagValue("title", eElement);
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		String msg = null;

		if(currentVersion != null) {
			int compare = pdfile.getVersion().compareTo(currentVersion);

			switch(compare) {
			case -1:
				msg = "The version of " + pdfile.getName() + " this server is running is out of date. Latest version: " + currentVersion;
				isLatestVersion = false;
				versionMessage = msg + " You can download the latest version at http://dev.bukkit.org/server-mods/signedit/";
				break;
			case 0:
				msg = pdfile.getName() + " is up to date!";
				log.info(msg);
				break;
			case 1:
				msg = "This server is running a Development version of " + pdfile.getName() + ". Expect bugs!";
				isLatestVersion = false;
				versionMessage = msg;
				log.warning(msg);
				break;
			}
		} else {
			msg = "Error retrieving latest version from BukkitDev.";
			log.warning(msg);
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}
	
	public boolean isLatestVersion() {
		return this.isLatestVersion;
	}
	
	public String getLatestVersion() {
		return this.currentVersion;
	}
	
	public String getVersionMessage() {
		return this.versionMessage;
	}
}