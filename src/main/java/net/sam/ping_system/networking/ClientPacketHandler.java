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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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

    public static Style goStyle = Style.EMPTY.withColor(TextColor.fromRgb(0x79AC03));
    public static Style attackStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xF5BB00));
    public static Style dangerStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xBF3100));
    public static Style breakStyle = Style.EMPTY.withColor(TextColor.fromRgb(0xB8B8F3));

    public static void handleS2CPingPacket(int senderId, int type, double x, double y, double z, BlockPos blockPos, int r, int g, int b, boolean isAlternative, int acknowledgerId, int selectedId) {
        ////System.out.println("received ping packet");
        //if remove ping packet
        if(isAlternative){
            for(Ping p: PingHandler.pingList){
                if(p.playerId == senderId && p.type == type && p.x == x && p.y == y && p.z == z){
                    if(acknowledgerId == -1){
                        p.toRemove = true;
                        if(Minecraft.getInstance().player.getId() == p.playerId){

                            PingSound sound = new PingSound(
                                    SoundEvents.UI_BUTTON_CLICK.get(),
                                    SoundSource.PLAYERS,
                                    0.1F, 2.0F,
                                    x, y, z
                            );
                            Minecraft.getInstance().getSoundManager().play(sound);

                        }

                        //acknowledge ping
                    }else{
                        PingHandler.newPingGhost(type, x,y,z,r,g,b, p.team);

                        PingSound ding = new PingSound(
                                SoundEvents.NOTE_BLOCK_BELL.get(),
                                SoundSource.PLAYERS,
                                0.3F, 1.0F,
                                x, y, z
                        );
                        Minecraft.getInstance().getSoundManager().play(ding);



                    }
                    return;
                }
            }
            return;
        }
        //if normal ping packet
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.player.level();
        Entity sender = mc.player.level().getEntity(senderId);
        Team team = sender.getTeam();
        ChatFormatting teamColor;

        if(team != null){
            teamColor = sender.getTeam().getColor();
        }else {
            teamColor = ChatFormatting.WHITE;
        }

        ////System.out.println("if sender");
        if(sender instanceof Player p){
            MutableComponent message = Component.literal(p.getName().getString()).withStyle(teamColor);
            if(selectedId != -1){
                Entity entity = level.getEntity(selectedId);
                if(entity instanceof ItemEntity ie){
                    message.append(Component.literal(String.format(" - Item (%s)",ie.getDisplayName().getString())).withStyle(ChatFormatting.WHITE));
                }else {
                    ChatFormatting nameColor;
                    if(entity instanceof Player){
                        nameColor = entity.getTeam().getColor();
                    }else{
                        nameColor = ChatFormatting.WHITE;
                    }

                    message.append(Component.literal(" - ").withStyle(ChatFormatting.WHITE));
                    if(entity.getCustomName() != null){
                        message.append(Component.literal(String.format("%s",entity.getCustomName().getString())).withStyle(nameColor));
                    }else{
                        message.append(Component.literal(String.format("%s",entity.getDisplayName().getString())).withStyle(nameColor));
                    }
                }

            } else if(blockPos.getY() != 10000){
                String blockName = level.getBlockState(blockPos).getBlock().getName().getString();
                message.append(Component.literal(String.format(" - %s", blockName)).withStyle(ChatFormatting.WHITE));
            }





            if(type == 1){
                message.append(Component.literal(" (GO)").setStyle(goStyle).withStyle(ChatFormatting.BOLD));
            } else if (type == 2) {
                message.append(Component.literal(" (ATTACK)").setStyle(attackStyle).withStyle(ChatFormatting.BOLD));
            }else if (type == 3) {
                message.append(Component.literal(" (DANGER)").setStyle(dangerStyle).withStyle(ChatFormatting.BOLD));
            }else if (type == 4) {
                message.append(Component.literal(" (BREAK)").setStyle(breakStyle).withStyle(ChatFormatting.BOLD));
            }
            player.sendSystemMessage(message);
        }
        PingHandler.newPing(senderId, type, x,y,z,r,g,b, blockPos, team, selectedId);
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


        if(senderId == mc.player.getId()){
            PingSound click = new PingSound(
                    SoundEvents.UI_BUTTON_CLICK.get(),
                    SoundSource.PLAYERS,
                    0.1F, 2.0F,
                    x, y, z
            );
            mc.getSoundManager().play(click);
        }
    }
}
