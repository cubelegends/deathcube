package me.baba43.DeathCube;

import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PositionTimer implements Runnable {
  private int minX;
  
  private int minZ;
  
  private int maxX;
  
  private int maxZ;
  
  private int minY;
  
  private int maxY;
  
  private int blockID;
  
  private ArrayList<Integer> activeLevels = new ArrayList<>();
  
  private ArrayList<Player> activePlayers;
  
  private World w;
  
  public PositionTimer(World w, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, ArrayList<Player> player, int blockID) {
    this.w = w;
    byte abstand = 6;
    this.minX = minX + abstand;
    this.maxX = maxX - abstand;
    this.minY = minY;
    this.maxY = maxY;
    this.minZ = minZ + abstand;
    this.maxZ = maxZ - abstand;
    this.blockID = blockID;
    this.activePlayers = player;
    for (int h = this.minY; h <= this.maxY; h++) {
      setInactive(w.getBlockAt(this.minX, h, this.minZ));
      setInactive(w.getBlockAt(this.minX + 1, h, this.minZ));
      setInactive(w.getBlockAt(this.minX, h, this.minZ + 1));
      setInactive(w.getBlockAt(this.minX, h, this.maxZ));
      setInactive(w.getBlockAt(this.minX + 1, h, this.maxZ));
      setInactive(w.getBlockAt(this.minX, h, this.maxZ - 1));
      setInactive(w.getBlockAt(this.maxX, h, this.minZ));
      setInactive(w.getBlockAt(this.maxX - 1, h, this.minZ));
      setInactive(w.getBlockAt(this.maxX, h, this.minZ + 1));
      setInactive(w.getBlockAt(this.maxX, h, this.maxZ));
      setInactive(w.getBlockAt(this.maxX - 1, h, this.maxZ));
      setInactive(w.getBlockAt(this.maxX, h, this.maxZ - 1));
    } 
  }
  
  public void run() {
    ArrayList<Integer> newLevels = new ArrayList();
    if (this.activePlayers.isEmpty())
      System.out.println("LEER"); 
    for (Iterator<Player> i = this.activePlayers.iterator(); i.hasNext(); newLevels.add(Integer.valueOf(h))) {
      Player p = i.next();
      int h = p.getLocation().getBlockY();
      if (h < this.minY) {
        h = this.minY;
      } else if (h > this.maxY) {
        h = this.maxY;
      } 
    } 
    Iterator<Integer> p1 = this.activeLevels.iterator();
    while (p1.hasNext()) {
      int i1 = ((Integer)p1.next()).intValue();
      if (!newLevels.contains(Integer.valueOf(i1))) {
        newLevels.remove(Integer.valueOf(i1));
        setInactive(this.w.getBlockAt(this.minX, i1, this.minZ));
        setInactive(this.w.getBlockAt(this.minX + 1, i1, this.minZ));
        setInactive(this.w.getBlockAt(this.minX, i1, this.minZ + 1));
        setInactive(this.w.getBlockAt(this.minX, i1, this.maxZ));
        setInactive(this.w.getBlockAt(this.minX + 1, i1, this.maxZ));
        setInactive(this.w.getBlockAt(this.minX, i1, this.maxZ - 1));
        setInactive(this.w.getBlockAt(this.maxX, i1, this.minZ));
        setInactive(this.w.getBlockAt(this.maxX - 1, i1, this.minZ));
        setInactive(this.w.getBlockAt(this.maxX, i1, this.minZ + 1));
        setInactive(this.w.getBlockAt(this.maxX, i1, this.maxZ));
        setInactive(this.w.getBlockAt(this.maxX - 1, i1, this.maxZ));
        setInactive(this.w.getBlockAt(this.maxX, i1, this.maxZ - 1));
      } 
    } 
    p1 = newLevels.iterator();
    while (p1.hasNext()) {
      int i1 = ((Integer)p1.next()).intValue();
      this.activeLevels.add(Integer.valueOf(i1));
      setActive(this.w.getBlockAt(this.minX, i1, this.minZ));
      setActive(this.w.getBlockAt(this.minX + 1, i1, this.minZ));
      setActive(this.w.getBlockAt(this.minX, i1, this.minZ + 1));
      setActive(this.w.getBlockAt(this.minX, i1, this.maxZ));
      setActive(this.w.getBlockAt(this.minX + 1, i1, this.maxZ));
      setActive(this.w.getBlockAt(this.minX, i1, this.maxZ - 1));
      setActive(this.w.getBlockAt(this.maxX, i1, this.minZ));
      setActive(this.w.getBlockAt(this.maxX - 1, i1, this.minZ));
      setActive(this.w.getBlockAt(this.maxX, i1, this.minZ + 1));
      setActive(this.w.getBlockAt(this.maxX, i1, this.maxZ));
      setActive(this.w.getBlockAt(this.maxX - 1, i1, this.maxZ));
      setActive(this.w.getBlockAt(this.maxX, i1, this.maxZ - 1));
    } 
  }
  
  private void setActive(Block b) {
    b.setTypeId(this.blockID, true);
  }
  
  private void setInactive(Block b) {
    b.setTypeId(0);
  }
  
  private void setBlock(Block b, boolean active) {
    if (active) {
      b.setTypeId(3);
    } else {
      b.setTypeId(0);
    } 
  }
}
