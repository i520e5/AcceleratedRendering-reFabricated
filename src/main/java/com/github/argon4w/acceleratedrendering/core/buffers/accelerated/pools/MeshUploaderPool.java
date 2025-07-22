package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
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

	@Getter
	public static class MeshInfo implements SimpleCachedArray.Element {

		private int color;
		private int light;
		private int overlay;
		private int sharing;
		private int shouldCull;

		public MeshInfo() {
			this.color		= -1;
			this.light		= -1;
			this.overlay	= -1;
			this.sharing	= -1;
		}

		public void setupMeshInfo(
				int color,
				int light,
				int overlay,
				int sharing,
				int shouldCull
		) {
			this.color		= color;
			this.light		= light;
			this.overlay	= overlay;
			this.sharing	= sharing;
			this.shouldCull	= shouldCull;
		}

		@Override
		public void reset() {

		}

		@Override
		public void delete() {

		}
	}

	public static class MeshUploader implements IntFunction<MeshInfo> {

		public static	final	int							MESH_INFO_BUFFER_INDEX		= 8;
		public static	final	long						MESH_INFO_SIZE				= 5L * 4L;

		public static	final	IMemoryInterface			MESH_INFO_SHARING			= new SimpleMemoryInterface(0L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_SHOULD_CULL		= new SimpleMemoryInterface(1L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_COLOR				= new SimpleMemoryInterface(2L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_UV1				= new SimpleMemoryInterface(3L * 4L, MESH_INFO_SIZE);
		public static	final	IMemoryInterface			MESH_INFO_UV2				= new SimpleMemoryInterface(4L * 4L, MESH_INFO_SIZE);

		private			final	MappedBuffer 				meshInfoBuffer;
		@Getter private	final	SimpleCachedArray<MeshInfo>	meshInfos;

		@Getter	private			long						meshCount;
		@Getter	@Setter private	ServerMesh					serverMesh;

		public MeshUploader() {
			this.meshInfoBuffer				= new MappedBuffer			(64L);
			this.meshInfos					= new SimpleCachedArray<>	(128, this);

			this.serverMesh					= null;
			this.meshCount					= 0L;
		}

		public void addUpload(
				int					color,
				int					light,
				int					overlay,
				int					sharing,
				int					shouldCull
		) {
			meshCount ++;
			meshInfos
					.get			()
					.setupMeshInfo	(
							color,
							light,
							overlay,
							sharing,
							shouldCull
					);
		}

		public void bindUploadBuffers() {
			var meshInfoAddress	= meshInfoBuffer.reserve	(MESH_INFO_SIZE * meshCount);
			meshInfoBuffer						.bindBase	(GL_SHADER_STORAGE_BUFFER, MESH_INFO_BUFFER_INDEX);

			for (var i = 0; i < meshCount; i ++) {
				var meshInfo = meshInfos.at(i);

				MESH_INFO_SHARING		.at(i).putInt(meshInfoAddress, meshInfo			.sharing);
				MESH_INFO_SHOULD_CULL	.at(i).putInt(meshInfoAddress, meshInfo			.shouldCull);
				MESH_INFO_COLOR			.at(i).putInt(meshInfoAddress, FastColor.ABGR32	.fromArgb32(meshInfo.color));
				MESH_INFO_UV1			.at(i).putInt(meshInfoAddress, meshInfo			.overlay);
				MESH_INFO_UV2			.at(i).putInt(meshInfoAddress, meshInfo			.light);
			}
		}

		public void reset() {
			meshInfos		.reset();
			meshInfoBuffer	.reset();

			meshCount	= 0L;
		}

		public void delete() {
			meshInfos		.delete();
			meshInfoBuffer	.delete();
		}

		@Override
		public MeshInfo apply(int value) {
			return new MeshInfo();
		}
	}
}
