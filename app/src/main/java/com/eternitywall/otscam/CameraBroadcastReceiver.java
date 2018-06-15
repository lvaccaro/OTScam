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
import com.eternitywall.otscam.models.Receipt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CameraBroadcastReceiver extends BroadcastReceiver {

    static ReceiptDBHelper receiptDBHelper;
    private final static String TAG = CameraBroadcastReceiver.class.toString();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "onReceive()");
        String action = intent.getAction();
        if (!isCamera(action)) {
            return;
        }
        try{
            receiptDBHelper = new ReceiptDBHelper(context);
            Receipt receipt = new Receipt();
            receipt.path = Receipt.resolveUri(context, intent.getData());
            receipt.id = receiptDBHelper.create(receipt);
            doProcess(context, receipt);
        }catch (Exception e){
            Log.d(TAG, "Invalid url");
        }
    }


    private static void doProcess(final Context context, final Receipt receipt) {
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

            }
        }.execute();
    }

    private static boolean isCamera(String action) {
        return android.hardware.Camera.ACTION_NEW_PICTURE.equals(action)
                || android.hardware.Camera.ACTION_NEW_VIDEO.equals(action);
    }

    public static void register(Context context) {
        Log.i(TAG, "register()");
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