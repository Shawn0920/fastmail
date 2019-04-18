package com.shawn.fastmail.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

public class BackgroundFunctionTask extends AsyncTask<Void, Void, Void> {

    private Runnable mRunnable;

    public BackgroundFunctionTask(@NonNull Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    protected Void doInBackground(Void... functions) {
        if (mRunnable != null) {
            mRunnable.run();
        }
        return null;
    }

    public static void runBackground(Runnable runnable) {
        new BackgroundFunctionTask(runnable).execute();
    }
}
