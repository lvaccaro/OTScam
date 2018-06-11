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

public class UpgradeAsyncTask extends AsyncTask<Void, Void, Boolean>{
    protected ReceiptDBHelper receiptDBHelper;
    protected InputStream fileInputStream;
    protected DetachedTimestampFile detachedOts;
    protected DetachedTimestampFile detached;
    protected Long date;
    protected Hash hash;

    public UpgradeAsyncTask(final ReceiptDBHelper receiptDBHelper, final InputStream fileInputStream){
        this.receiptDBHelper = receiptDBHelper;
        this.fileInputStream = fileInputStream;
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

        // Search hash into db
        Receipt receipt;
        try {
            receipt = receiptDBHelper.getByHash(hash.getValue());
            if (receipt == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // if file not timestamped
        if(receipt.ots == null){
            try {
                detached = DetachedTimestampFile.from(hash);
                OpenTimestamps.stamp(detached);
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        // get detached objs
        detached = DetachedTimestampFile.from(hash);
        detachedOts = DetachedTimestampFile.deserialize(receipt.ots);

        // upgrade OTS
        try {
            Boolean changed = OpenTimestamps.upgrade(detachedOts);
            if (changed == true){
                receipt.ots = detachedOts.serialize();
                receiptDBHelper.update(receipt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // verify OTS
        try {
            date = OpenTimestamps.verify(detachedOts, detached);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}