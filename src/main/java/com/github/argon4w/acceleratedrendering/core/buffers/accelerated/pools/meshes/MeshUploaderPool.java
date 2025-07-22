package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleCachedArray;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.FastColor;

import java.util.function.IntFunction;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class MeshUploaderPool extends SimpleResetPool<MeshUploaderPool.MeshUploader, Void> {

	public MeshUploaderPool() {
		super(128, null);
	}

	@Override
	protected MeshUploader create(Void context, int i) {
		return new MeshUploader();
	}

	@Override
	protected void reset(MeshUploader meshUploader) {
		meshUploader.reset();
	}

	@Override
	protected void delete(MeshUploader meshUploader) {
		meshUploader.delete();
	}

	@Override
	public MeshUploader fail() {
		expand();
		return get();
	}

	public static class MeshUploader implements IntFunction<MeshInfo> {

		public static	final	int							MESH_INFO_BUFFER_INDEX		= 8;
		public static	final	int							EXTRA_INFO_BUFFER_INDEX		= 9;

		public static	final	long						MESH_INFO_SIZE				= 5L * 4L;
		public static	final	long						EXTRA_INFO_SIZE				= 2L * 4L;

		public static	final	IMemoryInterface			MESH_INFO_SHARING			= new SimpleMemoryInterface(0L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_SHOULD_CULL		= new SimpleMemoryInterface(1L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_COLOR				= new SimpleMemoryInterface(2L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_UV1				= new SimpleMemoryInterface(3L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_UV2				= new SimpleMemoryInterface(4L * 4L, MESH_INFO_SIZE);

		private			final	MappedBuffer 				meshInfoBuffer;
		private			final	MappedBuffer				extraInfoBuffer;
		@Getter private	final	IMeshInfoCache				meshInfos;

		@Getter	@Setter private	ServerMesh					serverMesh;

		public MeshUploader() {
			this.meshInfoBuffer		= new MappedBuffer			(64L);
			this.extraInfoBuffer	= new MappedBuffer			(64L);
			this.meshInfos			= MeshInfoCacheType.create	(MeshInfoCacheType.UNSAFE);

			this.serverMesh		= null;
		}

		public void addUpload(
				int color,
				int light,
				int overlay,
				int sharing,
				int shouldCull
		) {
			meshInfos.setup(
					color,
					light,
					overlay,
					sharing,
					shouldCull
			);
		}

		public void upload() {
			var meshCount			= meshInfos			.getMeshCount	();
			var meshInfoAddress		= meshInfoBuffer	.reserve		(MESH_INFO_SIZE		* meshCount);
			var extraInfoAddress	= extraInfoBuffer	.reserve		(EXTRA_INFO_SIZE	* meshCount); //Reserve for Iris Shaders

			for (var i = 0; i < meshCount; i ++) {
				MESH_INFO_SHARING		.at(i).putInt(meshInfoAddress, meshInfos		.getSharing		(i));
				MESH_INFO_SHOULD_CULL	.at(i).putInt(meshInfoAddress, meshInfos		.getShouldCull	(i));
				MESH_INFO_COLOR			.at(i).putInt(meshInfoAddress, FastColor.ABGR32	.fromArgb32(meshInfos.getColor(i)));
				MESH_INFO_UV1			.at(i).putInt(meshInfoAddress, meshInfos		.getOverlay		(i));
				MESH_INFO_UV2			.at(i).putInt(meshInfoAddress, meshInfos		.getLight		(i));
			}
		}

		public void bindBuffers() {
			meshInfoBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_INFO_BUFFER_INDEX);
			extraInfoBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, EXTRA_INFO_BUFFER_INDEX);
		}

		public void reset() {
			meshInfos		.reset();
			meshInfoBuffer	.reset();
			extraInfoBuffer	.reset();
		}

		public void delete() {
			meshInfos		.delete();
			meshInfoBuffer	.delete();
			extraInfoBuffer	.delete();
		}

		@Override
		public MeshInfo apply(int value) {
			return new MeshInfo();
		}
	}
}
