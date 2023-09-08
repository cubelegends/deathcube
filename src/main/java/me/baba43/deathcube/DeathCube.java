package me.baba43.deathcube;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class DeathCube {
    private Location pos1;

    private Location pos2;

    private BlockBar bb;

    private final String name;

    private final DeathCubeManager plugin;

    private final GameManager gm;

    private int tpLocation = 0;

    public int minX;

    public int minZ;

    public int maxX;

    public int maxZ;

    public int minY;

    public int maxY;

    public World world;

    public int stageWidth;

    public Material posTowerId;

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

    public ArrayList<Player> activePlayers = new ArrayList<>();

    public ArrayList<Player> retiredPlayers = new ArrayList<>();

    public ArrayList<String> waiting = new ArrayList<>();

    public ArrayList<String> winCommands = new ArrayList<>();

    public ArrayList<String> pWinCommands = new ArrayList<>();

    public ArrayList<String> tWinCommands = new ArrayList<>();

    private final ArrayList<Integer> burnLevels = new ArrayList<>();

    public DeathCube(final String name, final DeathCubeManager plugin) {
        this.name = name;
        this.plugin = plugin;
        this.gm = new GameManager(this);
    }

    public void setBlockBar(final BlockBar bb) {
        this.bb = bb;
    }

    public BlockBar getBlockBar() {
        return this.bb;
    }

    public void saveSettings(final FileConfiguration config) {
    }

    public String getName() {
        return this.name;
    }

    public Location getPos(final int a) {
        return (a == 1) ? this.pos1 : this.pos2;
    }

    public void setPos(final int a, final Location loc) {
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
        final String time = this.elapsedTimeToString((new Date()).getTime() - this.startTime);
        this.broadcast(this.gl("roundTime").replaceFirst("%time%", time));
        this.broadcast(this.gl("privateStop"));
        final Iterator<Player> var3 = this.activePlayers.iterator();
        while (var3.hasNext()) {
            final Player p = var3.next();
            if (this.autoRemove) {
                this.tpToSpawn(p);
                continue;
            }
            p.getInventory().remove(Material.JACK_O_LANTERN);
            p.sendMessage(this.gl("howToLeave"));
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
        final boolean seconds = false;
        for (time /= 1000L; time > 60L && minutes < 61; time -= 60L) {
            minutes++;
        }
        if (minutes < 61) {
            final int var5 = (int) time;
            return this.gl("timeString").replaceFirst("%minutes%", "" + minutes).replaceFirst("%seconds%", "" + var5);
        }
        return this.gl("timeTooLong");
    }

    public boolean tpIn(final Player p) {
        final Location target;
        if (this.tpLocation == 3) {
            this.tpLocation = 0;
        } else {
            this.tpLocation++;
        }
        switch (this.tpLocation) {
            case 0:
                target = new Location(this.world, (this.minX + 2), (this.stageLevel + 1), (this.minZ + (this.maxZ - this.minZ) / 2 + 1), -90.0F, -10.0F);
                break;
            case 1:
                target = new Location(this.world, (this.minX + (this.maxX - this.minX) / 2 + 1), (this.stageLevel + 1), (this.minZ + 2), 0.0F, -10.0F);
                break;
            case 2:
                target = new Location(this.world, (this.maxX - 2), (this.stageLevel + 1), (this.minZ + (this.maxZ - this.minZ) / 2 + 1), 90.0F, -10.0F);
                break;
            default:
                target = new Location(this.world, (this.minX + (this.maxX - this.minX) / 2 + 1), (this.stageLevel + 1), (this.maxZ - 2), 180.0F, -10.0F);
                break;
        }
        if (target.getBlock().getType() == Material.AIR && this.world.getBlockAt(target.getBlockX(), target.getBlockY() + 1, target.getBlockZ()).getType() == Material.AIR) {
            if (this.world.getBlockAt(target.getBlockX(), target.getBlockY() - 1, target.getBlockZ()).getType() != Material.AIR) {
                p.setNoDamageTicks(20);
                p.teleport(target);
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

    public void playerReachedBurnLimit(final Player p) {
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
            this.burnLevels.add(Integer.valueOf(this.startHeight));
            if (this.threadBurn != -1) {
                this.plugin.getServer().getScheduler().cancelTask(this.threadBurn);
                this.threadBurn = -1;
            }
            if (this.threadID != -1) {
                this.plugin.getServer().getScheduler().cancelTask(this.threadID);
            }
            this.threadID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this.plugin, new DeathTick(this), 10L, this.burnSpeed);
            this.destroyTunnel();
        }
    }

    public void startGame(final Player mod) {
        this.startGame();
    }

    private boolean controlPlayer(final Player p) {
        if (!p.getActivePotionEffects().isEmpty()) {
            p.sendMessage(ChatColor.GRAY + "Bad boy = sad boy!");
            this.tpToSpawn(p);
            return false;
        }
        return true;
    }

    public void startGame() {
        if (this.hasCorrectLocation && !this.gameRunning && this.spawn != null) {
            Iterator<Player> timer = this.world.getPlayers().iterator();
            while (true) {
                if (!timer.hasNext()) {
                    if (this.activePlayers.size() > 0) {
                        final String var6;
                        if (this.broadcast) {
                            final Collection<? extends Player> var4 = this.plugin.getServer().getOnlinePlayers();
                            for (final Player message2 : var4) {
                                if (!this.activePlayers.contains(message2)) {
                                    message2.sendMessage(this.gl("publicStart"));
                                }
                            }
                        }
                        timer = this.world.getPlayers().iterator();
                        while (timer.hasNext()) {
                            final Entity var5 = timer.next();
                            if (this.contains(var5.getLocation())) {
                                if (!(var5 instanceof Player)) {
                                    var5.remove();
                                    continue;
                                }
                                final Player var10 = (Player) var5;
                                if (!this.activePlayers.contains(var10)) {
                                    if (this.plugin.checkPermissions((CommandSender) var10, 2)) {
                                        var10.sendMessage(this.gl("spec1"));
                                        var10.sendMessage(this.gl("spec2"));
                                        this.retiredPlayers.add(var10);
                                        continue;
                                    }
                                    this.tpToSpawn(var10);
                                    var10.sendMessage(this.gl("onRemove"));
                                }
                            }
                        }
                        if (this.activePlayers.size() == 1) {
                            var6 = this.gl("playingAllone");
                        } else if (this.activePlayers.size() == 2) {
                            var6 = this.gl("playingOne");
                        } else {
                            var6 = this.gl("playingMore").replaceFirst("%count%", "" + this.activePlayers.size());
                        }
                        final Iterator<Player> var12 = this.activePlayers.iterator();
                        while (var12.hasNext()) {
                            final Player var9 = var12.next();
                            var9.sendMessage(var6);
                            var9.getInventory().remove(Material.JACK_O_LANTERN);
                            if (this.pkinProbability > 0 || this.pkinStart > 0) {
                                if (this.pkinStart > 0) {
                                    var9.getInventory().addItem(new ItemStack[]{new ItemStack(Material.JACK_O_LANTERN, this.pkinStart)});
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
                            this.threadBurn = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this.plugin, new Runnable() {
                                @Override
                                public void run() {
                                    DeathCube.this.timeLimitOver();
                                }
                            }, (this.timeLimit * 60) * 20L);
                        }
                        if (this.threadID2 == -1) {
                            this.plugin.getServer().getScheduler().cancelTask(this.threadID2);
                        }
                        if (this.posTower && this.threadID2 == -1) {
                            final PositionTimer var11 = new PositionTimer(this.world, this.minX, this.maxX, this.startHeight, this.height, this.minZ, this.maxZ, this.activePlayers, this.posTowerId);
                            this.threadID2 = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this.plugin, var11, this.posTowerRate, 50L);
                            return;
                        }
                    }
                    return;
                }
                final Player message = (Player) timer.next();
                final Location p = message.getLocation();
                if (p.getBlockY() == this.stageLevel + 1 || p.getBlockY() == this.stageLevel + 2) {
                    if (this.contains(p, -1) && !this.contains(p, -4) && this.controlPlayer(message)) {
                        this.activePlayers.add(message);
                        message.sendMessage(this.plugin.lang.get("privateStart"));
                    }
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

    private void broadcast(final String text) {
        Iterator<Player> var3 = this.activePlayers.iterator();
        while (var3.hasNext()) {
            final Player p = var3.next();
            p.sendMessage(text);
        }
        var3 = this.retiredPlayers.iterator();
        while (var3.hasNext()) {
            final Player p = var3.next();
            p.sendMessage(text);
        }
    }

    private void broadcastServer(final String text) {
    }

    public void openGates() {
        this.isOpened = true;
        final int mitteX = this.minX + (this.maxX - this.minX) / 2 - this.stageWidth + 1;
        final int mitteZ = this.minZ + (this.maxZ - this.minZ) / 2 - this.stageWidth + 1;
        final int height = this.stageLevel + 1;
        int i;
        for (i = 0; i < this.stageWidth * 2; i++) {
            this.world.getBlockAt(mitteX + i, height, this.minZ + 3).setType(Material.AIR);
            this.world.getBlockAt(mitteX + i, height, this.maxZ - 3).setType(Material.AIR);
            for (int e = 4; e < 7; e++) {
                this.bb.setFloorBlock(this.world.getBlockAt(mitteX + i, this.stageLevel, this.minZ + e));
                this.bb.setFloorBlock(this.world.getBlockAt(mitteX + i, this.stageLevel, this.maxZ - e));
            }
        }
        for (i = 0; i < this.stageWidth * 2; i++) {
            this.world.getBlockAt(this.minX + 3, height, mitteZ + i).setType(Material.AIR);
            this.world.getBlockAt(this.maxX - 3, height, mitteZ + i).setType(Material.AIR);
            for (int e = 4; e < 7; e++) {
                this.bb.setFloorBlock(this.world.getBlockAt(this.minX + e, this.stageLevel, mitteZ + i));
                this.bb.setFloorBlock(this.world.getBlockAt(this.maxX - e, this.stageLevel, mitteZ + i));
            }
        }
    }

    public boolean playerDied(final Player p) {
        if (this.gameRunning && this.activePlayers.contains(p)) {
            this.playerLeft(p, this.gl("leaveDeathA"), this.gl("leaveDeathP"));
            return true;
        }
        return false;
    }

    public void tpToSpawn(final Player p) {
        if (this.spawn != null) {
            p.getInventory().remove(Material.JACK_O_LANTERN);
            p.teleport(this.spawn);
        }
    }

    public void playerReachedTop(final Player p) {
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

    public void playerLeft(final Player p, final String broadcast, final String personal) {
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

    private void runWinCommand(final Player winner) {
        if (winner != null) {
            final ConsoleCommandSender p = this.plugin.getServer().getConsoleSender();
            ArrayList<String> winCommands = null;
            if (this.mode == 0) {
                winCommands = this.winCommands;
            } else if (this.mode == 1) {
                winCommands = this.pWinCommands;
            } else {
                winCommands = this.tWinCommands;
            }
            final Iterator<String> var5 = winCommands.iterator();
            while (var5.hasNext()) {
                final String rawCommand = var5.next();
                final String command = rawCommand.replace("%name%", winner.getName());
                this.plugin.getServer().dispatchCommand((CommandSender) p, command);
            }
        }
        if (this.broadcast) {
            final Collection<? extends Player> var10 = this.plugin.getServer().getOnlinePlayers();
            for (final Player var7 : var10) {
                if (!this.activePlayers.contains(var7) && !this.retiredPlayers.contains(var7)) {
                    var7.sendMessage(this.gl("publicStop").replaceFirst("%name%", winner.getName()));
                }
            }
        }
    }

    private void playerWins() {
        final Player p = this.activePlayers.get(0);
        this.broadcast(this.gl("playerWon").replaceFirst("%name%", p.getName()));
        this.runWinCommand(p);
        this.endGame();
    }

    private void removeEntities() {
        final Iterator<Entity> var2 = this.world.getEntities().iterator();
        while (var2.hasNext()) {
            final Entity ente = var2.next();
            if (ente.getLocation().getBlockX() >= this.minX && ente.getLocation().getBlockX() <= this.maxX && ente.getLocation().getBlockZ() >= this.minZ && ente.getLocation().getBlockZ() <= this.maxZ && !(ente instanceof org.bukkit.entity.HumanEntity)) {
                ente.remove();
            }
        }
    }

    public int countWaitingPlayers() {
        int players = 0;
        final Iterator<Player> var3 = this.world.getPlayers().iterator();
        while (true) {
            if (!var3.hasNext()) {
                return players;
            }
            final Player p = var3.next();
            final Location loc = p.getLocation();
            if (loc.getBlockY() == this.stageLevel + 1 || loc.getBlockY() == this.stageLevel + 2) {
                if (this.contains(loc, -1) && !this.contains(loc, -4)) {
                    players++;
                }
            }
        }
    }

    public boolean contains(final Location loc) {
        return (loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX && loc.getBlockX() <= this.maxX && loc.getBlockZ() >= this.minZ && loc.getBlockZ() <= this.maxZ);
    }

    public boolean contains(final Location loc, final int variance) {
        return (loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX - variance && loc.getBlockX() <= this.maxX + variance && loc.getBlockZ() >= this.minZ - variance && loc.getBlockZ() <= this.maxZ + variance);
    }

    public boolean contains(final Location loc, final int variance, final int from, final int to) {
        return (loc.getWorld() == this.pos1.getWorld() && loc.getBlockX() >= this.minX - variance && loc.getBlockX() <= this.maxX + variance && loc.getBlockZ() >= this.minZ - variance && loc.getBlockZ() <= this.maxZ + variance && loc.getBlockY() >= from - variance && loc.getBlockY() <= to + variance);
    }

    public boolean isActive(final Player p) {
        return this.gameRunning ? this.activePlayers.contains(p) : false;
    }

    public void clearCube() {
        for (int h = this.startHeight; h <= 255; h++) {
            for (int i = this.minX + 5; i < this.maxX - 4; i++) {
                for (int e = this.minZ + 5; e < this.maxZ - 4; e++) {
                    this.world.getBlockAt(i, h, e).setType(Material.AIR);
                }
            }
        }
    }

    public void destroyCube() {
        for (int h = this.startHeight; h <= 255; h++) {
            for (int i = this.minX + 5; i < this.maxX - 4; i++) {
                for (int e = this.minZ + 5; e < this.maxZ - 4; e++) {
                    this.world.getBlockAt(i, h, e).setType(Material.AIR);
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
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this.plugin, new Runnable() {
            @Override
            public void run() {
                final long start = (new Date()).getTime();
                final boolean pkin = (DeathCube.this.pkinProbability != 0);
                for (int now = DeathCube.this.startHeight; now <= cubeHeight; now++) {
                    for (int i = minX + 5; i < maxX - 4; i++) {
                        for (int e = minZ + 5; e < maxZ - 4; e++) {
                            if (DeathCube.this.random(0, DeathCube.this.density) == 0) {
                                if (DeathCube.this.pkinProbability != 0 && DeathCube.this.random(0, DeathCube.this.pkinProbability) == 0) {
                                    w.getBlockAt(i, now, e).setType(Material.JACK_O_LANTERN, true);
                                    // Todo: Set random facing
                                } else {
                                    DeathCube.this.bb.setCubeBlock(w.getBlockAt(i, now, e));
                                }
                            } else {
                                w.getBlockAt(i, now, e).setType(Material.AIR);
                            }
                        }
                    }
                }
                if (sender != null) {
                    final long var7 = (new Date()).getTime();
                    sender.sendMessage(ChatColor.GREEN + "Cube regenerated in " + ChatColor.GOLD + (var7 - start) + "ms" + ChatColor.GREEN + "!");
                }
                DeathCube.this.removeEntities();
            }
        });
    }

    public void buildTunnel() {
        this.isOpened = false;
        final int tunnelPosition = this.stageLevel;
        final World w = this.world;
        int minX = Math.min(this.pos1.getBlockX(), this.pos2.getBlockX()) + 2;
        int minZ = Math.min(this.pos1.getBlockZ(), this.pos2.getBlockZ()) + 2;
        int maxX = Math.max(this.pos1.getBlockX(), this.pos2.getBlockX()) - 2;
        int maxZ = Math.max(this.pos1.getBlockZ(), this.pos2.getBlockZ()) - 2;
        int height;
        for (height = minX + 1; height < maxX; height++) {
            for (int j = minZ; j < maxZ + 1; j++) {
                this.bb.setFloorBlock(w.getBlockAt(height, tunnelPosition, minZ));
                this.bb.setFloorBlock(w.getBlockAt(height, tunnelPosition, maxZ));
                this.bb.setFloorBlock(w.getBlockAt(minX, tunnelPosition, j));
                this.bb.setFloorBlock(w.getBlockAt(maxX, tunnelPosition, j));
            }
        }
        height = tunnelPosition + 3;
        int mitteX;
        for (mitteX = minX + 1; mitteX < maxX; mitteX++) {
            for (int j = minZ; j < maxZ + 1; j++) {
                w.getBlockAt(mitteX, height, minZ).setType(Material.GLASS);
                w.getBlockAt(mitteX, height, maxZ).setType(Material.GLASS);
                w.getBlockAt(minX, height, j).setType(Material.GLASS);
                w.getBlockAt(maxX, height, j).setType(Material.GLASS);
            }
        }
        maxX--;
        minX++;
        maxZ--;
        minZ++;
        height = tunnelPosition + 1;
        mitteX = minX + (maxX - minX) / 2;
        final int mitteZ = minZ + (maxZ - minZ) / 2;
        for (int i = minX + 1; i < maxX; i++) {
            for (int e = minZ; e < maxZ + 1; e++) {
                if (i >= mitteX - this.stageWidth && i <= mitteX + 1 + this.stageWidth) {
                    this.bb.setFloorBlock(w.getBlockAt(i, height - 1, minZ));
                    this.bb.setFloorBlock(w.getBlockAt(i, height - 1, maxZ));
                }
                if (i >= mitteX - this.stageWidth + 1 && i <= mitteX + this.stageWidth) {
                    w.getBlockAt(i, height, minZ).setType(Material.GLASS_PANE);
                    w.getBlockAt(i, height, maxZ).setType(Material.GLASS_PANE);
                    w.getBlockAt(i, height, minZ).setBlockData(this.connectGlassPanes((MultipleFacing) w.getBlockAt(i, height, minZ).getBlockData(), false));
                    w.getBlockAt(i, height, maxZ).setBlockData(this.connectGlassPanes((MultipleFacing) w.getBlockAt(i, height, maxZ).getBlockData(), false));
                } else {
                    w.getBlockAt(i, height, minZ).setType(Material.GLASS);
                    w.getBlockAt(i, height, maxZ).setType(Material.GLASS);
                }
                if (e >= mitteZ - this.stageWidth && e <= mitteZ + 1 + this.stageWidth) {
                    this.bb.setFloorBlock(w.getBlockAt(minX, height - 1, e));
                    this.bb.setFloorBlock(w.getBlockAt(maxX, height - 1, e));
                }
                if (e >= mitteZ - this.stageWidth + 1 && e <= mitteZ + this.stageWidth) {
                    w.getBlockAt(minX, height, e).setType(Material.GLASS_PANE);
                    w.getBlockAt(maxX, height, e).setType(Material.GLASS_PANE);
                    w.getBlockAt(minX, height, e).setBlockData(this.connectGlassPanes((MultipleFacing) w.getBlockAt(minX, height, e).getBlockData(), true));
                    w.getBlockAt(maxX, height, e).setBlockData(this.connectGlassPanes((MultipleFacing) w.getBlockAt(maxX, height, e).getBlockData(), true));
                } else {
                    w.getBlockAt(minX, height, e).setType(Material.GLASS);
                    w.getBlockAt(maxX, height, e).setType(Material.GLASS);
                }
            }
        }
    }

    private MultipleFacing connectGlassPanes(final MultipleFacing multipleFacing, final boolean northToSouth) {
        multipleFacing.setFace(BlockFace.NORTH, northToSouth);
        multipleFacing.setFace(BlockFace.EAST, !northToSouth);
        multipleFacing.setFace(BlockFace.SOUTH, northToSouth);
        multipleFacing.setFace(BlockFace.WEST, !northToSouth);
        return multipleFacing;
    }

    public void destroyTunnel() {
        for (int h = 0; h < 4; h++) {
            for (int i = this.minX + 2; i < this.maxX - 1; i++) {
                for (int e = this.minZ + 2; e < this.maxZ - 2; e++) {
                    for (int b = 0; b < 5; b++) {
                        this.world.getBlockAt(i, this.stageLevel + h, this.minZ + 2 + b).setType(Material.AIR);
                        this.world.getBlockAt(i, this.stageLevel + h, this.maxZ - 2 - b).setType(Material.AIR);
                        this.world.getBlockAt(this.minX + 2 + b, this.stageLevel + h, e).setType(Material.AIR);
                        this.world.getBlockAt(this.maxX - 2 - b, this.stageLevel + h, e).setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void tickMe() {
        final int minX = this.minX + 9;
        final int minZ = this.minZ + 9;
        final int maxX = this.maxX - 8;
        final int maxZ = this.maxZ - 8;
        final Iterator<Integer> i = this.burnLevels.iterator();
        while (i.hasNext()) {
            final int oneLevel = ((Integer) i.next()).intValue();
            boolean empty = true;
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (this.world.getBlockAt(x, oneLevel, z).getType() != Material.AIR) {
                        if (empty) {
                            empty = false;
                        }
                        if (this.random(0, this.burnChance) == this.burnChance) {
                            this.world.getBlockAt(x, oneLevel, z).setType(Material.AIR);
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
            this.burnDelay++;
            if (this.burnDelay == this.burnExpand) {
                this.burnDelay = 0;
                this.burnHeight++;
                this.burnLevels.add(Integer.valueOf(this.burnHeight));
            }
        }
    }

    public void remove() {
        this.gm.destroyAll();
    }

    private String gl(final String key) {
        return this.plugin.lang.get(key);
    }

    private int random(final int from, int to) {
        to++;
        return (int) (Math.random() * (to - from) + from);
    }
}
