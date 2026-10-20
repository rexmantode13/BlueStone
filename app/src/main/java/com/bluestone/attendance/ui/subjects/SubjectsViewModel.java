package com.bluestone.attendance.ui.subjects;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.repository.AttendanceRepository;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SubjectsViewModel extends AndroidViewModel {

    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;

    private final LiveData<List<SubjectAttendanceSummary>> rawSummaries;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> semesterFilter = new MutableLiveData<>(0); // 0 = all
    private final MutableLiveData<Integer> sortMode = new MutableLiveData<>(0); // 0=name, 1=percentage, 2=code

    private final MediatorLiveData<List<SubjectAttendanceSummary>> filteredSummaries = new MediatorLiveData<>();

    public SubjectsViewModel(@NonNull Application application) {
        super(application);
        this.subjectRepository = new SubjectRepository(application);
        this.attendanceRepository = new AttendanceRepository(application);

        this.rawSummaries = attendanceRepository.getSubjectAttendanceSummaries();

        Runnable updateFiltered = () -> {
            List<SubjectAttendanceSummary> list = rawSummaries.getValue();
            String query = searchQuery.getValue();
            Integer sem = semesterFilter.getValue();
            Integer sort = sortMode.getValue();

            if (list == null) {
                filteredSummaries.setValue(Collections.emptyList());
                return;
            }

            List<SubjectAttendanceSummary> filtered = new ArrayList<>();
            for (SubjectAttendanceSummary item : list) {
                // Semester filter
                if (sem != null && sem > 0 && item.getSubject().getSemester() != sem) {
                    continue;
                }

                // Query filter
                if (query != null && !query.trim().isEmpty()) {
                    String q = query.trim().toLowerCase();
                    boolean matchName = item.getSubject().getName().toLowerCase().contains(q);
                    boolean matchCode = item.getSubject().getCode().toLowerCase().contains(q);
                    if (!matchName && !matchCode) {
                        continue;
                    }
                }

                filtered.add(item);
            }

            // Sort
            if (sort != null) {
                if (sort == 1) { // percentage desc
                    filtered.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));
                } else if (sort == 2) { // code asc
                    filtered.sort((a, b) -> a.getSubject().getCode().compareToIgnoreCase(b.getSubject().getCode()));
                } else { // name asc
                    filtered.sort((a, b) -> a.getSubject().getName().compareToIgnoreCase(b.getSubject().getName()));
                }
            }

            filteredSummaries.setValue(filtered);
        };

        filteredSummaries.addSource(rawSummaries, s -> updateFiltered.run());
        filteredSummaries.addSource(searchQuery, q -> updateFiltered.run());
        filteredSummaries.addSource(semesterFilter, sem -> updateFiltered.run());
        filteredSummaries.addSource(sortMode, sm -> updateFiltered.run());
    }

    public LiveData<List<SubjectAttendanceSummary>> getFilteredSummaries() {
        return filteredSummaries;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSemesterFilter(int sem) {
        semesterFilter.setValue(sem);
    }

    public void setSortMode(int mode) {
        sortMode.setValue(mode);
    }

    public LiveData<Subject> getSubjectById(long id) {
        return subjectRepository.getSubjectById(id);
    }

    public LiveData<List<AttendanceRecord>> getSubjectAttendanceRecords(long subjectId) {
        return attendanceRepository.getRecordsBySubject(subjectId);
    }

    public void saveSubject(Subject subject, Runnable onComplete) {
        subjectRepository.saveSubject(subject, onComplete);
    }

    public void softDeleteSubject(long id, Runnable onComplete) {
        subjectRepository.softDeleteSubject(id, onComplete);
    }
}
