package com.lowdragmc.photon.core.mixins;

import com.lowdragmc.photon.client.fx.fxpack.FXPacks;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fabric replacement for neoforge's {@code AddPackFindersEvent}: appends Photon's
 * {@link FXPacks#repositorySource()} to the client resource {@link PackRepository} — identified
 * by the {@link ClientPackSource} in its constructor sources — so .fxpack files mount as hidden,
 * always-on, lowest-priority packs and re-discover on every repository reload.
 */
@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Mutable
    @Shadow
    @Final
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void photon$addFxPackSource(RepositorySource[] sources, CallbackInfo ci) {
        for (var source : sources) {
            if (source instanceof ClientPackSource) {
                var merged = new LinkedHashSet<>(this.sources);
                merged.add(FXPacks.repositorySource());
                this.sources = merged;
                return;
            }
        }
    }
}
