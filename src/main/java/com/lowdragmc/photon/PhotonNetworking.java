package com.lowdragmc.photon;

import com.lowdragmc.photon.command.BlockEffectCommand;
import com.lowdragmc.photon.command.EntityEffectCommand;
import com.lowdragmc.photon.command.RemoveBlockEffectCommand;
import com.lowdragmc.photon.command.RemoveEntityEffectCommand;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PhotonNetworking {

    /** codec registration is side-safe; the receivers are registered by the client proxy. */
    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(BlockEffectCommand.TYPE, BlockEffectCommand.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityEffectCommand.TYPE, EntityEffectCommand.CODEC);
        PayloadTypeRegistry.playS2C().register(RemoveBlockEffectCommand.TYPE, RemoveBlockEffectCommand.CODEC);
        PayloadTypeRegistry.playS2C().register(RemoveEntityEffectCommand.TYPE, RemoveEntityEffectCommand.CODEC);
    }
}
