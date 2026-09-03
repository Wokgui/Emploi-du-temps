package com.wokgui.schedulewidget;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

final class PronoteSyncScheduler {
    private static final int PERIODIC_JOB_ID = 4101;
    private static final int IMMEDIATE_JOB_ID = 4102;
    private static final long PERIOD_MS = 30L * 60L * 1000L;

    private PronoteSyncScheduler() {}

    static void schedule(Context context) {
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    PERIODIC_JOB_ID,
                    new ComponentName(context, PronoteSyncJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(PERIOD_MS)
                    .build();
            scheduler.schedule(job);
        } catch (Exception ignored) {
        }
    }

    static void requestNow(Context context) {
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo job = new JobInfo.Builder(
                    IMMEDIATE_JOB_ID,
                    new ComponentName(context, PronoteSyncJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency(0L)
                    .setOverrideDeadline(8_000L)
                    .build();
            scheduler.schedule(job);
        } catch (Exception ignored) {
        }
    }

    static void requestNowIfStale(Context context, long staleMs) {
        long last = PronoteStore.getLastSyncAttempt(context);
        if (last <= 0L || System.currentTimeMillis() - last >= staleMs) requestNow(context);
    }
}
