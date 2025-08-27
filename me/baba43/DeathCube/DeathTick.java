package me.baba43.DeathCube;

public class DeathTick implements Runnable {
	private DeathCube dc;

	public DeathTick(DeathCube dc) {
		this.dc = dc;
	}

	public void run() {
		this.dc.tickMe();
	}
}
