package me.baba43.DeathCube;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class GameManager {
	private DeathCube dc;
	private BukkitScheduler scheduler;
	private int task;
	private int regTask;
	private long tempTime = 0L;
	private boolean taskByUser;
	private ArrayList<Integer> tempTimers = new ArrayList();

	public GameManager(DeathCube dc) {
		this.dc = dc;
		this.scheduler = dc.getPlugin().getServer().getScheduler();
	}

	private void startTaskGame() {
		Iterator var2;
		if (!this.dc.gameRunning) {
			if (this.taskByUser && this.dc.countWaitingPlayers() < this.dc.mPublicMinPlayers) {
				var2 = this.dc.world.getPlayers().iterator();

				label45:
				while (true) {
					Player id;
					Location loc;
					do {
						if (!var2.hasNext()) {
							break label45;
						}

						id = (Player) var2.next();
						loc = id.getLocation();
					} while (loc.getBlockY() != this.dc.stageLevel + 1 && loc.getBlockY() != this.dc.stageLevel + 2);

					if (this.dc.contains(loc, -2) && !this.dc.contains(loc, -4)) {
						id.sendMessage(ChatColor.GRAY + "Requested game will not start because you need " + ChatColor.GOLD + this.dc.mTimedMinPlayers + " players" + ChatColor.GRAY + " to start!");
					}
				}
			} else {
				if (this.taskByUser) {
					this.dc.mode = 1;
				} else {
					this.dc.mode = 0;
				}

				this.dc.startGame();
			}
		}

		if (!this.tempTimers.isEmpty()) {
			var2 = this.tempTimers.iterator();

			while (var2.hasNext()) {
				int id1 = (Integer) var2.next();
				this.scheduler.cancelTask(id1);
			}
		}

		this.tempTime = 0L;
	}

	public int[] getTempTime() {
		Date now = new Date();
		if (this.tempTime != 0L) {
			if (this.tempTime > now.getTime()) {
				int[] i = this.getLeftTime(this.tempTime - now.getTime());
				return i;
			}

			this.tempTime = 0L;
		}

		return null;
	}

	public void newTempTask(int minutes, boolean user) {
		this.taskByUser = user;
		int timeTo15s;
		if (!this.tempTimers.isEmpty()) {
			Iterator task = this.tempTimers.iterator();

			while (task.hasNext()) {
				timeTo15s = (Integer) task.next();
				this.scheduler.cancelTask(timeTo15s);
			}
		}

		if (minutes == 0) {
			this.tempTime = (new Date()).getTime() + 15000L;
			this.dc.destroyTunnel();
			this.dc.buildTunnel();
			this.dc.buildCube((CommandSender) null);
			this.broadcastLocal(this.dc.getPlugin().lang.get("15sBroadcast").replace("%dcname%", this.dc.getName()));
			timeTo15s = this.scheduler.scheduleSyncDelayedTask(this.dc.getPlugin(), new Runnable() {
				public void run() {
					GameManager.this.startTaskGame();
				}
			}, 300L);
		} else {
			this.tempTime = (new Date()).getTime() + (long) (minutes * 60 * 1000);
			int task1;
			if (minutes > 10) {
				timeTo15s = minutes - 10;
				task1 = this.scheduler.scheduleSyncDelayedTask(this.dc.getPlugin(), new Runnable() {
					public void run() {
						GameManager.this.broadcast(GameManager.this.dc.getPlugin().lang.get("XmBroadcast").replace("%dcname%", GameManager.this.dc.getName()).replace("%minutes%", "10"));
					}
				}, (long) (timeTo15s * 20 * 60));
				this.tempTimers.add(task1);
			} else if (minutes == 10) {
				this.broadcast(this.dc.getPlugin().lang.get("1mBroadcast").replace("%dcname%", this.dc.getName()).replace("%minutes%", "1"));
			}

			if (minutes > 1) {
				timeTo15s = minutes - 1;
				task1 = this.scheduler.scheduleSyncDelayedTask(this.dc.getPlugin(), new Runnable() {
					public void run() {
						if (!GameManager.this.dc.gameRunning) {
							GameManager.this.broadcast(GameManager.this.dc.getPlugin().lang.get("XmBroadcast").replace("%dcname%", GameManager.this.dc.getName()).replace("%minutes%", "1"));
						}

					}
				}, (long) (timeTo15s * 20 * 60));
				this.tempTimers.add(task1);
			} else if (minutes == 1) {
				this.broadcast(this.dc.getPlugin().lang.get("1mBroadcast").replace("%dcname%", this.dc.getName()));
			}

			timeTo15s = minutes * 60 - 15;
			task1 = this.scheduler.scheduleSyncDelayedTask(this.dc.getPlugin(), new Runnable() {
				public void run() {
					if (!GameManager.this.dc.gameRunning) {
						GameManager.this.dc.destroyTunnel();
						GameManager.this.dc.buildTunnel();
						GameManager.this.dc.buildCube((CommandSender) null);
						GameManager.this.broadcastLocal(GameManager.this.dc.getPlugin().lang.get("15sBroadcast").replace("%dcname%", GameManager.this.dc.getName()));
					}

				}
			}, (long) (timeTo15s * 20));
			this.tempTimers.add(task1);
			Long timeToStart = (long) (minutes * 60 * 20);
			task1 = this.scheduler.scheduleSyncDelayedTask(this.dc.getPlugin(), new Runnable() {
				public void run() {
					GameManager.this.startTaskGame();
				}
			}, timeToStart);
			this.tempTimers.add(task1);
		}

	}

	public void destroyAll() {
		if (!this.tempTimers.isEmpty()) {
			Iterator var2 = this.tempTimers.iterator();

			while (var2.hasNext()) {
				int id = (Integer) var2.next();
				this.scheduler.cancelTask(id);
			}
		}

		if (this.regTask != -1) {
			this.scheduler.cancelTask(this.regTask);
		}

		this.tempTime = 0L;
	}

	private int[] getLeftTime(long time) {
		int minutes = 0;
		boolean seconds = false;

		for (time /= 1000L; time > 60L && minutes < 61; time -= 60L) {
			++minutes;
		}

		if (minutes < 61) {
			int var6 = (int) time;
			int[] i = new int[]{minutes, var6};
			return i;
		} else {
			return null;
		}
	}

	private void broadcastLocal(String text) {
		Iterator var3 = this.dc.world.getPlayers().iterator();

		while (var3.hasNext()) {
			Player p = (Player) var3.next();
			Location loc = p.getLocation();
			if (loc.getBlockY() >= this.dc.startHeight && loc.getBlockY() <= this.dc.height + 1 && this.dc.contains(loc, 2)) {
				p.sendMessage(ChatColor.GRAY + text);
			}
		}

	}

	private void broadcast(String text) {
		this.dc.getPlugin().getServer().broadcastMessage(text);
	}
}
