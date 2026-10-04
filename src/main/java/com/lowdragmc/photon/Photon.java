package com.lowdragmc.photon;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.photon.client.PhotonClientProxy;
import com.lowdragmc.photon.client.compat.iris.IrisCompat;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Photon implements ModInitializer {
    public static final String MOD_ID = "photon";
    public static final String NAME = "Photon";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public void onInitialize() {
        Photon.init();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, PhotonConfig.CONFIG_SPEC);
            PhotonClientProxy.init();
        } else {
            PhotonCommonProxy.init();
        }
    }

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
        if (new File(LDLib2.getAssetsDir(), "photon").mkdirs()) {
            LOGGER.info("Created photon assets folder");
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /** @see com.lowdragmc.photon.client.compat.iris.IrisCompat */
    public static boolean isUsingShaderPack() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && IrisCompat.isUsingShaderPack();
    }
}

