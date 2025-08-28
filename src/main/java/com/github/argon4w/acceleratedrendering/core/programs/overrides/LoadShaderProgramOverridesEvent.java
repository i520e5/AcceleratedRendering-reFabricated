package com.github.argon4w.acceleratedrendering.core.programs.overrides;

import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

@Getter
public class LoadShaderProgramOverridesEvent extends Event implements IModBusEvent, IShaderProgramOverrides {

	private final VertexFormat													vertexFormat;
	private final Object2ObjectMap<RenderType, ITransformShaderProgramOverride> transformOverrides;
	private final Object2ObjectMap<RenderType, IUploadingShaderProgramOverride>	uploadingOverrides;

	public LoadShaderProgramOverridesEvent(
			VertexFormat					vertexFormat,
			ITransformShaderProgramOverride	transformOverride,
			IUploadingShaderProgramOverride	uploadingOverride
	) {
		this.vertexFormat		= vertexFormat;
		this.transformOverrides	= new Object2ObjectOpenHashMap<>();
		this.uploadingOverrides	= new Object2ObjectOpenHashMap<>();

		this.transformOverrides.defaultReturnValue(transformOverride);
		this.uploadingOverrides.defaultReturnValue(uploadingOverride);
	}

	public void loadFor(
			VertexFormat			vertexFormat,
			RenderType				renderType,
			IShaderProgramOverride	override
	) {
		if (this.vertexFormat == vertexFormat) {
			switch (override) {
				case ITransformShaderProgramOverride transform	-> transformOverrides.put(renderType, transform);
				case IUploadingShaderProgramOverride uploading	-> uploadingOverrides.put(renderType, uploading);
				default											-> throw new UnsupportedOperationException("Unsupported override type: " + override.getClass().getSimpleName());
			}
		}
	}
}
