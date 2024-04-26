package me.baba43.deathcube;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class GameManager {
    private final DeathCube dc;

    private final BukkitScheduler scheduler;

    private int task;

    private int regTask;

    private long tempTime = 0L;

    private boolean taskByUser;

    private final ArrayList<Integer> tempTimers = new ArrayList<>();

    public GameManager(final DeathCube dc) {
        this.dc = dc;
        this.scheduler = dc.getPlugin().getServer().getScheduler();
    }

    private void startTaskGame() {
        if (!this.dc.gameRunning) {
            if (this.taskByUser && this.dc.countWaitingPlayers() < this.dc.mPublicMinPlayers) {
                final Iterator<Player> var2 = this.dc.world.getPlayers().iterator();
                while (var2.hasNext()) {
                    final Player id = var2.next();
                    final Location loc = id.getLocation();
                    if (loc.getBlockY() == this.dc.stageLevel + 1 || loc.getBlockY() == this.dc.stageLevel + 2) {
                        if (this.dc.contains(loc, -2) && !this.dc.contains(loc, -4)) {
                            id.sendMessage(ChatColor.GRAY + "Requested game will not start because you need " + ChatColor.GOLD + this.dc.mTimedMinPlayers + " players" + ChatColor.GRAY + " to start!");
                        }
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
            final Iterator<Integer> var2 = this.tempTimers.iterator();
            while (var2.hasNext()) {
                final int id1 = ((Integer) var2.next()).intValue();
                this.scheduler.cancelTask(id1);
            }
        }
        this.tempTime = 0L;
    }

    public int[] getTempTime() {
        final Date now = new Date();
        if (this.tempTime != 0L) {
            if (this.tempTime > now.getTime()) {
                final int[] i = this.getLeftTime(this.tempTime - now.getTime());
                return i;
            }
            this.tempTime = 0L;
        }
        return null;
    }

    public void newTempTask(final int minutes, final boolean user) {
        this.taskByUser = user;
        if (!this.tempTimers.isEmpty()) {
            final Iterator<Integer> task = this.tempTimers.iterator();
            while (task.hasNext()) {
                final int timeTo15s = ((Integer) task.next()).intValue();
                this.scheduler.cancelTask(timeTo15s);
            }
        }
        if (minutes == 0) {
            this.tempTime = (new Date()).getTime() + 15000L;
            this.dc.destroyTunnel();
            this.dc.buildTunnel();
            this.dc.buildCube((CommandSender) null);
            this.broadcastLocal((this.dc.getPlugin()).lang.get("15sBroadcast").replace("%dcname%", this.dc.getName()));
            final int timeTo15s = this.scheduler.scheduleSyncDelayedTask((Plugin) this.dc.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    GameManager.this.startTaskGame();
                }
            }, 300L);
        } else {
            this.tempTime = (new Date()).getTime() + (minutes * 60 * 1000);
            if (minutes > 10) {
                final int i = minutes - 10;
                final int j = this.scheduler.scheduleSyncDelayedTask((Plugin) this.dc.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        GameManager.this.broadcast((GameManager.this.dc.getPlugin()).lang.get("XmBroadcast").replace("%dcname%", GameManager.this.dc.getName()).replace("%minutes%", "10"));
                    }
                }, (i * 20 * 60));
                this.tempTimers.add(Integer.valueOf(j));
            } else if (minutes == 10) {
                this.broadcast((this.dc.getPlugin()).lang.get("1mBroadcast").replace("%dcname%", this.dc.getName()).replace("%minutes%", "1"));
            }
            if (minutes > 1) {
                final int i = minutes - 1;
                final int j = this.scheduler.scheduleSyncDelayedTask((Plugin) this.dc.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        if (!GameManager.this.dc.gameRunning) {
                            GameManager.this.broadcast((GameManager.this.dc.getPlugin()).lang.get("XmBroadcast").replace("%dcname%", GameManager.this.dc.getName()).replace("%minutes%", "1"));
                        }
                    }
                }, (i * 20 * 60));
                this.tempTimers.add(Integer.valueOf(j));
            } else if (minutes == 1) {
                this.broadcast((this.dc.getPlugin()).lang.get("1mBroadcast").replace("%dcname%", this.dc.getName()));
            }
            final int timeTo15s = minutes * 60 - 15;
            int task1 = this.scheduler.scheduleSyncDelayedTask((Plugin) this.dc.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!GameManager.this.dc.gameRunning) {
                        GameManager.this.dc.destroyTunnel();
                        GameManager.this.dc.buildTunnel();
                        GameManager.this.dc.buildCube((CommandSender) null);
                        GameManager.this.broadcastLocal((GameManager.this.dc.getPlugin()).lang.get("15sBroadcast").replace("%dcname%", GameManager.this.dc.getName()));
                    }
                }
            }, (timeTo15s * 20));
            this.tempTimers.add(Integer.valueOf(task1));
            final Long timeToStart = Long.valueOf((minutes * 60 * 20));
            task1 = this.scheduler.scheduleSyncDelayedTask((Plugin) this.dc.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    GameManager.this.startTaskGame();
                }
            }, timeToStart.longValue());
            this.tempTimers.add(Integer.valueOf(task1));
        }
    }

    public void destroyAll() {
        if (!this.tempTimers.isEmpty()) {
            final Iterator<Integer> var2 = this.tempTimers.iterator();
            while (var2.hasNext()) {
                final int id = ((Integer) var2.next()).intValue();
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
        final boolean seconds = false;
        for (time /= 1000L; time > 60L && minutes < 61; time -= 60L) {
            minutes++;
        }
        if (minutes < 61) {
            final int var6 = (int) time;
            final int[] i = {minutes, var6};
            return i;
        }
        return null;
    }

    private void broadcastLocal(final String text) {
        final Iterator<Player> var3 = this.dc.world.getPlayers().iterator();
        while (var3.hasNext()) {
            final Player p = var3.next();
            final Location loc = p.getLocation();
            if (loc.getBlockY() >= this.dc.startHeight && loc.getBlockY() <= this.dc.height + 1 && this.dc.contains(loc, 2)) {
                p.sendMessage(ChatColor.GRAY + text);
            }
        }
    }

    private void broadcast(final String text) {
        this.dc.getPlugin().getServer().broadcastMessage(text);
    }
}
