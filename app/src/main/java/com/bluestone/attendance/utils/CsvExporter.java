package com.bluestone.attendance.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class CsvExporter {

    private CsvExporter() {}

    /**
     * Writes attendance records and subject details into CSV format.
     */
    public static void writeAttendanceCsv(OutputStream os,
                                         List<AttendanceRecord> records,
                                         Map<Long, Subject> subjectMap) throws IOException {
        try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            // Write CSV Header
            writer.write("Record ID,Date,Subject Code,Subject Name,Session,Status,Cancelled,Notes\n");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            for (AttendanceRecord record : records) {
                Subject s = subjectMap.get(record.getSubjectId());
                String code = (s != null) ? s.getCode() : "UNKNOWN";
                String name = (s != null) ? s.getName() : "Unknown";

                writer.write(escape(String.valueOf(record.getId())));
                writer.write(",");
                writer.write(escape(record.getDate().format(formatter)));
                writer.write(",");
                writer.write(escape(code));
                writer.write(",");
                writer.write(escape(name));
                writer.write(",");
                writer.write(escape(String.valueOf(record.getSessionNumber())));
                writer.write(",");
                writer.write(escape(record.getStatus().name()));
                writer.write(",");
                writer.write(escape(record.isCancelled() ? "Yes" : "No"));
                writer.write(",");
                writer.write(escape(record.getNotes() != null ? record.getNotes() : ""));
                writer.write("\n");
            }
            writer.flush();
        }
    }

    /**
     * Escapes CSV values (RFC 4180 standard).
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Generates a temporary CSV file and returns a Share Intent via FileProvider.
     */
    public static Intent createShareIntent(Context context,
                                          List<AttendanceRecord> records,
                                          Map<Long, Subject> subjectMap) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "reports");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File csvFile = new File(cacheDir, "bluestone_attendance_report.csv");
        try (FileOutputStream fos = new FileOutputStream(csvFile)) {
            writeAttendanceCsv(fos, records, subjectMap);
        }

        Uri contentUri = FileProvider.getUriForFile(
                context,
                "com.bluestone.attendance.fileprovider",
                csvFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(shareIntent, "Share Attendance Report");
    }
}
