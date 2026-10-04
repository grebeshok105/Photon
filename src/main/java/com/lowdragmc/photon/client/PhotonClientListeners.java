package com.lowdragmc.photon.client;

import com.lowdragmc.photon.client.compat.iris.IrisOverlay;
import com.lowdragmc.photon.client.gameobject.emitter.renderpipeline.OpaqueDepthCapture;
import com.lowdragmc.photon.client.postfx.PhotonPostFX;
import com.lowdragmc.photon.client.postfx.runtime.PostFXCamera;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.List;

public class PhotonClientListeners {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            List<LiteralArgumentBuilder<FabricClientCommandSource>> commands = ClientCommands.createClientCommands();
            commands.forEach(dispatcher::register);
        });

        // per-render-frame boundary (in-world and in the editor screen alike) — a GameRenderer
        // mixin fires this at every frame end; see core.mixins.GameRendererMixin
        // (PhotonPostFX.onFrameEnd() is called from there)

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            // The frame's camera, for post-processing passes that reconstruct world space. Captured on every
            // stage because the consumers run at different points in the level render, and LevelRenderer pops
            // the camera off the model-view stack before the last of them — see PostFXCamera.
            PostFXCamera.capture(context.matrixStack().last().pose(), context.projectionMatrix(),
                    context.camera().getPosition());
            // closest fabric seam to neoforge's AFTER_BLOCK_ENTITIES: the last moment before
            // RenderType.translucent() goes down — snapshot the opaque-only depth that
            // FXCompositeMode.LATE depth-tests against
            OpaqueDepthCapture.capture();
        });

        // neoforge's AFTER_PARTICLES stage has no fabric-api equivalent — a LevelRenderer mixin
        // (core.mixins.LevelRendererMixin) fires PhotonPostFX.onLevelStageAfterParticles() at that
        // exact call site instead.

        // opt-in shader-pack layout readout (/photon_iris overlay)
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> IrisOverlay.render(guiGraphics));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingScreen != null && --pendingScreenTicks <= 0) {
                client.setScreen(pendingScreen);
                pendingScreen = null;
            }
        });
    }

    private static net.minecraft.client.gui.screens.Screen pendingScreen;
    private static int pendingScreenTicks;

    /**
     * Schedules {@code screen} to be opened after {@code ticks} client ticks. Chat screens that run
     * commands close themselves right after the command executes, and a key event queued against the
     * stale chat screen can still fire afterwards and call {@code setScreen(null)} — both would kill
     * a screen opened synchronously from a command handler.
     */
    public static void openScreenAfterTicks(net.minecraft.client.gui.screens.Screen screen, int ticks) {
        pendingScreen = screen;
        pendingScreenTicks = ticks;
    }
}
