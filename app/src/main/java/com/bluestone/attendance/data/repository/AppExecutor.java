package com.bluestone.attendance.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static volatile AppExecutor instance;
    private final ExecutorService diskIO;
    private final Executor mainThread;

    private AppExecutor() {
        this.diskIO = Executors.newFixedThreadPool(4);
        this.mainThread = new MainThreadExecutor();
    }

    public static AppExecutor getInstance() {
        if (instance == null) {
            synchronized (AppExecutor.class) {
                if (instance == null) {
                    instance = new AppExecutor();
                }
            }
        }
        return instance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
