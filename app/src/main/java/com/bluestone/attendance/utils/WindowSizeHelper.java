package com.bluestone.attendance.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.bluestone.attendance.domain.model.WindowSizeClass;

public final class WindowSizeHelper {

    public static final float BREAKPOINT_COMPACT_MAX = 599f;
    public static final float BREAKPOINT_MEDIUM_MIN = 600f;
    public static final float BREAKPOINT_MEDIUM_MAX = 839f;
    public static final float BREAKPOINT_EXPANDED_MIN = 840f;

    private WindowSizeHelper() {}

    /**
     * Pure testable calculation of WindowSizeClass given width in density-independent pixels (dp).
     */
    @NonNull
    public static WindowSizeClass getWindowSizeClassFromWidthDp(float widthDp) {
        if (widthDp < BREAKPOINT_MEDIUM_MIN) {
            return WindowSizeClass.COMPACT;
        } else if (widthDp < BREAKPOINT_EXPANDED_MIN) {
            return WindowSizeClass.MEDIUM;
        } else {
            return WindowSizeClass.EXPANDED;
        }
    }

    /**
     * Computes the current WindowSizeClass for an Activity using androidx.window WindowMetricsCalculator.
     */
    @NonNull
    public static WindowSizeClass computeWindowSizeClass(@NonNull Activity activity) {
        WindowMetrics metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity);
        Rect bounds = metrics.getBounds();
        float density = activity.getResources().getDisplayMetrics().density;
        if (density <= 0f) {
            density = 1.0f;
        }
        float widthDp = bounds.width() / density;
        return getWindowSizeClassFromWidthDp(widthDp);
    }

    /**
     * Helper to get recommended grid span count based on WindowSizeClass.
     */
    public static int getRecommendedSpanCount(@NonNull WindowSizeClass sizeClass) {
        switch (sizeClass) {
            case EXPANDED:
                return 3;
            case MEDIUM:
                return 2;
            case COMPACT:
            default:
                return 1;
        }
    }
}
