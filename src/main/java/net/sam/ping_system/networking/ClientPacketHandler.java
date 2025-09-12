// ClientPacketHandler.java
package net.sam.ping_system.networking;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.scores.Team;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.sam.ping_system.client.overlay.Ping;
import net.sam.ping_system.client.overlay.PingHandler;
import net.sam.ping_system.sound.ModSounds;
import net.sam.ping_system.sound.PingSound;

public class ClientPacketHandler {
    public static void handleS2CPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos, int r, int g, int b, boolean isRemove) {


        //if remove ping packet
        if(isRemove){
            for(Ping p: PingHandler.pingList){
                if(p.playerId == senderId && p.type == type && p.x == x && p.y == y && p.z == z){
                    p.toRemove = true;
                    return;
                }
            }
            return;
        }

        //if normal ping packet
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.player.level();
        String blockName = level.getBlockState(blockPos).getBlock().getName().getString();
        Entity sender = mc.player.level().getEntity(senderId);
        Team team = sender.getTeam();

        ChatFormatting teamColor;
        if(team != null){
            teamColor = sender.getTeam().getColor();
        }else {
            teamColor = ChatFormatting.WHITE;
        }

        if(sender instanceof Player p){
            String message = String.format("%s - %s", p.getName().getString(), blockName);
            if(type == 1){
                message += " (GO)";
            } else if (type == 2) {
                message += " (ATTACK)";
            }else if (type == 3) {
                message += " (DANGER)";
            }else if (type == 4) {
                message += " (BREAK)";
            }
            player.sendSystemMessage(Component.literal(message).withStyle(teamColor));
        }

        PingHandler.newPing(senderId, type, x,y,z,r,g,b, blockPos, team);

        //Play ping sound
        PingSound sound;

        if(type == 1){
            sound = new PingSound(
                    ModSounds.GO.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.4F,
                    x, y, z
            );
        } else if (type == 2) {
            sound = new PingSound(
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS,
                    0.3F, 0.7F,
                    x, y, z
            );
        }else if (type == 3) {
            sound = new PingSound(
                    ModSounds.DANGER.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F,
                    x, y, z
            );
        }else if (type == 4) {
            sound = new PingSound(
                    ModSounds.BREAK.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.4F,
                    x, y, z
            );
        }else{
            sound = new PingSound(
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.3F, 1.3F,
                    x, y, z
            );
        }

        mc.getSoundManager().play(sound);


    }
}
