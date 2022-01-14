package me.baba43.deathcube;

public class DeathTick implements Runnable {
    private final DeathCube dc;

    public DeathTick(final DeathCube dc) {
        this.dc = dc;
    }

    public void run() {
        this.dc.tickMe();
    }
}
