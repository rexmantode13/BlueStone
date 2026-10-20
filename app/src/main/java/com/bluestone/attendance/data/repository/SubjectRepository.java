package com.bluestone.attendance.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.Subject;

import java.util.List;

public class SubjectRepository {

    private static volatile SubjectRepository INSTANCE;
    private final AppDatabase db;
    private final AppExecutor executor;

    public static SubjectRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SubjectRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SubjectRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public SubjectRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    public LiveData<List<Subject>> getAllActiveSubjects() {
        return db.subjectDao().getAllActive();
    }

    public List<Subject> getAllActiveSubjectsSync() {
        return db.subjectDao().getAllActiveSync();
    }

    public LiveData<List<Subject>> getSubjectsBySemester(int semester) {
        return db.subjectDao().getBySemester(semester);
    }

    public LiveData<Subject> getSubjectById(long id) {
        return db.subjectDao().getById(id);
    }

    public void saveSubject(Subject subject, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            if (subject.getId() > 0) {
                db.subjectDao().update(subject);
            } else {
                db.subjectDao().insert(subject);
            }
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void softDeleteSubject(long id, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.subjectDao().softDelete(id);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }
}
