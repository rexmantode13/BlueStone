package com.bluestone.attendance.ui.syllabus;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.data.repository.SubjectRepository;
import com.bluestone.attendance.data.repository.SyllabusRepository;

import java.util.List;

public class SyllabusViewModel extends AndroidViewModel {

    private final SyllabusRepository syllabusRepository;
    private final SubjectRepository subjectRepository;

    public SyllabusViewModel(@NonNull Application application) {
        super(application);
        this.syllabusRepository = new SyllabusRepository(application);
        this.subjectRepository = new SubjectRepository(application);
    }

    public LiveData<Subject> getSubject(long subjectId) {
        return subjectRepository.getSubjectById(subjectId);
    }

    public LiveData<List<SyllabusUnit>> getUnits(long subjectId) {
        return syllabusRepository.getUnitsBySubject(subjectId);
    }

    public void setUnitCompleted(long unitId, boolean isCompleted, Runnable onComplete) {
        syllabusRepository.setUnitCompleted(unitId, isCompleted, onComplete);
    }

    public void saveUnit(SyllabusUnit unit, Runnable onComplete) {
        syllabusRepository.saveUnit(unit, onComplete);
    }

    public void deleteUnit(long unitId, Runnable onComplete) {
        syllabusRepository.deleteUnit(unitId, onComplete);
    }
}
