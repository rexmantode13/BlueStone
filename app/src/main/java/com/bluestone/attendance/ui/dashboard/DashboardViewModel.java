package com.bluestone.attendance.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.data.entities.UserSettings;
import com.bluestone.attendance.data.repository.AttendanceRepository;
import com.bluestone.attendance.data.repository.SettingsRepository;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.data.repository.TimetableRepository;
import com.bluestone.attendance.domain.model.OverallAttendanceSummary;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardViewModel extends AndroidViewModel {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;
    private final SettingsRepository settingsRepository;

    private final LiveData<OverallAttendanceSummary> overallSummary;
    private final LiveData<List<SubjectAttendanceSummary>> subjectSummaries;
    private final LiveData<Student> student;
    private final LiveData<UserSettings> settings;
    private final MediatorLiveData<List<TimetableSessionWithSubject>> todaySessions = new MediatorLiveData<>();

    public static class TimetableSessionWithSubject {
        public final TimetableSession session;
        public final Subject subject;

        public TimetableSessionWithSubject(TimetableSession session, Subject subject) {
            this.session = session;
            this.subject = subject;
        }
    }

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.attendanceRepository = AttendanceRepository.getInstance(application);
        this.subjectRepository = SubjectRepository.getInstance(application);
        this.timetableRepository = TimetableRepository.getInstance(application);
        this.settingsRepository = SettingsRepository.getInstance(application);

        this.overallSummary = attendanceRepository.getOverallSummary();
        this.subjectSummaries = attendanceRepository.getSubjectAttendanceSummaries();
        this.student = settingsRepository.getActiveStudent();
        this.settings = settingsRepository.getSettings();

        // Calculate today's day of week (1=Monday ... 7=Sunday)
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        LiveData<List<TimetableSession>> rawSessions = timetableRepository.getSessionsByDay(currentDayOfWeek);
        LiveData<List<Subject>> subjectsLive = subjectRepository.getAllActiveSubjects();

        Runnable updateToday = () -> {
            com.bluestone.attendance.data.repository.AppExecutor.getInstance().diskIO().execute(() -> {
                List<TimetableSession> sessions = rawSessions.getValue();
                List<Subject> subjects = subjectsLive.getValue();
                if (sessions == null || subjects == null) return;

                Map<Long, Subject> map = new HashMap<>();
                for (Subject s : subjects) {
                    map.put(s.getId(), s);
                }

                List<TimetableSessionWithSubject> list = new java.util.ArrayList<>();
                for (TimetableSession ts : sessions) {
                    list.add(new TimetableSessionWithSubject(ts, map.get(ts.getSubjectId())));
                }
                todaySessions.postValue(list);
            });
        };

        todaySessions.addSource(rawSessions, s -> updateToday.run());
        todaySessions.addSource(subjectsLive, sub -> updateToday.run());
    }

    public LiveData<OverallAttendanceSummary> getOverallSummary() {
        return overallSummary;
    }

    public LiveData<List<SubjectAttendanceSummary>> getSubjectSummaries() {
        return subjectSummaries;
    }

    public LiveData<Student> getStudent() {
        return student;
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }

    public LiveData<List<TimetableSessionWithSubject>> getTodaySessions() {
        return todaySessions;
    }
}
