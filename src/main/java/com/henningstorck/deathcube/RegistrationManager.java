package com.henningstorck.deathcube;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

public class RegistrationManager {
	private DeathCubeManager plugin;
	private boolean allowRegistration;
	public ArrayList<String> warteSchlange = new ArrayList();
	private int thread = -1;

	public RegistrationManager(DeathCubeManager plugin, boolean allowedRegistration) {
		this.plugin = plugin;
		this.allowRegistration = allowedRegistration;
		if (this.allowRegistration) {
			if (this.thread != -1) {
				plugin.getServer().getScheduler().cancelTask(this.thread);
			}

			this.thread = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				public void run() {
					RegistrationManager.this.uebertrage();
				}
			}, 18000L, 18000L);
		}

	}

	public boolean isAllowRegistration() {
		return this.allowRegistration;
	}

	public void setAllowRegistration(boolean allowRegistration) {
		this.allowRegistration = allowRegistration;
	}

	public void onRegistrationRequest(CommandSender sender) {
		if (sender instanceof Player) {
			if (this.allowRegistration) {
				if (this.warteSchlange.size() <= 10) {
					sender.sendMessage(ChatColor.GREEN + "Du wurdest erfolgreich in die Warteliste eingetragen. Das Plugin verbindet sich alle 15 Minuten mit dem Server und informiert dich anschliessend über das Ergebnis. Sollte der Server vorher herunterfahren, musst du dich erneut eintragen.");
					Player p = (Player) sender;
					if (!this.warteSchlange.contains(p)) {
						this.warteSchlange.add(p.getName());
					}
				} else {
					sender.sendMessage(ChatColor.GRAY + "Die Warteschlange ist voll, bitte versuche es in 15 Minuten noch einmal oder informiere uns im Falle eines Fehlers.");
				}
			} else {
				sender.sendMessage(ChatColor.GRAY + "Dein Serveradmin hat diese Funktion deaktiviert. Nutze zur Registrierung den Server von " + ChatColor.GOLD + "minebench.de" + ChatColor.GRAY + " oder belaber die Admins ;)");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Dieser Befehl kann ausschliesslich von Spielern verwendet werden!");
		}

	}

	public void uebertrage() {
		if (!this.warteSchlange.isEmpty()) {
			this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
				public void run() {
					String n;
					Iterator key;
					try {
						String e = "";

						for (key = RegistrationManager.this.warteSchlange.iterator(); key.hasNext(); e = e + n + ",") {
							n = (String) key.next();
						}

						e = e.substring(0, e.length() - 1);
						int n2 = RegistrationManager.this.plugin.getServer().getPort();
						String key1 = "" + e.length() * 7 * n2 * Integer.parseInt(("" + n2).substring(0, 2));
						key1 = "" + Math.pow((double) Integer.parseInt(key1.substring(0, 5)), 2.0D);
						String q = "http://minebench.de/vvoranmeldung.php?names=" + e + "&port=" + n2 + "&key=" + key1;
						URL p2 = new URL(q);
						URLConnection yc = p2.openConnection();
						BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
						String inputLine = "";
						int feedback = 5;

						while ((inputLine = in.readLine()) != null) {
							if (inputLine.startsWith("feedback")) {
								feedback = Integer.parseInt(inputLine.substring(9));
								break;
							}
						}

						String nachricht;
						if (feedback > 0) {
							nachricht = ChatColor.RED + "Registrierung war nicht erfolgreich! Informiere mich bitte per E-Mail: info@deathcube.net";
						} else {
							nachricht = ChatColor.GREEN + "Du wurdest erfolgreich registriert.";
						}

						Iterator var12 = RegistrationManager.this.warteSchlange.iterator();

						while (var12.hasNext()) {
							String n1 = (String) var12.next();
							Player p1 = RegistrationManager.this.plugin.getServer().getPlayer(n1);
							if (p1 != null) {
								p1.sendMessage(ChatColor.AQUA + "DeathCube: " + nachricht);
							}
						}
					} catch (IOException var16) {
						key = RegistrationManager.this.warteSchlange.iterator();

						while (key.hasNext()) {
							n = (String) key.next();
							Player p = RegistrationManager.this.plugin.getServer().getPlayer(n);
							if (p != null) {
								p.sendMessage(ChatColor.GRAY + "Deine Registrierung konnte nicht vorgenommen werden, da die Kontaktaufnahme zum Server gescheitert ist. Versuche es spaeter erneut oder informiere den Serverbetreiber.");
							}
						}
					}

					RegistrationManager.this.warteSchlange.clear();
				}
			});
		}

	}
}
