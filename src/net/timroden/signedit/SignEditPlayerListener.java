package net.timroden.signedit;

import java.util.logging.Level;
import net.timroden.signedit.data.LogType;
import net.timroden.signedit.data.SignEditDataPackage;
import net.timroden.signedit.data.SignFunction;
import net.timroden.signedit.utils.SignEditUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SignEditPlayerListener implements Listener {
	private SignEdit plugin;
	private SignEditUtils utils;

	public SignEditPlayerListener(SignEdit plugin) {
		this.plugin = plugin;
		this.utils = plugin.utils;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		/**/
		if ((Config.notifyVersionUpdate()) && (player.isPermissionSet("signedit.notify")) && (!VersionChecker.isLatestVersion()))

			this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

				@Override
				public void run() {
					player.sendMessage(SignEditPlayerListener.this.plugin.chatPrefix + ChatColor.DARK_PURPLE + VersionChecker.getVersionMessage());

				}
			}, 1L);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		SignEditDataPackage dataPack = null;
		if (!event.getAction().equals(Config.clickAction())) {
			return;
		}
		if (!this.plugin.playerData.containsKey(player.getName())) {
			return;
		}
		if ((block == null) || (!this.utils.isSign(block))) {
			return;
		}
		Sign sign = (Sign) block.getState();
		dataPack = (SignEditDataPackage) this.plugin.playerData.get(player.getName());

		SignFunction function = dataPack.getFunction();

		if (function.equals(SignFunction.COPY)) {
			if (this.utils.shouldCancel(player)) {
				event.setCancelled(true);
				sign.update();
			}
			SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), sign.getLines(), dataPack.getAmount(), SignFunction.PASTE);
			this.plugin.playerData.put(player.getName(), tmp);
			player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("copySignAdded", new Object[] { this.plugin.config.clickActionStr() }));
		} else if (function.equals(SignFunction.COPYPERSIST)) {
			if (this.utils.shouldCancel(player)) {
				event.setCancelled(true);
				sign.update();
			}
			SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), SignFunction.PASTEPERSIST, sign.getLines());
			this.plugin.playerData.put(player.getName(), tmp);
			player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("copySignAdded", new Object[] { this.plugin.config.clickActionStr() }));
		} else if (function.equals(SignFunction.PASTE)) {
			if (this.utils.shouldCancel(player)) {
				event.setCancelled(true);
			}
			String[] lines = dataPack.getLines();

			if (this.utils.throwSignChange(block, player, sign.getLines()).booleanValue()) {
				player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("pasteError"));
				this.plugin.playerData.remove(player.getName());
				return;
			}

			for (int i = 0; i < lines.length; i++) {
				sign.setLine(i, lines[i]);
			}
			sign.update();

			int amount = dataPack.getAmount();

			amount--;
			if (amount == 0) {
				this.utils.throwSignChange(block, player, sign.getLines());
				player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("pasted") + " " + this.plugin.localization.get("pasteEmpty"));
				this.plugin.playerData.remove(player.getName());
				return;
			}
			SignEditDataPackage tmp = new SignEditDataPackage(player.getName(), lines, amount, SignFunction.PASTE);
			this.plugin.playerData.put(player.getName(), tmp);
			player.sendMessage(this.plugin.chatPrefix
				+ this.plugin.localization.get("pasted")
				+ " "
				+ this.plugin.localization.get(
					"pasteCopiesLeft",
					new Object[] { Integer.valueOf(amount),
						amount == 1 ? this.plugin.localization.get("pasteCopyStr") : this.plugin.localization.get("pasteCopiesStr") }));
		} else if (function.equals(SignFunction.PASTEPERSIST)) {
			if (this.utils.shouldCancel(player)) {
				event.setCancelled(true);
			}
			String[] lines = dataPack.getLines();

			if (this.utils.throwSignChange(block, player, sign.getLines()).booleanValue()) {
				player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("pasteError"));
				this.plugin.playerData.remove(player.getName());
				return;
			}

			for (int i = 0; i < lines.length; i++) {
				sign.setLine(i, lines[i]);
			}
			sign.update();
			player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("pasteCopiesLeft", new Object[] { "∞", "copies" }));
		} else if (function.equals(SignFunction.EDIT)) {
			if (this.utils.shouldCancel(player)) {
				event.setCancelled(true);
			}
			int line = dataPack.getLineNum();
			String originalLine = sign.getLine(line);

			String[] existingLines = sign.getLines();
			String newText = dataPack.getLine();
			existingLines[line] = newText;

			if (this.utils.throwSignChange(block, player, existingLines).booleanValue()) {
				player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("editError"));
				this.plugin.playerData.remove(player.getName());
				return;
			}

			sign.setLine(line, ChatColor.translateAlternateColorCodes('&', newText));

			this.plugin.log.logAll(player.getName(),
				": (" + sign.getLocation().getBlockX() + ", " + sign.getLocation().getBlockY() + ", " + sign.getLocation().getBlockZ() + ", "
					+ player.getWorld().getName() + ") \"" + originalLine + "\" " + this.plugin.localization.get("logChangedTo") + " \"" + newText + "\"",
				LogType.SIGNCHANGE, Level.INFO);
			sign.update();
			player.sendMessage(this.plugin.chatPrefix + this.plugin.localization.get("editChanged"));
			this.plugin.playerData.remove(player.getName());
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (this.plugin.config.colorsOnPlace()) {
			if ((this.plugin.config.useCOPPermission()) && (!e.getPlayer().hasPermission("signedit.colorsonplace"))) {
				return;
			}

			String[] lines = e.getLines();
			for (int i = 0; i < 4; i++) {
				String line = lines[i];
				line = ChatColor.translateAlternateColorCodes('&', line);
				e.setLine(i, line);
			}
		}
	}
}