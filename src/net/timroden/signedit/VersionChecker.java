package net.timroden.signedit;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.timroden.signedit.utils.SignEditLogger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionChecker extends Thread {
	private SignEdit plugin;
	private String rssURL = "http://dev.bukkit.org/server-mods/signedit/files.rss";
	private String dlURL = "http://dev.bukkit.org/server-mods/signedit/";
	private String currentVersion = null;	
	private String versionMessage;	
	private PluginDescriptionFile pdfile;	
	private SignEditLogger log;	
	private boolean isLatestVersion = false;

	public VersionChecker(SignEdit plugin) {
		this.plugin = plugin;
		this.pdfile = plugin.getDescription();
		this.log = plugin.log;
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
					currentVersion = getTagValue("title", eElement).toLowerCase().replace("signedit ", "");
					break;
				}
			}
		} catch(Exception e) {
			log.info(plugin.localization.get("versionRetrieveError"));
			log.info(e.toString());
			return;
		}

		String msg = null;

		if(currentVersion != null) {
			if(currentVersion.contains("broken")) {
				msg = plugin.localization.get("versionUpToDate", pdfile.getName());
				versionMessage = msg;
				log.info(msg);
				return;
			}

			int compare = pdfile.getVersion().compareTo(currentVersion);

			if(compare < 0) {
				msg = plugin.localization.get("versionOutOfDate", pdfile.getName(), currentVersion) + " " + plugin.localization.get("versionLatestDownload", dlURL);
				isLatestVersion = false;
				versionMessage = msg;
				log.warning(msg);
			} else if(compare == 0) {
				msg = plugin.localization.get("versionUpToDate", pdfile.getName());
				log.info(msg);
			} else {
				msg = plugin.localization.get("versionDevelopmental", pdfile.getName());
				isLatestVersion = false;
				versionMessage = msg;
				log.warning(msg);
			}
		} else {
			log.info(plugin.localization.get("versionRetrieveError"));
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