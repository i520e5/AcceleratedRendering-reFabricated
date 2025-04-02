package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;

import java.util.ArrayDeque;

public class CoreFeature {

    public static final ArrayDeque<FeatureStatus> FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK = new ArrayDeque<>();
    public static final ArrayDeque<FeatureStatus> CACHE_IDENTICAL_POSE_CONTROLLER_STACK = new ArrayDeque<>();

    public static boolean isDebugContextEnabled() {
        return FeatureConfig.CONFIG.coreDebugContextEnabled.get() == FeatureStatus.ENABLED;
    }

    public static int getPooledBufferSetSize() {
        return FeatureConfig.CONFIG.corePooledBufferSetSize.get();
    }

    public static int getPooledElementBufferSize() {
        return FeatureConfig.CONFIG.corePooledElementBufferSize.get();
    }

    public static int getCachedImageSize() {
        return FeatureConfig.CONFIG.coreCachedImageSize.get();
    }

    public static boolean shouldForceAccelerateTranslucent() {
        return getForceTranslucentAccelerationSetting() == FeatureStatus.ENABLED;
    }

    public static boolean shouldCacheIdenticalPose() {
        return getCacheIdenticalPoseSetting() == FeatureStatus.ENABLED;
    }

    public static void disableForceTranslucentAcceleration() {
        FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
    }

    public static void disableCacheIdenticalPose() {
        CACHE_IDENTICAL_POSE_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
    }

    public static void forceEnableForceTranslucentAcceleration() {
        FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
    }

    public static void forceEnableCacheIdenticalPose() {
        CACHE_IDENTICAL_POSE_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
    }

    public static void forceSetForceTranslucentAcceleration(FeatureStatus status) {
        FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.push(status);
    }

    public static void forceSetCacheIdenticalPose(FeatureStatus status) {
        CACHE_IDENTICAL_POSE_CONTROLLER_STACK.push(status);
    }

    public static void resetForceTranslucentAcceleration() {
        FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.pop();
    }

    public static void resetCacheIdenticalPose() {
        CACHE_IDENTICAL_POSE_CONTROLLER_STACK.pop();
    }

    public static FeatureStatus getForceTranslucentAccelerationSetting() {
        return FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultForceTranslucentAccelerationSetting() : FORCE_TRANSLUCENT_ACCELERATION_CONTROLLER_STACK.peek();
    }

    public static FeatureStatus getCacheIdenticalPoseSetting() {
        return CACHE_IDENTICAL_POSE_CONTROLLER_STACK.isEmpty() ? getDefaultCacheIdenticalPoseSetting() : CACHE_IDENTICAL_POSE_CONTROLLER_STACK.peek();
    }

    public static FeatureStatus getDefaultForceTranslucentAccelerationSetting() {
        return FeatureConfig.CONFIG.coreForceTranslucentAcceleration.get();
    }

    public static FeatureStatus getDefaultCacheIdenticalPoseSetting() {
        return FeatureConfig.CONFIG.coreCacheIdenticalPose.get();
    }
}
