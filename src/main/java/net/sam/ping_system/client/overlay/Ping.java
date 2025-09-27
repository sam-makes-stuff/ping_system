package net.sam.ping_system.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.sam.ping_system.config.ClientConfig;
import net.sam.ping_system.config.ServerConfig;
import net.sam.ping_system.util.ConfigUtils;
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
    public int attachedId;

    public static boolean shouldTrackMob;
    public static boolean shouldTrackPlayer;

    private BlockPos blockPos;
    public ItemStack itemStack = null;

    public int type;
    public float age = 0f;
    public static float lifetime = 600f; //in ticks

    public Ping(int playerId, int type, double x, double y, double z, int r, int g , int b, BlockPos blockPos, Team team, int attachedId){
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
        this.attachedId = attachedId;

        //No block hit or attached to entity

        if(attachedId != -1) {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.player.level().getEntity(attachedId);
            if(entity instanceof ItemEntity ie){
                this.itemStack = ie.getItem();
            }
        }
        else if(blockPos.getY() != 10000){
            Minecraft mc = Minecraft.getInstance();
            Block block = mc.player.level().getBlockState(blockPos).getBlock();
            this.itemStack = new ItemStack(block);
        }


    }

    public boolean update() {
        return age > lifetime;
    }

    public void tick(float partialTicks){
        this.age += PartialTickUtils.timeDif;
        followAttached(partialTicks);
    }

    public void followAttached(float partialTicks){
        if(this.attachedId != -1){


            Minecraft mc = Minecraft.getInstance();
            Entity attachedTo = mc.level.getEntity(attachedId);

            if(attachedTo instanceof Player && !shouldTrackPlayer){
                return;
            }

            if(attachedTo instanceof LivingEntity && !(attachedTo instanceof Player) && !shouldTrackMob){
                return;
            }

            if(attachedTo == null || !attachedTo.isAlive()){
                this.toRemove = true;
            }else{
                Vec3 pos = attachedTo.getPosition(partialTicks);
                this.x = pos.x();
                this.y = pos.y() + attachedTo.getBbHeight() * 0.5f;
                this.z = pos.z();
            }
        }
    }

    public static void initFromConfig() {

        //lifetime = ConfigUtils.getOrDefault(ClientConfig.NUMBER_DURATION);
        shouldTrackMob = ConfigUtils.getOrDefault(ServerConfig.TRACK_MOBS);
        shouldTrackPlayer = ConfigUtils.getOrDefault(ServerConfig.TRACK_PLAYERS);
    }

}