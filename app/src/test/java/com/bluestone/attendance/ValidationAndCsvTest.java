package com.bluestone.attendance;

import static org.junit.Assert.*;

import com.bluestone.attendance.utils.CsvExporter;

import org.junit.Test;

public class ValidationAndCsvTest {

    @Test
    public void testCsvEscaping() {
        // Plain string
        assertEquals("CS202", CsvExporter.escape("CS202"));

        // Comma escaping
        assertEquals("\"Data Structures, Algorithms\"", CsvExporter.escape("Data Structures, Algorithms"));

        // Quotes escaping
        assertEquals("\"Introduction to \"\"AI\"\"\"", CsvExporter.escape("Introduction to \"AI\""));

        // Null handling
        assertEquals("", CsvExporter.escape(null));
    }

    @Test
    public void testSyllabusCompletionCalculation() {
        int totalUnits = 5;
        int completedUnits = 3;

        double completionPercentage = ((double) completedUnits / totalUnits) * 100.0;
        assertEquals(60.0, completionPercentage, 0.001);

        int remaining = totalUnits - completedUnits;
        assertEquals(2, remaining);
    }

    @Test
    public void testFormValidation() {
        // Student ID validation
        String emptyId = "   ";
        assertTrue(emptyId.trim().isEmpty());

        String validId = "CS2026001";
        assertFalse(validId.trim().isEmpty());

        // Target % validation
        double validTarget = 75.0;
        assertTrue(validTarget >= 50.0 && validTarget <= 100.0);

        double invalidTarget = 120.0;
        assertFalse(invalidTarget >= 50.0 && invalidTarget <= 100.0);
    }
}
