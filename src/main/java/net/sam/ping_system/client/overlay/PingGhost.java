package net.sam.ping_system.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.scores.Team;
import net.sam.ping_system.util.PartialTickUtils;

public class PingGhost {

    public double x;
    public double y;
    public double z;
    public int r;
    public int g;
    public int b;
    public boolean toRemove = false;
    public Team team;
    public int type;
    public float sizeMult;

    public float age = 0f;
    public static float lifetime = 10f; //in ticks
    public static int baseSize = 7;
    public static float baseSizeMult = 1.0f;
    public static float maxSizeMult = 8.0f;

    public PingGhost(int type, double x, double y, double z, int r, int g , int b, Team team){
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.team = team;
        this.sizeMult = baseSize;
    }

    public void tick(){
        if(age > lifetime){
            this.toRemove = true;
        }
        this.sizeMult = (1 + ((maxSizeMult - baseSizeMult) * (age/lifetime)));
        this.age += PartialTickUtils.timeDif;
    }
}
