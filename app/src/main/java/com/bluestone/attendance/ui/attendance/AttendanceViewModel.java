package com.bluestone.attendance.ui.attendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.data.repository.AttendanceRepository;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.data.repository.TimetableRepository;
import com.bluestone.attendance.domain.model.AttendanceStatus;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.ui.dashboard.DashboardViewModel.TimetableSessionWithSubject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;

    private final LiveData<List<Subject>> subjects;
    private final LiveData<List<AttendanceRecord>> allRecords;
    private final LiveData<List<SubjectAttendanceSummary>> summaries;
    private final MediatorLiveData<List<TimetableSessionWithSubject>> todaySessions = new MediatorLiveData<>();

    private final MutableLiveData<Long> selectedSubjectId = new MutableLiveData<>(-1L);
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<Integer> selectedSession = new MutableLiveData<>(1);
    private final MutableLiveData<AttendanceStatus> selectedStatus = new MutableLiveData<>(AttendanceStatus.PRESENT);
    private final MutableLiveData<YearMonth> currentMonth = new MutableLiveData<>(YearMonth.now());

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        this.attendanceRepository = AttendanceRepository.getInstance(application);
        this.subjectRepository = SubjectRepository.getInstance(application);
        this.timetableRepository = TimetableRepository.getInstance(application);

        this.subjects = subjectRepository.getAllActiveSubjects();
        this.allRecords = attendanceRepository.getAllRecords();
        this.summaries = attendanceRepository.getSubjectAttendanceSummaries();

        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        LiveData<List<TimetableSession>> rawSessions = timetableRepository.getSessionsByDay(currentDayOfWeek);
        LiveData<List<Subject>> subjectsLive = subjectRepository.getAllActiveSubjects();

        Runnable updateToday = () -> {
            com.bluestone.attendance.data.repository.AppExecutor.getInstance().diskIO().execute(() -> {
                List<TimetableSession> sessions = rawSessions.getValue();
                List<Subject> subjList = subjectsLive.getValue();
                if (sessions == null || subjList == null) return;

                Map<Long, Subject> map = new HashMap<>();
                for (Subject s : subjList) {
                    map.put(s.getId(), s);
                }

                List<TimetableSessionWithSubject> list = new ArrayList<>();
                for (TimetableSession ts : sessions) {
                    list.add(new TimetableSessionWithSubject(ts, map.get(ts.getSubjectId())));
                }
                todaySessions.postValue(list);
            });
        };

        todaySessions.addSource(rawSessions, s -> updateToday.run());
        todaySessions.addSource(subjectsLive, s -> updateToday.run());
    }

    public LiveData<List<Subject>> getSubjects() {
        return subjects;
    }

    public LiveData<List<AttendanceRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<List<SubjectAttendanceSummary>> getSummaries() {
        return summaries;
    }

    public LiveData<List<TimetableSessionWithSubject>> getTodaySessions() {
        return todaySessions;
    }

    public LiveData<Long> getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(long id) {
        selectedSubjectId.setValue(id);
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<Integer> getSelectedSession() {
        return selectedSession;
    }

    public void setSelectedSession(int session) {
        selectedSession.setValue(session);
    }

    public LiveData<AttendanceStatus> getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(AttendanceStatus status) {
        selectedStatus.setValue(status);
    }

    public LiveData<YearMonth> getCurrentMonth() {
        return currentMonth;
    }

    public void nextMonth() {
        YearMonth ym = currentMonth.getValue();
        if (ym != null) currentMonth.setValue(ym.plusMonths(1));
    }

    public void prevMonth() {
        YearMonth ym = currentMonth.getValue();
        if (ym != null) currentMonth.setValue(ym.minusMonths(1));
    }

    public void saveAttendance(long subjectId, LocalDate date, int session,
                               AttendanceStatus status, String notes, Runnable onComplete) {
        AttendanceRecord record = new AttendanceRecord(subjectId, date, session, status, notes, false);
        attendanceRepository.saveRecord(record, onComplete);
    }

    public void deleteAttendance(long recordId, Runnable onComplete) {
        attendanceRepository.deleteRecord(recordId, onComplete);
    }
}
