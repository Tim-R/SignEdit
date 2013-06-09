package net.timroden.signedit;

import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.timroden.signedit.localization.SignEditLocalization;
import net.timroden.signedit.utils.SignEditLogger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionChecker extends Thread
{
  private SignEdit plugin;
  private String rssURL = "http://dev.bukkit.org/server-mods/signedit/files.rss";
  private String dlURL = "http://dev.bukkit.org/server-mods/signedit/";
  private static String currentVersion = null;
  private static String versionMessage;
  private PluginDescriptionFile pdfile;
  private SignEditLogger log;
  private static boolean isLatestVersion = false;

  public VersionChecker(SignEdit plugin) {
    this.plugin = plugin;
    this.pdfile = plugin.getDescription();
    this.log = plugin.log;
  }

  public void run()
  {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      URL url = new URL(this.rssURL);
      URLConnection con = url.openConnection();

      con.setConnectTimeout(8000);
      con.setReadTimeout(15000);
      con.setRequestProperty("User-agent", this.pdfile.getName() + " " + this.pdfile.getVersion());

      Document doc = docBuilder.parse(con.getInputStream());
      doc.getDocumentElement().normalize();
      NodeList nList = doc.getElementsByTagName("item");

      for (int i = 0; i < nList.getLength(); i++) {
        Node nNode = nList.item(i);
        if (nNode.getNodeType() == 1) {
          Element eElement = (Element)nNode;
          currentVersion = getTagValue("title", eElement).toLowerCase().replace("signedit ", "");
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    String msg = null;

    if (currentVersion != null) {
      if (currentVersion.contains("broken")) {
        msg = this.plugin.localization.get("versionUpToDate", new Object[] { this.pdfile.getName() });
        versionMessage = msg;
        isLatestVersion = true;
        this.log.info(msg);
        return;
      }

      int compare = this.pdfile.getVersion().compareTo(currentVersion);

      if (compare < 0) {
        msg = this.plugin.localization.get("versionOutOfDate", new Object[] { this.pdfile.getName(), currentVersion }) + " " + this.plugin.localization.get("versionLatestDownload", new Object[] { this.dlURL });
        versionMessage = msg;
        this.log.warning(msg);
      } else if (compare == 0) {
        msg = this.plugin.localization.get("versionUpToDate", new Object[] { this.pdfile.getName() });
        versionMessage = msg;
        isLatestVersion = true;
        this.log.info(msg);
      } else {
        msg = this.plugin.localization.get("versionDevelopmental", new Object[] { this.pdfile.getName() });
        versionMessage = msg;
        this.log.warning(msg);
      }
    } else {
      this.log.warning(this.plugin.localization.get("versionRetrieveError"));
    }
  }

  private static String getTagValue(String sTag, Element eElement) {
    NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

    Node nValue = nlList.item(0);

    return nValue.getNodeValue();
  }

  public static boolean isLatestVersion() {
    return isLatestVersion;
  }

  public static String getLatestVersion() {
    return currentVersion;
  }

  public static String getVersionMessage() {
    return versionMessage;
  }
}