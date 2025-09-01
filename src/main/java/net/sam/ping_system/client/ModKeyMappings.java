package net.sam.ping_system.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.sam.ping_system.PingSystem;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PingSystem.MOD_ID, value = Dist.CLIENT)
public class ModKeyMappings {

    public static final String KEY_CATEGORY = "key.categories." + PingSystem.MOD_ID;
    public static final String KEY_PING_KEY = "key." + PingSystem.MOD_ID +".pingKey";

    public static final KeyMapping PING_KEY = new KeyMapping(KEY_PING_KEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event){
        event.register(PING_KEY);
    }

}
