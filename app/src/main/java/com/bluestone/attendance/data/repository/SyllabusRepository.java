package com.bluestone.attendance.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.data.entities.Topic;

import java.util.List;

public class SyllabusRepository {

    private final AppDatabase db;
    private final AppExecutor executor;

    public SyllabusRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    public LiveData<List<SyllabusUnit>> getUnitsBySubject(long subjectId) {
        return db.syllabusDao().getUnitsBySubject(subjectId);
    }

    public void saveUnit(SyllabusUnit unit, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            if (unit.getId() > 0) {
                db.syllabusDao().updateUnit(unit);
            } else {
                db.syllabusDao().insertUnit(unit);
            }
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void setUnitCompleted(long unitId, boolean isCompleted, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.syllabusDao().setUnitCompleted(unitId, isCompleted);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void deleteUnit(long unitId, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.syllabusDao().deleteUnitById(unitId);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public LiveData<List<Topic>> getTopicsByUnit(long unitId) {
        return db.syllabusDao().getTopicsByUnit(unitId);
    }
}
