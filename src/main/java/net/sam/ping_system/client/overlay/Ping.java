package net.sam.ping_system.client.overlay;

import net.sam.ping_system.util.PartialTickUtils;

public class Ping{
    public int playerId;
    public double x;
    public double y;
    public double z;
    public int r;
    public int g;
    public int b;

    public int type;
    public float age = 0f;
    public static float lifetime = 600f; //in ticks

    public Ping(int playerId, int type, double x, double y, double z, int r, int g , int b){
        this.playerId = playerId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;

    }

    public boolean update() {
        return age > lifetime;
    }

    public void tick(){
        this.age += PartialTickUtils.timeDif;
    }
}