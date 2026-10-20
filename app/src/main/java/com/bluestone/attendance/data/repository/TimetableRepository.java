package com.bluestone.attendance.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.TimetableSession;

import java.time.LocalTime;
import java.util.List;

public class TimetableRepository {

    public interface OverlapCheckCallback {
        void onResult(boolean hasOverlap);
    }

    private static volatile TimetableRepository INSTANCE;
    private final AppDatabase db;
    private final AppExecutor executor;

    public static TimetableRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TimetableRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TimetableRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public TimetableRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    public LiveData<List<TimetableSession>> getAllSessions() {
        return db.timetableDao().getAll();
    }

    public LiveData<List<TimetableSession>> getSessionsByDay(int dayOfWeek) {
        return db.timetableDao().getByDay(dayOfWeek);
    }

    public void checkOverlap(int dayOfWeek, LocalTime start, LocalTime end, long excludeId, OverlapCheckCallback callback) {
        executor.diskIO().execute(() -> {
            List<TimetableSession> overlapping = db.timetableDao().findOverlappingSessionsSync(dayOfWeek, start, end, excludeId);
            boolean hasOverlap = overlapping != null && !overlapping.isEmpty();
            executor.mainThread().execute(() -> callback.onResult(hasOverlap));
        });
    }

    public void saveSession(TimetableSession session, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            if (session.getId() > 0) {
                db.timetableDao().update(session);
            } else {
                db.timetableDao().insert(session);
            }
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void deleteSession(long id, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.timetableDao().deleteById(id);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void setSessionCancelled(long id, boolean isCancelled, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            TimetableSession session = db.timetableDao().getByIdSync(id);
            if (session != null) {
                session.setCancelled(isCancelled);
                db.timetableDao().update(session);
            }
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }
}
