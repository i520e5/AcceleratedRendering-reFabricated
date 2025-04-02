package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.concurrent.atomic.AtomicInteger;

public class VertexFormatUtils {

    private static final Reference2IntMap<VertexFormat> CACHE = new Reference2IntOpenHashMap<>();
    private static final Reference2IntMap<VertexFormatElement> ELEMENT_ID = new Reference2IntOpenHashMap<>();
    public static final VertexFormatElement[] ELEMENTS_BY_ID = new VertexFormatElement[32];
    private static final AtomicInteger idCounter;

    static {
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_POSITION, 0);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_COLOR, 1);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_UV0, 2);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_UV1, 3);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_UV2, 4);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_NORMAL, 5);
        ELEMENT_ID.put(DefaultVertexFormat.ELEMENT_PADDING, 6);

        int i = 0;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_POSITION;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_COLOR;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_UV0;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_UV1;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_UV2;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_NORMAL;
        ELEMENTS_BY_ID[i++] = DefaultVertexFormat.ELEMENT_PADDING;
        idCounter = new AtomicInteger(i);
    }

    public static int hashCodeFast(VertexFormat format) {
        int result = CACHE.getInt(format);

        if (result == 0) {
            result = format.hashCode();
            CACHE.put(format, result);
        }

        return result;
    }

    public static void registerElement(VertexFormatElement element){
        int id = idCounter.getAndIncrement();
        ELEMENT_ID.put(element, id);
        ELEMENTS_BY_ID[id] = element;
    }

    public static int elementId(VertexFormatElement element){
        return ELEMENT_ID.getInt(element);
    }

    public static VertexFormatElement elementById(int id){
        return ELEMENTS_BY_ID[id];
    }
}
