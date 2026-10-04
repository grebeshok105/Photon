package com.lowdragmc.photon.core.mixins;

import com.lowdragmc.photon.client.postfx.PhotonPostFX;
import com.lowdragmc.photon.client.postfx.runtime.PostFXCamera;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric seam for neoforge's {@code RenderLevelStageEvent.AFTER_PARTICLES}: standalone
 * post-effect consumption for frames without Photon particles (the particle pipeline seam never
 * runs then). Injects right after the {@link ParticleEngine#render} call inside renderLevel —
 * the same point the neoforge stage fired at.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;render("
                            + "Lnet/minecraft/client/renderer/LightTexture;"
                            + "Lnet/minecraft/client/Camera;F)V",
                    shift = At.Shift.AFTER))
    private void photon$afterParticles(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                       Camera camera, GameRenderer gameRenderer,
                                       LightTexture lightTexture, Matrix4f frustumMatrix,
                                       Matrix4f projectionMatrix, CallbackInfo ci) {
        PostFXCamera.capture(frustumMatrix, projectionMatrix, camera.getPosition());
        PhotonPostFX.onLevelStageAfterParticles();
    }
}
