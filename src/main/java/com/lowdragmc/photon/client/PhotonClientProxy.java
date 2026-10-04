package com.lowdragmc.photon.client;

import com.lowdragmc.photon.Photon;
import com.lowdragmc.photon.PhotonCommonProxy;
import com.lowdragmc.photon.PhotonNetworking;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.fxpack.FXPacks;
import com.lowdragmc.photon.client.gameobject.emitter.data.model.PhotonMeshCache;
import com.lowdragmc.photon.command.BlockEffectCommand;
import com.lowdragmc.photon.command.EntityEffectCommand;
import com.lowdragmc.photon.command.RemoveBlockEffectCommand;
import com.lowdragmc.photon.command.RemoveEntityEffectCommand;
import com.lowdragmc.photon.gui.editor.resource.MeshResource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class PhotonClientProxy extends PhotonCommonProxy implements ClientModInitializer {
    private static boolean initialized;

    @Override
    public void onInitializeClient() {
        init();
    }

    /** Static because {@link Photon#onInitialize()} delegates here for the client env, and the
     *  fabric {@code client} entrypoint calls us a second time — both paths must hit one body. */
    public static void init() {
        if (initialized) return;
        initialized = true;
        // common init runs inside the client proxy, mirroring the upstream super-constructor call
        PhotonCommonProxy.init();
        registerPayloadReceivers();
        clientSetup();
        shaderRegistry();
        registerModels();
        registerReloadListeners();
        PhotonClientListeners.init();
        // .fxpack mounting happens through a PackRepository mixin
        // (core.mixins.PackRepositoryMixin); see FXPacks.repositorySource().
    }

    /** S2C receivers live on the client only; the codec side is registered in
     *  {@link PhotonNetworking#registerPayloads()}. */
    private static void registerPayloadReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BlockEffectCommand.TYPE, BlockEffectCommand::execute);
        ClientPlayNetworking.registerGlobalReceiver(EntityEffectCommand.TYPE, EntityEffectCommand::execute);
        ClientPlayNetworking.registerGlobalReceiver(RemoveBlockEffectCommand.TYPE, RemoveBlockEffectCommand::execute);
        ClientPlayNetworking.registerGlobalReceiver(RemoveEntityEffectCommand.TYPE, RemoveEntityEffectCommand::execute);
    }

    public static void registerReloadListeners() {
        var helper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        helper.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Photon.id("photon_mesh_cache");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                PhotonMeshCache.INSTANCE.onResourceManagerReload(manager);
            }
        });
        // both the loaded effects and the list of loadable ones are answers about the packs, so a reload
        // (a pack toggled, an .fxpack mounted, F3+T) is exactly when they stop being true
        helper.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Photon.id("fx_cache_invalidator");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                FXHelper.clearCache();
            }
        });
    }

    /** Equivalent of upstream's FMLClientSetupEvent work — deferred to the first client-started
     *  tick boundary like enqueueWork deferred to after mod construction. */
    public static void clientSetup() {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            PhotonShaders.init();
            // Touch the registry to trigger annotation scanning; classes annotated with @NodeAttribute
            // bound to ShaderGraph self-register (mirrors KilaGraph's own registry bootstrap).
            Photon.LOGGER.info("Photon shader graph nodes loaded: {}",
                    com.lowdragmc.photon.client.shadergraph.ShaderGraph.NODE_REGISTRY.getNodeClasses().size());
        });
    }

    public static void shaderRegistry() {
        CoreShaderRegistrationCallback.EVENT.register(PhotonShaders::registerShaders);
    }

    /** Equivalent of upstream's ModelEvent.RegisterAdditional: fabric's model loading plugin gets
     *  to enqueue extra standalone models before the bake. */
    public static void registerModels() {
        ModelLoadingPlugin.register(pluginContext -> {
            var extra = new java.util.ArrayList<ResourceLocation>();
            for (var entry : Minecraft.getInstance().getResourceManager().listResources("models",
                    id -> id.getNamespace().equals(Photon.MOD_ID) && id.getPath().endsWith(".json")).entrySet()) {
                var modelLocation = ResourceLocation.fromNamespaceAndPath(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath()
                                .replace("models/", "")
                                .replace(".json", ""));
                extra.add(modelLocation);
            }
            pluginContext.addModels(extra);
            MeshResource.INSTANCE.onAdditionalModel(pluginContext::addModels);
        });
    }
}
