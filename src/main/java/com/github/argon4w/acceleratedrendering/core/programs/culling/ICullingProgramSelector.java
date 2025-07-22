package com.github.argon4w.acceleratedrendering.core.programs.culling;

import net.minecraft.client.renderer.RenderType;

public interface ICullingProgramSelector {

	ICullingProgramDispatcher select(RenderType renderType);
}
