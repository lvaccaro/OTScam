package com.eternitywall.otscam.asynctasks;

import android.os.AsyncTask;

import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.op.OpSHA256;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.io.IOException;
import java.util.List;

public class StampAllAsyncTask extends AsyncTask<Void, Void, Void>{
    protected ReceiptDBHelper receiptDBHelper;

    public StampAllAsyncTask(final ReceiptDBHelper receiptDBHelper){
        this.receiptDBHelper = receiptDBHelper;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final List<Receipt> receipts = receiptDBHelper.getAllNullable();
        for (Receipt receipt : receipts) {
            if (receipt.hash != null && receipt.ots == null) {

                Hash hash = new Hash(receipt.hash, new OpSHA256()._TAG());
                DetachedTimestampFile detached = DetachedTimestampFile.from(hash);
                // Stamp
                try {
                    OpenTimestamps.stamp(detached);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Update the receipt ots
                receipt.ots = detached.serialize();
                receiptDBHelper.update(receipt);
            }
        }
        return null;
    }
}