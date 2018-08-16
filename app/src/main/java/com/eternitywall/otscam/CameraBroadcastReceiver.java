package com.eternitywall.otscam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.eternitywall.otscam.asynctasks.StampAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

public class CameraBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = CameraBroadcastReceiver.class.toString();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "onReceive()");
        final String action = intent.getAction();
        if (!isCamera(action))
            return;
        try{
            final ReceiptDBHelper receiptDBHelper = ReceiptDBHelper.createReceiptDBHelper(context);
            final Receipt receipt = new Receipt();
            receipt.path = Receipt.resolveUri(context, intent.getData());
            receipt.id = receiptDBHelper.create(receipt);
            doProcess(context, receiptDBHelper, receipt);
        }catch (final Exception e){
            Log.d(TAG, "Invalid url");
        }
    }

    private static void doProcess(final Context context, final ReceiptDBHelper receiptDBHelper, final Receipt receipt) {
        new StampAsyncTask(receiptDBHelper, receipt){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                super.onPostExecute(success);
                if (success)
                    Toast.makeText(context, context.getString(R.string.opentimestamped), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private static boolean isCamera(final String action) {
        return android.hardware.Camera.ACTION_NEW_PICTURE.equals(action)
                || android.hardware.Camera.ACTION_NEW_VIDEO.equals(action);
    }

    public static void register(final Context context) {
        Log.i(TAG, "register()");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(android.hardware.Camera.ACTION_NEW_PICTURE);
        filter.addAction(android.hardware.Camera.ACTION_NEW_VIDEO);

        try {
            filter.addDataType("image/*");
            filter.addDataType("video/*");
        } catch (final Exception e) {
            Log.e(TAG, "Fail: + " + e.getMessage());
        }

        final CameraBroadcastReceiver receiver = new CameraBroadcastReceiver();
        context.registerReceiver(receiver, filter);
    }
}