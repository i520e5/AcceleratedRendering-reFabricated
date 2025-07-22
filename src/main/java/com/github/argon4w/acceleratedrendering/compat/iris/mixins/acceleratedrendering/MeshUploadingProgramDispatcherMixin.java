package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisAcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisMeshInfo;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.MeshUploadingProgramDispatcher;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MeshUploadingProgramDispatcher.class)
public class MeshUploadingProgramDispatcherMixin {

	@Inject(
			method	= "dispatch",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putInt(JI)V",
					ordinal	= 2,
					shift	= At.Shift.AFTER
			)
	)
	public void addIrisData(
			Collection<AcceleratedBufferBuilder>						builders,
			AcceleratedBufferSetPool.BufferSet							bufferSet,
			CallbackInfo												ci,
			@Local(name = "meshInfo")		MeshUploaderPool.MeshInfo	meshInfo,
			@Local(name = "builder")		AcceleratedBufferBuilder	builder,
			@Local(name = "offset")			int							offset,
			@Local(name = "vertexAddress")	long						vertexAddress
	) {
		var irisInfo 	= (IIrisMeshInfo)					meshInfo;
		var irisBuilder	= (IIrisAcceleratedBufferBuilder)	builder;

		irisBuilder.getEntityIdOffset()	.at(offset).putShort(vertexAddress + 0L, irisInfo.getRenderedEntity		());
		irisBuilder.getEntityIdOffset()	.at(offset).putShort(vertexAddress + 2L, irisInfo.getRenderedBlockEntity	());
		irisBuilder.getEntityIdOffset()	.at(offset).putShort(vertexAddress + 4L, irisInfo.getRenderedItem		());

		irisBuilder.getEntityOffset()	.at(offset).putShort(vertexAddress + 0L, (short) -1);
		irisBuilder.getEntityOffset()	.at(offset).putShort(vertexAddress + 2L, (short) -1);
	}
}
