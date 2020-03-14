package com.xylink.sdk.sample.utils;

/**
 * 布局模式
 */
public enum LayoutMode {
    /**
     * 演讲者模式(一个大屏，多个小屏)
     */
    MODE_SPEAKER,
    /**
     * 画廊模式(多个相同尺寸的屏的排列组合，如，吅，品，田等)
     */
    MODE_GALLERY,
    /**
     * SDK的集成开发者可自定义自己的布局方式
     */
    MODE_CUSTOM,
}
