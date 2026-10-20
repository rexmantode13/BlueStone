package com.bluestone.attendance.domain.model;

public enum WindowSizeClass {
    COMPACT,   // < 600dp (standard phones)
    MEDIUM,    // 600dp - 839dp (foldables, small tablets)
    EXPANDED   // >= 840dp (large tablets, desktop windows)
}
