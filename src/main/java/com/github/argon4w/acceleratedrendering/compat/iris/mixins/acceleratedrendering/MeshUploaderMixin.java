package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisMeshInfo;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

@Mixin(MeshUploaderPool.MeshUploader.class)
public class MeshUploaderMixin {

	@Shadow private					long				meshCount;

	@Unique private static final	int					IRIS_INFO_BUFFER_INDEX	= 9;
	@Unique private static final	long				IRIS_INFO_SIZE			= 2L * 4L;
	@Unique private static final	IMemoryInterface	IRIS_INFO_ENTITY		= new SimpleMemoryInterface(0L * 2L, IRIS_INFO_SIZE);
	@Unique private static final	IMemoryInterface	IRIS_INFO_BLOCK_ENTITY	= new SimpleMemoryInterface(1L * 2L, IRIS_INFO_SIZE);
	@Unique private static final	IMemoryInterface	IRIS_INFO_ITEM			= new SimpleMemoryInterface(2L * 2L, IRIS_INFO_SIZE);

	@Unique private					MappedBuffer		irisInfoBuffer;

	@Inject(
			method	= "<init>",
			at		= @At("TAIL")
	)
	public void constructor(CallbackInfo ci) {
		irisInfoBuffer = new MappedBuffer(64L);
	}

	@Inject(
			method	= "bindUploadBuffers",
			at		= @At("HEAD")
	)
	public void reserveIrisInfoAddress(CallbackInfo ci, @Share(value = "irisInfoAddress") LocalLongRef irisInfoAddress) {
		irisInfoAddress	.set		(irisInfoBuffer.reserve(IRIS_INFO_SIZE * meshCount));
		irisInfoBuffer	.bindBase	(GL_SHADER_STORAGE_BUFFER, IRIS_INFO_BUFFER_INDEX);
	}

	@Inject(
			method	= "bindUploadBuffers",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putInt(JI)V",
					ordinal	= 4,
					shift	= At.Shift.AFTER
			)
	)
	public void uploadIrisData(
			CallbackInfo													ci,
			@Share(value = "irisInfoAddress")	LocalLongRef				irisInfoAddress,
			@Local(name = "meshInfo")			MeshUploaderPool.MeshInfo	meshInfo,
			@Local(name = "i")					int							i
	) {
		var address		= irisInfoAddress.get();
		var irisInfo	= (IIrisMeshInfo) meshInfo;

		IRIS_INFO_ENTITY		.at(i).putShort(address, irisInfo.getRenderedEntity		());
		IRIS_INFO_BLOCK_ENTITY	.at(i).putShort(address, irisInfo.getRenderedBlockEntity());
		IRIS_INFO_ITEM			.at(i).putShort(address, irisInfo.getRenderedItem		());
	}
}
