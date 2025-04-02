package com.github.argon4w.acceleratedrendering.features.utils;

import com.github.argon4w.acceleratedrendering.features.blocks.mixins.SodiumWorldRendererAccessor;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
import me.jellysquid.mods.sodium.client.util.iterator.ReversibleObjectArrayIterator;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public class SodiumUtils {
    public static void iterateVisibleBlockEntities(SodiumWorldRenderer sodiumWorldRenderer, Consumer<BlockEntity> blockEntityConsumer) {
        RenderSectionManager manager = ((SodiumWorldRendererAccessor) sodiumWorldRenderer).getRenderSectionManager();
        SortedRenderLists lists = manager.getRenderLists();
        ReversibleObjectArrayIterator<ChunkRenderList> it = lists.iterator(false);
        while (it.hasNext()) {
            ChunkRenderList renderList = it.next();
            RenderRegion renderRegion = renderList.getRegion();
            ByteIterator renderSectionIterator = renderList.sectionsWithEntitiesIterator();
            if (renderSectionIterator != null) {
                while (renderSectionIterator.hasNext()) {
                    int renderSectionId = renderSectionIterator.nextByteAsInt();
                    RenderSection renderSection = renderRegion.getSection(renderSectionId);
                    BlockEntity[] blockEntities = renderSection.getCulledBlockEntities();
                    if (blockEntities != null) {
                        for (BlockEntity blockEntity : blockEntities) {
                            blockEntityConsumer.accept(blockEntity);
                        }
                    }
                }
            }
        }
        for (RenderSection renderSection : manager.getSectionsWithGlobalEntities()) {
            BlockEntity[] blockEntities = renderSection.getGlobalBlockEntities();
            if (blockEntities != null) {
                for (BlockEntity blockEntity : blockEntities) {
                    blockEntityConsumer.accept(blockEntity);
                }
            }
        }
    }
}
