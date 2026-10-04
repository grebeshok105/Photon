package com.lowdragmc.photon.client.fx.timeline;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Activator track. The header binds one target object ({@link #targetId()}); each {@link Clip}
 * defines a window in which that object is <b>active (visible)</b>. Outside all clips the object is
 * inactive (hidden and frozen) with <b>resume</b> semantics — it keeps its age, it is never reset.
 * Multiple activator tracks on the same object OR together. No clips ⇒ inactive the whole time.
 */
@Environment(EnvType.CLIENT)
public class ActivatorTrack extends Track {

    public ActivatorTrack() {
    }
}
