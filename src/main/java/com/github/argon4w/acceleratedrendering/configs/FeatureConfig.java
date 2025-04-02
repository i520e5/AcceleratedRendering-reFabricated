package com.github.argon4w.acceleratedrendering.configs;

import com.github.argon4w.acceleratedrendering.core.meshes.MeshType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FeatureConfig {

    public static final FeatureConfig CONFIG;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.IntValue corePooledBufferSetSize;
    public final ForgeConfigSpec.IntValue corePooledElementBufferSize;
    public final ForgeConfigSpec.IntValue coreCachedImageSize;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> coreDebugContextEnabled;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> coreForceTranslucentAcceleration;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> coreCacheIdenticalPose;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> acceleratedEntityRenderingFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<PipelineSetting> acceleratedEntityRenderingDefaultPipeline;
    public final ForgeConfigSpec.ConfigValue<MeshType> acceleratedEntityRenderingMeshType;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> acceleratedTextRenderingFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<PipelineSetting> acceleratedTextRenderingDefaultPipeline;
    public final ForgeConfigSpec.ConfigValue<MeshType> acceleratedTextRenderingMeshType;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> acceleratedItemRenderingFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> acceleratedItemRenderingBakeMeshForQuads;
    public final ForgeConfigSpec.ConfigValue<PipelineSetting> acceleratedItemRenderingDefaultPipeline;
    public final ForgeConfigSpec.ConfigValue<MeshType> acceleratedItemRenderingMeshType;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> acceleratedBlockEntityRenderingFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<PipelineSetting> acceleratedBlockEntityRenderingDefaultPipeline;
    public final ForgeConfigSpec.ConfigValue<MeshType> acceleratedBlockEntityRenderingMeshType;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> orientationCullingFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> orientationCullingDefaultCulling;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> orientationCullingIgnoreCullState;

    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatFeatureStatus;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatOrientationCullingCompat;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatShadowCulling;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatEntitiesCompat;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatPolygonProcessing;
    public final ForgeConfigSpec.ConfigValue<FeatureStatus> irisCompatFastRenderTypeCheck;

    static {
        Pair<FeatureConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(FeatureConfig::new);

        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    private FeatureConfig(ForgeConfigSpec.Builder builder) {
        builder
                .comment("Core Settings")
                .comment("Core Settings allows you to change setting that are related to all rendering features.")
                .translation("acceleratedrendering.configuration.core_settings")
                .push("core_settings");

        corePooledBufferSetSize = builder
                .worldRestart()
                .comment("Count of buffer sets that holds data for in-flight frame rendering.")
                .comment("Changing this value may affects your FPS. Smaller value means less in-flight frames, while larger values means more in-flight frames. More in-flight frames means more FPS but more VRAM.")
                .translation("acceleratedrendering.configuration.core_settings.pooled_buffer_set_size")
                .defineInRange("pooled_buffer_set_size", 5, 1, Integer.MAX_VALUE);

        corePooledElementBufferSize = builder
                .worldRestart()
                .comment("Count of batches of RenderTypes that is allowed in a draw call.")
                .comment("Changing this value may affects your FPS. Smaller value means less batches allowed in a draw call, while larger values means more batches. More batches means more FPS but more VRAM and more CPU pressure on handling RenderTypes.")
                .translation("acceleratedrendering.configuration.core_settings.pooled_element_buffer_size")
                .defineInRange("pooled_element_buffer_size", 32, 1, Integer.MAX_VALUE);

        coreCachedImageSize = builder
                .comment("Count of images that cached for static mesh culling.")
                .comment("Changing this value may affects your FPS. Smaller value means less images allowed to be cached, while larger means more cached images. More cached images means more FPS but more RAM pressure.")
                .translation("acceleratedrendering.configuration.core_settings.cached_image_size")
                .defineInRange("cached_image_size", 32, 1, Integer.MAX_VALUE);

        coreDebugContextEnabled = builder
                .comment("- DISABLED: Debug context will be disabled, which may cause significant rendering glitches on some NVIDIA cards because of the \"theaded optimization\".")
                .comment("- ENABLED: Debug context will be enabled, which can prevent NVIDIA driver from applying the \"threaded optimization\" that causes the glitches.")
                .translation("acceleratedrendering.configuration.core_settings.debug_context")
                .worldRestart()
                .defineEnum("debug_context", FeatureStatus.ENABLED);

        coreForceTranslucentAcceleration = builder
                .comment("- DISABLED: Translucent RenderType will fallback to vanilla rendering pipeline if the accelerated pipeline does not support translucent sorting unless mods explicitly enable force translucent acceleration temporarily when rendering their own faces.")
                .comment("- ENABLED: Translucent RenderType will still be rendered in accelerated pipeline even if the pipeline does not support translucent sorting unless mods explicitly disable force translucent acceleration temporarily when rendering their own faces.")
                .translation("acceleratedrendering.configuration.core_settings.force_translucent_acceleration")
                .defineEnum("force_translucent_acceleration", FeatureStatus.DISABLED);

        coreCacheIdenticalPose = builder
                .comment("- DISABLED: Poses with identical transform matrix and normal matrix that used to transform vertices will not be cached in buffer which slightly decreases CPU pressure but increase VRAM usage unless mods explicitly disable it when rendering.")
                .comment("- ENABLED: Poses with identical transform matrix and normal matrix that used to transform vertices will be cached in buffer which save VRAM but slightly increase CPU pressure unless mods explicitly disable it when rendering.")
                .translation("acceleratedrendering.configuration.core_settings.cache_identical_pose")
                .defineEnum("cache_identical_pose", FeatureStatus.ENABLED);

        builder.pop();

        builder
                .comment("Accelerated Entity Rendering Settings")
                .comment("Accelerated Entity Rendering uses GPU to cache and transform vertices while rendering model parts of entities, instead of generating and transforming vertices every time the model parts are rendered in CPU.")
                .translation("acceleratedrendering.configuration.accelerated_entity_rendering")
                .push("accelerated_entity_rendering");

        acceleratedEntityRenderingFeatureStatus = builder
                .comment("- DISABLED: Disable accelerated entity rendering.")
                .comment("- ENABLED: Enable accelerated entity rendering.")
                .translation("acceleratedrendering.configuration.accelerated_entity_rendering.feature_status")
                .defineEnum("feature_status", FeatureStatus.ENABLED);

        acceleratedEntityRenderingDefaultPipeline = builder
                .comment("- VANILLA: Entities will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own entities.")
                .comment("- ACCELERATED: All entities will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own entities.")
                .translation("acceleratedrendering.configuration.accelerated_entity_rendering.default_pipeline")
                .defineEnum("default_pipeline", PipelineSetting.ACCELERATED);

        acceleratedEntityRenderingMeshType = builder
                .worldRestart()
                .comment("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
                .comment("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
                .translation("acceleratedrendering.configuration.accelerated_entity_rendering.mesh_type")
                .defineEnum("mesh_type", MeshType.SERVER);

        builder.pop();

        builder
                .comment("Accelerated Block Entity Rendering Settings")
                .comment("Accelerated Block Entity Rendering uses GPU to cache and transform vertices while rendering block entities, instead of generating and transforming vertices every time the block entities are rendered in CPU.")
                .translation("acceleratedrendering.configuration.accelerated_block_entity_rendering")
                .push("accelerated_block_entity_rendering");

        acceleratedBlockEntityRenderingFeatureStatus = builder
                .comment("- DISABLED: Disable accelerated block entity rendering.")
                .comment("- ENABLED: Enable accelerated block entity rendering.")
                .translation("acceleratedrendering.configuration.accelerated_block_entity_rendering.feature_status")
                .defineEnum("feature_status", FeatureStatus.ENABLED);

        acceleratedBlockEntityRenderingDefaultPipeline = builder
                .comment("- VANILLA: Block entities will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own block entities.")
                .comment("- ACCELERATED: All block entities will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own block entities.")
                .translation("acceleratedrendering.configuration.accelerated_block_entity_rendering.default_pipeline")
                .defineEnum("default_pipeline", PipelineSetting.ACCELERATED);

        acceleratedBlockEntityRenderingMeshType = builder
                .worldRestart()
                .comment("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
                .comment("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
                .translation("acceleratedrendering.configuration.accelerated_block_entity_rendering.mesh_type")
                .defineEnum("mesh_type", MeshType.SERVER);

        builder.pop();

        builder
                .comment("Accelerated Item Rendering Settings")
                .comment("Accelerated Item Rendering uses GPU to cache and transform vertices while rendering item models, instead of generating and transforming vertices every time the item models are rendered in CPU.")
                .translation("acceleratedrendering.configuration.accelerated_item_rendering")
                .push("accelerated_item_rendering");

        acceleratedItemRenderingFeatureStatus = builder
                .comment("- DISABLED: Disable accelerated item rendering.")
                .comment("- ENABLED: Enable accelerated item rendering.")
                .translation("acceleratedrendering.configuration.accelerated_item_rendering.feature_status")
                .defineEnum("feature_status", FeatureStatus.ENABLED);

        acceleratedItemRenderingBakeMeshForQuads = builder
                .comment("- DISABLED: Accelerated Rendering will not bake mesh for quads provided by dynamic item models (something that is not SimpleBakedModel) unless mods explicitly enable it temporarily when rendering their own item models.")
                .comment("- ENABLED: Accelerated Rendering will bake mesh for all quads provided by dynamic item models (something that is not SimpleBakedModel) unless mods explicitly disable it temporarily when rendering their own item models, which will accelerate the rendering of these models but will crash if they keep allocating new quad data. (but who will?)")
                .translation("acceleratedrendering.configuration.accelerated_item_rendering.bake_mesh_for_quads")
                .defineEnum("bake_mesh_for_quads", FeatureStatus.ENABLED);

        acceleratedItemRenderingDefaultPipeline = builder
                .comment("- VANILLA: Item models will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own item models.")
                .comment("- ACCELERATED: All item models will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own item models.")
                .translation("acceleratedrendering.configuration.accelerated_item_rendering.default_pipeline")
                .defineEnum("default_pipeline", PipelineSetting.ACCELERATED);

        acceleratedItemRenderingMeshType = builder
                .worldRestart()
                .comment("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
                .comment("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
                .translation("acceleratedrendering.configuration.accelerated_item_rendering.mesh_type")
                .defineEnum("mesh_type", MeshType.SERVER);

        builder.pop();

        builder
                .comment("Accelerated Text Rendering Settings")
                .comment("Accelerated Text Rendering uses GPU to cache and transform vertices while rendering text through BakedGlyph, instead of generating and transforming vertices every time the text are rendered in CPU.")
                .translation("acceleratedrendering.configuration.accelerated_text_rendering")
                .push("accelerated_text_rendering");

        acceleratedTextRenderingFeatureStatus = builder
                .comment("- DISABLED: Disable accelerated text rendering.")
                .comment("- ENABLED: Enable accelerated text rendering.")
                .translation("acceleratedrendering.configuration.accelerated_text_rendering.feature_status")
                .defineEnum("feature_status", FeatureStatus.ENABLED);

        acceleratedTextRenderingDefaultPipeline = builder
                .comment("- VANILLA: Text will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own text.")
                .comment("- ACCELERATED: All text will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own text.")
                .translation("acceleratedrendering.configuration.accelerated_text_rendering.default_pipeline")
                .defineEnum("default_pipeline", PipelineSetting.ACCELERATED);

        acceleratedTextRenderingMeshType = builder
                .worldRestart()
                .comment("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
                .comment("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
                .translation("acceleratedrendering.configuration.accelerated_text_rendering.mesh_type")
                .defineEnum("mesh_type", MeshType.SERVER);

        builder.pop();

        builder
                .comment("Simple Orientation Face Culling Settings")
                .comment("Simple Orientation face culling uses an compute shader before the draw call to discard faces that is not visible on screen by checking if it is facing to the screen using a determinant of 3 * 3 matrix.")
                .translation("acceleratedrendering.configuration.orientation_culling")
                .push("orientation_culling");

        orientationCullingFeatureStatus = builder
                .comment("- DISABLED: Disable simple orientation face culling.")
                .comment("- ENABLED: Enable simple orientation face culling.")
                .translation("acceleratedrendering.configuration.orientation_culling.feature_status")
                .defineEnum("feature_Status", FeatureStatus.ENABLED);

        orientationCullingDefaultCulling = builder
                .comment("- DISABLED: Faces will not be culled unless mods explicitly enable it temporarily when rendering their own faces.")
                .comment("- ENABLED: All faces will be culled unless mods explicitly disable it temporarily when rendering their own faces.")
                .translation("acceleratedrendering.configuration.orientation_culling.default_culling")
                .defineEnum("default_culling", FeatureStatus.ENABLED);

        orientationCullingIgnoreCullState = builder
                .comment("- DISABLED: Simple orientation face culling will not cull entities that are not declared as \"cullable\".")
                .comment("- ENABLED: Simple orientation face culling will cull all entities even if they are not declared as \"cullable\".")
                .translation("acceleratedrendering.configuration.orientation_culling.ignore_cull_state")
                .defineEnum("ignore_cull_state", FeatureStatus.DISABLED);

        builder.pop();

        builder
                .comment("Iris Compatibility Settings")
                .comment("Iris Compatibility Settings allows Accelerated Rendering to work correctly with Iris.")
                .translation("acceleratedrendering.configuration.iris_compatibility")
                .push("iris_compatibility");

        irisCompatFeatureStatus = builder
                .comment("- DISABLED: Accelerated Rendering will be incompatible with Iris and cause visual glitches when working with Iris.")
                .comment("- ENABLED: Accelerated Rendering will use compute shaders that fits Iris's vertex formats, which make it compatible with Iris.")
                .translation("acceleratedrendering.configuration.iris_compatibility.feature_status")
                .defineEnum("feature_status", FeatureStatus.ENABLED);

        irisCompatOrientationCullingCompat = builder
                .comment("- DISABLED: Simple Orientation culling will not work with Iris because the culling shader is for vanilla's vertex formats.")
                .comment("- ENABLED: Simple Orientation culling will use another culling shader that fits iris's vertex format, which make it compatible with Iris.")
                .translation("acceleratedrendering.configuration.iris_compatibility.orientation_culling_compatibility")
                .defineEnum("orientation_culling_compatibility", FeatureStatus.ENABLED);

        irisCompatShadowCulling = builder
                .comment("- DISABLED: Entities will not be culled when they are rendered as shadows unless mods explicitly enable it temporarily when rendering their own shadows. Which reduce FPS due to redundant faces.")
                .comment("- ENABLED: Entities will be culled when they are rendered as shadows unless mods explicitly disable it temporarily when rendering their own shadows. Redundant faces will be culled and improve FPS, but it may cause incorrect shadows.")
                .translation("acceleratedrendering.configuration.iris_compatibility.shadow_culling")
                .defineEnum("shadow_culling", FeatureStatus.ENABLED);

        irisCompatEntitiesCompat = builder
                .comment("- DISABLED: renderEntity called from Iris will not render entity into the accelerated pipeline.")
                .comment("- ENABLED: renderEntity called from Iris will render entity into the accelerated pipeline.")
                .translation("acceleratedrendering.configuration.iris_compatibility.entities_compatibility")
                .defineEnum("entities_compatibility", FeatureStatus.ENABLED);

        irisCompatPolygonProcessing = builder
                .comment("- DISABLED: Extra information in vertices provided by Iris will not be included or calculated in the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own faces, which may cause visual glitches or incorrect rendering.")
                .comment("- ENABLED: Extra information in vertices provided by Iris will be included and calculated in the accelerated pipeline by a compute shader unless mods explicitly disable it temporarily when rendering their own faces.")
                .translation("acceleratedrendering.configuration.iris_compatibility.polygon_processing")
                .defineEnum("polygon_processing", FeatureStatus.ENABLED);

        irisCompatFastRenderTypeCheck = builder
                .comment("- DISABLED: Accelerated Rendering will use slow but safe \"instanceof\" operation in checking wrapped RenderType created by Iris.")
                .comment("- ENABlED: Accelerated Rendering will use extension interface in checking wrapped RenderType created by Iris, which is faster but unsafe if other mods also implemented \"WrappableRenderType\". (but who will?)")
                .translation("acceleratedrendering.configuration.iris_compatability.fast_render_type_check")
                .defineEnum("fast_render_type_check", FeatureStatus.ENABLED);

        builder.pop();
    }
}
