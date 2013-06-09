package net.timroden.signedit;

import com.cyprias.YML;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.timroden.signedit.commands.CommandSignEdit;
import net.timroden.signedit.data.SignEditDataPackage;
import net.timroden.signedit.localization.SignEditLocalization;
import net.timroden.signedit.utils.SignEditLogger;
import net.timroden.signedit.utils.SignEditUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class SignEdit extends JavaPlugin
{
  public String chatPrefix = ChatColor.RESET + "[" + ChatColor.AQUA + "SignEdit" + ChatColor.WHITE + "] " + ChatColor.RESET;
  public PluginManager pluginMan;
  public Map<String, SignEditDataPackage> playerData = new HashMap<String, SignEditDataPackage>();
  public Map<String, Integer> pasteAmounts = new HashMap<String, Integer>();
  private SignEditPlayerListener listener;
  public SignEditLogger log;
  public SignEditUtils utils;
  public SignEditLocalization localization;
  public VersionChecker version;
  public Config config;
  public YML yml;

  public void onEnable()
  {
    this.config = new Config(this);
    this.yml = new YML(this);
    this.localization = new SignEditLocalization(this);

    this.utils = new SignEditUtils(this);
    this.log = new SignEditLogger(this);

    this.listener = new SignEditPlayerListener(this);
    this.version = new VersionChecker(this);

    this.version.start();
    
    if (config.useMetrics()){
	    try {
	      Metrics metrics = new Metrics(this);
	      metrics.start();
	    } catch (IOException e) {
	      this.log.severe(this.localization.get("metricsError"));
	    }
    }
    
    this.pluginMan = getServer().getPluginManager();

    this.pluginMan.registerEvents(this.listener, this);

    getCommand("signedit").setExecutor(new CommandSignEdit(this));
  }

  public void onDisable()
  {
  }
}