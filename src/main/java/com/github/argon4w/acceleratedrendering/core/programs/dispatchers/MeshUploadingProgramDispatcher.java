package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class MeshUploadingProgramDispatcher {

	private static	final int				GROUP_SIZE				= 128;
	private static	final int				DISPATCH_COUNT_Z		= 1;

	private			final ComputeProgram	program;
	private			final Uniform			meshCountUniform;
	private			final Uniform			meshSizeUniform;
	private			final Uniform			vertexOffsetUniform;

	public MeshUploadingProgramDispatcher(ResourceLocation key) {
		this.program				= ComputeShaderProgramLoader.getProgram(key);
		this.meshCountUniform		= program					.getUniform("meshCount");
		this.meshSizeUniform		= program					.getUniform("meshSize");
		this.vertexOffsetUniform	= program					.getUniform("vertexOffset");
	}

	public void dispatch(Collection<AcceleratedBufferBuilder> builders) {
		program.useProgram();

		for (var builder : builders) {
			var meshVertexCount	= builder.getMeshVertexCount();
			var vertexOffset	= builder.getVertexCount	();
			var vertexBuffer	= builder.getVertexBuffer	();
			var varyingBuffer	= builder.getVaryingBuffer	();

			vertexBuffer	.reserve	(meshVertexCount * builder.getVertexSize());
			varyingBuffer	.reserve	(meshVertexCount * AcceleratedBufferBuilder.VARYING_SIZE);

			vertexBuffer	.bindBase	(GL_SHADER_STORAGE_BUFFER, TransformProgramDispatcher.VERTEX_BUFFER_IN_INDEX);
			varyingBuffer	.bindBase	(GL_SHADER_STORAGE_BUFFER, TransformProgramDispatcher.VARYING_BUFFER_INDEX);

			for (var uploader : builder.getMeshUploaders()) {
				var mesh		= uploader		.getServerMesh	();
				var meshCount	= (int) uploader.getMeshCount	();
				var meshSize	= (int) mesh	.size			();

				builder.getElementSegment()	.countElements		(builder.getMode().indexCount(meshSize * meshCount));
				uploader					.bindUploadBuffers	();

				meshSizeUniform				.uploadUnsignedInt	(meshSize);
				meshCountUniform			.uploadUnsignedInt	(meshCount);
				vertexOffsetUniform			.uploadUnsignedInt	(vertexOffset);

				program.dispatch(
						(meshCount	+ GROUP_SIZE - 1) / GROUP_SIZE,
						meshSize,
						DISPATCH_COUNT_Z
				);

				vertexOffset += (int) uploader.getUploadSize();
			}
		}

		program.resetProgram();
		program.waitBarriers();
	}
}
