package com.eternitywall.otscam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.eternitywall.otscam.asynctasks.StampAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CameraEventReceiver extends BroadcastReceiver {

    ReceiptDBHelper receiptDBHelper;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        //Toast.makeText(context, "New Photo Clicked", Toast.LENGTH_LONG).show();

        // Read photo
        Cursor cursor = context.getContentResolver().query(intent.getData(),
                null, null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        //Toast.makeText(context, "New Photo is Saved as : " + image_path, Toast.LENGTH_LONG).show();

        // Init
        receiptDBHelper = new ReceiptDBHelper(context);
        Hash hash = null;
        DetachedTimestampFile detached = null;

        // Build hash & digest
        FileInputStream fileInputStream;
        try {
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
}