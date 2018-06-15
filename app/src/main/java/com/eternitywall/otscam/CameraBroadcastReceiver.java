package com.eternitywall.otscam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.eternitywall.otscam.asynctasks.StampAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CameraBroadcastReceiver extends BroadcastReceiver {

    static ReceiptDBHelper receiptDBHelper;
    String TAG = "";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "onReceive()");
        String action = intent.getAction();
        if (!isCamera(action)) {
            return;
        }

        //Toast.makeText(context, "New Photo Clicked", Toast.LENGTH_LONG).show();

        // Read photo
        //Cursor cursor = context.getContentResolver().query(intent.getData(),
        //        null, null, null, null);
        //cursor.moveToFirst();
        //String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        //Toast.makeText(context, "New Photo is Saved as : " + image_path, Toast.LENGTH_LONG).show();

        receiptDBHelper = new ReceiptDBHelper(context);
        doProcess(context, intent);

    }


    private static void doProcess(Context context, Intent intent) {
        // Init
        Hash hash = null;
        DetachedTimestampFile detached = null;
        String image_path = null;

        // Build hash & digest
        FileInputStream fileInputStream;
        try {
            image_path = intent.getAction();
            File file = new File(image_path);
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            return;
        }

        new StampAsyncTask(receiptDBHelper, fileInputStream, image_path){
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

            }
        }.execute();
    }

    private static boolean isCamera(String action) {
        return android.hardware.Camera.ACTION_NEW_PICTURE.equals(action)
                || android.hardware.Camera.ACTION_NEW_VIDEO.equals(action);
    }

    public static void register(Context context) {
        Log.i(TAG, "register");
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.hardware.Camera.ACTION_NEW_PICTURE);
        filter.addAction(android.hardware.Camera.ACTION_NEW_VIDEO);

        try {
            filter.addDataType("image/*");
            filter.addDataType("video/*");
        } catch (Exception e) {
            Log.e(TAG, "Fail: + " + e.getMessage());
        }

        CameraBroadcastReceiver receiver = new CameraBroadcastReceiver();
        context.registerReceiver(receiver, filter);
    }
}