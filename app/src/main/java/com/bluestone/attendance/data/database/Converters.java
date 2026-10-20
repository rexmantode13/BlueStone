package com.bluestone.attendance.data.database;

import androidx.room.TypeConverter;

import com.bluestone.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converters {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    @TypeConverter
    public static LocalDate toLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value, DATE_FORMATTER);
    }

    @TypeConverter
    public static String fromLocalTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    @TypeConverter
    public static LocalTime toLocalTime(String value) {
        return value == null ? null : LocalTime.parse(value, TIME_FORMATTER);
    }

    @TypeConverter
    public static String fromAttendanceStatus(AttendanceStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static AttendanceStatus toAttendanceStatus(String value) {
        return value == null ? null : AttendanceStatus.valueOf(value);
    }
}
