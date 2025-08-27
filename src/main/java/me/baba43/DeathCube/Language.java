package me.baba43.DeathCube;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Language {
	private File langYML = new File("plugins/DeathCube/language.yml");
	private FileConfiguration language;
	private File confFile;
	private DeathCubeManager plugin;

	public Language(DeathCubeManager plugin) {
		this.plugin = plugin;
		this.confFile = new File(plugin.getDataFolder(), "language.yml");
		this.language = YamlConfiguration.loadConfiguration(this.confFile);
		this.load();
	}

	public void load() {
		try {
			this.language.load(this.confFile);
		} catch (FileNotFoundException var3) {
			this.o("Language file not found!");
		} catch (IOException var4) {
			this.o("Unknown error reading language-file");
		} catch (InvalidConfigurationException var5) {
			this.o("Unknown language file error");
		}

		this.set("noPermissions", ChatColor.RED + "No Permissions!");
		this.set("tpInHello", ChatColor.GREEN + "Hello :)");
		this.set("inventoryNotEmpty1", ChatColor.RED + "Your inventory should be empty because you could die!");
		this.set("inventoryNotEmpty2", ChatColor.AQUA + "If you don't care about your items use " + ChatColor.GOLD + "/dc tpi");
		this.set("equipWarning", ChatColor.RED + "You can't take the following cheater-equipment with you:");
		this.set("equipWarningBoots", ChatColor.RED + "Sorry I'm afraid of your cheater-boots!");
		this.set("equipWarningHelmet", ChatColor.RED + "Sorry I'm afraid of your cheater-helmet!");
		this.set("equipWarningLeggings", ChatColor.RED + "Sorry I'm afraid of your cheater-leggings!");
		this.set("equipWarningArmor", ChatColor.RED + "Sorry I'm afraid of your cheater-armor!");
		this.set("hasActivePotion", ChatColor.RED + "You are on drugs! Come back if you feel good again.");
		this.set("hasJackos", ChatColor.RED + "You can't take that Jack O'Lantern with you!");
		this.set("warningPotion", ChatColor.GRAY + "If I see any drugs in your hand I will take them!");
		this.set("warningEnderpearl", ChatColor.GRAY + "Use your enderpearl ingame and you will be kicked.");
		this.set("inventoryWarning", ChatColor.AQUA + "Notice that you may loose your inventory!");
		this.set("toFarAway", ChatColor.RED + "Sorry you are too far away.");
		this.set("inAnotherWorld", ChatColor.RED + "You are not part of my world!");
		this.set("needSpawn", ChatColor.RED + "Your admin has to set a spawn location first!");
		this.set("gameIsRunning", ChatColor.RED + "Game is already running!");
		this.set("commandBlocked", ChatColor.RED + "This command is blocked in the Cube area. Use " + ChatColor.GOLD + "/dc L");
		this.set("barProtection", ChatColor.RED + "You can't touch this!");
		this.set("innerProtection", ChatColor.RED + "Noooez!");
		this.set("outerProtection", ChatColor.RED + "Please don't destroy the DeathCube. Thx!");
		this.set("jokerOffline", ChatColor.RED + "We are not playing!");
		this.set("jokerOnline", ChatColor.GREEN + "Here is your joker, don't miss it :)");
		this.set("leavePrefix", ChatColor.GOLD + "%name%" + ChatColor.AQUA + " is out: ");
		this.set("leaveBlockplaceA", "He tried to place a block.");
		this.set("leaveBlockplaceP", "Like I told you.. cheater");
		this.set("leaveAreaA", "He left the cube area.");
		this.set("leaveAreaP", "Run away..");
		this.set("leaveEnderpearlA", "Tried to use an enderpearl.");
		this.set("leaveEnderpearlP", "Are you trolling me?");
		this.set("leaveDisconnectA", "He disconnected.");
		this.set("leaveDeathA", "He died.");
		this.set("leaveDeathP", "Haha I killed you!");
		this.set("leaveFallA", "He kissed the ground.");
		this.set("leaveFallP", "Let me save you!");
		this.set("playersLeft", ChatColor.AQUA + "%count% players left.");
		this.set("enterProtection", ChatColor.RED + "You are not part of this game, sorry!");
		this.set("protectionBar", ChatColor.RED + "Don't touch this!");
		this.set("removeOnSpawn", ChatColor.AQUA + "Sorry.. I can't let you spawn here.");
		this.set("potionSelect", ChatColor.GRAY + "No drugs while playing. I'll keep them!");
		this.set("noSelection", ChatColor.GRAY + "No DeathCube selected. Use " + ChatColor.GOLD + " /dc select");
		this.set("invalidLocation", ChatColor.RED + "Invalid Location!");
		this.set("privateStart", ChatColor.AQUA + "+++ DeathCube Game started +++");
		this.set("privateStop", ChatColor.AQUA + "+++ DeathCube Game over +++");
		this.set("publicStart", ChatColor.AQUA + "A Deathcube round has just begun!");
		this.set("publicStop", ChatColor.GOLD + "%name%" + ChatColor.AQUA + " has won a DeathCube match!");
		this.set("manualStop", ChatColor.AQUA + "Game has been stopped by an admin!");
		this.set("spec1", ChatColor.GREEN + "You are spectator!");
		this.set("spec2", ChatColor.GRAY + "You will receive any game-related messages!");
		this.set("onRemove", ChatColor.AQUA + "Sorry bro but some people want to play DeathCube!");
		this.set("playingAllone", ChatColor.AQUA + "You are playing allone :(");
		this.set("playingOne", ChatColor.AQUA + "You are playing with " + ChatColor.GOLD + "one" + ChatColor.AQUA + " other player");
		this.set("playingMore", ChatColor.AQUA + "You are playing with " + ChatColor.GOLD + "%count%" + ChatColor.AQUA + " other players");
		this.set("notifyJacko", ChatColor.DARK_AQUA + "You can destroy Jack O'Lanterns and replace them one time");
		this.set("notifyBlockPlace", ChatColor.DARK_AQUA + "You will be kicked if you try to place a block");
		this.set("reachedTopA", ChatColor.GOLD + "%name%" + ChatColor.AQUA + " reached the top!");
		this.set("reachedTopP", ChatColor.AQUA + "You reached the top. " + ChatColor.GOLD + "Good job!");
		this.set("reachedLimit", ChatColor.GOLD + "%name%" + ChatColor.AQUA + " reached level %level%: Lets start BURNING THIS SHIT!");
		this.set("playerWon", ChatColor.GOLD + "%name%" + ChatColor.AQUA + " has won this round because he was playing with noobs!");
		this.set("howToLeave", ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc leave" + ChatColor.GRAY + " to get out!");
		this.set("nobodyWon", ChatColor.AQUA + "Nobody has won this round!");
		this.set("towerDestroyed", ChatColor.AQUA + "Tower is completly destroyed!");
		this.set("roundTime", ChatColor.AQUA + "This game took %time%.");
		this.set("timeString", ChatColor.GOLD + "%minutes% minutes " + ChatColor.AQUA + "and " + ChatColor.GOLD + "%seconds% seconds" + ChatColor.AQUA);
		this.set("timeTooLong", ChatColor.GOLD + "over 60 minutes");
		this.set("onFeed", ChatColor.GRAY + "Okay bro, here is your bread!");
		this.set("notHungry", ChatColor.GRAY + "You are not hungry!");
		this.set("notIngame", ChatColor.GRAY + "Sorry but you need to be in a game!");
		this.set("onFeed", ChatColor.GRAY + "Here is your bread!");
		this.set("helpCommands", ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc tp " + ChatColor.GRAY + " to teleport to the default deathcube;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc food " + ChatColor.GRAY + " to receive food if you are hungry;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc leave " + ChatColor.GRAY + " to leave the deathcube area;;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc list" + ChatColor.GRAY + " to get a list of available deathcubes;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc select <name>" + ChatColor.GRAY + " to select a deathcube;" + ChatColor.GRAY + "Note: Usually you don't need the selection command.");
		this.set("helpIndex", ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc help info" + ChatColor.GRAY + " if you have no idea what deathcube is.;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc help commands" + ChatColor.GRAY + " to get a list of useable commands.;" + ChatColor.GRAY + "Use " + ChatColor.GOLD + "/dc help rules" + ChatColor.GRAY + " to get a list of rules for your cube.");
		this.set("helpInfo", ChatColor.GRAY + "Your simple goal is to climb up a tower with air in it, so that jumps are possible. If someone reaches the top or his enemies died he wins the game. Depending on the settings, the tower can start crumbling from the bottom if a player reached level X or XX minutes are over. Try the rules help page to get a list of the current settings.");
		this.set("15sBroadcast", ChatColor.GRAY + "This DeathCube will start in 15 seconds!");
		this.set("1mBroadcast", ChatColor.GRAY + "DeathCube " + ChatColor.GOLD + "%dcname%" + ChatColor.GRAY + " will start in 1 minute!");
		this.set("XmBroadcast", ChatColor.GRAY + "DeathCube " + ChatColor.GOLD + "%dcname%" + ChatColor.GRAY + " will start in %minutes% minutes!");

		try {
			this.language.save(this.confFile);
		} catch (IOException var2) {
			this.o("Couldn't save the language-file!");
			var2.printStackTrace();
		}

	}

	public void set(String key, String value) {
		if (!this.language.contains(key)) {
			this.language.set(key, value);
		}

	}

	public String get(String name) {
		return this.language.contains(name) ? this.language.getString(name) : ChatColor.GRAY + "Your language file is broken (" + name + ")";
	}

	private void o(String text) {
		System.out.println("DC: " + text);
	}
}
