package com.github.argon4w.acceleratedrendering.core.programs.overrides;

public interface ITransformShaderProgramOverride extends IShaderProgramOverride {

	void	uploadVarying		(long	varyingAddress,	int offset);
	int		dispatchTransform	(int	vertexCount,	int vertexOffset, int varyingOffset);
	long	getVaryingSize		();
}
