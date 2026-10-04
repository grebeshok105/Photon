package com.lowdragmc.photon.client.fx;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * {@link ParticleTickHost} view of the vanilla {@code ParticleEngine} singleton. Fed by
 * {@code ParticleEngineMixin} (tick + setLevel) and Photon's clear command; consumed by
 * {@link FXRuntime#isValid()}.
 */
@Environment(EnvType.CLIENT)
public final class VanillaParticleHost implements ParticleTickHost {
    public static final VanillaParticleHost INSTANCE = new VanillaParticleHost();

    private long tickCount = 0;
    private int generation = 0;

    private VanillaParticleHost() {
    }

    /** Called from {@code ParticleEngineMixin} at the head of every {@code ParticleEngine.tick()}. */
    public static void onEngineTick() {
        INSTANCE.tickCount++;
    }

    /** Called whenever the engine mass-discards particles ({@code setLevel}, clear commands). */
    public static void onWipe() {
        INSTANCE.generation++;
    }

    @Override
    public long tickCount() {
        return tickCount;
    }

    @Override
    public int generation() {
        return generation;
    }
}
