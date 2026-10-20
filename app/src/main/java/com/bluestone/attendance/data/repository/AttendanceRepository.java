package com.bluestone.attendance.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.UserSettings;
import com.bluestone.attendance.domain.model.AttendanceStatus;
import com.bluestone.attendance.domain.model.OverallAttendanceSummary;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.utils.AttendanceCalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {

    private static volatile AttendanceRepository INSTANCE;
    private final AppDatabase db;
    private final AppExecutor executor;
    private MediatorLiveData<OverallAttendanceSummary> cachedOverallSummary;
    private MediatorLiveData<List<SubjectAttendanceSummary>> cachedSubjectSummaries;

    public static AttendanceRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AttendanceRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AttendanceRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public AttendanceRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    public LiveData<List<AttendanceRecord>> getAllRecords() {
        return db.attendanceDao().getAll();
    }

    public LiveData<List<AttendanceRecord>> getRecordsBySubject(long subjectId) {
        return db.attendanceDao().getBySubject(subjectId);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDate(LocalDate date) {
        return db.attendanceDao().getByDate(date);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDateRange(LocalDate start, LocalDate end) {
        return db.attendanceDao().getByDateRange(start, end);
    }

    public void saveRecord(AttendanceRecord record, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.attendanceDao().insert(record);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void deleteRecord(long recordId, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.attendanceDao().deleteById(recordId);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    /**
     * Returns a MediatorLiveData emitting overall attendance stats whenever subjects,
     * attendance records, or user settings change, computed smoothly on a background thread.
     */
    public synchronized LiveData<OverallAttendanceSummary> getOverallSummary() {
        if (cachedOverallSummary != null) {
            return cachedOverallSummary;
        }

        cachedOverallSummary = new MediatorLiveData<>();
        LiveData<List<AttendanceRecord>> recordsLive = db.attendanceDao().getAll();
        LiveData<List<Subject>> subjectsLive = db.subjectDao().getAllActive();
        LiveData<UserSettings> settingsLive = db.settingsDao().getSettings();

        Runnable update = () -> {
            executor.diskIO().execute(() -> {
                List<AttendanceRecord> records = recordsLive.getValue();
                List<Subject> subjects = subjectsLive.getValue();
                UserSettings settings = settingsLive.getValue();
                double defaultThreshold = (settings != null) ? settings.getAttendanceThreshold() : 75.0;

                if (records == null || subjects == null) {
                    return;
                }

                LocalDate today = LocalDate.now();
                int totalPresent = 0;
                int totalAbsent = 0;

                Map<Long, int[]> subjectCounts = new HashMap<>();
                for (Subject s : subjects) {
                    subjectCounts.put(s.getId(), new int[]{0, 0}); // [present, absent]
                }

                for (AttendanceRecord r : records) {
                    if (AttendanceCalculator.isEligibleRecord(r, today)) {
                        int[] counts = subjectCounts.get(r.getSubjectId());
                        if (r.getStatus() == AttendanceStatus.PRESENT) {
                            totalPresent++;
                            if (counts != null) counts[0]++;
                        } else if (r.getStatus() == AttendanceStatus.ABSENT) {
                            totalAbsent++;
                            if (counts != null) counts[1]++;
                        }
                    }
                }

                int atRiskCount = 0;
                for (Subject s : subjects) {
                    int[] counts = subjectCounts.get(s.getId());
                    if (counts != null) {
                        double pct = AttendanceCalculator.calculatePercentage(counts[0], counts[1]);
                        double target = s.getTargetPercentage() > 0 ? s.getTargetPercentage() : defaultThreshold;
                        if (pct != AttendanceCalculator.NO_DATA && pct < target) {
                            atRiskCount++;
                        }
                    }
                }

                int eligible = totalPresent + totalAbsent;
                double pct = AttendanceCalculator.calculatePercentage(totalPresent, totalAbsent);

                cachedOverallSummary.postValue(new OverallAttendanceSummary(
                        totalPresent,
                        totalAbsent,
                        eligible,
                        pct,
                        atRiskCount
                ));
            });
        };

        cachedOverallSummary.addSource(recordsLive, r -> update.run());
        cachedOverallSummary.addSource(subjectsLive, s -> update.run());
        cachedOverallSummary.addSource(settingsLive, set -> update.run());

        return cachedOverallSummary;
    }

    /**
     * Returns a MediatorLiveData emitting subject attendance summaries whenever
     * active subjects, records, or settings change, computed smoothly on a background thread.
     */
    public synchronized LiveData<List<SubjectAttendanceSummary>> getSubjectAttendanceSummaries() {
        if (cachedSubjectSummaries != null) {
            return cachedSubjectSummaries;
        }

        cachedSubjectSummaries = new MediatorLiveData<>();
        LiveData<List<Subject>> subjectsLive = db.subjectDao().getAllActive();
        LiveData<List<AttendanceRecord>> recordsLive = db.attendanceDao().getAll();
        LiveData<UserSettings> settingsLive = db.settingsDao().getSettings();

        Runnable update = () -> {
            executor.diskIO().execute(() -> {
                List<Subject> subjects = subjectsLive.getValue();
                List<AttendanceRecord> records = recordsLive.getValue();
                UserSettings settings = settingsLive.getValue();
                double defaultThreshold = (settings != null) ? settings.getAttendanceThreshold() : 75.0;

                if (subjects == null) {
                    return;
                }

                LocalDate today = LocalDate.now();
                Map<Long, List<AttendanceRecord>> map = new HashMap<>();
                if (records != null) {
                    for (AttendanceRecord r : records) {
                        if (!map.containsKey(r.getSubjectId())) {
                            map.put(r.getSubjectId(), new ArrayList<>());
                        }
                        map.get(r.getSubjectId()).add(r);
                    }
                }

                List<SubjectAttendanceSummary> result = new ArrayList<>();
                for (Subject s : subjects) {
                    List<AttendanceRecord> subRecords = map.get(s.getId());
                    int present = 0;
                    int absent = 0;
                    if (subRecords != null) {
                        for (AttendanceRecord r : subRecords) {
                            if (AttendanceCalculator.isEligibleRecord(r, today)) {
                                if (r.getStatus() == AttendanceStatus.PRESENT) present++;
                                else if (r.getStatus() == AttendanceStatus.ABSENT) absent++;
                            }
                        }
                    }
                    int eligible = present + absent;
                    double pct = AttendanceCalculator.calculatePercentage(present, absent);
                    double target = s.getTargetPercentage() > 0 ? s.getTargetPercentage() : defaultThreshold;
                    int needed = AttendanceCalculator.calculateSessionsNeeded(present, absent, target);
                    int canMiss = AttendanceCalculator.calculateClassesCanMiss(present, absent, target);
                    boolean below = (pct != AttendanceCalculator.NO_DATA && pct < target);

                    result.add(new SubjectAttendanceSummary(s, present, absent, eligible, pct, needed, canMiss, below));
                }

                cachedSubjectSummaries.postValue(result);
            });
        };

        cachedSubjectSummaries.addSource(subjectsLive, s -> update.run());
        cachedSubjectSummaries.addSource(recordsLive, r -> update.run());
        cachedSubjectSummaries.addSource(settingsLive, set -> update.run());

        return cachedSubjectSummaries;
    }

    public List<AttendanceRecord> getAllRecordsSync() {
        return db.attendanceDao().getAllSync();
    }
}
