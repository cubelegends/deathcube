package com.henningstorck.deathcube;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class BlockBar {
	private ArrayList<Block> cageBlocks = new ArrayList();
	private ArrayList<Block> cubeBlocks = new ArrayList();
	private boolean isSet;
	private Location origin;
	private Vector vector;
	private final Material[] fixedBlocks = new Material[]{Material.YELLOW_WOOL, Material.BLUE_WOOL, Material.RED_WOOL};
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;
	private Block floorBlock;

	public BlockBar() {
		this.isSet = false;
	}

	public BlockBar(Location loc, Vector vector) {
		this.origin = loc;
		this.vector = vector;
		this.load((CommandSender) null);
	}

	public void load(CommandSender sender) {
		Location start = this.origin;
		Location searchLocation = start.clone();
		Location searchLocation2 = searchLocation.clone().add(0.0D, 1.0D, 0.0D);
		this.floorBlock = searchLocation2.getBlock();
		if (this.floorBlock.getType() == Material.AIR) {
			searchLocation2.getBlock().setType(Material.LIGHT_GRAY_WOOL, true);
			this.floorBlock = searchLocation2.getBlock();
		}

		searchLocation.add(this.vector);
		searchLocation2.add(this.vector);
		int bugCounter = 0;
		this.cageBlocks.clear();
		this.cubeBlocks.clear();

		while (bugCounter < 20) {
			boolean max = true;
			if (searchLocation.getBlock().getType() != Material.AIR) {
				max = false;
				this.cageBlocks.add(searchLocation.getBlock());
				searchLocation = searchLocation.add(this.vector);
			}

			if (searchLocation.getBlock().getType() != Material.AIR) {
				max = false;
				this.cubeBlocks.add(searchLocation2.getBlock());
				searchLocation2 = searchLocation2.add(this.vector);
			}

			if (max) {
				break;
			}

			++bugCounter;
		}

		if (!this.cubeBlocks.isEmpty() && !this.cageBlocks.isEmpty()) {
			Location var7 = this.origin.clone().add(this.vector.clone().multiply(bugCounter + 1));
			this.minX = Math.min(this.origin.getBlockX(), var7.getBlockX());
			this.minZ = Math.min(this.origin.getBlockZ(), var7.getBlockZ());
			this.maxX = Math.max(this.origin.getBlockX(), var7.getBlockX());
			this.maxZ = Math.max(this.origin.getBlockZ(), var7.getBlockZ());
			this.minY = this.origin.getBlockY();
			this.isSet = true;
			if (sender != null) {
				sender.sendMessage(ChatColor.GREEN + "Blockbar seems valid!");
			}
		} else if (sender != null) {
			sender.sendMessage(ChatColor.RED + "Blockbar is invalid!");
		}

	}

	public boolean contains(Location loc) {
		return this.isSet && loc.getWorld() == this.origin.getWorld() && loc.getBlockX() >= this.minX && loc.getBlockX() <= this.maxX && loc.getBlockZ() >= this.minZ && loc.getBlockZ() <= this.maxZ && (loc.getBlockY() == this.origin.getBlockY() || loc.getBlockY() == this.origin.getBlockY() + 1);
	}

	public void setCageBlock(Block b) {
		if (this.isSet) {
			Block newBlock = (Block) this.cageBlocks.get(this.random(0, this.cageBlocks.size() - 1));
			b.setType(newBlock.getType(), true);
		} else {
			b.setType(this.fixedBlocks[this.random(0, 2)], false);
		}

	}

	public void setCubeBlock(Block b) {
		if (this.isSet) {
			Block newBlock = (Block) this.cubeBlocks.get(this.random(0, this.cubeBlocks.size() - 1));
			b.setType(newBlock.getType(), true);
		} else {
			b.setType(this.fixedBlocks[this.random(0, 2)], false);
		}

	}

	public void setFloorBlock(Block b) {
		if (this.isSet) {
			b.setType(this.floorBlock.getType(), true);
		} else {
			b.setType(Material.LIGHT_GRAY_WOOL, false);
		}

	}

	public void verifyBlocks() {
		if (this.floorBlock != null && this.floorBlock.getType() == Material.AIR) {
			this.floorBlock.setType(Material.LIGHT_GRAY_WOOL, false);
		}

		if (this.cubeBlocks.size() > 0 && this.cageBlocks.size() > 0) {
			this.isSet = true;
		} else {
			this.isSet = false;
		}

	}

	public boolean isSet() {
		return this.isSet;
	}

	private int random(int from, int to) {
		++to;
		return (int) (Math.random() * (double) (to - from) + (double) from);
	}
}
