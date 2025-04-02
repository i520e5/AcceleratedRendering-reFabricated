package com.github.argon4w.acceleratedrendering.core.utils;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationUtils {

    public static ResourceLocation create(String path) {
        return new ResourceLocation(AcceleratedRenderingModEntry.MOD_ID, path);
    }
}
