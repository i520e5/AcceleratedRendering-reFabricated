package com.github.argon4w.acceleratedrendering.core.utils;

import net.minecraft.util.FastColor;

public class ColorUtils {
    public static int ARGB32toABGR32(int color) {
        return color & -16711936 | (color & 16711680) >> 16 | (color & 255) << 16;
    }

    public static int ARGB32toRGBA32(int color) {
        return color << 8 | color >>> 24;
    }

    public static int argb32Color(
        float red,
        float green,
        float blue,
        float alpha
    ) {
        return FastColor.ARGB32.color((int) (alpha * 255), (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }
}
