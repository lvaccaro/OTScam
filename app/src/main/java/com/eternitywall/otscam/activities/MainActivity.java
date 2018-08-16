package com.eternitywall.otscam.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eternitywall.otscam.R;
import com.eternitywall.otscam.asynctasks.StampAllAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE = 1;
    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE };

    private CoordinatorLayout mCoordinatorLayout;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!hasPermissions(this, PERMISSIONS))
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);

        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mProgressBar = findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);
    }

    // stamping all receipts with empty ots field
    private void synchronize() {
        final ReceiptDBHelper receiptDBHelper = ReceiptDBHelper.createReceiptDBHelper(this);
        final List<Receipt> receipts = receiptDBHelper.getAllNullable();
        if (receipts.isEmpty())
            return;

        Snackbar.make(mCoordinatorLayout,
                getString(R.string.Find_incomplete_timestamps), Snackbar.LENGTH_LONG)
                .show();

        new StampAllAsyncTask(receiptDBHelper) {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(final Void aVoid) {
                super.onPostExecute(aVoid);
                mProgressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        synchronize();
    }

    public static boolean hasPermissions(final Context context, final String... permissions) {
        if (context == null || permissions == null)
            return false;
        for (final String permission : permissions)
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
        }
        Toast.makeText(this, "Permissions not enabled", Toast.LENGTH_LONG).show();
    }
}
