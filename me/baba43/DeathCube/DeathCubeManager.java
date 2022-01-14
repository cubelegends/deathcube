package me.baba43.DeathCube;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class DeathCubeManager extends JavaPlugin implements Listener {
  private Map<CommandSender, DeathCube> cubeSelections = new HashMap<>();
  
  private Map<String, DeathCube> cubeNames = new HashMap<>();
  
  private File configPath = new File("plugins/DeathCube/config.yml");
  
  private FileConfiguration config;
  
  public Language lang;
  
  private String version = "v2.53";
  
  private ArrayList<String> allowedCommands = new ArrayList<>();
  
  private DeathCube defaultDC;
  
  private boolean upToDate = true;
  
  private boolean notUpdated = false;
  
  private RegistrationManager registrator;
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (cmd.getName().equalsIgnoreCase("dc"))
      if (this.notUpdated) {
        newVersionNotification(sender);
        sender.sendMessage(ChatColor.GRAY + "Command was not executed!");
      } else if (args.length > 0) {
        if (args[0].equalsIgnoreCase("create")) {
          if (checkPermissions(sender, 3)) {
            onCubeCreationRequest(sender);
          } else {
            sender.sendMessage(ChatColor.RED + "No Permissions");
          } 
        } else if (args[0].equalsIgnoreCase("select")) {
          if (checkPermissions(sender, 1)) {
            if (args.length == 1) {
              tellSelection(sender);
            } else if (args.length == 2) {
              onSelectionRequest(sender, args[1]);
            } else {
              sender.sendMessage(ChatColor.RED + "Wrong usage. Use /dc select [name]");
            } 
          } else {
            sender.sendMessage(this.lang.get("noPermissions"));
          } 
        } else if (args[0].equalsIgnoreCase("pos")) {
          if (checkPermissions(sender, 3)) {
            if (sender instanceof Player) {
              if (args.length == 2) {
                onSetLocationRequest(sender, args[1]);
              } else if (args.length == 1) {
                onCheckPositionRequest(sender);
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
            if (checkPermissions(sender, 2)) {
              if (args.length == 3) {
                onChangeConfigurationRequest(sender, args[1], args[2]);
              } else {
                sender.sendMessage(ChatColor.RED + "Wrong usage! Use /dc set <key> <value>");
                sender.sendMessage(ChatColor.RED + "Use '_' for spaces in strings");
              } 
            } else {
              sender.sendMessage(this.lang.get("noPermissions"));
            } 
          } else if (args[0].equalsIgnoreCase("get")) {
            if (checkPermissions(sender, 2)) {
              if (args.length == 2) {
                onGetConfigurationRequest(sender, args[1]);
              } else {
                sender.sendMessage(ChatColor.GRAY + "Use /dc get <key>");
              } 
            } else {
              sender.sendMessage(this.lang.get("noPermissions"));
            } 
          } else if (args[0].equalsIgnoreCase("open")) {
            if (checkPermissions(sender, 2)) {
              onOpenGateRequest(sender);
            } else {
              sender.sendMessage(this.lang.get("noPermissions"));
            } 
          } else if (args[0].equalsIgnoreCase("tp")) {
            if (checkPermissions(sender, 1)) {
              if (args.length == 2) {
                onTeleportationRequest(sender, false, args[1]);
              } else {
                onTeleportationRequest(sender, false, (String)null);
              } 
            } else {
              sender.sendMessage(this.lang.get("noPermissions"));
            } 
          } else if (args[0].equalsIgnoreCase("tpi")) {
            if (checkPermissions(sender, 1)) {
              if (args.length == 2) {
                onTeleportationRequest(sender, true, args[1]);
              } else {
                onTeleportationRequest(sender, true, (String)null);
              } 
            } else {
              sender.sendMessage(this.lang.get("noPermissions"));
            } 
          } else if (!args[0].equalsIgnoreCase("food") && !args[0].equalsIgnoreCase("f")) {
            if (args[0].equalsIgnoreCase("list")) {
              if (checkPermissions(sender, 1)) {
                onListDeathCubesRequest(sender);
              } else {
                sender.sendMessage(this.lang.get("noPermissions"));
              } 
            } else if (!args[0].equalsIgnoreCase("info") && !args[0].equalsIgnoreCase("i")) {
              if (!args[0].equalsIgnoreCase("start") && !args[0].equalsIgnoreCase("s")) {
                if (args[0].equalsIgnoreCase("stop")) {
                  if (checkPermissions(sender, 2)) {
                    onStopRequest(sender);
                  } else {
                    sender.sendMessage(this.lang.get("noPermissions"));
                  } 
                } else if (args[0].equalsIgnoreCase("spawn")) {
                  if (checkPermissions(sender, 3)) {
                    onSpawnSetRequest(sender);
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
                  if (checkPermissions(sender, 3)) {
                    if (args.length == 2) {
                      onCubeDeletionRequest(sender, args[1]);
                    } else {
                      sender.sendMessage(ChatColor.RED + "Use /dc delete <name>");
                    } 
                  } else {
                    sender.sendMessage(this.lang.get("noPermissions"));
                  } 
                } else if (args[0].equalsIgnoreCase("default")) {
                  if (checkPermissions(sender, 3)) {
                    if (args.length == 2) {
                      onCubeDefaultRequest(sender, args[1]);
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
                        if (checkPermissions(sender, 2)) {
                          if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
                            onBurnStopRequest(sender);
                          } else {
                            onBurnRequest(sender);
                          } 
                        } else {
                          sender.sendMessage(this.lang.get("noPermissions"));
                        } 
                      } else if (args[0].equalsIgnoreCase("db")) {
                        debugMessage(sender, args);
                      } else if (args[0].equalsIgnoreCase("baba")) {
                        onBabaIsHere(sender);
                      } else if (args[0].equalsIgnoreCase("clear")) {
                        if (checkPermissions(sender, 2)) {
                          onCubeClearRequest(sender);
                        } else {
                          sender.sendMessage(this.lang.get("noPermissions"));
                        } 
                      } else if (!args[0].equalsIgnoreCase("timer") && !args[0].equalsIgnoreCase("t")) {
                        sender.sendMessage(ChatColor.RED + "Unknown Command. Try /dc help");
                      } else if (checkPermissions(sender, 1)) {
                        onGetTimerRequest(sender);
                      } else {
                        sender.sendMessage(this.lang.get("noPermissions"));
                      } 
                    } else if (args.length == 1) {
                      onHelpRequest(sender, (String)null);
                    } else {
                      onHelpRequest(sender, args[1]);
                    } 
                  } else if (checkPermissions(sender, 1)) {
                    onCubeOutRequest(sender);
                  } else {
                    sender.sendMessage(this.lang.get("noPermissions"));
                  } 
                } else if (args.length == 2 && (args[1].startsWith("l") || args[1].startsWith("r"))) {
                  if (checkPermissions(sender, 2)) {
                    onBlockbarReloadRequest(sender);
                  } else {
                    sender.sendMessage(this.lang.get("noPermissions"));
                  } 
                } else if (args.length == 1) {
                  if (checkPermissions(sender, 3)) {
                    onBlockbarSetRequest(sender);
                  } else {
                    sender.sendMessage(this.lang.get("noPermissions"));
                  } 
                } else {
                  sender.sendMessage(ChatColor.RED + "Wrong Usage: /dc bar [l/r]");
                } 
              } else if (!checkPermissions(sender, 2) && !sender.hasPermission("deathcube.start")) {
                sender.sendMessage(this.lang.get("noPermissions"));
              } else if (args.length == 2) {
                onStartRequest(sender, args[1]);
              } else {
                onStartRequest(sender, (String)null);
              } 
            } else {
              sender.sendMessage(ChatColor.GREEN + "This server is running DeathCube " + this.version + "!");
              if (!this.upToDate) {
                sender.sendMessage(ChatColor.GRAY + "Your DeathCube version is outdated, please update!");
                sender.sendMessage(ChatColor.GRAY + "Enable autoUpdate and restart or follow this link:");
                sender.sendMessage(ChatColor.GREEN + "http://dev.bukkit.org/server-mods/deathcube");
              } 
            } 
          } else if (checkPermissions(sender, 1)) {
            onPlayerWantsFood(sender);
          } else {
            sender.sendMessage(this.lang.get("noPermissions"));
          } 
        } else if (checkPermissions(sender, 2)) {
          onCubeRenewRequest(sender);
        } else {
          sender.sendMessage(this.lang.get("noPermissions"));
        } 
      } else {
        sender.sendMessage(ChatColor.RED + "Try /dc help");
      }  
    return true;
  }
  
  private void onCubeClearRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      dc.destroyCube();
      sender.sendMessage(ChatColor.GREEN + "Cube was destroyed from " + dc.startHeight + " to 255");
    } else {
      sender.sendMessage(ChatColor.RED + "Select a DeathCube first using /dc select");
    } 
  }
  
  private void onBabaIsHere(CommandSender sender) {
    if (sender instanceof Player && getServer().getOnlineMode()) {
      Player p = (Player)sender;
      if (p.getName().equals("baba43"))
        getServer().broadcastMessage(ChatColor.GRAY + "DC: Hello Baba! :)"); 
    } 
  }
  
  private void debugMessage(CommandSender sender, String[] args) {
    sender.sendMessage(getServer().getName());
    Player p = (Player)sender;
    p.sendMessage("Aa" + p.getHealth());
  }
  
  public boolean checkPermissions(CommandSender sender, int level) {
    return (!sender.hasPermission("deathcube.admin") && !sender.isOp()) ? ((sender.hasPermission("deathcube.mod") && level < 3) ? true : ((level == 1 && sender.hasPermission("deathcube.use")))) : true;
  }
  
  public void newVersionNotification(CommandSender sender) {
    sender.sendMessage(ChatColor.GRAY + "Important " + ChatColor.GOLD + "notification" + ChatColor.GRAY + ": DeathCube has updated itself to a new version! The config system changed, so you need to recreate your DeathCube and delete your old " + ChatColor.GREEN + "config.yml" + ChatColor.GRAY + ". For safety reasons a placeholder was created that should protect your old DeathCube area.");
    sender.sendMessage("" + ChatColor.GRAY);
    sender.sendMessage(ChatColor.GRAY + "Since commands have changed as well it's highly recommended to read my new instructions by following this link:");
    sender.sendMessage(ChatColor.GREEN + "http://dev.bukkit.org/server-mods/deathcube");
  }
  
  private DeathCube findDeathCubeByString(String name) {
    return this.cubeNames.get(name);
  }
  
  private DeathCube findDeathCubeBySender(CommandSender sender) {
    DeathCube dc = this.cubeSelections.get(sender);
    return (dc == null) ? ((this.cubeNames.size() == 1) ? (DeathCube)this.cubeNames.values().toArray()[0] : ((this.defaultDC != null) ? this.defaultDC : null)) : dc;
  }
  
  private void tellSelection(CommandSender sender) {
    if (this.cubeSelections.containsKey(sender)) {
      DeathCube dc = this.cubeSelections.get(sender);
      sender.sendMessage(ChatColor.GRAY + "You have currently selected " + ChatColor.GOLD + dc.getName());
    } else {
      DeathCube dc = findDeathCubeBySender(sender);
      if (dc != null) {
        sender.sendMessage(ChatColor.GRAY + "No selection!");
        sender.sendMessage(ChatColor.GRAY + "But you will automatically use " + ChatColor.GOLD + dc.getName());
      } else {
        sender.sendMessage(ChatColor.GRAY + "No selection!");
      } 
    } 
  }
  
  private void saveLocation(String path, Location loc) {
    this.config.set(path + ".x", Integer.valueOf(loc.getBlockX()));
    this.config.set(path + ".y", Integer.valueOf(loc.getBlockY()));
    this.config.set(path + ".z", Integer.valueOf(loc.getBlockZ()));
    this.config.set(path + ".world", loc.getWorld().getUID().toString());
    saveSettings();
  }
  
  private Location readLocation(String path) {
    if (this.config.contains(path))
      try {
        int e = this.config.getInt(path + ".x");
        int y = this.config.getInt(path + ".y");
        int z = this.config.getInt(path + ".z");
        World w = getServer().getWorld(UUID.fromString(this.config.getString(path + ".world")));
        return new Location(w, e, y, z);
      } catch (Exception var6) {
        return null;
      }  
    return null;
  }
  
  private boolean checkPosition(DeathCube dc, CommandSender sender) {
    Location loc1 = dc.getPos(1);
    Location loc2 = dc.getPos(2);
    if (loc1 != null) {
      if (loc2 != null) {
        if (loc1.getWorld().equals(loc2.getWorld())) {
          String s1, s2;
          int size1 = Math.abs(loc1.getBlockX() - loc2.getBlockX()) + 1;
          int size2 = Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) + 1;
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
            if (size1 % 2 != 0 || size2 % 2 != 0)
              sender.sendMessage(ChatColor.AQUA + "Please use even numbers if possible!"); 
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
    if (sender instanceof Player)
      return true; 
    sender.sendMessage("Only players can run this command!");
    return false;
  }
  
  public boolean isInventoryEmpty(Player player) {
    PlayerInventory i = player.getInventory();
    if (i.getBoots() != null)
      return false; 
    if (i.getHelmet() != null)
      return false; 
    if (i.getLeggings() != null)
      return false; 
    if (i.getChestplate() != null)
      return false; 
    ItemStack[] contents = player.getInventory().getContents();
    ItemStack[] var7 = contents;
    int var6 = contents.length;
    for (int var5 = 0; var5 < var6; var5++) {
      ItemStack content = var7[var5];
      if (content != null)
        return false; 
    } 
    return true;
  }
  
  public boolean hasEnchantments(Player player) {
    PlayerInventory i = player.getInventory();
    return (i.getBoots() != null && !i.getBoots().getEnchantments().isEmpty()) ? true : ((i.getHelmet() != null && !i.getHelmet().getEnchantments().isEmpty()) ? true : ((i.getLeggings() != null && !i.getLeggings().getEnchantments().isEmpty()) ? true : ((i.getChestplate() != null && !i.getChestplate().getEnchantments().isEmpty()))));
  }
  
  private void onCubeOutRequest(CommandSender sender) {
    if (isPlayer(sender)) {
      Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
      while (var3.hasNext()) {
        DeathCube dc = var3.next();
        Player p = (Player)sender;
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
    if (!isPlayer(sender)) {
      sender.sendMessage(ChatColor.RED + "Only players can run this command!");
    } else {
      if (name != null) {
        DeathCube deathCube = findDeathCubeByString(name);
        if (deathCube == null) {
          sender.sendMessage(ChatColor.RED + "Cube not found. Try " + ChatColor.GOLD + "/dc list");
          return true;
        } 
      } 
      Player p = (Player)sender;
      boolean emptyInventory = true;
      String items = "";
      ItemStack[] contents = p.getInventory().getContents();
      ItemStack[] var12 = contents;
      int var11 = contents.length;
      for (int l = 0; l < var11; l++) {
        ItemStack i = var12[l];
        if (i != null) {
          if (emptyInventory)
            emptyInventory = false; 
          if (!i.getEnchantments().isEmpty())
            items = items + i.getType().name() + ", "; 
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
      if (p.getInventory().contains(91)) {
        sender.sendMessage(this.lang.get("hasJackos"));
        return false;
      } 
      if (p.getInventory().contains(Material.POTION))
        sender.sendMessage(this.lang.get("warningPotion")); 
      if (p.getInventory().contains(Material.ENDER_PEARL))
        sender.sendMessage(this.lang.get("warningEnderpearl")); 
      if (p.getGameMode() == GameMode.CREATIVE)
        sender.sendMessage(ChatColor.GRAY + "Your gamemode will be removed on start!"); 
      DeathCube dc = findDeathCubeBySender((CommandSender)p);
      if (dc != null) {
        if (dc.hasCorrectLocation) {
          if (!dc.gameRunning) {
            if (dc.spawn != null) {
              Location var14 = p.getLocation();
              if (dc.tpRange == 0 || checkPermissions((CommandSender)p, 2)) {
                if (!emptyInventory)
                  p.sendMessage(this.lang.get("inventoryWarning")); 
                dc.tpIn(p);
                if (!this.upToDate)
                  p.sendMessage(ChatColor.GRAY + "A new version is available. Type " + ChatColor.GOLD + "/dc i"); 
                return true;
              } 
              if (var14.getWorld().equals(dc.world)) {
                if (dc.contains(var14, dc.tpRange)) {
                  if (!emptyInventory)
                    p.sendMessage(this.lang.get("inventoryWarning")); 
                  dc.tpIn(p);
                  if (!this.upToDate)
                    p.sendMessage(ChatColor.GRAY + "Please update DeathCube. Type /dc i"); 
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
    DeathCube dc = this.cubeSelections.get(sender);
    if (dc != null) {
      Player p = (Player)sender;
      Location loc = p.getLocation();
      dc.setPos(pos, p.getLocation());
      if (!this.cubeNames.containsKey(dc.getName()))
        this.cubeNames.put(dc.getName(), dc); 
      p.sendMessage(ChatColor.GRAY + s + " position set to " + locationToString(loc));
      saveLocation("cubes." + dc.getName() + ".locations.loc" + pos, loc);
      if (checkPosition(dc, sender)) {
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
        loadConfig(dc);
        saveConfig();
      } else {
        dc.hasCorrectLocation = false;
      } 
    } else {
      sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
    } 
  }
  
  private boolean onCheckPositionRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      sender.sendMessage(ChatColor.AQUA + "First Position: " + ChatColor.GOLD + locationToString(dc.getPos(1)));
      sender.sendMessage(ChatColor.AQUA + "Second Position: " + ChatColor.GOLD + locationToString(dc.getPos(2)));
      checkPosition(dc, sender);
      return true;
    } 
    return false;
  }
  
  private void onOpenGateRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
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
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null && sender instanceof Player && !dc.gameRunning)
      dc.getBlockBar().load(sender); 
  }
  
  private void onBlockbarSetRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (sender instanceof Player) {
        if (!dc.gameRunning) {
          Player p = (Player)sender;
          int direction = (int)Math.floor(Math.abs(p.getLocation().getYaw()));
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
          for (int x = 0; x < 5; x++) {
            if (loc2.getBlock().getTypeId() != 0)
              hasSpace = false; 
            if (loc3.getBlock().getTypeId() != 0)
              hasSpace = false; 
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
            loc.getBlock().setTypeId(20);
            loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setTypeIdAndData(35, (byte)8, true);
            loc.add(vector).getBlock().setTypeIdAndData(35, (byte)14, true);
            loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setTypeIdAndData(35, (byte)14, true);
            loc.add(vector).getBlock().setTypeIdAndData(35, (byte)4, true);
            loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setTypeIdAndData(35, (byte)4, true);
            loc.add(vector).getBlock().setTypeIdAndData(35, (byte)11, true);
            loc.clone().add(0.0D, 1.0D, 0.0D).getBlock().setTypeIdAndData(35, (byte)11, true);
            saveConfig();
            loadConfig(dc);
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
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (sender instanceof Player) {
        Player p = (Player)sender;
        Location loc = p.getLocation().add(0.0D, 1.0D, 0.0D);
        dc.spawn = loc;
        this.config.set("cubes." + dc.getName() + ".locations.spawn.world", loc.getWorld().getUID().toString());
        this.config.set("cubes." + dc.getName() + ".locations.spawn.x", Integer.valueOf(loc.getBlockX()));
        this.config.set("cubes." + dc.getName() + ".locations.spawn.y", Integer.valueOf(loc.getBlockY()));
        this.config.set("cubes." + dc.getName() + ".locations.spawn.z", Integer.valueOf(loc.getBlockZ()));
        this.config.set("cubes." + dc.getName() + ".locations.spawn.yaw", Float.valueOf(loc.getYaw()));
        this.config.set("cubes." + dc.getName() + ".locations.spawn.pitch", Float.valueOf(loc.getPitch()));
        saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Spawn position set!");
      } else {
        sender.sendMessage(ChatColor.RED + "Only players can run this command!");
      } 
    } else {
      sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
    } 
  }
  
  private void onGetConfigurationRequest(CommandSender sender, String key) {
    DeathCube dc = findDeathCubeBySender(sender);
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
      DeathCube dc = findDeathCubeBySender(sender);
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
                int e = Integer.parseInt(value);
                this.config.set(path, Integer.valueOf(e));
              } catch (NumberFormatException var7) {
                sender.sendMessage(ChatColor.RED + "Please enter an integer for this value!");
                return false;
              } 
            } else if (this.config.isString(path)) {
              this.config.set(path, value.replaceAll("_", " "));
            } 
            loadConfig(dc);
            sender.sendMessage(ChatColor.GREEN + key + " was changed to " + ChatColor.GOLD + value);
            saveSettings();
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
    DeathCube dc = findDeathCubeBySender(sender);
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
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (!dc.hasCorrectLocation)
        sender.sendMessage(ChatColor.RED + "Invalid Location: " + dc.errorState); 
    } else {
      sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
    } 
  }
  
  private void onCubeRenewRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
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
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (dc.gameRunning) {
        dc.stopGame();
        onTunnelCreationRequest(sender);
        sender.sendMessage(ChatColor.GREEN + "Game stopped!");
      } else {
        sender.sendMessage(ChatColor.RED + "There is no game running.");
      } 
    } else {
      sender.sendMessage(ChatColor.GRAY + "No DeathCube selected. Type " + ChatColor.GOLD + "/dc select <name>");
    } 
  }
  
  private void onBurnStopRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (dc.isBurning) {
        getServer().getScheduler().cancelTask(dc.threadID);
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
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      if (dc.hasCorrectLocation) {
        if (dc.gameRunning) {
          if (!dc.isBurning) {
            dc.playerReachedBurnLimit((Player)null);
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
    boolean isUser = !checkPermissions(sender, 2);
    if (minutes != null && isUser) {
      sender.sendMessage(ChatColor.RED + "You are not allowed to use parameters bro.");
    } else {
      try {
        int e = -1;
        if (minutes != null)
          e = Integer.parseInt(minutes); 
        if (e == -1 || (e >= 0 && e <= 120)) {
          DeathCube dc = findDeathCubeBySender(sender);
          if (dc != null) {
            if (dc.hasCorrectLocation) {
              if (!dc.gameRunning) {
                if (e == 0) {
                  dc.startGame();
                } else if (e == -1) {
                  if (isUser) {
                    if (dc.countWaitingPlayers() >= dc.mPublicMinPlayers) {
                      int[] tempTime = dc.getGameManager().getTempTime();
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
                    int[] tempTime = dc.getGameManager().getTempTime();
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
                  int[] tempTime = dc.getGameManager().getTempTime();
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
      } catch (NumberFormatException var7) {
        sender.sendMessage(ChatColor.RED + "Give me an integer please!");
      } 
    } 
  }
  
  private void onCubeCreationRequest(CommandSender sender) {
    DeathCube dc = this.cubeSelections.get(sender);
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
        Iterator<Entity> bb = w.getEntities().iterator();
        while (bb.hasNext()) {
          Entity bedrockHeight = bb.next();
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
                w.getBlockAt(i, var17, j).setTypeId(7);
              } else if (var17 == 1) {
                if (random(0, 2) == 2) {
                  w.getBlockAt(i, var17, j).setTypeId(11);
                } else {
                  w.getBlockAt(i, var17, j).setTypeId(0);
                } 
              } else {
                w.getBlockAt(i, var17, j).setTypeId(0);
              } 
            } 
          } 
        } 
        for (var17 = 4; var17 < surface; var17++) {
          int i;
          for (i = minX; i < maxX + 1; i++) {
            w.getBlockAt(i, var17, minZ).setTypeId(1);
            w.getBlockAt(i, var17, maxZ).setTypeId(1);
          } 
          for (i = minZ; i < maxZ + 1; i++) {
            w.getBlockAt(minX, var17, i).setTypeId(1);
            w.getBlockAt(maxX, var17, i).setTypeId(1);
          } 
        } 
        byte var19 = 4;
        for (int var18 = 1; var18 <= var19; var18++) {
          int i;
          for (i = minX; i < maxX + 1; i++) {
            w.getBlockAt(i, var18, minZ).setTypeId(7);
            w.getBlockAt(i, var18, maxZ).setTypeId(7);
          } 
          for (i = minZ; i < maxZ + 1; i++) {
            w.getBlockAt(minX, var18, i).setTypeId(7);
            w.getBlockAt(maxX, var18, i).setTypeId(7);
          } 
        } 
        minZ++;
        maxZ--;
        minX++;
        maxX--;
        BlockBar var20 = dc.getBlockBar();
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
        dc.buildCube((CommandSender)null);
      } else {
        sender.sendMessage(ChatColor.RED + "Wrong Location: " + dc.errorState);
      } 
    } else {
      sender.sendMessage(ChatColor.RED + "Select a cube using '/dc select <name>'");
    } 
  }
  
  private void onGetTimerRequest(CommandSender sender) {
    DeathCube dc = findDeathCubeBySender(sender);
    if (dc != null) {
      sender.sendMessage(ChatColor.GREEN + "Timers for DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + ":");
      if (dc.gameRunning)
        sender.sendMessage(ChatColor.GRAY + "Currently there is a game " + ChatColor.GOLD + "running" + ChatColor.GRAY + "!"); 
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
      Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
      while (var3.hasNext()) {
        DeathCube dc = var3.next();
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
  
  private void onCubeDeletionRequest(CommandSender sender, String name) {
    DeathCube dc = findDeathCubeByString(name);
    if (dc != null) {
      if (!dc.gameRunning) {
        this.cubeNames.remove(dc.getName());
        this.config.set("cubes." + dc.getName(), null);
        saveConfig();
        sender.sendMessage(ChatColor.GREEN + "DeathCube " + ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " removed!");
      } else {
        sender.sendMessage(ChatColor.RED + "Stop the game first!");
      } 
    } else {
      sender.sendMessage(ChatColor.RED + "A cube with that name does not exist!");
    } 
  }
  
  private void onCubeDefaultRequest(CommandSender sender, String name) {
    DeathCube dc = findDeathCubeByString(name);
    if (dc != null) {
      this.defaultDC = dc;
      this.config.set("global.default", dc.getName());
      saveConfig();
      sender.sendMessage(ChatColor.GOLD + dc.getName() + ChatColor.GREEN + " is now your default cube!");
    } else {
      sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
    } 
  }
  
  private void onHelpRequest(CommandSender sender, String submenu) {
    if (checkPermissions(sender, 1)) {
      if (submenu != null) {
        if (!submenu.equalsIgnoreCase("i") && !submenu.equalsIgnoreCase("info")) {
          if (submenu.equalsIgnoreCase("c") || submenu.equalsIgnoreCase("commands")) {
            if (checkPermissions(sender, 2))
              sender.sendMessage(ChatColor.GRAY + "I will display the user commands for you. If you don't know the admin commands rtfm!"); 
            String[] var6;
            int var5 = (var6 = this.lang.get("helpCommands").split(";")).length;
            for (int var4 = 0; var4 < var5; var4++) {
              String var7 = var6[var4];
              sender.sendMessage(var7);
            } 
          } else if (!submenu.equalsIgnoreCase("tournament") && !submenu.equalsIgnoreCase("t")) {
            if (!submenu.equalsIgnoreCase("r") && !submenu.equalsIgnoreCase("rules")) {
              sender.sendMessage(ChatColor.GRAY + "Info page not found. Try " + ChatColor.GOLD + "info, commands or rules");
            } else {
              DeathCube line = findDeathCubeBySender(sender);
              if (line != null) {
                if (line.hasCorrectLocation) {
                  sender.sendMessage(ChatColor.GRAY + "* Its not allowed to take " + ChatColor.GOLD + "Jack O'Lanterns" + ChatColor.GRAY + " or " + ChatColor.GOLD + "enchanted items" + ChatColor.GRAY + " with you. " + ChatColor.GOLD + "Enderpearls" + ChatColor.GRAY + " and " + ChatColor.GOLD + "potions" + ChatColor.GRAY + " are blocked while playing");
                  if (line.kickOnCheat)
                    sender.sendMessage(ChatColor.GRAY + "* If you try to place any block while playing you will be kicked"); 
                  if (line.pkinStart > 0 || line.pkinProbability > 0)
                    sender.sendMessage(ChatColor.GRAY + "* You can destroy " + ChatColor.GOLD + "Jack O'Lanterns" + ChatColor.GRAY + " to receive a joker. Then you can replace it " + ChatColor.GOLD + "one time" + ChatColor.GRAY); 
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
        String[] var6;
        int var5 = (var6 = this.lang.get("helpIndex").split(";")).length;
        for (int var4 = 0; var4 < var5; var4++) {
          String var7 = var6[var4];
          sender.sendMessage(var7);
        } 
      } 
    } else {
      sender.sendMessage(ChatColor.RED + "First you should tell your admin that you need " + ChatColor.GOLD + "deathcube.use" + ChatColor.RED + " permissions ;)");
    } 
  }
  
  private void onSelectionRequest(CommandSender sender, String name) {
    if (this.cubeNames.containsKey(name)) {
      DeathCube newDC = findDeathCubeByString(name);
      if (newDC != null) {
        this.cubeSelections.put(sender, newDC);
        sender.sendMessage(ChatColor.GRAY + "Selection changed to " + ChatColor.GOLD + name);
      } else {
        sender.sendMessage(ChatColor.RED + "Cube not found. Use /dc list");
      } 
    } else if (checkPermissions(sender, 3)) {
      if (!name.equalsIgnoreCase("protectionHolder")) {
        sender.sendMessage(ChatColor.GRAY + "The selected Cube does not exist yet!");
        sender.sendMessage(ChatColor.GRAY + "It will be created as soon as you set the first position.");
        DeathCube newDC = new DeathCube(name, this);
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
    getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
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
  public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Location loc = event.getPlayer().getLocation();
      if (dc.hasCorrectLocation && dc.contains(loc, 1)) {
        if (dc.contains(loc, -9)) {
          Item item = event.getItem();
          if (item.getItemStack().getTypeId() != 91)
            event.setCancelled(true); 
          continue;
        } 
        event.setCancelled(true);
      } 
    } 
  }
  
  @EventHandler
  public void onPlayeyDropItem(PlayerDropItemEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      if (dc.hasCorrectLocation && dc.contains(event.getItemDrop().getLocation(), dc.dropProtection)) {
        Player p = event.getPlayer();
        if (dc.contains(event.getItemDrop().getLocation(), -9) && event.getItemDrop().getItemStack().getTypeId() == 91) {
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
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Player p = event.getPlayer();
      if (dc.contains(p.getLocation(), -2)) {
        if (!checkPermissions((CommandSender)p, 2)) {
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
    if (event.getBlock().getType() == Material.REDSTONE_LAMP_ON) {
      Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
      while (var3.hasNext()) {
        DeathCube dc = var3.next();
        if (dc.activeRedstone && dc.hasCorrectLocation && dc.contains(event.getBlock().getLocation())) {
          event.setNewCurrent(15);
          break;
        } 
      } 
    } 
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      if (dc.hasCorrectLocation) {
        if (!dc.getBlockBar().contains(event.getBlockPlaced().getLocation()) || checkPermissions((CommandSender)event.getPlayer(), 2)) {
          if (dc.contains(event.getBlockPlaced().getLocation())) {
            Player p = event.getPlayer();
            if (!dc.isActive(p)) {
              if (!checkPermissions((CommandSender)p, 2)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(this.lang.get("innerProtection"));
              } 
              break;
            } 
            if (event.getBlock().getTypeId() == 91) {
              Location loc = event.getBlockPlaced().getLocation();
              if (dc.contains(loc, -8) && loc.getBlockY() >= dc.startHeight && loc.getBlockY() <= dc.height) {
                event.getBlockPlaced().setTypeIdAndData(86, (byte)random(0, 3), true);
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
  public void onPlayerMove(PlayerMoveEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Player p = event.getPlayer();
      if (dc.hasCorrectLocation && dc.gameRunning) {
        if (dc.isActive(p)) {
          if (!dc.contains(p.getLocation(), -4)) {
            Location loc = p.getLocation();
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
          if (p.getLocation().getBlockY() >= dc.height + 1)
            dc.playerReachedTop(p); 
          break;
        } 
        if (dc.contains(p.getLocation(), -2)) {
          if (!checkPermissions((CommandSender)p, 2)) {
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
    if (event.getEntity() instanceof org.bukkit.entity.HumanEntity && event.getDamager().getType() == EntityType.ENDER_PEARL) {
      Player p = (Player)event.getEntity();
      Iterator<DeathCube> var4 = this.cubeNames.values().iterator();
      while (var4.hasNext()) {
        DeathCube dc = var4.next();
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
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      final DeathCube dc = var3.next();
      final Player p = event.getPlayer();
      if (dc.waiting.contains(p.getName())) {
        dc.waiting.remove(p.getName());
        getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
              public void run() {
                dc.tpToSpawn(p);
              }
            },  10L);
      } 
    } 
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      final DeathCube dc = var3.next();
      final Player p = event.getPlayer();
      if (dc.hasCorrectLocation && dc.contains(p.getLocation(), -2)) {
        getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
              public void run() {
                dc.tpToSpawn(p);
                p.sendMessage(DeathCubeManager.this.lang.get("removeOnSpawn"));
              }
            },  10L);
        break;
      } 
    } 
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Player p = event.getPlayer();
      if (dc.waiting.contains(p.getName()))
        dc.waiting.remove(p.getName()); 
      if (dc.isActive(p)) {
        dc.playerLeft(p, this.lang.get("leaveDisconnectA"), "");
        break;
      } 
    } 
    if (this.cubeSelections.containsKey(event.getPlayer()))
      this.cubeSelections.remove(event.getPlayer()); 
  }
  
  @EventHandler
  public void onPlayerItemChange(PlayerItemHeldEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
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
    if (isPlayer(sender)) {
      Player p = (Player)sender;
      Iterator<DeathCube> var4 = this.cubeNames.values().iterator();
      while (var4.hasNext()) {
        DeathCube dc = var4.next();
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
  public void onPlayerDeath(PlayerDeathEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Player p = event.getEntity();
      if (dc.playerDied(p)) {
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage("");
        if (!dc.waiting.contains(p.getName()))
          dc.waiting.add(p.getName()); 
        break;
      } 
    } 
  }
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      Location loc = event.getBlock().getLocation();
      if (dc.hasCorrectLocation) {
        if (dc.getBlockBar().contains(loc) && !checkPermissions((CommandSender)event.getPlayer(), 2)) {
          event.getPlayer().sendMessage(this.lang.get("barProtection"));
          event.setCancelled(true);
          continue;
        } 
        if (dc.contains(loc)) {
          Player p = event.getPlayer();
          if (dc.contains(loc, -8)) {
            if (event.getBlock().getTypeId() == 91 && checkPermissions((CommandSender)p, 1)) {
              if (!dc.isActive(p)) {
                if (!checkPermissions((CommandSender)p, 3)) {
                  event.setCancelled(true);
                  p.sendMessage(this.lang.get("jokerOffline"));
                } 
                break;
              } 
              p.sendMessage(this.lang.get("jokerOnline"));
              break;
            } 
            if (!checkPermissions((CommandSender)p, 3)) {
              event.setCancelled(true);
              p.sendMessage(this.lang.get("innerProtection"));
            } 
            break;
          } 
          if (!checkPermissions((CommandSender)p, 2)) {
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
    Iterator<DeathCube> var3 = this.cubeNames.values().iterator();
    while (var3.hasNext()) {
      DeathCube dc = var3.next();
      if (dc.hasCorrectLocation && dc.contains(event.getLocation()))
        event.setCancelled(true); 
    } 
  }
  
  public void onEnable() {
    this.config = getConfig();
    if (!this.configPath.exists()) {
      try {
        this.config.load(this.configPath);
      } catch (Exception var3) {
        o("config.yml generated or error while reading!");
        saveSettings();
      } 
    } else {
      try {
        this.config.load(this.configPath);
      } catch (Exception var2) {
        o("Error while reading reading from config.yml!");
      } 
    } 
    loadDeathCubes();
    this.lang = new Language(this);
    getServer().getPluginManager().registerEvents(this, (Plugin)this);
    System.out.println("DeathCube v2 enabled!");
  }
  
  private void loadDeathCubes() {
    if (this.config.contains("pos")) {
      this.notUpdated = true;
      World dc = getServer().getWorld(UUID.fromString(this.config.getString("pos.world")));
      if (dc != null) {
        Location s = new Location(dc, this.config.getInt("pos.1.x"), this.config.getInt("pos.1.y"), this.config.getInt("pos.1.z"));
        Location pos2 = new Location(dc, this.config.getInt("pos.2.x"), this.config.getInt("pos.2.y"), this.config.getInt("pos.2.z"));
        if (s != null && pos2 != null) {
          DeathCube dct = new DeathCube("protectionHolder", this);
          saveLocation("cubes." + dct.getName() + ".locations.loc1", s);
          saveLocation("cubes." + dct.getName() + ".locations.loc2", pos2);
        } 
      } 
    } 
    DeathCube var6 = findDeathCubeByString(getString("global.default", ""));
    if (var6 != null)
      this.defaultDC = var6; 
    this.allowedCommands.clear();
    this.allowedCommands.add("dc");
    String[] var5;
    int var10 = (var5 = getString("global.allowedCommands", "who,list").split(",")).length;
    for (int var8 = 0; var8 < var10; var8++) {
      String var7 = var5[var8];
      this.allowedCommands.add(var7);
    } 
    if (!this.config.contains("global.autoUpdate"))
      this.config.set("global.autoUpdate", Boolean.valueOf(true)); 
    this.registrator = new RegistrationManager(this, getBoolean("global.allowRegistration", true));
    if (this.config.contains("cubes")) {
      Iterator<String> var9 = this.config.getConfigurationSection("cubes").getKeys(false).iterator();
      while (var9.hasNext()) {
        String var7 = var9.next();
        o("Reading DeathCube \"" + var7 + "\"");
        DeathCube dct = new DeathCube(var7, this);
        loadConfig(dct);
      } 
    } 
    saveConfig();
    checkVersion();
  }
  
  private void loadConfig(DeathCube dc) {
    BlockBar var12;
    String path = "cubes." + dc.getName() + ".";
    Location pos1 = readLocation(path + "locations.loc1");
    Location pos2 = readLocation(path + "locations.loc2");
    String errorMessage = "";
    if (pos1 != null) {
      if (pos2 != null) {
        if (pos1.getWorld() != null && pos2.getWorld() != null) {
          if (pos1.getWorld().equals(pos2.getWorld())) {
            int bb = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
            int x = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
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
      o(dc.getName() + " is incorrect: " + errorMessage);
    } 
    dc.setPos(1, pos1);
    dc.setPos(2, pos2);
    dc.density = getInt(path + "density", 4);
    dc.stageLevel = getInt(path + "stageLevel", 15);
    dc.startHeight = getInt(path + "startHeight", 10);
    dc.height = getInt(path + "height", 100);
    if (dc.height > 254)
      dc.height = 254; 
    dc.tpRange = getInt(path + "tpRange", 20);
    dc.pkinProbability = getInt(path + "pkinProbability", 400);
    dc.pkinStart = getInt(path + "pkinStart", 0);
    dc.kickOnCheat = getBoolean(path + "kickOnCheat", true);
    dc.burnLevel = getInt(path + "burnLevel", 40);
    dc.burnSpeed = getInt(path + "burnSpeed", 20);
    dc.burnChance = getInt(path + "burnChance", 1);
    dc.burnExpand = getInt(path + "burnExpand", 3);
    dc.posTower = getBoolean(path + "posTower", true);
    dc.posTowerRate = getLong(path + "posTowerRate", 100L);
    dc.posTowerId = getInt(path + "posTowerId", 123);
    dc.broadcast = getBoolean(path + "broadcast", true);
    dc.offlineFeed = getBoolean(path + "offlineFeed", true);
    dc.autoRemove = getBoolean(path + "autoRemove", false);
    dc.openAfterGame = getBoolean(path + "openAfterGame", false);
    dc.timeLimit = getInt(path + "timeLimit", 15);
    dc.userStart = getBoolean(path + "userStart", false);
    dc.dropProtection = getInt(path + "dropProtection", 5);
    dc.looseOnFall = getBoolean(path + "looseOnFall", false);
    dc.activeRedstone = getBoolean(path + "activeRedstone", false);
    dc.advertiseTournament = getBoolean(path + "advertiseTournament", true);
    dc.stageWidth = getInt(path + "stageWidth", 1);
    loadWinCommands(path + "winCommands", dc.winCommands);
    dc.mTimedMinPlayers = getInt(path + "mTimedMinPlayers", 1);
    loadWinCommands(path + "mTimedWinCommands", dc.tWinCommands);
    dc.mPublicMinPlayers = getInt(path + "mPublicMinPlayers", 1);
    loadWinCommands(path + "mPublicWinCommands", dc.pWinCommands);
    if (dc.mTimedMinPlayers <= 0)
      dc.mTimedMinPlayers = 1; 
    if (dc.mPublicMinPlayers <= 0)
      dc.mPublicMinPlayers = 1; 
    if (dc.stageLevel <= 0)
      dc.stageLevel = 1; 
    dc.winCommands.clear();
    if (this.config.contains("cubes." + dc.getName() + ".winCommands")) {
      String var11 = this.config.getString("cubes." + dc.getName() + ".winCommands");
      if (var11.contains(";")) {
        String[] w;
        int z = (w = var11.split(";")).length;
        for (int y = 0; y < z; y++) {
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
    if (this.config.contains(path + ".blockbar")) {
      Location var14 = new Location(getServer().getWorld(UUID.fromString(this.config.getString(path + ".blockbar.world"))), this.config.getInt(path + ".blockbar.x"), this.config.getInt(path + ".blockbar.y"), this.config.getInt(path + ".blockbar.z"));
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
      int x = this.config.getInt(path + "spawn.x");
      int y = this.config.getInt(path + "spawn.y");
      int z = this.config.getInt(path + "spawn.z");
      World var16 = getServer().getWorld(UUID.fromString(this.config.getString(path + "spawn.world")));
      if (var16 != null)
        dc.spawn = new Location(var16, x, y, z, (float)this.config.getLong(path + "spawn.yaw"), (float)this.config.getLong(path + "spawn.pitch")); 
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
        for (int var5 = 0; var5 < var6; var5++) {
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
    if (this.config.contains(path))
      return this.config.getLong(path); 
    this.config.set(path, Long.valueOf(value));
    return value;
  }
  
  private int getInt(String path, int value) {
    if (this.config.contains(path))
      return this.config.getInt(path); 
    this.config.set(path, Integer.valueOf(value));
    return value;
  }
  
  private boolean getBoolean(String path, boolean value) {
    if (this.config.contains(path))
      return this.config.getBoolean(path); 
    this.config.set(path, Boolean.valueOf(value));
    return value;
  }
  
  private String getString(String path, String value) {
    if (this.config.contains(path))
      return this.config.getString(path); 
    this.config.set(path, value);
    return value;
  }
  
  private void saveSettings() {
    try {
      this.config.save(this.configPath);
    } catch (IOException var2) {
      System.out.println("Unknown Exception while saving config File!");
    } 
  }
  
  private boolean downloadFile(String link, String localFile, CommandSender sender) {
    try {
      URL e = new URL(link);
      URLConnection conn = e.openConnection();
      BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(localFile));
      byte[] chunk = new byte[1024];
      int chunkSize;
      while ((chunkSize = is.read(chunk)) != -1)
        os.write(chunk, 0, chunkSize); 
      os.flush();
      os.close();
      is.close();
      return true;
    } catch (IOException var10) {
      o("Error while downloading File!");
      return false;
    } 
  }
  
  private void checkVersion() {
    getServer().getScheduler().scheduleAsyncDelayedTask((Plugin)this, new Runnable() {
          public void run() {
            try {
              URL e1 = new URL("http://dev.bukkit.org/server-mods/deathcube/files/");
              URLConnection yc = e1.openConnection();
              BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
              String inputLine = "";
              while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("col-file\"")) {
                  String link = inputLine.split("deathcube/files/")[1].split("/")[0];
                  String newestVersion = inputLine.split("DeathCube ")[1].split("<")[0];
                  link = "http://dev.bukkit.org/server-mods/deathcube/files/" + link + "/";
                  if (!newestVersion.equals(DeathCubeManager.this.version)) {
                    DeathCubeManager.this.upToDate = false;
                    System.out.println("DeathCube is not UpToDate. Please update!");
                    if (!DeathCubeManager.this.config.getBoolean("global.autoUpdate"))
                      break; 
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
            } catch (IOException var8) {
              System.out.println("Unkown error while looking for updates.");
            } 
          }
        }10L);
  }
  
  private void fileDownload(String link, String localFile) {
    try {
      URL e = new URL(link);
      URLConnection conn = e.openConnection();
      BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(localFile));
      byte[] chunk = new byte[1024];
      int chunkSize;
      while ((chunkSize = is.read(chunk)) != -1)
        os.write(chunk, 0, chunkSize); 
      os.flush();
      os.close();
      is.close();
    } catch (IOException var9) {
      o("Error while downloading! Try it manually.");
      var9.printStackTrace();
    } 
  }
  
  private String locationToString(Location loc) {
    return (loc != null) ? (loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " in '" + loc.getWorld().getName() + "'") : "Location not set!";
  }
  
  private int random(int from, int to) {
    to++;
    return (int)(Math.random() * (to - from) + from);
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
