# =====================================================
# BlueStone ProGuard / R8 Rules
# =====================================================

# Keep Room entities — Room uses reflection for column mapping
-keepclassmembers class com.bluestone.attendance.data.entities.** { *; }

# Keep DAO interfaces — Room generates implementation at compile time
-keep interface com.bluestone.attendance.data.dao.** { *; }

# Keep enum values — needed for Room TypeConverters
-keepclassmembers enum com.bluestone.attendance.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep PasswordHasher — uses crypto API reflectively
-keep class com.bluestone.attendance.utils.PasswordHasher { *; }

# Keep Converters — Room TypeConverters must not be stripped
-keep class com.bluestone.attendance.data.database.Converters { *; }

# AndroidX + Material Components
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# Prevent stripping of Parcelable creators
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
