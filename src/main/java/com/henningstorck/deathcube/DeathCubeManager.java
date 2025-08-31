package com.henningstorck.deathcube;

import org.bukkit.*;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DeathCubeManager extends JavaPlugin implements Listener {
	private Map<CommandSender, DeathCube> cubeSelections = new HashMap();
	private Map<String, DeathCube> cubeNames = new HashMap();
	private File configPath = new File("plugins/DeathCube/config.yml");
	private FileConfiguration config;
	public Language lang;
	private String version = "v3.0.0-SNAPSHOT";
	private ArrayList<String> allowedCommands = new ArrayList();
	private DeathCube defaultDC;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dc")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("create")) {
					if (this.checkPermissions(sender, 3)) {
						this.onCubeCreationRequest(sender);
					} else {
						sender.sendMessage(ChatColor.RED + "No Permissions");
					}
				} else if (args[0].equalsIgnoreCase("select")) {
					if (this.checkPermissions(sender, 1)) {
						if (args.length == 1) {
							this.tellSelection(sender);
						} else if (args.length == 2) {
							this.onSelectionRequest(sender, args[1]);
						} else {
							sender.sendMessage(ChatColor.RED + "Wrong usage. Use /dc select [name]");
						}
					} else {
						sender.sendMessage(this.lang.get("noPermissions"));
					}
				} else if (args[0].equalsIgnoreCase("pos")) {
					if (this.checkPermissions(sender, 3)) {
						if (sender instanceof Player) {
							if (args.length == 2) {
								this.onSetLocationRequest(sender, args[1]);
							} else if (args.length == 1) {
								this.onCheckPositionRequest(sender);
							} else {
								sender.sendMessage(ChatColor.RED + "Wrong usage. Use /dc pos 1|2");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "This command need to be run by a player!");
						}
					} else {
						sender.sendMessage(this.lang.get("noPermissions"));
					}
				} else if (!args[0].equalsIgnoreCase("new") && !args[0].equalsIgnoreCase("n")) {
					if (args[0].equalsIgnoreCase("set")) {
						if (this.checkPermissions(sender, 2)) {
							if (args.length == 3) {
								this.onChangeConfigurationRequest(sender, args[1], args[2]);
							} else {
								sender.sendMessage(ChatColor.RED + "Wrong usage! Use /dc set <key> <value>");
								sender.sendMessage(ChatColor.RED + "Use '_' for spaces in strings");
							}
						} else {
							sender.sendMessage(this.lang.get("noPermissions"));
						}
					} else if (args[0].equalsIgnoreCase("get")) {
						if (this.checkPermissions(sender, 2)) {
							if (args.length == 2) {
								this.onGetConfigurationRequest(sender, args[1]);
							} else {
								sender.sendMessage(ChatColor.GRAY + "Use /dc get <key>");
							}
						} else {
							sender.sendMessage(this.lang.get("noPermissions"));
						}
					} else if (args[0].equalsIgnoreCase("open")) {
						if (this.checkPermissions(sender, 2)) {
							this.onOpenGateRequest(sender);
						} else {
							sender.sendMessage(this.lang.get("noPermissions"));
						}
					} else if (args[0].equalsIgnoreCase("tp")) {
						if (this.checkPermissions(sender, 1)) {
							if (args.length == 2) {
								this.onTeleportationRequest(sender, false, args[1]);
							} else {
								this.onTeleportationRequest(sender, false, (String) null);
							}
						} else {
							sender.sendMessage(this.lang.get("noPermissions"));
						}
					} else if (args[0].equalsIgnoreCase("tpi")) {
						if (this.checkPermissions(sender, 1)) {
							if (args.length == 2) {
								this.onTeleportationRequest(sender, true, args[1]);
							} else {
								this.onTeleportationRequest(sender, true, (String) null);
							}
						} else {
							sender.sendMessage(this.lang.get("noPermissions"));
						}
					} else if (!args[0].equalsIgnoreCase("food") && !args[0].equalsIgnoreCase("f")) {
						if (args[0].equalsIgnoreCase("list")) {
							if (this.checkPermissions(sender, 1)) {
								this.onListDeathCubesRequest(sender);
							} else {
								sender.sendMessage(this.lang.get("noPermissions"));
							}
						} else if (!args[0].equalsIgnoreCase("info") && !args[0].equalsIgnoreCase("i")) {
							if (!args[0].equalsIgnoreCase("start") && !args[0].equalsIgnoreCase("s")) {
								if (args[0].equalsIgnoreCase("stop")) {
									if (this.checkPermissions(sender, 2)) {
										this.onStopRequest(sender);
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (args[0].equalsIgnoreCase("spawn")) {
									if (this.checkPermissions(sender, 3)) {
										this.onSpawnSetRequest(sender);
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (args[0].equalsIgnoreCase("delete")) {
									if (this.checkPermissions(sender, 3)) {
										if (args.length == 2) {
											this.onCubeDeletionRequest(sender, args[1]);
										} else {
											sender.sendMessage(ChatColor.RED + "Use /dc delete <name>");
										}
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (args[0].equalsIgnoreCase("default")) {
									if (this.checkPermissions(sender, 3)) {
										if (args.length == 2) {
											this.onCubeDefaultRequest(sender, args[1]);
										} else if (args.length == 1) {
											if (this.defaultDC != null) {
												sender.sendMessage(ChatColor.GRAY + "Current default cube: " + ChatColor.GOLD + this.defaultDC.getName());
											} else {
												sender.sendMessage(ChatColor.GRAY + "Default cube not set!");
											}
										} else {
											sender.sendMessage(ChatColor.RED + "Use /dc default <name>");
										}
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (!args[0].equalsIgnoreCase("bar") && !args[0].equalsIgnoreCase("b")) {
									if (!args[0].equalsIgnoreCase("leave") && !args[0].equalsIgnoreCase("l")) {
										if (!args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("h")) {
											if (args[0].equalsIgnoreCase("burn")) {
												if (this.checkPermissions(sender, 2)) {
													if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
														this.onBurnStopRequest(sender);
													} else {
														this.onBurnRequest(sender);
													}
												} else {
													sender.sendMessage(this.lang.get("noPermissions"));
												}
											} else if (args[0].equalsIgnoreCase("db")) {
												this.debugMessage(sender, args);
											} else if (args[0].equalsIgnoreCase("baba")) {
												this.onBabaIsHere(sender);
											} else if (args[0].equalsIgnoreCase("clear")) {
												if (this.checkPermissions(sender, 2)) {
													this.onCubeClearRequest(sender);
												} else {
													sender.sendMessage(this.lang.get("noPermissions"));
												}
											} else if (!args[0].equalsIgnoreCase("timer") && !args[0].equalsIgnoreCase("t")) {
												sender.sendMessage(ChatColor.RED + "Unknown Command. Try /dc help");
											} else if (this.checkPermissions(sender, 1)) {
												this.onGetTimerRequest(sender);
											} else {
												sender.sendMessage(this.lang.get("noPermissions"));
											}
										} else if (args.length == 1) {
											this.onHelpRequest(sender, (String) null);
										} else {
											this.onHelpRequest(sender, args[1]);
										}
									} else if (this.checkPermissions(sender, 1)) {
										this.onCubeOutRequest(sender);
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (args.length == 2 && (args[1].startsWith("l") || args[1].startsWith("r"))) {
									if (this.checkPermissions(sender, 2)) {
										this.onBlockbarReloadRequest(sender);
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else if (args.length == 1) {
									if (this.checkPermissions(sender, 3)) {
										this.onBlockbarSetRequest(sender);
									} else {
										sender.sendMessage(this.lang.get("noPermissions"));
									}
								} else {
									sender.sendMessage(ChatColor.RED + "Wrong Usage: /dc bar [l/r]");
								}
							} else if (!this.checkPermissions(sender, 2) && !sender.hasPermission("deathcube.start")) {
								sender.sendMessage(this.lang.get("noPermissions"));
							} else if (args.length == 2) {
								this.onStartRequest(sender, args[1]);
							} else {
								this.onStartRequest(sender, (String) null);
							}
						} else {
							sender.sendMessage(ChatColor.GREEN + "This server is running DeathCube " + this.version + "!");
						}
					} else if (this.checkPermissions(sender, 1)) {
						this.onPlayerWantsFood(sender);
					} else {
						sender.sendMessage(this.lang.get("noPermissions"));
					}
				} else if (this.checkPermissions(sender, 2)) {
					this.onCubeRenewRequest(sender);
				} else {
					sender.sendMessage(this.lang.get("noPermissions"));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Try /dc help");
			}
		}

		return true;
	}

	private void onCubeClearRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			dc.destroyCube();
			sender.sendMessage(ChatColor.GREEN + "Cube was destroyed from " + dc.startHeight + " to 255");
		} else {
			sender.sendMessage(ChatColor.RED + "Select a DeathCube first using /dc select");
		}

	}

	private void onBabaIsHere(CommandSender sender) {
		if (sender instanceof Player && this.getServer().getOnlineMode()) {
			Player p = (Player) sender;
			if (p.getName().equals("baba43")) {
				this.getServer().broadcastMessage(ChatColor.GRAY + "DC: Hello Baba! :)");
			}
		}

	}

	private void debugMessage(CommandSender sender, String[] args) {
		sender.sendMessage(this.getServer().getName());
		Player p = (Player) sender;
		p.sendMessage("Aa" + p.getHealth());
	}

	public boolean checkPermissions(CommandSender sender, int level) {
		return !sender.hasPermission("deathcube.admin") && !sender.isOp() ? (sender.hasPermission("deathcube.mod") && level < 3 ? true : level == 1 && sender.hasPermission("deathcube.use")) : true;
	}

	private DeathCube findDeathCubeByString(String name) {
		return (DeathCube) this.cubeNames.get(name);
	}

	private DeathCube findDeathCubeBySender(CommandSender sender) {
		DeathCube dc = (DeathCube) this.cubeSelections.get(sender);
		return dc == null ? (this.cubeNames.size() == 1 ? (DeathCube) this.cubeNames.values().toArray()[0] : (this.defaultDC != null ? this.defaultDC : null)) : dc;
	}

	private void tellSelection(CommandSender sender) {
		DeathCube dc;
		if (this.cubeSelections.containsKey(sender)) {
			dc = (DeathCube) this.cubeSelections.get(sender);
			sender.sendMessage(ChatColor.GRAY + "You have currently selected " + ChatColor.GOLD + dc.getName());
		} else {
			dc = this.findDeathCubeBySender(sender);
			if (dc != null) {
				sender.sendMessage(ChatColor.GRAY + "No selection!");
				sender.sendMessage(ChatColor.GRAY + "But you will automatically use " + ChatColor.GOLD + dc.getName());
			} else {
				sender.sendMessage(ChatColor.GRAY + "No selection!");
			}
		}

	}

	private void saveLocation(String path, Location loc) {
		this.config.set(path + ".x", loc.getBlockX());
		this.config.set(path + ".y", loc.getBlockY());
		this.config.set(path + ".z", loc.getBlockZ());
		this.config.set(path + ".world", loc.getWorld().getUID().toString());
		this.saveSettings();
	}

	private Location readLocation(String path) {
		if (this.config.contains(path)) {
			try {
				int e = this.config.getInt(path + ".x");
				int y = this.config.getInt(path + ".y");
				int z = this.config.getInt(path + ".z");
				World w = this.getServer().getWorld(UUID.fromString(this.config.getString(path + ".world")));
				return new Location(w, (double) e, (double) y, (double) z);
			} catch (Exception var6) {
				return null;
			}
		} else {
			return null;
		}
	}

	private boolean checkPosition(DeathCube dc, CommandSender sender) {
		Location loc1 = dc.getPos(1);
		Location loc2 = dc.getPos(2);
		if (loc1 != null) {
			if (loc2 != null) {
				if (loc1.getWorld().equals(loc2.getWorld())) {
					int size1 = Math.abs(loc1.getBlockX() - loc2.getBlockX()) + 1;
					int size2 = Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) + 1;
					String s1;
					if (size1 >= 16 && size1 <= 80) {
						if (size1 % 2 != 0) {
							s1 = "" + ChatColor.AQUA + size1 + ChatColor.GREEN;
						} else {
							s1 = "" + size1;
						}
					} else {
						s1 = "" + ChatColor.RED + size1 + ChatColor.GREEN;
					}

					String s2;
					if (size2 >= 16 && size2 <= 80) {
						if (size2 % 2 != 0) {
							s2 = "" + ChatColor.AQUA + size2 + ChatColor.GREEN;
						} else {
							s2 = "" + size2;
						}
					} else {
						s2 = "" + ChatColor.RED + size2 + ChatColor.GREEN;
					}

					sender.sendMessage(ChatColor.GREEN + "Size: " + s1 + "x" + s2 + " = " + size1 * size2);
					if (size1 >= 16 && size1 <= 80 && size2 >= 16 && size2 <= 80) {
						if (size1 % 2 != 0 || size2 % 2 != 0) {
							sender.sendMessage(ChatColor.AQUA + "Please use even numbers if possible!");
						}

						return true;
					}

					sender.sendMessage(ChatColor.RED + "The edges must have a size between 16 and 80 blocks.");
				} else {
					sender.sendMessage(ChatColor.GRAY + "Both locations must be on the same world!");
				}
			} else {
				sender.sendMessage(ChatColor.GRAY + "Position 2 not set!");
			}
		} else {
			sender.sendMessage(ChatColor.GRAY + "Position 1 not set!");
		}

		return false;
	}

	private boolean isPlayer(CommandSender sender) {
		if (sender instanceof Player) {
			return true;
		} else {
			sender.sendMessage("Only players can run this command!");
			return false;
		}
	}

	public boolean isInventoryEmpty(Player player) {
		PlayerInventory i = player.getInventory();
		if (i.getBoots() != null) {
			return false;
		} else if (i.getHelmet() != null) {
			return false;
		} else if (i.getLeggings() != null) {
			return false;
		} else if (i.getChestplate() != null) {
			return false;
		} else {
			ItemStack[] contents = player.getInventory().getContents();
			ItemStack[] var7 = contents;
			int var6 = contents.length;

			for (int var5 = 0; var5 < var6; ++var5) {
				ItemStack content = var7[var5];
				if (content != null) {
					return false;
				}
			}

			return true;
		}
	}

	public boolean hasEnchantments(Player player) {
		PlayerInventory i = player.getInventory();
		return i.getBoots() != null && !i.getBoots().getEnchantments().isEmpty() ? true : (i.getHelmet() != null && !i.getHelmet().getEnchantments().isEmpty() ? true : (i.getLeggings() != null && !i.getLeggings().getEnchantments().isEmpty() ? true : i.getChestplate() != null && !i.getChestplate().getEnchantments().isEmpty()));
	}

	private void onCubeOutRequest(CommandSender sender) {
		if (this.isPlayer(sender)) {
			Iterator var3 = this.cubeNames.values().iterator();

			while (var3.hasNext()) {
				DeathCube dc = (DeathCube) var3.next();
				Player p = (Player) sender;
				if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -2)) {
					if (dc.isActive(p)) {
						dc.playerLeft(p, "He resigned.", "What a chicken!");
					} else {
						p.sendMessage(ChatColor.AQUA + "See you later!");
					}

					dc.tpToSpawn(p);
					break;
				}
			}
		}

	}

	private boolean onTeleportationRequest(CommandSender sender, boolean ignore, String name) {
		if (!this.isPlayer(sender)) {
			sender.sendMessage(ChatColor.RED + "Only players can run this command!");
		} else {
			DeathCube dc;
			if (name != null) {
				dc = this.findDeathCubeByString(name);
				if (dc == null) {
					sender.sendMessage(ChatColor.RED + "Cube not found. Try " + ChatColor.GOLD + "/dc list");
					return true;
				}
			}

			Player p = (Player) sender;
			boolean emptyInventory = true;
			String items = "";
			ItemStack[] contents = p.getInventory().getContents();
			ItemStack[] var12 = contents;
			int var11 = contents.length;

			for (int l = 0; l < var11; ++l) {
				ItemStack i = var12[l];
				if (i != null) {
					if (emptyInventory) {
						emptyInventory = false;
					}

					if (!i.getEnchantments().isEmpty()) {
						items = items + i.getType().name() + ", ";
					}
				}
			}

			if (!emptyInventory && !ignore) {
				sender.sendMessage(this.lang.get("inventoryNotEmpty1"));
				sender.sendMessage(this.lang.get("inventoryNotEmpty2"));
				return false;
			}

			if (items != "") {
				items = items.substring(0, items.length() - 2);
				sender.sendMessage(this.lang.get("equipWarning"));
				sender.sendMessage(ChatColor.GRAY + items);
				return false;
			}

			PlayerInventory var13 = p.getInventory();
			if (var13.getBoots() != null && !var13.getBoots().getEnchantments().isEmpty()) {
				sender.sendMessage(this.lang.get("equipWarningBoots"));
				return false;
			}

			if (var13.getHelmet() != null && !var13.getHelmet().getEnchantments().isEmpty()) {
				sender.sendMessage(this.lang.get("equipWarningHelmet"));
				return false;
			}

			if (var13.getLeggings() != null && !var13.getLeggings().getEnchantments().isEmpty()) {
				sender.sendMessage(this.lang.get("equipWarningLeggings"));
				return false;
			}

			if (var13.getChestplate() != null && !var13.getChestplate().getEnchantments().isEmpty()) {
				sender.sendMessage(this.lang.get("equipWarningArmor"));
				return false;
			}

			if (!p.getActivePotionEffects().isEmpty()) {
				p.sendMessage(this.lang.get("hasActivePotion"));
				return false;
			}

			if (p.getInventory().contains(Material.JACK_O_LANTERN)) {
				sender.sendMessage(this.lang.get("hasJackos"));
				return false;
			}

			if (p.getInventory().contains(Material.POTION)) {
				sender.sendMessage(this.lang.get("warningPotion"));
			}

			if (p.getInventory().contains(Material.ENDER_PEARL)) {
				sender.sendMessage(this.lang.get("warningEnderpearl"));
			}

			if (p.getGameMode() == GameMode.CREATIVE) {
				sender.sendMessage(ChatColor.GRAY + "Your gamemode will be removed on start!");
			}

			dc = this.findDeathCubeBySender(p);
			if (dc != null) {
				if (dc.hasCorrectLocation) {
					if (!dc.gameRunning) {
						if (dc.spawn != null) {
							Location var14 = p.getLocation();
							if (dc.tpRange == 0 || this.checkPermissions(p, 2)) {
								if (!emptyInventory) {
									p.sendMessage(this.lang.get("inventoryWarning"));
								}

								dc.tpIn(p);
								return true;
							}

							if (var14.getWorld().equals(dc.world)) {
								if (dc.contains(var14, dc.tpRange)) {
									if (!emptyInventory) {
										p.sendMessage(this.lang.get("inventoryWarning"));
									}

									dc.tpIn(p);
									return true;
								}

								sender.sendMessage(this.lang.get("toFarAway"));
							} else {
								sender.sendMessage(this.lang.get("inAnotherWorld"));
							}
						} else {
							sender.sendMessage(this.lang.get("needSpawn"));
						}
					} else {
						sender.sendMessage(this.lang.get("gameIsRunning"));
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Your selected cube has incorrect locations!");
				}
			} else {
				sender.sendMessage(this.lang.get("noSelection"));
			}
		}

		return false;
	}

	private void onSetLocationRequest(CommandSender sender, String spos) {
		byte pos;
		String s;
		if (spos.equals("1")) {
			pos = 1;
			s = "First";
		} else {
			pos = 2;
			s = "Second";
		}

		DeathCube dc = (DeathCube) this.cubeSelections.get(sender);
		if (dc != null) {
			Player p = (Player) sender;
			Location loc = p.getLocation();
			dc.setPos(pos, p.getLocation());
			if (!this.cubeNames.containsKey(dc.getName())) {
				this.cubeNames.put(dc.getName(), dc);
			}

			p.sendMessage(ChatColor.GRAY + s + " position set to " + this.locationToString(loc));
			this.saveLocation("cubes." + dc.getName() + ".locations.loc" + pos, loc);
			if (this.checkPosition(dc, sender)) {
				Location loc1 = dc.getPos(1);
				Location loc2 = dc.getPos(2);
				dc.minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
				dc.minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
				dc.maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
				dc.maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
				dc.minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
				dc.maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
				dc.world = loc1.getWorld();
				dc.hasCorrectLocation = true;
				this.loadConfig(dc);
				this.saveConfig();
			} else {
				dc.hasCorrectLocation = false;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private boolean onCheckPositionRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			sender.sendMessage(ChatColor.AQUA + "First Position: " + ChatColor.GOLD + this.locationToString(dc.getPos(1)));
			sender.sendMessage(ChatColor.AQUA + "Second Position: " + ChatColor.GOLD + this.locationToString(dc.getPos(2)));
			this.checkPosition(dc, sender);
			return true;
		} else {
			return false;
		}
	}

	private void onOpenGateRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.hasCorrectLocation) {
				if (!dc.gameRunning) {
					dc.openGates();
				} else {
					sender.sendMessage(ChatColor.RED + "You can't open gates in a running game, sorry!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid Location: " + dc.errorState);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private void onBlockbarReloadRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null && sender instanceof Player && !dc.gameRunning) {
			dc.getBlockBar().load(sender);
		}

	}

	private void onBlockbarSetRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (sender instanceof Player) {
				if (!dc.gameRunning) {
					Player p = (Player) sender;
					int direction = (int) Math.floor((double) Math.abs(p.getLocation().getYaw()));
					byte distance = 1;
					Vector vector = new Vector();
					if (direction > 45 && direction < 135) {
						vector.setX(-distance);
					} else if (direction > 135 && direction < 225) {
						vector.setZ(-distance);
					} else if (direction > 225 && direction < 315) {
						vector.setX(distance);
					} else if (direction > 135 && direction < 360) {
						vector.setZ(distance);
					} else {
						vector.setZ(distance);
					}

					Location loc = p.getLocation();
					Location loc2 = loc.clone();
					Location loc3 = loc2.clone();
					loc3.add(0.0D, 1.0D, 0.0D);
					boolean hasSpace = true;

					for (int x = 0; x < 5; ++x) {
						if (loc2.getBlock().getType() != Material.AIR) {
							hasSpace = false;
						}

						if (loc3.getBlock().getType() != Material.AIR) {
							hasSpace = false;
						}

						loc2.add(vector);
						loc3.add(vector);
					}

					if (hasSpace) {
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.world", loc.getWorld().getUID().toString());
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.x", loc.getBlockX());
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.y", loc.getBlockY());
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.z", loc.getBlockZ());
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.vx", vector.getBlockX());
						this.config.set("cubes." + dc.getName() + ".locations.blockbar.vz", vector.getBlockZ());
						p.setNoDamageTicks(80);
						loc.getBlock().setType(Material.GLASS);
						loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.LIGHT_GRAY_WOOL);
						loc.add(vector).getBlock().setType(Material.RED_WOOL);
						loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.RED_WOOL);
						loc.add(vector).getBlock().setType(Material.YELLOW_WOOL);
						loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.YELLOW_WOOL);
						loc.add(vector).getBlock().setType(Material.BLUE_WOOL);
						loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.BLUE_WOOL);
						this.saveConfig();
						this.loadConfig(dc);
						p.sendMessage(ChatColor.GRAY + "BlockBar created successfully!");
					} else {
						p.sendMessage(ChatColor.RED + "You need 1x2x5 free space!");
					}

					sender.sendMessage(ChatColor.GREEN + "Spawn position set!");
				} else {
					sender.sendMessage(ChatColor.RED + "Stop the game first!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private void onSpawnSetRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				Location loc = p.getLocation().add(0.0D, 1.0D, 0.0D);
				dc.spawn = loc;
				this.config.set("cubes." + dc.getName() + ".locations.spawn.world", loc.getWorld().getUID().toString());
				this.config.set("cubes." + dc.getName() + ".locations.spawn.x", loc.getBlockX());
				this.config.set("cubes." + dc.getName() + ".locations.spawn.y", loc.getBlockY());
				this.config.set("cubes." + dc.getName() + ".locations.spawn.z", loc.getBlockZ());
				this.config.set("cubes." + dc.getName() + ".locations.spawn.yaw", loc.getYaw());
				this.config.set("cubes." + dc.getName() + ".locations.spawn.pitch", loc.getPitch());
				this.saveConfig();
				sender.sendMessage(ChatColor.GREEN + "Spawn position set!");
			} else {
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private void onGetConfigurationRequest(CommandSender sender, String key) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			String path = null;
			if (this.config.contains("cubes." + dc.getName() + "." + key)) {
				path = "cubes." + dc.getName() + "." + key;
			} else if (this.config.contains("global." + key)) {
				path = "global." + key;
			}

			if (path != null) {
				sender.sendMessage(ChatColor.GREEN + "Value of " + ChatColor.GOLD + key + ": " + ChatColor.GREEN + this.config.get(path).toString());
			} else {
				sender.sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + " was not found!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private boolean onChangeConfigurationRequest(CommandSender sender, String key, String value) {
		if (!value.contains(".")) {
			DeathCube dc = this.findDeathCubeBySender(sender);
			if (dc != null) {
				if (!dc.gameRunning) {
					String path = null;
					if (this.config.contains("cubes." + dc.getName() + "." + key)) {
						path = "cubes." + dc.getName() + "." + key;
					} else if (this.config.contains("global." + key)) {
						path = "global." + key;
					}

					if (path != null) {
						if (this.config.isBoolean(path)) {
							if (value.equalsIgnoreCase("true")) {
								this.config.set(path, true);
							} else {
								if (!value.equalsIgnoreCase("false")) {
									sender.sendMessage(ChatColor.RED + "Please use 'true' or 'false' to specify booleans.");
									return false;
								}

								this.config.set(path, false);
							}
						} else if (this.config.isInt(path)) {
							try {
								int e = Integer.parseInt(value);
								this.config.set(path, e);
							} catch (NumberFormatException var7) {
								sender.sendMessage(ChatColor.RED + "Please enter an integer for this value!");
								return false;
							}
						} else if (this.config.isString(path)) {
							this.config.set(path, value.replaceAll("_", " "));
						}

						this.loadConfig(dc);
						sender.sendMessage(ChatColor.GREEN + key + " was changed to " + ChatColor.GOLD + value);
						this.saveSettings();
						return true;
					}

					sender.sendMessage(ChatColor.RED + "They key '" + key + "' was not found!");
				} else {
					sender.sendMessage(ChatColor.RED + "Stop the game first.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Bro I don't think you are allowed to do this.");
		}

		return false;
	}

	private void onTunnelCreationRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.hasCorrectLocation) {
				dc.buildTunnel();
			} else {
				sender.sendMessage(ChatColor.RED + "You have to set a valid location for the cube first!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Selected cube does not exist.");
		}

	}

	private void onCubeGenerationRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (!dc.hasCorrectLocation) {
				sender.sendMessage(ChatColor.RED + "Invalid Location: " + dc.errorState);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private void onCubeRenewRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.hasCorrectLocation) {
				dc.buildCube(sender);
				dc.destroyTunnel();
				dc.buildTunnel();
			} else {
				sender.sendMessage(ChatColor.RED + "First you should select a valid location for the cube using 'dc pos 1|2'");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
		}

	}

	private void onStopRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.gameRunning) {
				dc.stopGame();
				this.onTunnelCreationRequest(sender);
				sender.sendMessage(ChatColor.GREEN + "Game stopped!");
			} else {
				sender.sendMessage(ChatColor.RED + "There is no game running.");
			}
		} else {
			sender.sendMessage(ChatColor.GRAY + "No DeathCube selected. Type " + ChatColor.GOLD + "/dc select <name>");
		}

	}

	private void onBurnStopRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.isBurning) {
				this.getServer().getScheduler().cancelTask(dc.threadID);
				dc.isBurning = false;
				sender.sendMessage(ChatColor.GREEN + "Stopped burning (Thread " + dc.threadID + ")");
			} else {
				sender.sendMessage(ChatColor.RED + "The tower is not burning!");
			}
		} else {
			sender.sendMessage(ChatColor.GRAY + "No DeathCube selected. Type " + ChatColor.GOLD + "/dc select <name>");
		}

	}

	private void onBurnRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			if (dc.hasCorrectLocation) {
				if (dc.gameRunning) {
					if (!dc.isBurning) {
						dc.playerReachedBurnLimit((Player) null);
						sender.sendMessage(ChatColor.GREEN + "Tower has been ignited (Thread " + dc.threadID + ")");
					} else {
						sender.sendMessage(ChatColor.RED + "The tower is already burning!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "There is no game running!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid Location: " + dc.errorState);
			}
		} else {
			sender.sendMessage(ChatColor.GRAY + "No DeathCube selected. Type " + ChatColor.GOLD + "/dc select <name>");
		}

	}

	private void onStartRequest(CommandSender sender, String minutes) {
		boolean isUser = !this.checkPermissions(sender, 2);
		if (minutes != null && isUser) {
			sender.sendMessage(ChatColor.RED + "You are not allowed to use parameters bro.");
		} else {
			try {
				int e = -1;
				if (minutes != null) {
					e = Integer.parseInt(minutes);
				}

				if (e == -1 || e >= 0 && e <= 120) {
					DeathCube dc = this.findDeathCubeBySender(sender);
					if (dc != null) {
						if (dc.hasCorrectLocation) {
							if (!dc.gameRunning) {
								if (e == 0) {
									dc.startGame();
								} else {
									int[] tempTime;
									if (e == -1) {
										if (isUser) {
											if (dc.countWaitingPlayers() >= dc.mPublicMinPlayers) {
												tempTime = dc.getGameManager().getTempTime();
												if (tempTime != null) {
													sender.sendMessage(ChatColor.GRAY + "The current timer will start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds.");
													sender.sendMessage(ChatColor.RED + "You are not permitted to overwrite this timer!");
												} else {
													dc.getGameManager().newTempTask(1, true);
													sender.sendMessage(ChatColor.GREEN + "New timer started!");
												}
											} else {
												sender.sendMessage(ChatColor.RED + "You need " + (dc.mPublicMinPlayers - 1) + " other players to start!");
											}
										} else if (dc.countWaitingPlayers() > 0) {
											tempTime = dc.getGameManager().getTempTime();
											if (tempTime != null) {
												sender.sendMessage(ChatColor.GRAY + "The old timer would start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds.");
												sender.sendMessage(ChatColor.GREEN + "You have overwritten the old timer.");
											} else {
												sender.sendMessage(ChatColor.GREEN + "New timer started!");
											}

											dc.getGameManager().newTempTask(0, false);
										} else {
											sender.sendMessage(ChatColor.RED + "Nobody is there to play :(");
										}
									} else {
										tempTime = dc.getGameManager().getTempTime();
										if (tempTime != null) {
											sender.sendMessage(ChatColor.GRAY + "The old timer would start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds.");
											sender.sendMessage(ChatColor.GREEN + "You have overwritten the old timer!");
										} else {
											sender.sendMessage(ChatColor.GREEN + "New timer started!");
										}

										dc.getGameManager().newTempTask(e, false);
									}
								}
							} else {
								sender.sendMessage(this.lang.get("gameIsRunning"));
							}
						} else {
							sender.sendMessage(this.lang.get("invalidLocation"));
						}
					} else {
						sender.sendMessage(this.lang.get("noSelection"));
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Please choose a value between 1 and 120 (minutes) or 0 for instant start.");
				}
			} catch (NumberFormatException var7) {
				sender.sendMessage(ChatColor.RED + "Give me an integer please!");
			}
		}

	}

	private void onCubeCreationRequest(CommandSender sender) {
		DeathCube dc = (DeathCube) this.cubeSelections.get(sender);
		if (dc != null) {
			if (dc.hasCorrectLocation) {
				short maxHeight = 200;
				Location pos1 = dc.getPos(1);
				Location pos2 = dc.getPos(2);
				World w = pos1.getWorld();
				int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
				int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
				int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
				int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
				Math.min(pos1.getBlockY(), pos2.getBlockY());
				int surface = Math.max(pos1.getBlockY(), pos2.getBlockY());
				int count = 0;
				Iterator bb = w.getEntities().iterator();

				while (bb.hasNext()) {
					Entity bedrockHeight = (Entity) bb.next();
					if (bedrockHeight.getLocation().getBlockX() >= minX && bedrockHeight.getLocation().getBlockX() <= maxX && bedrockHeight.getLocation().getBlockZ() >= minZ && bedrockHeight.getLocation().getBlockZ() <= maxZ && !(bedrockHeight instanceof HumanEntity)) {
						++count;
						bedrockHeight.remove();
					}
				}

				sender.sendMessage("" + ChatColor.AQUA + count + " entities removed!");

				int var17;
				int var18;
				int h;
				for (var17 = 0; var17 < maxHeight; ++var17) {
					for (var18 = minX; var18 < maxX + 1; ++var18) {
						for (h = minZ; h < maxZ + 1; ++h) {
							if (var17 < 1) {
								w.getBlockAt(var18, var17, h).setType(Material.BEDROCK);
							} else if (var17 == 1) {
								if (this.random(0, 2) == 2) {
									w.getBlockAt(var18, var17, h).setType(Material.LAVA);
								} else {
									w.getBlockAt(var18, var17, h).setType(Material.AIR);
								}
							} else {
								w.getBlockAt(var18, var17, h).setType(Material.AIR);
							}
						}
					}
				}

				for (var17 = 4; var17 < surface; ++var17) {
					for (var18 = minX; var18 < maxX + 1; ++var18) {
						w.getBlockAt(var18, var17, minZ).setType(Material.STONE);
						w.getBlockAt(var18, var17, maxZ).setType(Material.STONE);
					}

					for (var18 = minZ; var18 < maxZ + 1; ++var18) {
						w.getBlockAt(minX, var17, var18).setType(Material.STONE);
						w.getBlockAt(maxX, var17, var18).setType(Material.STONE);
					}
				}

				byte var19 = 4;

				for (var18 = 1; var18 <= var19; ++var18) {
					for (h = minX; h < maxX + 1; ++h) {
						w.getBlockAt(h, var18, minZ).setType(Material.BEDROCK);
						w.getBlockAt(h, var18, maxZ).setType(Material.BEDROCK);
					}

					for (h = minZ; h < maxZ + 1; ++h) {
						w.getBlockAt(minX, var18, h).setType(Material.BEDROCK);
						w.getBlockAt(maxX, var18, h).setType(Material.BEDROCK);
					}
				}

				++minZ;
				--maxZ;
				++minX;
				--maxX;
				BlockBar var20 = dc.getBlockBar();

				for (h = 5; h < surface; ++h) {
					int e;
					for (e = minX + 1; e < maxX; ++e) {
						var20.setCageBlock(w.getBlockAt(e, h, minZ));
						var20.setCageBlock(w.getBlockAt(e, h, maxZ));
					}

					for (e = minZ; e < maxZ + 1; ++e) {
						var20.setCageBlock(w.getBlockAt(minX, h, e));
						var20.setCageBlock(w.getBlockAt(maxX, h, e));
					}
				}

				sender.sendMessage(ChatColor.GREEN + "DeathCube-Area prepared!");
				sender.sendMessage(ChatColor.GRAY + "Notice that you should use " + ChatColor.GOLD + "/dc new" + ChatColor.GRAY + " to renew the cube ;)");
				dc.buildTunnel();
				dc.buildCube((CommandSender) null);
			} else {
				sender.sendMessage(ChatColor.RED + "Wrong Location: " + dc.errorState);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
		}

	}

	private void onGetTimerRequest(CommandSender sender) {
		DeathCube dc = this.findDeathCubeBySender(sender);
		if (dc != null) {
			sender.sendMessage(ChatColor.GREEN + "Timers for DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + ":");
			if (dc.gameRunning) {
				sender.sendMessage(ChatColor.GRAY + "Currently there is a game " + ChatColor.GOLD + "running" + ChatColor.GRAY + "!");
			}

			int[] tempTime = dc.getGameManager().getTempTime();
			if (tempTime != null) {
				sender.sendMessage(ChatColor.GRAY + "DeathCube will try to start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds" + ChatColor.GRAY + "!");
			} else {
				sender.sendMessage(ChatColor.GRAY + "No irregulär timer active!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "A cube with that name does not exist!");
		}

	}

	private void onListDeathCubesRequest(CommandSender sender) {
		if (this.cubeNames.isEmpty()) {
			sender.sendMessage(ChatColor.GRAY + "Could not find any active DeathCubes!");
		} else {
			Iterator var3 = this.cubeNames.values().iterator();

			while (var3.hasNext()) {
				DeathCube dc = (DeathCube) var3.next();
				if (dc.hasCorrectLocation) {
					String status = "";
					if (dc.gameRunning) {
						status = status + ChatColor.GREEN + "<running>";
					} else if (dc.isOpened) {
						status = status + ChatColor.AQUA + "<opened>";
					} else {
						status = status + ChatColor.GRAY + "<closed>";
					}

					sender.sendMessage(ChatColor.GRAY + "[" + dc.world.getName() + "] " + ChatColor.GOLD + dc.getName() + ChatColor.GRAY + ": " + status);
				} else {
					sender.sendMessage(ChatColor.GOLD + dc.getName() + ChatColor.GRAY + " is not set properly.");
				}
			}
		}

	}

	private void onCubeDeletionRequest(CommandSender sender, String name) {
		DeathCube dc = this.findDeathCubeByString(name);
		if (dc != null) {
			if (!dc.gameRunning) {
				this.cubeNames.remove(dc.getName());
				this.config.set("cubes." + dc.getName(), (Object) null);
				this.saveConfig();
				sender.sendMessage(ChatColor.GREEN + "DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " removed!");
			} else {
				sender.sendMessage(ChatColor.RED + "Stop the game first!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "A cube with that name does not exist!");
		}

	}

	private void onCubeDefaultRequest(CommandSender sender, String name) {
		DeathCube dc = this.findDeathCubeByString(name);
		if (dc != null) {
			this.defaultDC = dc;
			this.config.set("global.default", dc.getName());
			this.saveConfig();
			sender.sendMessage(ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " is now your default cube!");
		} else {
			sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
		}

	}

	private void onHelpRequest(CommandSender sender, String submenu) {
		if (this.checkPermissions(sender, 1)) {
			int var4;
			int var5;
			String[] var6;
			String var7;
			if (submenu != null) {
				if (!submenu.equalsIgnoreCase("i") && !submenu.equalsIgnoreCase("info")) {
					if (submenu.equalsIgnoreCase("c") || submenu.equalsIgnoreCase("commands")) {
						if (this.checkPermissions(sender, 2)) {
							sender.sendMessage(ChatColor.GRAY + "I will display the user commands for you. If you don't know the admin commands rtfm!");
						}

						var5 = (var6 = this.lang.get("helpCommands").split(";")).length;

						for (var4 = 0; var4 < var5; ++var4) {
							var7 = var6[var4];
							sender.sendMessage(var7);
						}
					} else {
						if (!submenu.equalsIgnoreCase("r") && !submenu.equalsIgnoreCase("rules")) {
							sender.sendMessage(ChatColor.GRAY + "Info page not found. Try " + ChatColor.GOLD + "info, commands or rules");
						} else {
							DeathCube line = this.findDeathCubeBySender(sender);
							if (line != null) {
								if (line.hasCorrectLocation) {
									sender.sendMessage(ChatColor.GRAY + "* Its not allowed to take " + ChatColor.GOLD + "Jack O'Lanterns" + ChatColor.GRAY + " or " + ChatColor.GOLD + "enchanted items" + ChatColor.GRAY + " with you. " + ChatColor.GOLD + "Enderpearls" + ChatColor.GRAY + " and " + ChatColor.GOLD + "potions" + ChatColor.GRAY + " are blocked while playing");
									if (line.kickOnCheat) {
										sender.sendMessage(ChatColor.GRAY + "* If you try to place any block while playing you will be kicked");
									}

									if (line.pkinStart > 0 || line.pkinProbability > 0) {
										sender.sendMessage(ChatColor.GRAY + "* You can destroy " + ChatColor.GOLD + "Jack O'Lanterns" + ChatColor.GRAY + " to receive a joker. Then you can replace it " + ChatColor.GOLD + "one time" + ChatColor.GRAY);
									}

									sender.sendMessage(ChatColor.GRAY + "* You start at level " + ChatColor.GOLD + line.stageLevel + ChatColor.GRAY + " and must climb up " + ChatColor.GOLD + (line.height - line.stageLevel) + ChatColor.GRAY + " levels");
									if (line.burnLevel < line.height && line.burnLevel > line.stageLevel + 1) {
										sender.sendMessage(ChatColor.GRAY + "* If someone reaches level " + ChatColor.GOLD + line.burnLevel + ChatColor.GRAY + " the tower will start crumbling");
									} else {
										sender.sendMessage(ChatColor.GRAY + "* The tower will " + ChatColor.GOLD + "never" + ChatColor.GRAY + " start crumbling");
									}
								} else {
									sender.sendMessage(ChatColor.GRAY + "Your selected cube is not active yet.");
								}
							} else {
								sender.sendMessage(ChatColor.GRAY + "Could not find a selected cube. Use" + ChatColor.GOLD + "/dc select <name> or /dc list");
							}
						}
					}
				} else {
					sender.sendMessage(this.lang.get("helpInfo"));
				}
			} else {
				var5 = (var6 = this.lang.get("helpIndex").split(";")).length;

				for (var4 = 0; var4 < var5; ++var4) {
					var7 = var6[var4];
					sender.sendMessage(var7);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "First you should tell your admin that you need " + ChatColor.GOLD + "deathcube.use" + ChatColor.RED + " permissions ;)");
		}

	}

	private void onSelectionRequest(CommandSender sender, String name) {
		DeathCube newDC;
		if (this.cubeNames.containsKey(name)) {
			newDC = this.findDeathCubeByString(name);
			if (newDC != null) {
				this.cubeSelections.put(sender, newDC);
				sender.sendMessage(ChatColor.GRAY + "Selection changed to " + ChatColor.GOLD + name);
			} else {
				sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
			}
		} else if (this.checkPermissions(sender, 3)) {
			if (!name.equalsIgnoreCase("protectionHolder")) {
				sender.sendMessage(ChatColor.GRAY + "The selected Cube does not exist yet!");
				sender.sendMessage(ChatColor.GRAY + "It will be created as soon as you set the first position.");
				newDC = new DeathCube(name, this);
				newDC.setBlockBar(new BlockBar());
				this.cubeSelections.put(sender, newDC);
			} else {
				sender.sendMessage(ChatColor.RED + "Choose another name please!");
			}
		} else {
			this.lang.get("noPermissions");
		}

	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Location loc = event.getPlayer().getLocation();
			if (dc.hasCorrectLocation && dc.contains(loc, 1)) {
				if (dc.contains(loc, -9)) {
					Item item = event.getItem();
					if (item.getItemStack().getType() != Material.JACK_O_LANTERN) {
						event.setCancelled(true);
					}
				} else {
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler
	public void onPlayeyDropItem(PlayerDropItemEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			if (dc.hasCorrectLocation && dc.contains(event.getItemDrop().getLocation(), dc.dropProtection)) {
				Player p = event.getPlayer();
				if (dc.contains(event.getItemDrop().getLocation(), -9) && event.getItemDrop().getItemStack().getType() == Material.JACK_O_LANTERN) {
					p.sendMessage(ChatColor.GRAY + "Hey it's just your joker!");
				} else {
					p.sendMessage(ChatColor.GRAY + "Please dispose your junk somewhere else.");
				}

				event.setCancelled(true);
				break;
			}
		}

	}

	@EventHandler
	public void onPlayerRunsCommand(PlayerCommandPreprocessEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Player p = event.getPlayer();
			if (dc.contains(p.getLocation(), -2)) {
				if (!this.checkPermissions(p, 2)) {
					String command = event.getMessage().split(" ")[0];
					if (!this.allowedCommands.contains(command.substring(1))) {
						event.setCancelled(true);
						p.sendMessage(this.lang.get("commandBlocked"));
					}
				}
				break;
			}
		}

	}

	@EventHandler
	public void onRedstoneEvent(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.REDSTONE_LAMP && ((Lightable) event.getBlock().getBlockData()).isLit()) {
			Iterator var3 = this.cubeNames.values().iterator();

			while (var3.hasNext()) {
				DeathCube dc = (DeathCube) var3.next();
				if (dc.activeRedstone && dc.hasCorrectLocation && dc.contains(event.getBlock().getLocation())) {
					event.setNewCurrent(15);
					break;
				}
			}
		}

	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			if (dc.hasCorrectLocation) {
				if (!dc.getBlockBar().contains(event.getBlockPlaced().getLocation()) || this.checkPermissions(event.getPlayer(), 2)) {
					if (dc.contains(event.getBlockPlaced().getLocation())) {
						Player p = event.getPlayer();
						if (!dc.isActive(p)) {
							if (!this.checkPermissions(p, 2)) {
								event.setCancelled(true);
								event.getPlayer().sendMessage(this.lang.get("innerProtection"));
							}
						} else if (event.getBlock().getType() == Material.JACK_O_LANTERN) {
							Location loc = event.getBlockPlaced().getLocation();
							if (dc.contains(loc, -8) && loc.getBlockY() >= dc.startHeight && loc.getBlockY() <= dc.height) {
								event.getBlockPlaced().setType(Material.PUMPKIN);
								// Todo: Set random facing
							} else {
								event.setCancelled(true);
							}
						} else {
							dc.playerLeft(p, this.lang.get("leaveBlockplaceA"), this.lang.get("leaveBlockplaceP"));
							dc.tpToSpawn(p);
							event.setCancelled(true);
						}
					}
					break;
				}

				event.getPlayer().sendMessage(this.lang.get("protectionBar"));
				event.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Player p = event.getPlayer();
			if (dc.hasCorrectLocation && dc.gameRunning) {
				if (dc.isActive(p)) {
					if (!dc.contains(p.getLocation(), -4)) {
						Location loc = p.getLocation();
						if (loc.getBlockY() < dc.stageLevel + 1 || loc.getBlockY() > dc.stageLevel + 3) {
							dc.playerLeft(p, this.lang.get("leaveAreaA"), this.lang.get("leaveAreaP"));
							dc.tpToSpawn(p);
						}
					} else if (dc.looseOnFall && p.getLocation().getBlockY() <= dc.startHeight - 1) {
						dc.playerLeft(p, this.lang.get("leaveFallA"), this.lang.get("leaveFallA"));
						dc.tpToSpawn(p);
					} else if (!dc.isBurned && p.getLocation().getBlockY() >= dc.burnLevel && dc.burnLevel >= dc.stageLevel + 1 && dc.burnLevel <= dc.height) {
						dc.playerReachedBurnLimit(p);
					} else if (p.getLocation().getBlockY() >= dc.height + 1) {
						dc.playerReachedTop(p);
					}
					break;
				}

				if (dc.contains(p.getLocation(), -2)) {
					if (!this.checkPermissions(p, 2)) {
						p.sendMessage(this.lang.get("enterProtection"));
						dc.tpToSpawn(p);
					}
					break;
				}
			}
		}

	}

	@EventHandler
	public void onPlayerInteract(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof HumanEntity && event.getDamager().getType() == EntityType.ENDER_PEARL) {
			Player p = (Player) event.getEntity();
			Iterator var4 = this.cubeNames.values().iterator();

			while (var4.hasNext()) {
				DeathCube dc = (DeathCube) var4.next();
				if (dc.isActive(p)) {
					dc.playerLeft(p, this.lang.get("leaveEnderpearlA"), this.lang.get("leaveEnderpearlP"));
					dc.tpToSpawn(p);
					break;
				}
			}
		}

	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			final DeathCube dc = (DeathCube) var3.next();
			final Player p = event.getPlayer();
			if (dc.waiting.contains(p.getName())) {
				dc.waiting.remove(p.getName());
				this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						dc.tpToSpawn(p);
					}
				}, 10L);
			}
		}

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			final DeathCube dc = (DeathCube) var3.next();
			final Player p = event.getPlayer();
			if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -2)) {
				this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						dc.tpToSpawn(p);
						p.sendMessage(DeathCubeManager.this.lang.get("removeOnSpawn"));
					}
				}, 10L);
				break;
			}
		}

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Player p = event.getPlayer();
			if (dc.waiting.contains(p.getName())) {
				dc.waiting.remove(p.getName());
			}

			if (dc.isActive(p)) {
				dc.playerLeft(p, this.lang.get("leaveDisconnectA"), "");
				break;
			}
		}

		if (this.cubeSelections.containsKey(event.getPlayer())) {
			this.cubeSelections.remove(event.getPlayer());
		}

	}

	@EventHandler
	public void onPlayerItemChange(PlayerItemHeldEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Player p = event.getPlayer();
			if (dc.isActive(p)) {
				ItemStack i = p.getInventory().getItem(event.getNewSlot());
				if (i != null && i.getType() == Material.POTION) {
					p.getInventory().setItem(event.getNewSlot(), new ItemStack(Material.AIR));
					p.sendMessage(this.lang.get("potionSelect"));
				}
			}
		}

	}

	private void onPlayerWantsFood(CommandSender sender) {
		if (this.isPlayer(sender)) {
			Player p = (Player) sender;
			Iterator var4 = this.cubeNames.values().iterator();

			while (var4.hasNext()) {
				DeathCube dc = (DeathCube) var4.next();
				if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -5)) {
					if (!dc.offlineFeed) {
						if (dc.isActive(p)) {
							if (p.getFoodLevel() < 20) {
								p.setFoodLevel(20);
								p.sendMessage(this.lang.get("onFeed"));
							} else {
								p.sendMessage(this.lang.get("notHungry"));
							}
						} else {
							p.sendMessage(this.lang.get("notIngame"));
						}
					} else if (p.getFoodLevel() < 20) {
						p.setFoodLevel(20);
						p.sendMessage(this.lang.get("onFeed"));
					} else {
						p.sendMessage(this.lang.get("notHungry"));
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Only players can run this command!");
		}

	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Player p = event.getEntity();
			if (dc.playerDied(p)) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
				event.getDrops().clear();
				event.setDeathMessage("");
				if (!dc.waiting.contains(p.getName())) {
					dc.waiting.add(p.getName());
				}
				break;
			}
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			Location loc = event.getBlock().getLocation();
			if (dc.hasCorrectLocation) {
				if (dc.getBlockBar().contains(loc) && !this.checkPermissions(event.getPlayer(), 2)) {
					event.getPlayer().sendMessage(this.lang.get("barProtection"));
					event.setCancelled(true);
				} else if (dc.contains(loc)) {
					Player p = event.getPlayer();
					if (dc.contains(loc, -8)) {
						if (event.getBlock().getType() == Material.JACK_O_LANTERN && this.checkPermissions(p, 1)) {
							if (!dc.isActive(p)) {
								if (!this.checkPermissions(p, 3)) {
									event.setCancelled(true);
									p.sendMessage(this.lang.get("jokerOffline"));
								}
							} else {
								p.sendMessage(this.lang.get("jokerOnline"));
							}
						} else if (!this.checkPermissions(p, 3)) {
							event.setCancelled(true);
							p.sendMessage(this.lang.get("innerProtection"));
						}
					} else if (!this.checkPermissions(p, 2)) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(this.lang.get("outerProtection"));
					}
					break;
				}
			}
		}

	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		Iterator var3 = this.cubeNames.values().iterator();

		while (var3.hasNext()) {
			DeathCube dc = (DeathCube) var3.next();
			if (dc.hasCorrectLocation && dc.contains(event.getLocation())) {
				event.setCancelled(true);
			}
		}

	}

	public void onEnable() {
		this.config = this.getConfig();
		if (!this.configPath.exists()) {
			try {
				this.config.load(this.configPath);
			} catch (Exception var3) {
				this.o("config.yml generated or error while reading!");
				this.saveSettings();
			}
		} else {
			try {
				this.config.load(this.configPath);
			} catch (Exception var2) {
				this.o("Error while reading reading from config.yml!");
			}
		}

		this.loadDeathCubes();
		this.lang = new Language(this);
		this.getServer().getPluginManager().registerEvents(this, this);
		System.out.println("DeathCube v2 enabled!");
	}

	private void loadDeathCubes() {
		DeathCube dct;
		if (this.config.contains("pos")) {
			World dc = this.getServer().getWorld(UUID.fromString(this.config.getString("pos.world")));
			if (dc != null) {
				Location s = new Location(dc, (double) this.config.getInt("pos.1.x"), (double) this.config.getInt("pos.1.y"), (double) this.config.getInt("pos.1.z"));
				Location pos2 = new Location(dc, (double) this.config.getInt("pos.2.x"), (double) this.config.getInt("pos.2.y"), (double) this.config.getInt("pos.2.z"));
				if (s != null && pos2 != null) {
					dct = new DeathCube("protectionHolder", this);
					this.saveLocation("cubes." + dct.getName() + ".locations.loc1", s);
					this.saveLocation("cubes." + dct.getName() + ".locations.loc2", pos2);
				}
			}
		}

		DeathCube var6 = this.findDeathCubeByString(this.getString("global.default", ""));
		if (var6 != null) {
			this.defaultDC = var6;
		}

		this.allowedCommands.clear();
		this.allowedCommands.add("dc");
		String[] var5;
		int var10 = (var5 = this.getString("global.allowedCommands", "who,list").split(",")).length;

		String var7;
		for (int var8 = 0; var8 < var10; ++var8) {
			var7 = var5[var8];
			this.allowedCommands.add(var7);
		}

		if (this.config.contains("cubes")) {
			Iterator var9 = this.config.getConfigurationSection("cubes").getKeys(false).iterator();

			while (var9.hasNext()) {
				var7 = (String) var9.next();
				this.o("Reading DeathCube \"" + var7 + "\"");
				dct = new DeathCube(var7, this);
				this.loadConfig(dct);
			}
		}

		this.saveConfig();
	}

	private void loadConfig(DeathCube dc) {
		String path = "cubes." + dc.getName() + ".";
		Location pos1 = this.readLocation(path + "locations.loc1");
		Location pos2 = this.readLocation(path + "locations.loc2");
		String errorMessage = "";
		int x;
		int y;
		if (pos1 != null) {
			if (pos2 != null) {
				if (pos1.getWorld() != null && pos2.getWorld() != null) {
					if (pos1.getWorld().equals(pos2.getWorld())) {
						y = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
						x = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
						if (y >= 16 && y <= 80 && x >= 16 && x <= 80) {
							errorMessage = "fine";
							dc.hasCorrectLocation = true;
						} else {
							errorMessage = "The edges must have a size between 16 and 80 blocks.";
						}
					} else {
						errorMessage = "Both positions must be on the same world!";
					}
				} else {
					errorMessage = "The selected world does not exist!";
				}
			} else {
				errorMessage = "Position 2 not set!";
			}
		} else {
			errorMessage = "Position 1 not set!";
		}

		dc.errorState = errorMessage;
		if (dc.hasCorrectLocation) {
			dc.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
			dc.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
			dc.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
			dc.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
			dc.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
			dc.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
			dc.world = pos1.getWorld();
		} else {
			this.o(dc.getName() + " is incorrect: " + errorMessage);
		}

		dc.setPos(1, pos1);
		dc.setPos(2, pos2);
		dc.density = this.getInt(path + "density", 4);
		dc.stageLevel = this.getInt(path + "stageLevel", 15);
		dc.startHeight = this.getInt(path + "startHeight", 10);
		dc.height = this.getInt(path + "height", 100);
		if (dc.height > 254) {
			dc.height = 254;
		}

		dc.tpRange = this.getInt(path + "tpRange", 20);
		dc.pkinProbability = this.getInt(path + "pkinProbability", 400);
		dc.pkinStart = this.getInt(path + "pkinStart", 0);
		dc.kickOnCheat = this.getBoolean(path + "kickOnCheat", true);
		dc.burnLevel = this.getInt(path + "burnLevel", 40);
		dc.burnSpeed = this.getInt(path + "burnSpeed", 20);
		dc.burnChance = this.getInt(path + "burnChance", 1);
		dc.burnExpand = this.getInt(path + "burnExpand", 3);
		dc.posTower = this.getBoolean(path + "posTower", true);
		dc.posTowerRate = this.getLong(path + "posTowerRate", 100L);
		dc.posTowerId = Material.getMaterial(this.getString(path + "posTowerId", Material.REDSTONE_LAMP.name()));
		dc.broadcast = this.getBoolean(path + "broadcast", true);
		dc.offlineFeed = this.getBoolean(path + "offlineFeed", true);
		dc.autoRemove = this.getBoolean(path + "autoRemove", false);
		dc.openAfterGame = this.getBoolean(path + "openAfterGame", false);
		dc.timeLimit = this.getInt(path + "timeLimit", 15);
		dc.userStart = this.getBoolean(path + "userStart", false);
		dc.dropProtection = this.getInt(path + "dropProtection", 5);
		dc.looseOnFall = this.getBoolean(path + "looseOnFall", false);
		dc.activeRedstone = this.getBoolean(path + "activeRedstone", false);
		dc.stageWidth = this.getInt(path + "stageWidth", 1);
		this.loadWinCommands(path + "winCommands", dc.winCommands);
		dc.mTimedMinPlayers = this.getInt(path + "mTimedMinPlayers", 1);
		this.loadWinCommands(path + "mTimedWinCommands", dc.tWinCommands);
		dc.mPublicMinPlayers = this.getInt(path + "mPublicMinPlayers", 1);
		this.loadWinCommands(path + "mPublicWinCommands", dc.pWinCommands);
		if (dc.mTimedMinPlayers <= 0) {
			dc.mTimedMinPlayers = 1;
		}

		if (dc.mPublicMinPlayers <= 0) {
			dc.mPublicMinPlayers = 1;
		}

		if (dc.stageLevel <= 0) {
			dc.stageLevel = 1;
		}

		dc.winCommands.clear();
		int z;
		if (this.config.contains("cubes." + dc.getName() + ".winCommands")) {
			String var11 = this.config.getString("cubes." + dc.getName() + ".winCommands");
			if (var11.contains(";")) {
				String[] w;
				z = (w = var11.split(";")).length;

				for (y = 0; y < z; ++y) {
					String var13 = w[y];
					dc.winCommands.add(var13);
				}
			} else {
				dc.winCommands.add(var11);
			}
		} else {
			this.config.set("cubes." + dc.getName() + ".winCommands", "");
		}

		path = path + "locations.";
		BlockBar var12;
		if (this.config.contains(path + ".blockbar")) {
			Location var14 = new Location(this.getServer().getWorld(UUID.fromString(this.config.getString(path + ".blockbar.world"))), (double) this.config.getInt(path + ".blockbar.x"), (double) this.config.getInt(path + ".blockbar.y"), (double) this.config.getInt(path + ".blockbar.z"));
			Vector var15 = new Vector(this.config.getInt(path + ".blockbar.vx"), 0, this.config.getInt(path + ".blockbar.vz"));
			if (var14 != null && var15 != null) {
				var12 = new BlockBar(var14, var15);
			} else {
				var12 = new BlockBar();
			}
		} else {
			var12 = new BlockBar();
		}

		dc.setBlockBar(var12);
		if (this.config.contains(path + ".spawn")) {
			x = this.config.getInt(path + "spawn.x");
			y = this.config.getInt(path + "spawn.y");
			z = this.config.getInt(path + "spawn.z");
			World var16 = this.getServer().getWorld(UUID.fromString(this.config.getString(path + "spawn.world")));
			if (var16 != null) {
				dc.spawn = new Location(var16, (double) x, (double) y, (double) z, (float) this.config.getLong(path + "spawn.yaw"), (float) this.config.getLong(path + "spawn.pitch"));
			}
		}

		this.cubeNames.put(dc.getName(), dc);
	}

	private void loadWinCommands(String path, ArrayList<String> list) {
		list.clear();
		if (this.config.contains(path)) {
			String commandString = this.config.getString(path);
			if (commandString.contains(";")) {
				String[] var7;
				int var6 = (var7 = commandString.split(";")).length;

				for (int var5 = 0; var5 < var6; ++var5) {
					String s = var7[var5];
					list.add(s);
				}
			} else {
				list.add(commandString);
			}
		} else {
			this.config.set(path, "");
		}

	}

	private long getLong(String path, long value) {
		if (this.config.contains(path)) {
			return this.config.getLong(path);
		} else {
			this.config.set(path, value);
			return value;
		}
	}

	private int getInt(String path, int value) {
		if (this.config.contains(path)) {
			return this.config.getInt(path);
		} else {
			this.config.set(path, value);
			return value;
		}
	}

	private boolean getBoolean(String path, boolean value) {
		if (this.config.contains(path)) {
			return this.config.getBoolean(path);
		} else {
			this.config.set(path, value);
			return value;
		}
	}

	private String getString(String path, String value) {
		if (this.config.contains(path)) {
			return this.config.getString(path);
		} else {
			this.config.set(path, value);
			return value;
		}
	}

	private void saveSettings() {
		try {
			this.config.save(this.configPath);
		} catch (IOException var2) {
			System.out.println("Unknown Exception while saving config File!");
		}

	}

	private String locationToString(Location loc) {
		return loc != null ? loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " in '" + loc.getWorld().getName() + "'" : "Location not set!";
	}

	private int random(int from, int to) {
		++to;
		return (int) (Math.random() * (double) (to - from) + (double) from);
	}

	private boolean isNumber(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException var3) {
			return false;
		}
	}

	private void o(String text) {
		System.out.println("DC: " + text);
	}
}
