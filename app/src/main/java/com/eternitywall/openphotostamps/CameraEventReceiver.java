package com.eternitywall.openphotostamps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import com.eternitywall.openphotostamps.dbhelpers.ReceiptDBHelper;
import com.eternitywall.openphotostamps.models.Receipt;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.op.OpSHA256;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CameraEventReceiver extends BroadcastReceiver {

    ReceiptDBHelper receiptDBHelper;

    @Override
    public void onReceive(Context context, Intent intent) {

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
        try {
            File file = new File(image_path);
            FileInputStream fileInputStream = new FileInputStream(file);
            hash = Hash.from(fileInputStream, new OpSHA256()._TAG());
            detached = DetachedTimestampFile.from(hash);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stamp
        try {
            OpenTimestamps.stamp(detached);
        } catch (IOException e) {
            e.printStackTrace();

            // Save the receipt ots
            Receipt receipt = new Receipt();
            receipt.hash = hash.getValue();
            receipt.path = image_path;
            receipt.ots = null;
            receiptDBHelper.create(receipt);
        }

        // Save the receipt ots
        Receipt receipt = new Receipt();
        receipt.hash = hash.getValue();
        receipt.path = image_path;
        receipt.ots = detached.serialize();
        receiptDBHelper.create(receipt);

        Toast.makeText(context, context.getString(R.string.opentimestamped), Toast.LENGTH_LONG).show();
    }
}