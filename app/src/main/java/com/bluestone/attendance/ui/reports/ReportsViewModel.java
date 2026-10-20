package com.bluestone.attendance.ui.reports;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.UserSettings;
import com.bluestone.attendance.data.repository.AppExecutor;
import com.bluestone.attendance.data.repository.AttendanceRepository;
import com.bluestone.attendance.data.repository.SettingsRepository;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.domain.model.OverallAttendanceSummary;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.utils.CsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsViewModel extends AndroidViewModel {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final SettingsRepository settingsRepository;

    private final LiveData<OverallAttendanceSummary> overallSummary;
    private final LiveData<List<SubjectAttendanceSummary>> subjectSummaries;
    private final LiveData<UserSettings> settings;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        this.attendanceRepository = AttendanceRepository.getInstance(application);
        this.subjectRepository = SubjectRepository.getInstance(application);
        this.settingsRepository = SettingsRepository.getInstance(application);

        this.overallSummary = attendanceRepository.getOverallSummary();
        this.subjectSummaries = attendanceRepository.getSubjectAttendanceSummaries();
        this.settings = settingsRepository.getSettings();
    }

    public LiveData<OverallAttendanceSummary> getOverallSummary() {
        return overallSummary;
    }

    public LiveData<List<SubjectAttendanceSummary>> getSubjectSummaries() {
        return subjectSummaries;
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }

    public void exportCsvToUri(Context context, Uri uri, Runnable onSuccess, Runnable onError) {
        AppExecutor.getInstance().diskIO().execute(() -> {
            try {
                List<AttendanceRecord> records = attendanceRepository.getAllRecordsSync();
                List<Subject> subjects = subjectRepository.getAllActiveSubjectsSync();
                Map<Long, Subject> map = new HashMap<>();
                for (Subject s : subjects) {
                    map.put(s.getId(), s);
                }

                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    CsvExporter.writeAttendanceCsv(os, records, map);
                    if (onSuccess != null) {
                        AppExecutor.getInstance().mainThread().execute(onSuccess);
                    }
                } else if (onError != null) {
                    AppExecutor.getInstance().mainThread().execute(onError);
                }
            } catch (IOException e) {
                if (onError != null) {
                    AppExecutor.getInstance().mainThread().execute(onError);
                }
            }
        });
    }

    public void createShareIntent(Context context, ShareCallback callback) {
        AppExecutor.getInstance().diskIO().execute(() -> {
            try {
                List<AttendanceRecord> records = attendanceRepository.getAllRecordsSync();
                List<Subject> subjects = subjectRepository.getAllActiveSubjectsSync();
                Map<Long, Subject> map = new HashMap<>();
                for (Subject s : subjects) {
                    map.put(s.getId(), s);
                }

                Intent intent = CsvExporter.createShareIntent(context, records, map);
                AppExecutor.getInstance().mainThread().execute(() -> callback.onReady(intent));
            } catch (IOException e) {
                AppExecutor.getInstance().mainThread().execute(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public interface ShareCallback {
        void onReady(Intent intent);
        void onError(String message);
    }
}
