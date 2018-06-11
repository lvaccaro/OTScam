package com.eternitywall.otscam.asynctasks;

import android.os.AsyncTask;

import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.op.OpSHA256;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StampAsyncTask extends AsyncTask<Void, Void, Boolean>{
    protected ReceiptDBHelper receiptDBHelper;
    protected InputStream fileInputStream;
    protected DetachedTimestampFile detached;
    protected Long date;
    protected Hash hash;
    protected String image_path;

    public StampAsyncTask(final ReceiptDBHelper receiptDBHelper, final InputStream fileInputStream, final String image_path){
        this.receiptDBHelper = receiptDBHelper;
        this.fileInputStream = fileInputStream;
        this.image_path = image_path;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // Build hash & digest
        try {
            //InputStream fileInputStream = mContentResolver.openInputStream(uri);
            hash = Hash.from(fileInputStream, new OpSHA256()._TAG());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // Save into db
        Receipt receipt = new Receipt();
        receipt.hash = hash.getValue();
        receipt.path = image_path;
        receipt.ots = null;
        receipt.id = receiptDBHelper.create(receipt);


        // Stamp
        try {
            OpenTimestamps.stamp(detached);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Update the receipt ots
        receipt.ots = detached.serialize();
        receiptDBHelper.update(receipt);
        return true;
    }
}