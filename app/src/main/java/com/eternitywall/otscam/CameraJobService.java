package com.hjm.jobschedulersample;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.N)
public class CameraJobService extends JobService {
    private final static String TAG = CameraJobService.class.toString();

    // The document says that
    // Job ID must be unique across all clients of the same uid (not just the same package).
    private final static int CONTENT_URI_JOB_ID = 1000;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(TAG, "onStartJob: " + this);

        // do in background thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parameters) {
                doProcess(params);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // mark the job as 'finished'
                jobFinished(params, false);

                // create a new job
                startJob(getApplicationContext());
            }
        }.execute();

        // mark the job as 'on processing'
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob: " + this);

        // drop the job
        return false;
    }

    private static void doProcess(JobParameters params) {
        if (params.getTriggeredContentAuthorities() != null) {
            if (params.getTriggeredContentUris() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    Log.i(TAG, "  - " + uri.toString());
                }
            }
        } else {
            Log.i(TAG, "no content");
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void startJob(Context context) {
        Log.i(TAG, "startJob");
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(
                CONTENT_URI_JOB_ID,
                new ComponentName(context, CameraJobService.class));

        builder.addTriggerContentUri(
                new JobInfo.TriggerContentUri(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));

        scheduler.schedule(builder.build());
    }
}
