package me.baba43.deathcube;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;

public class PositionTimer implements Runnable {
    private final int minX;

    private final int minZ;

    private final int maxX;

    private final int maxZ;

    private final int minY;

    private final int maxY;

    private final int blockID;

    private final ArrayList<Integer> activeLevels = new ArrayList<>();

    private final ArrayList<Player> activePlayers;

    private final World w;

    public PositionTimer(final World w, final int minX, final int maxX, final int minY, final int maxY, final int minZ, final int maxZ, final ArrayList<Player> player, final int blockID) {
        this.w = w;
        final byte abstand = 6;
        this.minX = minX + abstand;
        this.maxX = maxX - abstand;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ + abstand;
        this.maxZ = maxZ - abstand;
        this.blockID = blockID;
        this.activePlayers = player;
        for (int h = this.minY; h <= this.maxY; h++) {
            this.setInactive(w.getBlockAt(this.minX, h, this.minZ));
            this.setInactive(w.getBlockAt(this.minX + 1, h, this.minZ));
            this.setInactive(w.getBlockAt(this.minX, h, this.minZ + 1));
            this.setInactive(w.getBlockAt(this.minX, h, this.maxZ));
            this.setInactive(w.getBlockAt(this.minX + 1, h, this.maxZ));
            this.setInactive(w.getBlockAt(this.minX, h, this.maxZ - 1));
            this.setInactive(w.getBlockAt(this.maxX, h, this.minZ));
            this.setInactive(w.getBlockAt(this.maxX - 1, h, this.minZ));
            this.setInactive(w.getBlockAt(this.maxX, h, this.minZ + 1));
            this.setInactive(w.getBlockAt(this.maxX, h, this.maxZ));
            this.setInactive(w.getBlockAt(this.maxX - 1, h, this.maxZ));
            this.setInactive(w.getBlockAt(this.maxX, h, this.maxZ - 1));
        }
    }

    @Override
    public void run() {
        final ArrayList<Integer> newLevels = new ArrayList();
        if (this.activePlayers.isEmpty()) {
            System.out.println("LEER");
        }
        for (final Iterator<Player> i = this.activePlayers.iterator(); i.hasNext(); newLevels.add(Integer.valueOf(h))) {
            final Player p = i.next();
            int h = p.getLocation().getBlockY();
            if (h < this.minY) {
                h = this.minY;
            } else if (h > this.maxY) {
                h = this.maxY;
            }
        }
        Iterator<Integer> p1 = this.activeLevels.iterator();
        while (p1.hasNext()) {
            final int i1 = ((Integer) p1.next()).intValue();
            if (!newLevels.contains(Integer.valueOf(i1))) {
                newLevels.remove(Integer.valueOf(i1));
                this.setInactive(this.w.getBlockAt(this.minX, i1, this.minZ));
                this.setInactive(this.w.getBlockAt(this.minX + 1, i1, this.minZ));
                this.setInactive(this.w.getBlockAt(this.minX, i1, this.minZ + 1));
                this.setInactive(this.w.getBlockAt(this.minX, i1, this.maxZ));
                this.setInactive(this.w.getBlockAt(this.minX + 1, i1, this.maxZ));
                this.setInactive(this.w.getBlockAt(this.minX, i1, this.maxZ - 1));
                this.setInactive(this.w.getBlockAt(this.maxX, i1, this.minZ));
                this.setInactive(this.w.getBlockAt(this.maxX - 1, i1, this.minZ));
                this.setInactive(this.w.getBlockAt(this.maxX, i1, this.minZ + 1));
                this.setInactive(this.w.getBlockAt(this.maxX, i1, this.maxZ));
                this.setInactive(this.w.getBlockAt(this.maxX - 1, i1, this.maxZ));
                this.setInactive(this.w.getBlockAt(this.maxX, i1, this.maxZ - 1));
            }
        }
        p1 = newLevels.iterator();
        while (p1.hasNext()) {
            final int i1 = ((Integer) p1.next()).intValue();
            this.activeLevels.add(Integer.valueOf(i1));
            this.setActive(this.w.getBlockAt(this.minX, i1, this.minZ));
            this.setActive(this.w.getBlockAt(this.minX + 1, i1, this.minZ));
            this.setActive(this.w.getBlockAt(this.minX, i1, this.minZ + 1));
            this.setActive(this.w.getBlockAt(this.minX, i1, this.maxZ));
            this.setActive(this.w.getBlockAt(this.minX + 1, i1, this.maxZ));
            this.setActive(this.w.getBlockAt(this.minX, i1, this.maxZ - 1));
            this.setActive(this.w.getBlockAt(this.maxX, i1, this.minZ));
            this.setActive(this.w.getBlockAt(this.maxX - 1, i1, this.minZ));
            this.setActive(this.w.getBlockAt(this.maxX, i1, this.minZ + 1));
            this.setActive(this.w.getBlockAt(this.maxX, i1, this.maxZ));
            this.setActive(this.w.getBlockAt(this.maxX - 1, i1, this.maxZ));
            this.setActive(this.w.getBlockAt(this.maxX, i1, this.maxZ - 1));
        }
    }

    private void setActive(final Block b) {
        b.setTypeId(this.blockID, true);
    }

    private void setInactive(final Block b) {
        b.setTypeId(0);
    }

    private void setBlock(final Block b, final boolean active) {
        if (active) {
            b.setTypeId(3);
        } else {
            b.setTypeId(0);
        }
    }
}
