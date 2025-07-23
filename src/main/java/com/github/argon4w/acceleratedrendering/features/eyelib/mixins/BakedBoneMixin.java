package com.github.argon4w.acceleratedrendering.features.eyelib.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.tt432.eyelib.client.model.bake.BakedModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;

import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(BakedModel.BakedBone	.class)
public class BakedBoneMixin implements IAcceleratedRenderer<Void> {

	@Shadow @Final private	int							vertexSize;
	@Shadow @Final private	float[]						position;
	@Shadow @Final private	float[]						normal;
	@Shadow @Final private	float[]						u;
	@Shadow @Final private	float[]						v;

	@Unique private final	Map<IBufferGraph, IMesh>	meshes = new Object2ObjectOpenHashMap<>();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transform, normal);

		if (mesh != null) {
			mesh.write(
					extension,
					color,
					light,
					overlay
			);

			extension.endTransform();
			return;
		}

		var culledMeshCollector	= new CulledMeshCollector(extension.getRenderType(), extension.getBufferSet().getBufferEnvironment().getLayout());
		var meshBuilder			= extension.decorate		(culledMeshCollector);

		for (var i = 0; i < vertexSize; i ++) {
			meshBuilder.addVertex(
					position[i * 3 + 0],
					position[i * 3 + 1],
					position[i * 3 + 2],
					0xFF_FF_FF_FF,
					u[i],
					v[i],
					overlay,
					0,
					this.normal[i * 3 + 0],
					this.normal[i * 3 + 1],
					this.normal[i * 3 + 2]
			);
		}

		culledMeshCollector.flush();

		mesh = AcceleratedEntityRenderingFeature
				.getMeshType()
				.getBuilder	()
				.build		(culledMeshCollector);

		meshes	.put	(extension, mesh);
		mesh	.write	(
				extension,
				color,
				light,
				overlay
		);

		extension.endTransform();
	}
}
