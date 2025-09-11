package net.sam.ping_system.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.scores.Team;
import net.sam.ping_system.util.PartialTickUtils;

public class Ping{
    public int playerId;
    public double x;
    public double y;
    public double z;
    public int r;
    public int g;
    public int b;
    public boolean isSelected;
    public boolean toRemove = false;
    public Team team;

    private BlockPos blockPos;
    public ItemStack blockStack;

    public int type;
    public float age = 0f;
    public static float lifetime = 600f; //in ticks

    public Ping(int playerId, int type, double x, double y, double z, int r, int g , int b, BlockPos blockPos, Team team){
        this.playerId = playerId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.blockPos = blockPos;
        this.team = team;
        this.isSelected = false;

        Minecraft mc = Minecraft.getInstance();
        Block block = mc.player.level().getBlockState(blockPos).getBlock();
        this.blockStack = new ItemStack(block);
    }

    public boolean update() {
        return age > lifetime;
    }

    public void tick(){
        this.age += PartialTickUtils.timeDif;
    }
}