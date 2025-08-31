package com.henningstorck.deathcube;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class DeathCube {
	private Location pos1;
	private Location pos2;
	private BlockBar bb;
	private String name;
	private DeathCubeManager plugin;
	private GameManager gm;
	private int tpLocation = 0;
	public int minX;
	public int minZ;
	public int maxX;
	public int maxZ;
	public int minY;
	public int maxY;
	public World world;
	public int stageWidth;
	public int posTowerId;
	public int dropProtection;
	public int mode;
	public int timeLimit;
	public int startHeight;
	public int density;
	public int stageLevel;
	public int height;
	public int tpRange;
	public int pkinProbability;
	public int pkinStart;
	public int burnLevel;
	public int burnSpeed;
	public int burnChance;
	public int burnExpand;
	public int mTimedMinPlayers;
	public int mPublicMinPlayers;
	public long posTowerRate;
	public boolean advertiseTournament;
	public boolean activeRedstone;
	public boolean looseOnFall;
	public boolean userStart;
	public boolean posTower;
	public boolean kickOnCheat;
	public boolean broadcastGames;
	public boolean broadcast;
	public boolean isOpened;
	public boolean offlineFeed;
	public boolean autoRemove;
	public boolean openAfterGame;
	public Location spawn;
	public boolean gameRunning = false;
	public boolean hasCorrectLocation = false;
	public boolean isBurning = false;
	public boolean isBurned = false;
	public String errorState = "not set";
	public long startTime;
	public int threadID = -1;
	public int threadID2 = -1;
	public int threadBurn = -1;
	private int burnHeight;
	private int burnDelay;
	public ArrayList<Player> activePlayers = new ArrayList();
	public ArrayList<Player> retiredPlayers = new ArrayList();
	public ArrayList<String> waiting = new ArrayList();
	public ArrayList<String> winCommands = new ArrayList();
	public ArrayList<String> pWinCommands = new ArrayList();
	public ArrayList<String> tWinCommands = new ArrayList();
	private ArrayList<Integer> burnLevels = new ArrayList();

	public DeathCube(String name, DeathCubeManager plugin) {
		this.name = name;
		this.plugin = plugin;
		this.gm = new GameManager(this);
	}

	public void setBlockBar(BlockBar bb) {
		this.bb = bb;
	}

	public BlockBar getBlockBar() {
		return this.bb;
	}

	public void saveSettings(FileConfiguration config) {
	}

	public String getName() {
		return this.name;
	}

	public Location getPos(int a) {
		return a == 1 ? this.pos1 : this.pos2;
	}

	public void setPos(int a, Location loc) {
		if (a == 1) {
			this.pos1 = loc;
		} else {
			this.pos2 = loc;
		}

	}

	public void endGame() {
		if (this.isBurning) {
			this.plugin.getServer().getScheduler().cancelTask(this.threadID);
			this.isBurning = false;
		}

		if (this.threadID2 != -1) {
			this.plugin.getServer().getScheduler().cancelTask(this.threadID2);
			this.threadID2 = -1;
		}

		if (this.threadBurn != -1) {
			this.plugin.getServer().getScheduler().cancelTask(this.threadBurn);
			this.threadBurn = -1;
		}

		String time = this.elapsedTimeToString((new Date()).getTime() - this.startTime);
		this.broadcast(this.gl("roundTime").replaceFirst("%time%", time));
		this.broadcast(this.gl("privateStop"));
		Iterator var3 = this.activePlayers.iterator();

		while (var3.hasNext()) {
			Player p = (Player) var3.next();
			if (this.autoRemove) {
				this.tpToSpawn(p);
			} else {
				p.getInventory().remove(Material.JACK_O_LANTERN);
				p.sendMessage(this.gl("howToLeave"));
			}
		}

		this.tpLocation = 0;
		this.activePlayers.clear();
		this.retiredPlayers.clear();
		this.burnLevels.clear();
		this.gameRunning = false;
		this.isBurning = false;
		this.isBurned = false;
		this.buildTunnel();
		this.removeEntities();
		if (this.openAfterGame) {
			this.openGates();
			this.buildCube((CommandSender) null);
		}

	}

	private String elapsedTimeToString(long time) {
		int minutes = 0;
		boolean seconds = false;

		for (time /= 1000L; time > 60L && minutes < 61; time -= 60L) {
			++minutes;
		}

		if (minutes < 61) {
			int var5 = (int) time;
			return this.gl("timeString").replaceFirst("%minutes%", "" + minutes).replaceFirst("%seconds%", "" + var5);
		} else {
			return this.gl("timeTooLong");
		}
	}

	public boolean tpIn(Player p) {
		if (this.tpLocation == 3) {
			this.tpLocation = 0;
		} else {
			++this.tpLocation;
		}

		Location target;
		switch (this.tpLocation) {
			case 0:
				target = new Location(this.world, (double) (this.minX + 2), (double) (this.stageLevel + 1), (double) (this.minZ + (this.maxZ - this.minZ) / 2 + 1), -90.0F, -10.0F);
				break;
			case 1:
				target = new Location(this.world, (double) (this.minX + (this.maxX - this.minX) / 2 + 1), (double) (this.stageLevel + 1), (double) (this.minZ + 2), 0.0F, -10.0F);
				break;
			case 2:
				target = new Location(this.world, (double) (this.maxX - 2), (double) (this.stageLevel + 1), (double) (this.minZ + (this.maxZ - this.minZ) / 2 + 1), 90.0F, -10.0F);
				break;
			default:
				target = new Location(this.world, (double) (this.minX + (this.maxX - this.minX) / 2 + 1), (double) (this.stageLevel + 1), (double) (this.maxZ - 2), 180.0F, -10.0F);
		}

		if (target.getBlock().getTypeId() == 0 && this.world.getBlockAt(target.getBlockX(), target.getBlockY() + 1, target.getBlockZ()).getTypeId() == 0) {
			if (this.world.getBlockAt(target.getBlockX(), target.getBlockY() - 1, target.getBlockZ()).getTypeId() != 0) {
				p.setNoDamageTicks(20);
				p.teleport(target);
				if (this.advertiseTournament) {
					p.sendMessage(ChatColor.GREEN + "You think you are good and want to fight for some prices? ");
					p.sendMessage(ChatColor.GREEN + "Check " + ChatColor.GOLD + "/dc h t" + ChatColor.GREEN + " for our upcoming tournament!");
				}

				p.sendMessage(this.plugin.lang.get("tpInHello"));
				return true;
			}

			p.sendMessage(ChatColor.RED + "There is nothing you could stand on!");
		} else {
			p.sendMessage(ChatColor.RED + "You would die because your admin is a retard.");
		}

		return false;
	}

	public void stopBurning() {
	}

	public void playerReachedBurnLimit(Player p) {
		if (p != null) {
			this.broadcast(this.gl("reachedLimit").replaceFirst("%name%", p.getName()).replaceFirst("%level%", "" + this.burnLevel));
		} else {
			this.broadcast(ChatColor.AQUA + "A moderator has ignited the tower: Lets start BURNING THIS SHIT!");
		}

		this.startBurning();
	}

	public void startBurning() {
		if (!this.isBurning && !this.isBurned) {
			this.isBurned = true;
			this.isBurning = true;
			this.burnDelay = 0;
			this.burnHeight = this.startHeight;
			this.burnLevels.add(this.startHeight);
			if (this.threadBurn != -1) {
				this.plugin.getServer().getScheduler().cancelTask(this.threadBurn);
				this.threadBurn = -1;
			}

			if (this.threadID != -1) {
				this.plugin.getServer().getScheduler().cancelTask(this.threadID);
			}

			this.threadID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new DeathTick(this), 10L, (long) this.burnSpeed);
			this.destroyTunnel();
		}

	}

	public void startGame(Player mod) {
		this.startGame();
	}

	private boolean controlPlayer(Player p) {
		if (!p.getActivePotionEffects().isEmpty()) {
			p.sendMessage(ChatColor.GRAY + "Bad boy = sad boy!");
			this.tpToSpawn(p);
			return false;
		} else {
			return true;
		}
	}

	public void startGame() {
		if (this.hasCorrectLocation && !this.gameRunning && this.spawn != null) {
			Iterator timer = this.world.getPlayers().iterator();

			while (true) {
				Player message;
				Location p;
				do {
					if (!timer.hasNext()) {
						if (this.activePlayers.size() > 0) {
							Iterator var12;
							Player var9;
							if (this.broadcast) {
								Collection<? extends Player> var4 = this.plugin.getServer().getOnlinePlayers();
								var12 = var4.iterator();

								while (var12.hasNext()) {
									var9 = (Player) var12.next();
									if (!this.activePlayers.contains(var9)) {
										var9.sendMessage(this.gl("publicStart"));
									}
								}
							}

							timer = this.world.getEntities().iterator();

							while (timer.hasNext()) {
								Entity var5 = (Entity) timer.next();
								if (this.contains(var5.getLocation())) {
									if (!(var5 instanceof Player)) {
										var5.remove();
									} else {
										Player var10 = (Player) var5;
										if (!this.activePlayers.contains(var10)) {
											if (this.plugin.checkPermissions(var10, 2)) {
												var10.sendMessage(this.gl("spec1"));
												var10.sendMessage(this.gl("spec2"));
												this.retiredPlayers.add(var10);
											} else {
												this.tpToSpawn(var10);
												var10.sendMessage(this.gl("onRemove"));
											}
										}
									}
								}
							}

							String var6;
							if (this.activePlayers.size() == 1) {
								var6 = this.gl("playingAllone");
							} else if (this.activePlayers.size() == 2) {
								var6 = this.gl("playingOne");
							} else {
								var6 = this.gl("playingMore").replaceFirst("%count%", "" + this.activePlayers.size());
							}

							var12 = this.activePlayers.iterator();

							while (var12.hasNext()) {
								var9 = (Player) var12.next();
								var9.sendMessage(var6);
								var9.getInventory().remove(Material.JACK_O_LANTERN);
								if (this.pkinProbability > 0 || this.pkinStart > 0) {
									if (this.pkinStart > 0) {
										var9.getInventory().addItem(new ItemStack[]{new ItemStack(91, this.pkinStart)});
									}

									var9.sendMessage(this.gl("notifyJacko"));
								}

								if (this.kickOnCheat) {
									var9.sendMessage(this.gl("notifyBlockPlace"));
								}

								if (var9.getInventory().getItemInHand().getType() == Material.POTION) {
									var9.setItemInHand(new ItemStack(Material.AIR));
								}

								if (var9.getHealth() <= var9.getHealth()) {
									var9.setHealth(var9.getMaxHealth());
								}

								if (var9.getFoodLevel() < 20) {
									var9.setFoodLevel(20);
								}

								if (var9.getGameMode() == GameMode.CREATIVE) {
									var9.setGameMode(GameMode.SURVIVAL);
								}
							}

							this.openGates();
							this.gameRunning = true;
							this.startTime = (new Date()).getTime();
							if (this.threadBurn != -1) {
								this.plugin.getServer().getScheduler().cancelTask(this.threadBurn);
							}

							if (this.timeLimit > 0) {
								this.threadBurn = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
									public void run() {
										DeathCube.this.timeLimitOver();
									}
								}, (long) (this.timeLimit * 60) * 20L);
							}

							if (this.threadID2 == -1) {
								this.plugin.getServer().getScheduler().cancelTask(this.threadID2);
							}

							if (this.posTower && this.threadID2 == -1) {
								PositionTimer var11 = new PositionTimer(this.world, this.minX, this.maxX, this.startHeight, this.height, this.minZ, this.maxZ, this.activePlayers, this.posTowerId);
								this.threadID2 = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, var11, this.posTowerRate, 50L);
								return;
							}
						}

						return;
					}

					message = (Player) timer.next();
					p = message.getLocation();
				} while (p.getBlockY() != this.stageLevel + 1 && p.getBlockY() != this.stageLevel + 2);

				if (this.contains(p, -1) && !this.contains(p, -4) && this.controlPlayer(message)) {
					this.activePlayers.add(message);
					message.sendMessage(this.plugin.lang.get("privateStart"));
				}
			}
		}
	}

	public void timeLimitOver() {
		if (this.gameRunning && !this.isBurning && !this.isBurned) {
			this.startBurning();
			this.broadcast("" + ChatColor.GOLD + this.timeLimit + " minutes" + ChatColor.AQUA + " are over: LETS START BURNING THIS SHIT!");
		}

	}

	public GameManager getGameManager() {
		return this.gm;
	}

	public DeathCubeManager getPlugin() {
		return this.plugin;
	}

	public void stopGame() {
		this.broadcast(this.gl("manualStop"));
		this.endGame();
	}

	private void broadcast(String text) {
		Iterator var3 = this.activePlayers.iterator();

		Player p;
		while (var3.hasNext()) {
			p = (Player) var3.next();
			p.sendMessage(text);
		}

		var3 = this.retiredPlayers.iterator();

		while (var3.hasNext()) {
			p = (Player) var3.next();
			p.sendMessage(text);
		}

	}

	private void broadcastServer(String text) {
	}

	public void openGates() {
		this.isOpened = true;
		int mitteX = this.minX + (this.maxX - this.minX) / 2 - (this.stageWidth - 1);
		int mitteZ = this.minZ + (this.maxZ - this.minZ) / 2 - (this.stageWidth - 1);
		int height = this.stageLevel + 1;

		int i;
		int e;
		for (i = 0; i < this.stageWidth * 2; ++i) {
			this.world.getBlockAt(mitteX + i, height, this.minZ + 3).setTypeId(0);
			this.world.getBlockAt(mitteX + i, height, this.maxZ - 3).setTypeId(0);

			for (e = 4; e < 7; ++e) {
				this.bb.setFloorBlock(this.world.getBlockAt(mitteX + i, this.stageLevel, this.minZ + e));
				this.bb.setFloorBlock(this.world.getBlockAt(mitteX + i, this.stageLevel, this.maxZ - e));
			}
		}

		for (i = 0; i < this.stageWidth * 2; ++i) {
			this.world.getBlockAt(this.minX + 3, height, mitteZ + i).setTypeId(0);
			this.world.getBlockAt(this.maxX - 3, height, mitteZ + i).setTypeId(0);

			for (e = 4; e < 7; ++e) {
				this.bb.setFloorBlock(this.world.getBlockAt(this.minX + e, this.stageLevel, mitteZ + i));
				this.bb.setFloorBlock(this.world.getBlockAt(this.maxX - e, this.stageLevel, mitteZ + i));
			}
		}

	}

	public boolean playerDied(Player p) {
		if (this.gameRunning && this.activePlayers.contains(p)) {
			this.playerLeft(p, this.gl("leaveDeathA"), this.gl("leaveDeathP"));
			return true;
		} else {
			return false;
		}
	}

	public void tpToSpawn(Player p) {
		if (this.spawn != null) {
			p.getInventory().remove(Material.JACK_O_LANTERN);
			p.teleport(this.spawn);
		}

	}

	public void playerReachedTop(Player p) {
		this.activePlayers.remove(p);
		this.broadcast(this.gl("reachedTopA").replaceFirst("%name%", p.getName()));
		p.sendMessage(this.gl("reachedTopP"));
		if (this.autoRemove) {
			this.tpToSpawn(p);
		} else {
			p.sendMessage(this.gl("howToLeave"));
		}

		this.retiredPlayers.add(p);
		this.runWinCommand(p);
		p.getInventory().remove(Material.JACK_O_LANTERN);
		this.endGame();
	}

	public void playerLeft(Player p, String broadcast, String personal) {
		this.activePlayers.remove(p);
		this.broadcast(this.gl("leavePrefix").replaceFirst("%name%", p.getName()) + broadcast);
		p.sendMessage(ChatColor.DARK_AQUA + personal);
		this.retiredPlayers.add(p);
		if (this.activePlayers.size() == 1) {
			this.playerWins();
		} else if (this.activePlayers.size() == 0) {
			this.broadcast(this.gl("nobodyWon"));
			this.endGame();
		} else {
			this.broadcast(this.gl("playersLeft").replaceFirst("%count%", "" + this.activePlayers.size()));
		}

	}

	private void runWinCommand(Player winner) {
		if (winner != null) {
			ConsoleCommandSender p = this.plugin.getServer().getConsoleSender();
			ArrayList winCommands = null;
			if (this.mode == 0) {
				winCommands = this.winCommands;
			} else if (this.mode == 1) {
				winCommands = this.pWinCommands;
			} else {
				winCommands = this.tWinCommands;
			}

			Iterator var5 = winCommands.iterator();

			while (var5.hasNext()) {
				String rawCommand = (String) var5.next();
				String command = rawCommand.replace("%name%", winner.getName());
				this.plugin.getServer().dispatchCommand(p, command);
			}
		}

		if (this.broadcast) {
			Collection<? extends Player> var10 = this.plugin.getServer().getOnlinePlayers();
			Iterator var9 = var10.iterator();

			while (var9.hasNext()) {
				Player var7 = (Player) var9.next();
				if (!this.activePlayers.contains(var7) && !this.retiredPlayers.contains(var7)) {
					var7.sendMessage(this.gl("publicStop").replaceFirst("%name%", winner.getName()));
				}
			}
		}

	}

	private void playerWins() {
		Player p = (Player) this.activePlayers.get(0);
		this.broadcast(this.gl("playerWon").replaceFirst("%name%", p.getName()));
		this.runWinCommand(p);
		this.endGame();
	}

	private void removeEntities() {
		Iterator var2 = this.world.getEntities().iterator();

		while (var2.hasNext()) {
			Entity ente = (Entity) var2.next();
			if (ente.getLocation().getBlockX() >= this.minX && ente.getLocation().getBlockX() <= this.maxX && ente.getLocation().getBlockZ() >= this.minZ && ente.getLocation().getBlockZ() <= this.maxZ && !(ente instanceof HumanEntity)) {
				ente.remove();
			}
		}

	}

	public int countWaitingPlayers() {
		int players = 0;
		Iterator var3 = this.world.getPlayers().iterator();

		while (true) {
			Location loc;
			do {
				if (!var3.hasNext()) {
					return players;
				}

				Player p = (Player) var3.next();
				loc = p.getLocation();
			} while (loc.getBlockY() != this.stageLevel + 1 && loc.getBlockY() != this.stageLevel + 2);

			if (this.contains(loc, -1) && !this.contains(loc, -4)) {
				++players;
			}
		}
	}

	public boolean contains(Location loc) {
		return loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX && loc.getBlockX() <= this.maxX && loc.getBlockZ() >= this.minZ && loc.getBlockZ() <= this.maxZ;
	}

	public boolean contains(Location loc, int variance) {
		return loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX - variance && loc.getBlockX() <= this.maxX + variance && loc.getBlockZ() >= this.minZ - variance && loc.getBlockZ() <= this.maxZ + variance;
	}

	public boolean contains(Location loc, int variance, int from, int to) {
		return loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX - variance && loc.getBlockX() <= this.maxX + variance && loc.getBlockZ() >= this.minZ - variance && loc.getBlockZ() <= this.maxZ + variance && loc.getBlockY() >= from - variance && loc.getBlockY() <= to + variance;
	}

	public boolean isActive(Player p) {
		return this.gameRunning ? this.activePlayers.contains(p) : false;
	}

	public void clearCube() {
		for (int h = this.startHeight; h <= 255; ++h) {
			for (int i = this.minX + 5; i < this.maxX - 4; ++i) {
				for (int e = this.minZ + 5; e < this.maxZ - 4; ++e) {
					this.world.getBlockAt(i, h, e).setTypeId(0);
				}
			}
		}

	}

	public void destroyCube() {
		for (int h = this.startHeight; h <= 255; ++h) {
			for (int i = this.minX + 5; i < this.maxX - 4; ++i) {
				for (int e = this.minZ + 5; e < this.maxZ - 4; ++e) {
					this.world.getBlockAt(i, h, e).setTypeId(0);
				}
			}
		}

	}

	public void buildCube(final CommandSender sender) {
		final int cubeHeight = this.height;
		final World w = this.world;
		final int minX = this.minX + 4;
		final int minZ = this.minZ + 4;
		final int maxX = this.maxX - 4;
		final int maxZ = this.maxZ - 4;
		this.bb.verifyBlocks();
		this.removeEntities();
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				long start = (new Date()).getTime();
				boolean pkin = DeathCube.this.pkinProbability != 0;

				for (int now = DeathCube.this.startHeight; now <= cubeHeight; ++now) {
					for (int i = minX + 5; i < maxX - 4; ++i) {
						for (int e = minZ + 5; e < maxZ - 4; ++e) {
							if (DeathCube.this.random(0, DeathCube.this.density) == 0) {
								if (DeathCube.this.pkinProbability != 0 && DeathCube.this.random(0, DeathCube.this.pkinProbability) == 0) {
									w.getBlockAt(i, now, e).setTypeIdAndData(91, (byte) DeathCube.this.random(0, 4), true);
								} else {
									DeathCube.this.bb.setCubeBlock(w.getBlockAt(i, now, e));
								}
							} else {
								w.getBlockAt(i, now, e).setTypeId(0);
							}
						}
					}
				}

				if (sender != null) {
					long var7 = (new Date()).getTime();
					sender.sendMessage(ChatColor.GREEN + "Cube regenerated in " + ChatColor.GOLD + (var7 - start) + "ms" + ChatColor.GREEN + "!");
				}

				DeathCube.this.removeEntities();
			}
		});
	}

	public void buildTunnel() {
		this.isOpened = false;
		int tunnelPosition = this.stageLevel;
		World w = this.world;
		int minX = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX()) + 2;
		int minZ = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ()) + 2;
		int maxX = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX()) - 2;
		int maxZ = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ()) - 2;

		int height;
		int mitteX;
		for (height = minX + 1; height < maxX; ++height) {
			for (mitteX = minZ; mitteX < maxZ + 1; ++mitteX) {
				this.bb.setFloorBlock(w.getBlockAt(height, tunnelPosition, minZ));
				this.bb.setFloorBlock(w.getBlockAt(height, tunnelPosition, maxZ));
				this.bb.setFloorBlock(w.getBlockAt(minX, tunnelPosition, mitteX));
				this.bb.setFloorBlock(w.getBlockAt(maxX, tunnelPosition, mitteX));
			}
		}

		height = tunnelPosition + 3;

		int mitteZ;
		for (mitteX = minX + 1; mitteX < maxX; ++mitteX) {
			for (mitteZ = minZ; mitteZ < maxZ + 1; ++mitteZ) {
				w.getBlockAt(mitteX, height, minZ).setTypeId(20);
				w.getBlockAt(mitteX, height, maxZ).setTypeId(20);
				w.getBlockAt(minX, height, mitteZ).setTypeId(20);
				w.getBlockAt(maxX, height, mitteZ).setTypeId(20);
			}
		}

		--maxX;
		++minX;
		--maxZ;
		++minZ;
		height = tunnelPosition + 1;
		mitteX = minX + (maxX - minX) / 2;
		mitteZ = minZ + (maxZ - minZ) / 2;

		for (int i = minX + 1; i < maxX; ++i) {
			for (int e = minZ; e < maxZ + 1; ++e) {
				if (i >= mitteX - this.stageWidth && i <= mitteX + 1 + this.stageWidth) {
					this.bb.setFloorBlock(w.getBlockAt(i, height - 1, minZ));
					this.bb.setFloorBlock(w.getBlockAt(i, height - 1, maxZ));
				}

				if (i >= mitteX - (this.stageWidth - 1) && i <= mitteX + this.stageWidth) {
					w.getBlockAt(i, height, minZ).setTypeId(102);
					w.getBlockAt(i, height, maxZ).setTypeId(102);
				} else {
					w.getBlockAt(i, height, minZ).setTypeId(20);
					w.getBlockAt(i, height, maxZ).setTypeId(20);
				}

				if (e >= mitteZ - this.stageWidth && e <= mitteZ + 1 + this.stageWidth) {
					this.bb.setFloorBlock(w.getBlockAt(minX, height - 1, e));
					this.bb.setFloorBlock(w.getBlockAt(maxX, height - 1, e));
				}

				if (e >= mitteZ - (this.stageWidth - 1) && e <= mitteZ + this.stageWidth) {
					w.getBlockAt(minX, height, e).setTypeId(102);
					w.getBlockAt(maxX, height, e).setTypeId(102);
				} else {
					w.getBlockAt(minX, height, e).setTypeId(20);
					w.getBlockAt(maxX, height, e).setTypeId(20);
				}
			}
		}

	}

	public void destroyTunnel() {
		for (int h = 0; h < 4; ++h) {
			for (int i = this.minX + 2; i < this.maxX - 1; ++i) {
				for (int e = this.minZ + 2; e < this.maxZ - 2; ++e) {
					for (int b = 0; b < 5; ++b) {
						this.world.getBlockAt(i, this.stageLevel + h, this.minZ + 2 + b).setTypeId(0);
						this.world.getBlockAt(i, this.stageLevel + h, this.maxZ - 2 - b).setTypeId(0);
						this.world.getBlockAt(this.minX + 2 + b, this.stageLevel + h, e).setTypeId(0);
						this.world.getBlockAt(this.maxX - 2 - b, this.stageLevel + h, e).setTypeId(0);
					}
				}
			}
		}

	}

	public void tickMe() {
		int minX = this.minX + 9;
		int minZ = this.minZ + 9;
		int maxX = this.maxX - 8;
		int maxZ = this.maxZ - 8;
		Iterator i = this.burnLevels.iterator();

		while (i.hasNext()) {
			int oneLevel = (Integer) i.next();
			boolean empty = true;

			for (int x = minX; x < maxX; ++x) {
				for (int z = minZ; z < maxZ; ++z) {
					if (this.world.getBlockAt(x, oneLevel, z).getTypeId() != 0) {
						if (empty) {
							empty = false;
						}

						if (this.random(0, this.burnChance) == this.burnChance) {
							this.world.getBlockAt(x, oneLevel, z).setTypeId(0);
						}
					}
				}
			}

			if (empty) {
				i.remove();
			}
		}

		if (this.burnLevels.isEmpty() && this.burnHeight == this.height) {
			this.plugin.getServer().getScheduler().cancelTask(this.threadID);
			this.isBurning = false;
			this.broadcast(this.gl("towerDestroyed"));
		}

		if (this.burnHeight < this.height) {
			++this.burnDelay;
			if (this.burnDelay == this.burnExpand) {
				this.burnDelay = 0;
				++this.burnHeight;
				this.burnLevels.add(this.burnHeight);
			}
		}

	}

	public void remove() {
		this.gm.destroyAll();
	}

	private String gl(String key) {
		return this.plugin.lang.get(key);
	}

	private int random(int from, int to) {
		++to;
		return (int) (Math.random() * (double) (to - from) + (double) from);
	}
}
