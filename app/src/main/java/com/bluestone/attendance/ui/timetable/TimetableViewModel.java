package com.bluestone.attendance.ui.timetable;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.data.repository.TimetableRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimetableViewModel extends AndroidViewModel {

    private final TimetableRepository timetableRepository;
    private final SubjectRepository subjectRepository;

    private final LiveData<List<Subject>> subjects;
    private final LiveData<List<TimetableSession>> allSessions;
    private final MutableLiveData<Integer> selectedDay = new MutableLiveData<>(
            LocalDate.now().getDayOfWeek().getValue() <= 5 ? LocalDate.now().getDayOfWeek().getValue() : 1
    );

    private final MediatorLiveData<List<TimetableSession>> filteredSessions = new MediatorLiveData<>();

    public TimetableViewModel(@NonNull Application application) {
        super(application);
        this.timetableRepository = new TimetableRepository(application);
        this.subjectRepository = new SubjectRepository(application);

        this.subjects = subjectRepository.getAllActiveSubjects();
        this.allSessions = timetableRepository.getAllSessions();

        Runnable updateFiltered = () -> {
            List<TimetableSession> list = allSessions.getValue();
            Integer day = selectedDay.getValue();

            if (list == null) {
                filteredSessions.setValue(Collections.emptyList());
                return;
            }

            if (day == null || day == 0) {
                // All days
                filteredSessions.setValue(list);
            } else {
                List<TimetableSession> dayList = new ArrayList<>();
                for (TimetableSession s : list) {
                    if (s.getDayOfWeek() == day) {
                        dayList.add(s);
                    }
                }
                filteredSessions.setValue(dayList);
            }
        };

        filteredSessions.addSource(allSessions, s -> updateFiltered.run());
        filteredSessions.addSource(selectedDay, d -> updateFiltered.run());
    }

    public LiveData<List<Subject>> getSubjects() {
        return subjects;
    }

    public LiveData<Integer> getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(int day) {
        selectedDay.setValue(day);
    }

    public LiveData<List<TimetableSession>> getFilteredSessions() {
        return filteredSessions;
    }

    public void checkOverlap(int day, LocalTime start, LocalTime end, long excludeId, TimetableRepository.OverlapCheckCallback callback) {
        timetableRepository.checkOverlap(day, start, end, excludeId, callback);
    }

    public void saveSession(TimetableSession session, Runnable onComplete) {
        timetableRepository.saveSession(session, onComplete);
    }

    public void deleteSession(long id, Runnable onComplete) {
        timetableRepository.deleteSession(id, onComplete);
    }
}
