package com.lowdragmc.photon;

import com.lowdragmc.photon.command.EntityEffectCommand;
import com.lowdragmc.photon.command.FxLocationArgument;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class PhotonCommonProxy {
    private static boolean initialized;

    public static void init() {
        if (initialized) return;
        initialized = true;
        PhotonNetworking.registerPayloads();
        PhotonCommonListeners.init();
        ArgumentTypeRegistry.registerArgumentType(Photon.id("fx_location"), FxLocationArgument.class,
                SingletonArgumentInfo.contextFree(FxLocationArgument::new));
        ArgumentTypeRegistry.registerArgumentType(Photon.id("fx_auto_rotate"), EntityEffectCommand.AutoRotateType.class,
                SingletonArgumentInfo.contextFree(EntityEffectCommand.AutoRotateType::new));
        PhotonRegistries.init();
    }
}
