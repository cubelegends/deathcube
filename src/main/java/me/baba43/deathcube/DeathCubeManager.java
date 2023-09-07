package me.baba43.deathcube;

import org.bukkit.*;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DeathCubeManager extends JavaPlugin implements Listener {
    private final Map<CommandSender, DeathCube> cubeSelections = new HashMap<>();

    private final Map<String, DeathCube> cubeNames = new HashMap<>();

    private final File configPath = new File("plugins/DeathCube/config.yml");

    private FileConfiguration config;

    public Language lang;

    private final String version = "3.0.0";

    private final ArrayList<String> allowedCommands = new ArrayList<>();

    private DeathCube defaultDC;

    private boolean upToDate = true;

    private boolean notUpdated = false;

    private RegistrationManager registrator;

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("dc")) {
            if (this.notUpdated) {
                this.newVersionNotification(sender);
                sender.sendMessage(ChatColor.GRAY + "Command was not executed!");
            } else if (args.length > 0) {
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
                                } else if (args[0].equalsIgnoreCase("register")) {
                                    if (args.length == 2 && args[1].equalsIgnoreCase("zap-hosting.de")) {
                                        this.registrator.onRegistrationRequest(sender);
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "You need to enter a correct password to use this command.");
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
                            if (!this.upToDate) {
                                sender.sendMessage(ChatColor.GRAY + "Your DeathCube version is outdated, please update!");
                                sender.sendMessage(ChatColor.GRAY + "Enable autoUpdate and restart or follow this link:");
                                sender.sendMessage(ChatColor.GREEN + "http://dev.bukkit.org/server-mods/deathcube");
                            }
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

    private void onCubeClearRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            dc.destroyCube();
            sender.sendMessage(ChatColor.GREEN + "Cube was destroyed from " + dc.startHeight + " to 255");
        } else {
            sender.sendMessage(ChatColor.RED + "Select a DeathCube first using /dc select");
        }
    }

    private void onBabaIsHere(final CommandSender sender) {
        if (sender instanceof Player && this.getServer().getOnlineMode()) {
            final Player p = (Player) sender;
            if (p.getName().equals("baba43")) {
                this.getServer().broadcastMessage(ChatColor.GRAY + "DC: Hello Baba! :)");
            }
        }
    }

    private void debugMessage(final CommandSender sender, final String[] args) {
        sender.sendMessage(this.getServer().getName());
        final Player p = (Player) sender;
        p.sendMessage("Aa" + p.getHealth());
    }

    public boolean checkPermissions(final CommandSender sender, final int level) {
        return (!sender.hasPermission("deathcube.admin") && !sender.isOp()) ? ((sender.hasPermission("deathcube.mod") && level < 3) ? true : ((level == 1 && sender.hasPermission("deathcube.use")))) : true;
    }

    public void newVersionNotification(final CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Important " + ChatColor.GOLD + "notification" + ChatColor.GRAY + ": DeathCube has updated itself to a new version! The config system changed, so you need to recreate your DeathCube and delete your old " + ChatColor.GREEN + "config.yml" + ChatColor.GRAY + ". For safety reasons a placeholder was created that should protect your old DeathCube area.");
        sender.sendMessage("" + ChatColor.GRAY);
        sender.sendMessage(ChatColor.GRAY + "Since commands have changed as well it's highly recommended to read my new instructions by following this link:");
        sender.sendMessage(ChatColor.GREEN + "http://dev.bukkit.org/server-mods/deathcube");
    }

    private DeathCube findDeathCubeByString(final String name) {
        return this.cubeNames.get(name);
    }

    private DeathCube findDeathCubeBySender(final CommandSender sender) {
        final DeathCube dc = this.cubeSelections.get(sender);
        return (dc == null) ? ((this.cubeNames.size() == 1) ? (DeathCube) this.cubeNames.values().toArray()[0] : ((this.defaultDC != null) ? this.defaultDC : null)) : dc;
    }

    private void tellSelection(final CommandSender sender) {
        if (this.cubeSelections.containsKey(sender)) {
            final DeathCube dc = this.cubeSelections.get(sender);
            sender.sendMessage(ChatColor.GRAY + "You have currently selected " + ChatColor.GOLD + dc.getName());
        } else {
            final DeathCube dc = this.findDeathCubeBySender(sender);
            if (dc != null) {
                sender.sendMessage(ChatColor.GRAY + "No selection!");
                sender.sendMessage(ChatColor.GRAY + "But you will automatically use " + ChatColor.GOLD + dc.getName());
            } else {
                sender.sendMessage(ChatColor.GRAY + "No selection!");
            }
        }
    }

    private void saveLocation(final String path, final Location loc) {
        this.config.set(path + ".x", Integer.valueOf(loc.getBlockX()));
        this.config.set(path + ".y", Integer.valueOf(loc.getBlockY()));
        this.config.set(path + ".z", Integer.valueOf(loc.getBlockZ()));
        this.config.set(path + ".world", loc.getWorld().getUID().toString());
        this.saveSettings();
    }

    private Location readLocation(final String path) {
        if (this.config.contains(path)) {
            try {
                final int e = this.config.getInt(path + ".x");
                final int y = this.config.getInt(path + ".y");
                final int z = this.config.getInt(path + ".z");
                final World w = this.getServer().getWorld(UUID.fromString(this.config.getString(path + ".world")));
                return new Location(w, e, y, z);
            } catch (final Exception var6) {
                return null;
            }
        }
        return null;
    }

    private boolean checkPosition(final DeathCube dc, final CommandSender sender) {
        final Location loc1 = dc.getPos(1);
        final Location loc2 = dc.getPos(2);
        if (loc1 != null) {
            if (loc2 != null) {
                if (loc1.getWorld().equals(loc2.getWorld())) {
                    final String s1;
                    final String s2;
                    final int size1 = Math.abs(loc1.getBlockX() - loc2.getBlockX()) + 1;
                    final int size2 = Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) + 1;
                    if (size1 >= 16 && size1 <= 80) {
                        if (size1 % 2 != 0) {
                            s1 = "" + ChatColor.AQUA + size1 + ChatColor.GREEN;
                        } else {
                            s1 = "" + size1;
                        }
                    } else {
                        s1 = "" + ChatColor.RED + size1 + ChatColor.GREEN;
                    }
                    if (size2 >= 16 && size2 <= 80) {
                        if (size2 % 2 != 0) {
                            s2 = "" + ChatColor.AQUA + size2 + ChatColor.GREEN;
                        } else {
                            s2 = "" + size2;
                        }
                    } else {
                        s2 = "" + ChatColor.RED + size2 + ChatColor.GREEN;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Size: " + s1 + "x" + s2 + " = " + (size1 * size2));
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

    private boolean isPlayer(final CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        sender.sendMessage("Only players can run this command!");
        return false;
    }

    public boolean isInventoryEmpty(final Player player) {
        final PlayerInventory i = player.getInventory();
        if (i.getBoots() != null) {
            return false;
        }
        if (i.getHelmet() != null) {
            return false;
        }
        if (i.getLeggings() != null) {
            return false;
        }
        if (i.getChestplate() != null) {
            return false;
        }
        final ItemStack[] contents = player.getInventory().getContents();
        final ItemStack[] var7 = contents;
        final int var6 = contents.length;
        for (int var5 = 0; var5 < var6; var5++) {
            final ItemStack content = var7[var5];
            if (content != null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasEnchantments(final Player player) {
        final PlayerInventory i = player.getInventory();
        return (i.getBoots() != null && !i.getBoots().getEnchantments().isEmpty()) ? true : ((i.getHelmet() != null && !i.getHelmet().getEnchantments().isEmpty()) ? true : ((i.getLeggings() != null && !i.getLeggings().getEnchantments().isEmpty()) ? true : ((i.getChestplate() != null && !i.getChestplate().getEnchantments().isEmpty()))));
    }

    private void onCubeOutRequest(final CommandSender sender) {
        if (this.isPlayer(sender)) {
            final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
            while (var3.hasNext()) {
                final DeathCube dc = var3.next();
                final Player p = (Player) sender;
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

    private boolean onTeleportationRequest(final CommandSender sender, final boolean ignore, final String name) {
        if (!this.isPlayer(sender)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command!");
        } else {
            if (name != null) {
                final DeathCube deathCube = this.findDeathCubeByString(name);
                if (deathCube == null) {
                    sender.sendMessage(ChatColor.RED + "Cube not found. Try " + ChatColor.GOLD + "/dc list");
                    return true;
                }
            }
            final Player p = (Player) sender;
            boolean emptyInventory = true;
            String items = "";
            final ItemStack[] contents = p.getInventory().getContents();
            final ItemStack[] var12 = contents;
            final int var11 = contents.length;
            for (int l = 0; l < var11; l++) {
                final ItemStack i = var12[l];
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
            final PlayerInventory var13 = p.getInventory();
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
            final DeathCube dc = this.findDeathCubeBySender((CommandSender) p);
            if (dc != null) {
                if (dc.hasCorrectLocation) {
                    if (!dc.gameRunning) {
                        if (dc.spawn != null) {
                            final Location var14 = p.getLocation();
                            if (dc.tpRange == 0 || this.checkPermissions((CommandSender) p, 2)) {
                                if (!emptyInventory) {
                                    p.sendMessage(this.lang.get("inventoryWarning"));
                                }
                                dc.tpIn(p);
                                if (!this.upToDate) {
                                    p.sendMessage(ChatColor.GRAY + "A new version is available. Type " + ChatColor.GOLD + "/dc i");
                                }
                                return true;
                            }
                            if (var14.getWorld().equals(dc.world)) {
                                if (dc.contains(var14, dc.tpRange)) {
                                    if (!emptyInventory) {
                                        p.sendMessage(this.lang.get("inventoryWarning"));
                                    }
                                    dc.tpIn(p);
                                    if (!this.upToDate) {
                                        p.sendMessage(ChatColor.GRAY + "Please update DeathCube. Type /dc i");
                                    }
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

    private void onSetLocationRequest(final CommandSender sender, final String spos) {
        final byte pos;
        final String s;
        if (spos.equals("1")) {
            pos = 1;
            s = "First";
        } else {
            pos = 2;
            s = "Second";
        }
        final DeathCube dc = this.cubeSelections.get(sender);
        if (dc != null) {
            final Player p = (Player) sender;
            final Location loc = p.getLocation();
            dc.setPos(pos, p.getLocation());
            if (!this.cubeNames.containsKey(dc.getName())) {
                this.cubeNames.put(dc.getName(), dc);
            }
            p.sendMessage(ChatColor.GRAY + s + " position set to " + this.locationToString(loc));
            this.saveLocation("cubes." + dc.getName() + ".locations.loc" + pos, loc);
            if (this.checkPosition(dc, sender)) {
                final Location loc1 = dc.getPos(1);
                final Location loc2 = dc.getPos(2);
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

    private boolean onCheckPositionRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            sender.sendMessage(ChatColor.AQUA + "First Position: " + ChatColor.GOLD + this.locationToString(dc.getPos(1)));
            sender.sendMessage(ChatColor.AQUA + "Second Position: " + ChatColor.GOLD + this.locationToString(dc.getPos(2)));
            this.checkPosition(dc, sender);
            return true;
        }
        return false;
    }

    private void onOpenGateRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onBlockbarReloadRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null && sender instanceof Player && !dc.gameRunning) {
            dc.getBlockBar().load(sender);
        }
    }

    private void onBlockbarSetRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            if (sender instanceof Player) {
                if (!dc.gameRunning) {
                    final Player p = (Player) sender;
                    final int direction = (int) Math.floor(Math.abs(p.getLocation().getYaw()));
                    final byte distance = 1;
                    final Vector vector = new Vector();
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
                    final Location loc = p.getLocation();
                    final Location loc2 = loc.clone();
                    final Location loc3 = loc2.clone();
                    loc3.add(0.0D, 1.0D, 0.0D);
                    boolean hasSpace = true;
                    for (int x = 0; x < 5; x++) {
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
                        this.config.set("cubes." + dc.getName() + ".locations.blockbar.x", Integer.valueOf(loc.getBlockX()));
                        this.config.set("cubes." + dc.getName() + ".locations.blockbar.y", Integer.valueOf(loc.getBlockY()));
                        this.config.set("cubes." + dc.getName() + ".locations.blockbar.z", Integer.valueOf(loc.getBlockZ()));
                        this.config.set("cubes." + dc.getName() + ".locations.blockbar.vx", Integer.valueOf(vector.getBlockX()));
                        this.config.set("cubes." + dc.getName() + ".locations.blockbar.vz", Integer.valueOf(vector.getBlockZ()));
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

    private void onSpawnSetRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            if (sender instanceof Player) {
                final Player p = (Player) sender;
                final Location loc = p.getLocation().add(0.0D, 1.0D, 0.0D);
                dc.spawn = loc;
                this.config.set("cubes." + dc.getName() + ".locations.spawn.world", loc.getWorld().getUID().toString());
                this.config.set("cubes." + dc.getName() + ".locations.spawn.x", Integer.valueOf(loc.getBlockX()));
                this.config.set("cubes." + dc.getName() + ".locations.spawn.y", Integer.valueOf(loc.getBlockY()));
                this.config.set("cubes." + dc.getName() + ".locations.spawn.z", Integer.valueOf(loc.getBlockZ()));
                this.config.set("cubes." + dc.getName() + ".locations.spawn.yaw", Float.valueOf(loc.getYaw()));
                this.config.set("cubes." + dc.getName() + ".locations.spawn.pitch", Float.valueOf(loc.getPitch()));
                this.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Spawn position set!");
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can run this command!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
        }
    }

    private void onGetConfigurationRequest(final CommandSender sender, final String key) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private boolean onChangeConfigurationRequest(final CommandSender sender, final String key, final String value) {
        if (!value.contains(".")) {
            final DeathCube dc = this.findDeathCubeBySender(sender);
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
                                this.config.set(path, Boolean.valueOf(true));
                            } else {
                                if (!value.equalsIgnoreCase("false")) {
                                    sender.sendMessage(ChatColor.RED + "Please use 'true' or 'false' to specify booleans.");
                                    return false;
                                }
                                this.config.set(path, Boolean.valueOf(false));
                            }
                        } else if (this.config.isInt(path)) {
                            try {
                                final int e = Integer.parseInt(value);
                                this.config.set(path, Integer.valueOf(e));
                            } catch (final NumberFormatException var7) {
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

    private void onTunnelCreationRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onCubeGenerationRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            if (!dc.hasCorrectLocation) {
                sender.sendMessage(ChatColor.RED + "Invalid Location: " + dc.errorState);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
        }
    }

    private void onCubeRenewRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onStopRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onBurnStopRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onBurnRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
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

    private void onStartRequest(final CommandSender sender, final String minutes) {
        final boolean isUser = !this.checkPermissions(sender, 2);
        if (minutes != null && isUser) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to use parameters bro.");
        } else {
            try {
                int e = -1;
                if (minutes != null) {
                    e = Integer.parseInt(minutes);
                }
                if (e == -1 || (e >= 0 && e <= 120)) {
                    final DeathCube dc = this.findDeathCubeBySender(sender);
                    if (dc != null) {
                        if (dc.hasCorrectLocation) {
                            if (!dc.gameRunning) {
                                if (e == 0) {
                                    dc.startGame();
                                } else if (e == -1) {
                                    if (isUser) {
                                        if (dc.countWaitingPlayers() >= dc.mPublicMinPlayers) {
                                            final int[] tempTime = dc.getGameManager().getTempTime();
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
                                        final int[] tempTime = dc.getGameManager().getTempTime();
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
                                    final int[] tempTime = dc.getGameManager().getTempTime();
                                    if (tempTime != null) {
                                        sender.sendMessage(ChatColor.GRAY + "The old timer would start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds.");
                                        sender.sendMessage(ChatColor.GREEN + "You have overwritten the old timer!");
                                    } else {
                                        sender.sendMessage(ChatColor.GREEN + "New timer started!");
                                    }
                                    dc.getGameManager().newTempTask(e, false);
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
            } catch (final NumberFormatException var7) {
                sender.sendMessage(ChatColor.RED + "Give me an integer please!");
            }
        }
    }

    private void onCubeCreationRequest(final CommandSender sender) {
        final DeathCube dc = this.cubeSelections.get(sender);
        if (dc != null) {
            if (dc.hasCorrectLocation) {
                final short maxHeight = 200;
                final Location pos1 = dc.getPos(1);
                final Location pos2 = dc.getPos(2);
                final World w = pos1.getWorld();
                int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
                int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
                int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
                int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
                Math.min(pos1.getBlockY(), pos2.getBlockY());
                final int surface = Math.max(pos1.getBlockY(), pos2.getBlockY());
                int count = 0;
                final Iterator<Entity> bb = w.getEntities().iterator();
                while (bb.hasNext()) {
                    final Entity bedrockHeight = bb.next();
                    if (bedrockHeight.getLocation().getBlockX() >= minX && bedrockHeight.getLocation().getBlockX() <= maxX && bedrockHeight.getLocation().getBlockZ() >= minZ && bedrockHeight.getLocation().getBlockZ() <= maxZ && !(bedrockHeight instanceof org.bukkit.entity.HumanEntity)) {
                        count++;
                        bedrockHeight.remove();
                    }
                }
                sender.sendMessage("" + ChatColor.AQUA + count + " entities removed!");
                int var17;
                for (var17 = 0; var17 < maxHeight; var17++) {
                    for (int i = minX; i < maxX + 1; i++) {
                        for (int j = minZ; j < maxZ + 1; j++) {
                            if (var17 < 1) {
                                w.getBlockAt(i, var17, j).setType(Material.BEDROCK);
                            } else if (var17 == 1) {
                                if (this.random(0, 2) == 2) {
                                    w.getBlockAt(i, var17, j).setType(Material.LAVA);
                                } else {
                                    w.getBlockAt(i, var17, j).setType(Material.AIR);
                                }
                            } else {
                                w.getBlockAt(i, var17, j).setType(Material.AIR);
                            }
                        }
                    }
                }
                for (var17 = 4; var17 < surface; var17++) {
                    int i;
                    for (i = minX; i < maxX + 1; i++) {
                        w.getBlockAt(i, var17, minZ).setType(Material.STONE);
                        w.getBlockAt(i, var17, maxZ).setType(Material.STONE);
                    }
                    for (i = minZ; i < maxZ + 1; i++) {
                        w.getBlockAt(minX, var17, i).setType(Material.STONE);
                        w.getBlockAt(maxX, var17, i).setType(Material.STONE);
                    }
                }
                final byte var19 = 4;
                for (int var18 = 1; var18 <= var19; var18++) {
                    int i;
                    for (i = minX; i < maxX + 1; i++) {
                        w.getBlockAt(i, var18, minZ).setType(Material.BEDROCK);
                        w.getBlockAt(i, var18, maxZ).setType(Material.BEDROCK);
                    }
                    for (i = minZ; i < maxZ + 1; i++) {
                        w.getBlockAt(minX, var18, i).setType(Material.BEDROCK);
                        w.getBlockAt(maxX, var18, i).setType(Material.BEDROCK);
                    }
                }
                minZ++;
                maxZ--;
                minX++;
                maxX--;
                final BlockBar var20 = dc.getBlockBar();
                for (int h = 5; h < surface; h++) {
                    int e;
                    for (e = minX + 1; e < maxX; e++) {
                        var20.setCageBlock(w.getBlockAt(e, h, minZ));
                        var20.setCageBlock(w.getBlockAt(e, h, maxZ));
                    }
                    for (e = minZ; e < maxZ + 1; e++) {
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

    private void onGetTimerRequest(final CommandSender sender) {
        final DeathCube dc = this.findDeathCubeBySender(sender);
        if (dc != null) {
            sender.sendMessage(ChatColor.GREEN + "Timers for DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + ":");
            if (dc.gameRunning) {
                sender.sendMessage(ChatColor.GRAY + "Currently there is a game " + ChatColor.GOLD + "running" + ChatColor.GRAY + "!");
            }
            final int[] tempTime = dc.getGameManager().getTempTime();
            if (tempTime != null) {
                sender.sendMessage(ChatColor.GRAY + "DeathCube will try to start in " + ChatColor.GOLD + tempTime[0] + " minutes and " + tempTime[1] + " seconds" + ChatColor.GRAY + "!");
            } else {
                sender.sendMessage(ChatColor.GRAY + "No irregulär timer active!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "A cube with that name does not exist!");
        }
    }

    private void onListDeathCubesRequest(final CommandSender sender) {
        if (this.cubeNames.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Could not find any active DeathCubes!");
        } else {
            final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
            while (var3.hasNext()) {
                final DeathCube dc = var3.next();
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
                    continue;
                }
                sender.sendMessage(ChatColor.GOLD + dc.getName() + ChatColor.GRAY + " is not set properly.");
            }
        }
    }

    private void onCubeDeletionRequest(final CommandSender sender, final String name) {
        final DeathCube dc = this.findDeathCubeByString(name);
        if (dc != null) {
            if (!dc.gameRunning) {
                this.cubeNames.remove(dc.getName());
                this.config.set("cubes." + dc.getName(), null);
                this.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " removed!");
            } else {
                sender.sendMessage(ChatColor.RED + "Stop the game first!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "A cube with that name does not exist!");
        }
    }

    private void onCubeDefaultRequest(final CommandSender sender, final String name) {
        final DeathCube dc = this.findDeathCubeByString(name);
        if (dc != null) {
            this.defaultDC = dc;
            this.config.set("global.default", dc.getName());
            this.saveConfig();
            sender.sendMessage(ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " is now your default cube!");
        } else {
            sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
        }
    }

    private void onHelpRequest(final CommandSender sender, final String submenu) {
        if (this.checkPermissions(sender, 1)) {
            if (submenu != null) {
                if (!submenu.equalsIgnoreCase("i") && !submenu.equalsIgnoreCase("info")) {
                    if (submenu.equalsIgnoreCase("c") || submenu.equalsIgnoreCase("commands")) {
                        if (this.checkPermissions(sender, 2)) {
                            sender.sendMessage(ChatColor.GRAY + "I will display the user commands for you. If you don't know the admin commands rtfm!");
                        }
                        final String[] var6;
                        final int var5 = (var6 = this.lang.get("helpCommands").split(";")).length;
                        for (int var4 = 0; var4 < var5; var4++) {
                            final String var7 = var6[var4];
                            sender.sendMessage(var7);
                        }
                    } else if (!submenu.equalsIgnoreCase("tournament") && !submenu.equalsIgnoreCase("t")) {
                        if (!submenu.equalsIgnoreCase("r") && !submenu.equalsIgnoreCase("rules")) {
                            sender.sendMessage(ChatColor.GRAY + "Info page not found. Try " + ChatColor.GOLD + "info, commands or rules");
                        } else {
                            final DeathCube line = this.findDeathCubeBySender(sender);
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
                    } else {
                        sender.sendMessage(ChatColor.AQUA + "First of all: Our first official deathcube tournament is for german players only, because we want to limit the number of participants. The next tournament will be international, so keep up your training ;)");
                        sender.sendMessage("" + ChatColor.AQUA);
                        sender.sendMessage(ChatColor.GREEN + "Bei unserem ersten offiziellen DeathCube Turnier kaempfen 3-5 Spieler pro Team um Geld, Serverpakete und iPods! Interesse geweckt? Auf " + ChatColor.GOLD + "deathcube.minebench.de" + ChatColor.GREEN + " erfaehrst du, wie du teilnehmen kannst.");
                    }
                } else {
                    sender.sendMessage(this.lang.get("helpInfo"));
                }
            } else {
                final String[] var6;
                final int var5 = (var6 = this.lang.get("helpIndex").split(";")).length;
                for (int var4 = 0; var4 < var5; var4++) {
                    final String var7 = var6[var4];
                    sender.sendMessage(var7);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "First you should tell your admin that you need " + ChatColor.GOLD + "deathcube.use" + ChatColor.RED + " permissions ;)");
        }
    }

    private void onSelectionRequest(final CommandSender sender, final String name) {
        if (this.cubeNames.containsKey(name)) {
            final DeathCube newDC = this.findDeathCubeByString(name);
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
                final DeathCube newDC = new DeathCube(name, this);
                newDC.setBlockBar(new BlockBar());
                this.cubeSelections.put(sender, newDC);
            } else {
                sender.sendMessage(ChatColor.RED + "Choose another name please!");
            }
        } else {
            this.lang.get("noPermissions");
        }
    }

    private void onDownloadRequest(final CommandSender sender) {
        this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, new Runnable() {
            @Override
            public void run() {
                if (DeathCubeManager.this.downloadFile("http://google.de", "plugins/DeathCube.jar", sender)) {
                    sender.sendMessage(ChatColor.GREEN + "Download finished.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Download failed.");
                }
            }
        });
    }

    @EventHandler
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Location loc = event.getPlayer().getLocation();
            if (dc.hasCorrectLocation && dc.contains(loc, 1)) {
                if (dc.contains(loc, -9)) {
                    final Item item = event.getItem();
                    if (item.getItemStack().getType() != Material.JACK_O_LANTERN) {
                        event.setCancelled(true);
                    }
                    continue;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayeyDropItem(final PlayerDropItemEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            if (dc.hasCorrectLocation && dc.contains(event.getItemDrop().getLocation(), dc.dropProtection)) {
                final Player p = event.getPlayer();
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
    public void onPlayerRunsCommand(final PlayerCommandPreprocessEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
            if (dc.contains(p.getLocation(), -2)) {
                if (!this.checkPermissions((CommandSender) p, 2)) {
                    final String command = event.getMessage().split(" ")[0];
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
    public void onRedstoneEvent(final BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_LAMP && ((Lightable) event.getBlock().getBlockData()).isLit()) {
            final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
            while (var3.hasNext()) {
                final DeathCube dc = var3.next();
                if (dc.activeRedstone && dc.hasCorrectLocation && dc.contains(event.getBlock().getLocation())) {
                    event.setNewCurrent(15);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            if (dc.hasCorrectLocation) {
                if (!dc.getBlockBar().contains(event.getBlockPlaced().getLocation()) || this.checkPermissions((CommandSender) event.getPlayer(), 2)) {
                    if (dc.contains(event.getBlockPlaced().getLocation())) {
                        final Player p = event.getPlayer();
                        if (!dc.isActive(p)) {
                            if (!this.checkPermissions((CommandSender) p, 2)) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(this.lang.get("innerProtection"));
                            }
                            break;
                        }
                        if (event.getBlock().getType() == Material.JACK_O_LANTERN) {
                            final Location loc = event.getBlockPlaced().getLocation();
                            if (dc.contains(loc, -8) && loc.getBlockY() >= dc.startHeight && loc.getBlockY() <= dc.height) {
                                event.getBlockPlaced().setType(Material.PUMPKIN);
                                // Todo: Set random facing
                                break;
                            }
                            event.setCancelled(true);
                            break;
                        }
                        dc.playerLeft(p, this.lang.get("leaveBlockplaceA"), this.lang.get("leaveBlockplaceP"));
                        dc.tpToSpawn(p);
                        event.setCancelled(true);
                    }
                    break;
                }
                event.getPlayer().sendMessage(this.lang.get("protectionBar"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
            if (dc.hasCorrectLocation && dc.gameRunning) {
                if (dc.isActive(p)) {
                    if (!dc.contains(p.getLocation(), -4)) {
                        final Location loc = p.getLocation();
                        if (loc.getBlockY() < dc.stageLevel + 1 || loc.getBlockY() > dc.stageLevel + 3) {
                            dc.playerLeft(p, this.lang.get("leaveAreaA"), this.lang.get("leaveAreaP"));
                            dc.tpToSpawn(p);
                        }
                        break;
                    }
                    if (dc.looseOnFall && p.getLocation().getBlockY() <= dc.startHeight - 1) {
                        dc.playerLeft(p, this.lang.get("leaveFallA"), this.lang.get("leaveFallA"));
                        dc.tpToSpawn(p);
                        break;
                    }
                    if (!dc.isBurned && p.getLocation().getBlockY() >= dc.burnLevel && dc.burnLevel >= dc.stageLevel + 1 && dc.burnLevel <= dc.height) {
                        dc.playerReachedBurnLimit(p);
                        break;
                    }
                    if (p.getLocation().getBlockY() >= dc.height + 1) {
                        dc.playerReachedTop(p);
                    }
                    break;
                }
                if (dc.contains(p.getLocation(), -2)) {
                    if (!this.checkPermissions((CommandSender) p, 2)) {
                        p.sendMessage(this.lang.get("enterProtection"));
                        dc.tpToSpawn(p);
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.HumanEntity && event.getDamager().getType() == EntityType.ENDER_PEARL) {
            final Player p = (Player) event.getEntity();
            final Iterator<DeathCube> var4 = this.cubeNames.values().iterator();
            while (var4.hasNext()) {
                final DeathCube dc = var4.next();
                if (dc.isActive(p)) {
                    dc.playerLeft(p, this.lang.get("leaveEnderpearlA"), this.lang.get("leaveEnderpearlP"));
                    dc.tpToSpawn(p);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
            if (dc.waiting.contains(p.getName())) {
                dc.waiting.remove(p.getName());
                this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, new Runnable() {
                    @Override
                    public void run() {
                        dc.tpToSpawn(p);
                    }
                }, 10L);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
            if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -2)) {
                this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, new Runnable() {
                    @Override
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
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
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
    public void onPlayerItemChange(final PlayerItemHeldEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getPlayer();
            if (dc.isActive(p)) {
                final ItemStack i = p.getInventory().getItem(event.getNewSlot());
                if (i != null && i.getType() == Material.POTION) {
                    p.getInventory().setItem(event.getNewSlot(), new ItemStack(Material.AIR));
                    p.sendMessage(this.lang.get("potionSelect"));
                }
            }
        }
    }

    private void onPlayerWantsFood(final CommandSender sender) {
        if (this.isPlayer(sender)) {
            final Player p = (Player) sender;
            final Iterator<DeathCube> var4 = this.cubeNames.values().iterator();
            while (var4.hasNext()) {
                final DeathCube dc = var4.next();
                if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -5)) {
                    if (!dc.offlineFeed) {
                        if (dc.isActive(p)) {
                            if (p.getFoodLevel() < 20) {
                                p.setFoodLevel(20);
                                p.sendMessage(this.lang.get("onFeed"));
                                continue;
                            }
                            p.sendMessage(this.lang.get("notHungry"));
                            continue;
                        }
                        p.sendMessage(this.lang.get("notIngame"));
                        continue;
                    }
                    if (p.getFoodLevel() < 20) {
                        p.setFoodLevel(20);
                        p.sendMessage(this.lang.get("onFeed"));
                        continue;
                    }
                    p.sendMessage(this.lang.get("notHungry"));
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can run this command!");
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Player p = event.getEntity();
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
    public void onBlockBreak(final BlockBreakEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            final Location loc = event.getBlock().getLocation();
            if (dc.hasCorrectLocation) {
                if (dc.getBlockBar().contains(loc) && !this.checkPermissions((CommandSender) event.getPlayer(), 2)) {
                    event.getPlayer().sendMessage(this.lang.get("barProtection"));
                    event.setCancelled(true);
                    continue;
                }
                if (dc.contains(loc)) {
                    final Player p = event.getPlayer();
                    if (dc.contains(loc, -8)) {
                        if (event.getBlock().getType() == Material.JACK_O_LANTERN && this.checkPermissions((CommandSender) p, 1)) {
                            if (!dc.isActive(p)) {
                                if (!this.checkPermissions((CommandSender) p, 3)) {
                                    event.setCancelled(true);
                                    p.sendMessage(this.lang.get("jokerOffline"));
                                }
                                break;
                            }
                            p.sendMessage(this.lang.get("jokerOnline"));
                            break;
                        }
                        if (!this.checkPermissions((CommandSender) p, 3)) {
                            event.setCancelled(true);
                            p.sendMessage(this.lang.get("innerProtection"));
                        }
                        break;
                    }
                    if (!this.checkPermissions((CommandSender) p, 2)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(this.lang.get("outerProtection"));
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
        while (var3.hasNext()) {
            final DeathCube dc = var3.next();
            if (dc.hasCorrectLocation && dc.contains(event.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onEnable() {
        this.config = this.getConfig();
        if (!this.configPath.exists()) {
            try {
                this.config.load(this.configPath);
            } catch (final Exception var3) {
                this.o("config.yml generated or error while reading!");
                this.saveSettings();
            }
        } else {
            try {
                this.config.load(this.configPath);
            } catch (final Exception var2) {
                this.o("Error while reading reading from config.yml!");
            }
        }
        this.loadDeathCubes();
        this.lang = new Language(this);
        this.getServer().getPluginManager().registerEvents(this, (Plugin) this);
        System.out.println("DeathCube 3.0.0 enabled!");
    }

    private void loadDeathCubes() {
        if (this.config.contains("pos")) {
            this.notUpdated = true;
            final World dc = this.getServer().getWorld(UUID.fromString(this.config.getString("pos.world")));
            if (dc != null) {
                final Location s = new Location(dc, this.config.getInt("pos.1.x"), this.config.getInt("pos.1.y"), this.config.getInt("pos.1.z"));
                final Location pos2 = new Location(dc, this.config.getInt("pos.2.x"), this.config.getInt("pos.2.y"), this.config.getInt("pos.2.z"));
                if (s != null && pos2 != null) {
                    final DeathCube dct = new DeathCube("protectionHolder", this);
                    this.saveLocation("cubes." + dct.getName() + ".locations.loc1", s);
                    this.saveLocation("cubes." + dct.getName() + ".locations.loc2", pos2);
                }
            }
        }
        final DeathCube var6 = this.findDeathCubeByString(this.getString("global.default", ""));
        if (var6 != null) {
            this.defaultDC = var6;
        }
        this.allowedCommands.clear();
        this.allowedCommands.add("dc");
        final String[] var5;
        final int var10 = (var5 = this.getString("global.allowedCommands", "who,list").split(",")).length;
        for (int var8 = 0; var8 < var10; var8++) {
            final String var7 = var5[var8];
            this.allowedCommands.add(var7);
        }
        if (!this.config.contains("global.autoUpdate")) {
            this.config.set("global.autoUpdate", Boolean.valueOf(true));
        }
        this.registrator = new RegistrationManager(this, this.getBoolean("global.allowRegistration", true));
        if (this.config.contains("cubes")) {
            final Iterator<String> var9 = this.config.getConfigurationSection("cubes").getKeys(false).iterator();
            while (var9.hasNext()) {
                final String var7 = var9.next();
                this.o("Reading DeathCube \"" + var7 + "\"");
                final DeathCube dct = new DeathCube(var7, this);
                this.loadConfig(dct);
            }
        }
        this.saveConfig();
        this.checkVersion();
    }

    private void loadConfig(final DeathCube dc) {
        final BlockBar var12;
        String path = "cubes." + dc.getName() + ".";
        final Location pos1 = this.readLocation(path + "locations.loc1");
        final Location pos2 = this.readLocation(path + "locations.loc2");
        String errorMessage = "";
        if (pos1 != null) {
            if (pos2 != null) {
                if (pos1.getWorld() != null && pos2.getWorld() != null) {
                    if (pos1.getWorld().equals(pos2.getWorld())) {
                        final int bb = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
                        final int x = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
                        if (bb >= 16 && bb <= 80 && x >= 16 && x <= 80) {
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
        dc.advertiseTournament = this.getBoolean(path + "advertiseTournament", true);
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
        if (this.config.contains("cubes." + dc.getName() + ".winCommands")) {
            final String var11 = this.config.getString("cubes." + dc.getName() + ".winCommands");
            if (var11.contains(";")) {
                final String[] w;
                final int z = (w = var11.split(";")).length;
                for (int y = 0; y < z; y++) {
                    final String var13 = w[y];
                    dc.winCommands.add(var13);
                }
            } else {
                dc.winCommands.add(var11);
            }
        } else {
            this.config.set("cubes." + dc.getName() + ".winCommands", "");
        }
        path = path + "locations.";
        if (this.config.contains(path + ".blockbar")) {
            final Location var14 = new Location(this.getServer().getWorld(UUID.fromString(this.config.getString(path + ".blockbar.world"))), this.config.getInt(path + ".blockbar.x"), this.config.getInt(path + ".blockbar.y"), this.config.getInt(path + ".blockbar.z"));
            final Vector var15 = new Vector(this.config.getInt(path + ".blockbar.vx"), 0, this.config.getInt(path + ".blockbar.vz"));
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
            final int x = this.config.getInt(path + "spawn.x");
            final int y = this.config.getInt(path + "spawn.y");
            final int z = this.config.getInt(path + "spawn.z");
            final World var16 = this.getServer().getWorld(UUID.fromString(this.config.getString(path + "spawn.world")));
            if (var16 != null) {
                dc.spawn = new Location(var16, x, y, z, (float) this.config.getLong(path + "spawn.yaw"), (float) this.config.getLong(path + "spawn.pitch"));
            }
        }
        this.cubeNames.put(dc.getName(), dc);
    }

    private void loadWinCommands(final String path, final ArrayList<String> list) {
        list.clear();
        if (this.config.contains(path)) {
            final String commandString = this.config.getString(path);
            if (commandString.contains(";")) {
                final String[] var7;
                final int var6 = (var7 = commandString.split(";")).length;
                for (int var5 = 0; var5 < var6; var5++) {
                    final String s = var7[var5];
                    list.add(s);
                }
            } else {
                list.add(commandString);
            }
        } else {
            this.config.set(path, "");
        }
    }

    private long getLong(final String path, final long value) {
        if (this.config.contains(path)) {
            return this.config.getLong(path);
        }
        this.config.set(path, Long.valueOf(value));
        return value;
    }

    private int getInt(final String path, final int value) {
        if (this.config.contains(path)) {
            return this.config.getInt(path);
        }
        this.config.set(path, Integer.valueOf(value));
        return value;
    }

    private boolean getBoolean(final String path, final boolean value) {
        if (this.config.contains(path)) {
            return this.config.getBoolean(path);
        }
        this.config.set(path, Boolean.valueOf(value));
        return value;
    }

    private String getString(final String path, final String value) {
        if (this.config.contains(path)) {
            return this.config.getString(path);
        }
        this.config.set(path, value);
        return value;
    }

    private void saveSettings() {
        try {
            this.config.save(this.configPath);
        } catch (final IOException var2) {
            System.out.println("Unknown Exception while saving config File!");
        }
    }

    private boolean downloadFile(final String link, final String localFile, final CommandSender sender) {
        try {
            final URL e = new URL(link);
            final URLConnection conn = e.openConnection();
            final BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(localFile));
            final byte[] chunk = new byte[1024];
            int chunkSize;
            while ((chunkSize = is.read(chunk)) != -1) {
                os.write(chunk, 0, chunkSize);
            }
            os.flush();
            os.close();
            is.close();
            return true;
        } catch (final IOException var10) {
            this.o("Error while downloading File!");
            return false;
        }
    }

    private void checkVersion() {
        this.getServer().getScheduler().scheduleAsyncDelayedTask((Plugin) this, new Runnable() {
            @Override
            public void run() {
                try {
                    URL e1 = new URL("http://dev.bukkit.org/server-mods/deathcube/files/");
                    URLConnection yc = e1.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                    String inputLine = "";
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.contains("col-file\"")) {
                            String link = inputLine.split("deathcube/files/")[1].split("/")[0];
                            final String newestVersion = inputLine.split("DeathCube ")[1].split("<")[0];
                            link = "http://dev.bukkit.org/server-mods/deathcube/files/" + link + "/";
                            if (!newestVersion.equals(DeathCubeManager.this.version)) {
                                DeathCubeManager.this.upToDate = false;
                                System.out.println("DeathCube is not UpToDate. Please update!");
                                if (!DeathCubeManager.this.config.getBoolean("global.autoUpdate")) {
                                    break;
                                }
                                if ((new File("plugins/DeathCube.jar.new")).exists()) {
                                    DeathCubeManager.this.o("Please rename \"DeathCube.jar.new\" to \"DeathCube.jar\"");
                                    break;
                                }
                                e1 = new URL(link);
                                yc = e1.openConnection();
                                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                                while ((inputLine = in.readLine()) != null) {
                                    if (inputLine.contains("media/files/")) {
                                        String downloadLink = inputLine.split("http://dev.bukkit.org/media/files/")[1].split("/DeathCube")[0];
                                        downloadLink = "http://dev.bukkit.org/media/files/" + downloadLink + "/DeathCube.jar";
                                        DeathCubeManager.this.fileDownload(downloadLink, "plugins/DeathCube.jar");
                                        DeathCubeManager.this.o("Version " + newestVersion + " downloaded!");
                                        break;
                                    }
                                }
                                break;
                            }
                            DeathCubeManager.this.upToDate = true;
                            System.out.println("DeathCube is UpToDate!");
                            break;
                        }
                    }
                    in.close();
                } catch (final IOException var8) {
                    System.out.println("Unkown error while looking for updates.");
                }
            }
        }, 10L);
    }

    private void fileDownload(final String link, final String localFile) {
        try {
            final URL e = new URL(link);
            final URLConnection conn = e.openConnection();
            final BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(localFile));
            final byte[] chunk = new byte[1024];
            int chunkSize;
            while ((chunkSize = is.read(chunk)) != -1) {
                os.write(chunk, 0, chunkSize);
            }
            os.flush();
            os.close();
            is.close();
        } catch (final IOException var9) {
            this.o("Error while downloading! Try it manually.");
            var9.printStackTrace();
        }
    }

    private String locationToString(final Location loc) {
        return (loc != null) ? (loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " in '" + loc.getWorld().getName() + "'") : "Location not set!";
    }

    private int random(final int from, int to) {
        to++;
        return (int) (Math.random() * (to - from) + from);
    }

    private boolean isNumber(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException var3) {
            return false;
        }
    }

    private void o(final String text) {
        System.out.println("DC: " + text);
    }
}
