package com.github.argon4w.acceleratedrendering.features.eyelib.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.tt432.eyelib.client.model.Model;
import io.github.tt432.eyelib.client.model.bake.BakedModel;
import io.github.tt432.eyelib.client.model.locator.GroupLocator;
import io.github.tt432.eyelib.client.model.transformer.ModelTransformer;
import io.github.tt432.eyelib.client.render.RenderParams;
import io.github.tt432.eyelib.client.render.visitor.HighSpeedRenderModelVisitor;
import io.github.tt432.eyelib.client.render.visitor.ModelVisitContext;
import lombok.experimental.ExtensionMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ExtensionMethod(VertexConsumerExtension	.class)
@Mixin			(HighSpeedRenderModelVisitor.class)
public class HighSpeedRenderModelVisitorMixin {

	@SuppressWarnings("unchecked")
	@Inject(
			method		= "visitPreBone",
			cancellable	= true,
			at			= @At(
					value	= "INVOKE",
					target	= "Ljava/util/concurrent/atomic/AtomicBoolean;get()Z",
					shift	= At.Shift.BEFORE
			)
	)
	public void visitPreBoneFast(
			RenderParams									renderParams,
			ModelVisitContext								context,
			Model.Bone										group,
			@Coerce Object									data,
			GroupLocator									groupLocator,
			ModelTransformer<Model.Bone, ?>					transformer,
			CallbackInfo									ci,
			@Local(name = "last") PoseStack.Pose			last,
			@Local(name = "bakedBone") BakedModel.BakedBone	bakedBone
	) {
		var extension	= renderParams
				.consumer		()
				.getAccelerated	();

		if (		CoreFeature.isRenderingLevel										()
				&&	AcceleratedEntityRenderingFeature.isEnabled							()
				&&	AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	extension							.isAccelerated					()
		) {
			ci			.cancel		();
			extension	.doRender	(
					(IAcceleratedRenderer<Void>) (Object) bakedBone,
					null,
					last			.pose	(),
					last			.normal	(),
					renderParams	.light	(),
					renderParams	.overlay(),
					0xFF_FF_FF_FF
			);
		}
	}
}
