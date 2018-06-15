package com.eternitywall.otscam;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.eternitywall.otscam.asynctasks.StampAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.N)
public class CameraJobService extends JobService {
    private final static String TAG = CameraJobService.class.toString();
    ReceiptDBHelper receiptDBHelper;

    // The document says that
    // Job ID must be unique across all clients of the same uid (not just the same package).
    private final static int CONTENT_URI_JOB_ID = 1000;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(TAG, "onStartJob: " + this);

        Context context = getApplicationContext();
        receiptDBHelper = new ReceiptDBHelper(context);
        List<Receipt> receipts = new ArrayList<>();
        if (params.getTriggeredContentAuthorities() != null) {
            if (params.getTriggeredContentUris() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    Receipt r = new Receipt();
                    r.path = Receipt.resolveUri(context, uri);
                    r.id = receiptDBHelper.create(r);
                    receipts.add(r);
                }
            }
        }
        if(receipts.size()==0){
            return true;
        }

        doProcess(params, context, receipts.get(0));

        // mark the job as 'on processing'
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob: " + this);

        // drop the job
        return false;
    }


    private void doProcess(final JobParameters params, final Context context, final Receipt receipt) {
        new StampAsyncTask(receiptDBHelper, receipt){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);

                if(success==true) {
                    Toast.makeText(context, context.getString(R.string.opentimestamped), Toast.LENGTH_LONG).show();
                }
                // mark the job as 'finished'
                jobFinished(params, false);

                // create a new job
                startJob(getApplicationContext());

            }
        }.execute();
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
