package com.github.argon4w.acceleratedrendering.core.programs.overrides;

import net.minecraft.client.renderer.RenderType;

import java.util.Map;

public interface IShaderProgramOverrides {

	Map<RenderType, ITransformShaderProgramOverride> getTransformOverrides();
	Map<RenderType, IUploadingShaderProgramOverride> getUploadingOverrides();
}
