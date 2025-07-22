package com.github.argon4w.acceleratedrendering.core.programs.culling;

import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;

public interface ICullingProgramDispatcher extends IPolygonProgramDispatcher {

	boolean shouldCull();
}
