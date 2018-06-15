package com.eternitywall.otscam.asynctasks;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.op.OpSHA256;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StampAsyncTask extends AsyncTask<Void, Void, Boolean>{
    protected ReceiptDBHelper receiptDBHelper;
    protected DetachedTimestampFile detached;
    protected Long date;
    protected Hash hash;
    protected Receipt receipt;

    public StampAsyncTask(final ReceiptDBHelper receiptDBHelper, final Receipt receipt){
        this.receiptDBHelper = receiptDBHelper;
        this.receipt = receipt;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Build hash & digest
        try {
            File file = new File(receipt.path);
            FileInputStream fileInputStream = new FileInputStream(file);
            hash = Hash.from(fileInputStream, new OpSHA256()._TAG());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // Save into db
        receipt.hash = hash.getValue();
        receipt.ots = null;
        if(receipt.id == 0) {
            receipt.id = receiptDBHelper.create(receipt);
        } else {
            receipt.id = receiptDBHelper.update(receipt);
        }

        // Stamp
        try {
            detached = DetachedTimestampFile.from(hash);
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