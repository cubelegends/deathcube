package com.henningstorck.deathcube;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardConnector {
	public static final Criteria CRITERIA = Criteria.DUMMY;
	public static final String TITLE = ChatColor.AQUA + "DeathCube";
	public static final String ENTRY = ChatColor.DARK_AQUA + "%s";

	private final ScoreboardManager scoreboardManager;
	private final Scoreboard scoreboard;
	private final Team team;
	private final Objective objective;
	private final List<Player> players = new ArrayList<>();

	public ScoreboardConnector(DeathCube deathCube) {
		this.scoreboardManager = Bukkit.getScoreboardManager();

		if (this.scoreboardManager == null) {
			throw new RuntimeException("Cannot initialize scoreboard manager.");
		}

		this.scoreboard = this.scoreboardManager.getNewScoreboard();
		this.team = this.scoreboard.registerNewTeam(deathCube.getName());
		this.objective = this.scoreboard.registerNewObjective(deathCube.getName(), CRITERIA, TITLE);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void addPlayer(Player player) {
		this.players.add(player);
		this.team.addEntry(this.getEntry(player));
		player.setScoreboard(this.scoreboard);
		this.updateScore(player);
	}

	public void updateScore(Player player) {
		Score score = this.objective.getScore(this.getEntry(player));
		score.setScore(player.getLocation().getBlockY());
	}

	public void clear() {
		for (Player player : this.players) {
			this.team.removeEntry(this.getEntry(player));
			player.setScoreboard(this.scoreboardManager.getNewScoreboard());
		}

		this.players.clear();
	}

	private String getEntry(Player player) {
		return String.format(ENTRY, player.getDisplayName());
	}
}
