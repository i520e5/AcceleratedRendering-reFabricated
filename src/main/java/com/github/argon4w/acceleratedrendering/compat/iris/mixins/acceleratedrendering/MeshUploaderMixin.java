package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.IMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeshUploaderPool.MeshUploader.class)
public class MeshUploaderMixin {

	@Shadow @Final private			IMeshInfoCache		meshInfos;

	@Unique private static final	long				IRIS_INFO_SIZE			= 2L * 4L;
	@Unique private static final	IMemoryInterface	IRIS_INFO_ENTITY		= new SimpleMemoryInterface(0L * 2L, IRIS_INFO_SIZE);
	@Unique private static final	IMemoryInterface	IRIS_INFO_BLOCK_ENTITY	= new SimpleMemoryInterface(1L * 2L, IRIS_INFO_SIZE);
	@Unique private static final	IMemoryInterface	IRIS_INFO_ITEM			= new SimpleMemoryInterface(2L * 2L, IRIS_INFO_SIZE);

	@Inject(
			method	= "upload",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putInt(JI)V",
					ordinal	= 4,
					shift	= At.Shift.AFTER
			)
	)
	public void uploadIrisData(
			CallbackInfo													ci,
			@Local(name = "extraInfoAddress")	long						irisInfoAddress,
			@Local(name = "i")					int							i
	) {
		IRIS_INFO_ENTITY		.at(i).putShort(irisInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedEntity		(i));
		IRIS_INFO_BLOCK_ENTITY	.at(i).putShort(irisInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedBlockEntity(i));
		IRIS_INFO_ITEM			.at(i).putShort(irisInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedItem		(i));
	}
}
