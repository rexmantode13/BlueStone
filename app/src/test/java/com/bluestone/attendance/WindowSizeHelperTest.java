package com.bluestone.attendance;

import static org.junit.Assert.*;

import com.bluestone.attendance.domain.model.WindowSizeClass;
import com.bluestone.attendance.utils.WindowSizeHelper;

import org.junit.Test;

public class WindowSizeHelperTest {

    @Test
    public void testBreakpoints() {
        // Compact: < 600dp
        assertEquals(WindowSizeClass.COMPACT, WindowSizeHelper.getWindowSizeClassFromWidthDp(320f));
        assertEquals(WindowSizeClass.COMPACT, WindowSizeHelper.getWindowSizeClassFromWidthDp(599f));
        assertEquals(WindowSizeClass.COMPACT, WindowSizeHelper.getWindowSizeClassFromWidthDp(599.9f));

        // Medium: 600dp - 839dp
        assertEquals(WindowSizeClass.MEDIUM, WindowSizeHelper.getWindowSizeClassFromWidthDp(600f));
        assertEquals(WindowSizeClass.MEDIUM, WindowSizeHelper.getWindowSizeClassFromWidthDp(720f));
        assertEquals(WindowSizeClass.MEDIUM, WindowSizeHelper.getWindowSizeClassFromWidthDp(839f));
        assertEquals(WindowSizeClass.MEDIUM, WindowSizeHelper.getWindowSizeClassFromWidthDp(839.9f));

        // Expanded: >= 840dp
        assertEquals(WindowSizeClass.EXPANDED, WindowSizeHelper.getWindowSizeClassFromWidthDp(840f));
        assertEquals(WindowSizeClass.EXPANDED, WindowSizeHelper.getWindowSizeClassFromWidthDp(1200f));
        assertEquals(WindowSizeClass.EXPANDED, WindowSizeHelper.getWindowSizeClassFromWidthDp(1920f));
    }

    @Test
    public void testSpanCounts() {
        assertEquals(1, WindowSizeHelper.getRecommendedSpanCount(WindowSizeClass.COMPACT));
        assertEquals(2, WindowSizeHelper.getRecommendedSpanCount(WindowSizeClass.MEDIUM));
        assertEquals(3, WindowSizeHelper.getRecommendedSpanCount(WindowSizeClass.EXPANDED));
    }
}
