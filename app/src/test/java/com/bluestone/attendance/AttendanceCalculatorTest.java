package com.bluestone.attendance;

import static org.junit.Assert.*;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.domain.model.AttendanceStatus;
import com.bluestone.attendance.utils.AttendanceCalculator;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceCalculatorTest {

    @Test
    public void testPercentageCalculation() {
        // Normal: 8 present, 2 absent = 80.0%
        double pct = AttendanceCalculator.calculatePercentage(8, 2);
        assertEquals(80.0, pct, 0.001);

        // 100% attendance
        assertEquals(100.0, AttendanceCalculator.calculatePercentage(10, 0), 0.001);

        // 0% attendance
        assertEquals(0.0, AttendanceCalculator.calculatePercentage(0, 5), 0.001);

        // Empty / No data
        assertEquals(AttendanceCalculator.NO_DATA, AttendanceCalculator.calculatePercentage(0, 0), 0.001);
    }

    @Test
    public void testExclusions() {
        LocalDate today = LocalDate.of(2026, 10, 18);

        // Eligible record: past date, present
        AttendanceRecord eligible = new AttendanceRecord(1, today.minusDays(1), 1, AttendanceStatus.PRESENT, "", false);
        assertTrue(AttendanceCalculator.isEligibleRecord(eligible, today));

        // Exclude cancelled
        AttendanceRecord cancelled = new AttendanceRecord(1, today.minusDays(1), 1, AttendanceStatus.PRESENT, "", true);
        assertFalse(AttendanceCalculator.isEligibleRecord(cancelled, today));

        // Exclude unmarked
        AttendanceRecord unmarked = new AttendanceRecord(1, today.minusDays(1), 1, AttendanceStatus.NOT_MARKED, "", false);
        assertFalse(AttendanceCalculator.isEligibleRecord(unmarked, today));

        // Exclude future
        AttendanceRecord future = new AttendanceRecord(1, today.plusDays(2), 1, AttendanceStatus.PRESENT, "", false);
        assertFalse(AttendanceCalculator.isEligibleRecord(future, today));

        // Combined calculation with exclusions
        List<AttendanceRecord> records = new ArrayList<>();
        records.add(eligible); // 1 present
        records.add(new AttendanceRecord(1, today, 1, AttendanceStatus.ABSENT, "", false)); // 1 absent
        records.add(cancelled); // excluded
        records.add(unmarked); // excluded
        records.add(future); // excluded

        double calculated = AttendanceCalculator.calculatePercentageFromRecords(records, today);
        // 1 present out of 2 eligible = 50%
        assertEquals(50.0, calculated, 0.001);
    }

    @Test
    public void testRecoveryCalculation() {
        // Current: 6 present, 4 absent = 60%. Target = 75%.
        // Formula: (6 + x) / (10 + x) >= 0.75 -> 6 + x >= 7.5 + 0.75x -> 0.25x >= 1.5 -> x >= 6.
        int needed = AttendanceCalculator.calculateSessionsNeeded(6, 4, 75.0);
        assertEquals(6, needed);

        // Check if attending 6 more gives (6+6)/(10+6) = 12/16 = 75%
        assertEquals(75.0, AttendanceCalculator.calculatePercentage(6 + 6, 4), 0.001);

        // Already above target
        assertEquals(0, AttendanceCalculator.calculateSessionsNeeded(18, 2, 75.0));

        // No eligible sessions
        assertEquals(0, AttendanceCalculator.calculateSessionsNeeded(0, 0, 75.0));
    }

    @Test
    public void testFormatPercentage() {
        assertEquals("—", AttendanceCalculator.formatPercentage(AttendanceCalculator.NO_DATA));
        assertEquals("—", AttendanceCalculator.formatPercentage(Double.NaN));
        assertEquals("80.0%", AttendanceCalculator.formatPercentage(80.0));
        assertEquals("75.5%", AttendanceCalculator.formatPercentage(75.54));
    }
}
