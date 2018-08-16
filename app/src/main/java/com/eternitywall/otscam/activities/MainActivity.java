package com.eternitywall.otscam.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eternitywall.otscam.MainApplication;
import com.eternitywall.otscam.R;
import com.eternitywall.otscam.asynctasks.StampAllAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;

import java.util.List;

import static com.eternitywall.otscam.MainApplication.PERMISSION_ALL;
import static com.eternitywall.otscam.MainApplication.READ_PERMISSIONS;
import static com.eternitywall.otscam.MainApplication.showExplanation;

public class MainActivity extends AppCompatActivity {


    private CoordinatorLayout mCoordinatorLayout;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mProgressBar = findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);

        if (!MainApplication.hasPermissions(this, READ_PERMISSIONS))
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PERMISSIONS[0]))
                showExplanation(this, getString(R.string.app_name), "Enable permissions to timestamp", READ_PERMISSIONS, PERMISSION_ALL);
            else
                ActivityCompat.requestPermissions(this, READ_PERMISSIONS, PERMISSION_ALL);
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

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    return;
        }
        Toast.makeText(this, "Permissions not enabled", Toast.LENGTH_LONG).show();
    }
}
