package com.eternitywall.otscam.asynctasks;

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

public class UpgradeAsyncTask extends AsyncTask<Void, Void, Boolean>{
    protected ReceiptDBHelper receiptDBHelper;
    protected DetachedTimestampFile detachedOts;
    protected DetachedTimestampFile detached;
    protected Long date;
    protected String path;
    protected Hash hash;

    public UpgradeAsyncTask(final ReceiptDBHelper receiptDBHelper, final String path) {
        this.receiptDBHelper = receiptDBHelper;
        this.path = path;
    }

    public static UpgradeAsyncTask createUpgradeAsyncTask(final ReceiptDBHelper receiptDBHelper, final String path) {
        return new UpgradeAsyncTask(receiptDBHelper, path);
    }

    @Override
    protected Boolean doInBackground(final Void... voids) {
        // Build hash & digest
        try {
            final File file = new File(path);
            final FileInputStream fileInputStream = new FileInputStream(file);
            hash = Hash.from(fileInputStream, new OpSHA256()._TAG());
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        // Search hash into db
        final Receipt receipt;
        try {
            receipt = receiptDBHelper.getByHash(hash.getValue());
            if (receipt == null)
                throw new Exception();
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        // if file not timestamped
        if (receipt.ots == null)
            try {
                detached = DetachedTimestampFile.from(hash);
                OpenTimestamps.stamp(detached);
            } catch (final Exception e){
                e.printStackTrace();
                return false;
            }

        // get detached objs
        detached = DetachedTimestampFile.from(hash);
        detachedOts = DetachedTimestampFile.deserialize(receipt.ots);

        // upgrade OTS
        try {
            final Boolean changed = OpenTimestamps.upgrade(detachedOts);
            if (changed == true) {
                receipt.ots = detachedOts.serialize();
                receiptDBHelper.update(receipt);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        // verify OTS
        try {
            if (detached.getTimestamp().isTimestampComplete())
                date = OpenTimestamps.verify(detachedOts, detached);
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}