package com.bluestone.attendance.utils;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;

public final class AttendanceCalculator {

    public static final double NO_DATA = -1.0;

    private AttendanceCalculator() {}

    /**
     * Determines whether a record is eligible to be counted in attendance percentage.
     * Excludes unmarked, future dates, and cancelled sessions.
     */
    public static boolean isEligibleRecord(AttendanceRecord record, LocalDate today) {
        if (record == null) return false;
        if (record.isCancelled()) return false;
        if (record.getStatus() == AttendanceStatus.NOT_MARKED) return false;
        if (record.getDate() != null && record.getDate().isAfter(today)) return false;
        return true;
    }

    /**
     * Calculates attendance percentage from present and absent counts.
     * Returns NO_DATA (-1.0) if total eligible (present + absent) is 0.
     */
    public static double calculatePercentage(int presentCount, int absentCount) {
        int eligible = presentCount + absentCount;
        if (eligible <= 0) {
            return NO_DATA;
        }
        return ((double) presentCount / eligible) * 100.0;
    }

    /**
     * Calculates percentage from a list of records relative to a reference date.
     */
    public static double calculatePercentageFromRecords(Collection<AttendanceRecord> records, LocalDate today) {
        if (records == null || records.isEmpty()) {
            return NO_DATA;
        }
        int present = 0;
        int absent = 0;
        for (AttendanceRecord record : records) {
            if (isEligibleRecord(record, today)) {
                if (record.getStatus() == AttendanceStatus.PRESENT) {
                    present++;
                } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }
            }
        }
        return calculatePercentage(present, absent);
    }

    /**
     * Calculates projected consecutive future classes needed to reach target percentage.
     * Formula: (P + x) / (E + x) >= target / 100
     * x >= (target/100 * E - P) / (1 - target/100)
     */
    public static int calculateSessionsNeeded(int presentCount, int absentCount, double targetPercentage) {
        int eligible = presentCount + absentCount;
        if (eligible <= 0) {
            return 0;
        }
        double currentPercentage = calculatePercentage(presentCount, absentCount);
        if (currentPercentage >= targetPercentage) {
            return 0;
        }
        if (targetPercentage >= 100.0) {
            return (absentCount > 0) ? -1 : 0;
        }

        double t = targetPercentage / 100.0;
        double numerator = (t * eligible) - presentCount;
        double denominator = 1.0 - t;
        double needed = Math.ceil(numerator / denominator);
        return Math.max(0, (int) needed);
    }

    /**
     * Calculates how many consecutive classes a student can safely miss (bunk)
     * without falling below the target attendance percentage.
     * Formula: P / (E + m) >= target / 100
     * m <= (100 * P - target * E) / target
     */
    public static int calculateClassesCanMiss(int presentCount, int absentCount, double targetPercentage) {
        int eligible = presentCount + absentCount;
        if (eligible <= 0 || targetPercentage <= 0.0) {
            return 0;
        }
        double currentPercentage = calculatePercentage(presentCount, absentCount);
        if (currentPercentage < targetPercentage) {
            return 0;
        }
        double t = targetPercentage / 100.0;
        double maxMiss = (presentCount - (t * eligible)) / t;
        return Math.max(0, (int) Math.floor(maxMiss));
    }

    /**
     * Formats the percentage for display: "—" if no eligible sessions, otherwise "85.0%".
     */
    public static String formatPercentage(double percentage) {
        if (percentage < 0.0 || Double.isNaN(percentage)) {
            return "—";
        }
        return String.format(Locale.getDefault(), "%.1f%%", percentage);
    }
}
