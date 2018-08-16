package com.eternitywall.otscam.activities;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eternitywall.otscam.GoogleUrlShortener;
import com.eternitywall.otscam.MainApplication;
import com.eternitywall.otscam.R;
import com.eternitywall.otscam.adapters.ItemAdapter;
import com.eternitywall.otscam.asynctasks.UpgradeAsyncTask;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.eternitywall.otscam.MainApplication.PERMISSION_ALL;
import static com.eternitywall.otscam.MainApplication.WRITE_PERMISSIONS;
import static com.eternitywall.otscam.MainApplication.showExplanation;

public class ReceiptActivity extends AppCompatActivity {

    private ItemAdapter mAdapter;
    private LinkedHashMap<String,String> mDataset;
    private ProgressBar mProgressBar;
    private ReceiptDBHelper receiptDBHelper;
    private DetachedTimestampFile ots;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        final RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);
        mProgressBar = findViewById(R.id.progressBar);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final DividerItemDecoration horizontalDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        final Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.divider_grey);
        horizontalDecoration.setDrawable(horizontalDivider);
        mRecyclerView.addItemDecoration(horizontalDecoration);

        // Specify and fill the adapter
        mDataset = new LinkedHashMap<>();
        mAdapter = new ItemAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Set button
        findViewById(R.id.btnInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onInfoClick();
            }
        });
        findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onDownloadClick();
            }
        });
        // Check DB
        receiptDBHelper = ReceiptDBHelper.createReceiptDBHelper(this);

        // Init content
        //final ContentResolver mContentResolver = getContentResolver();

        // Get intent file
        final Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            final Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM))
                load(extras.<Uri>getParcelable(Intent.EXTRA_STREAM));
        }
    }

    private void load (final Uri uri) {
        final String url;
        try{
            url = Receipt.resolveUri(this, uri);
        }catch (final Exception e){
            Log.d("", "Invalid url");
            return;
        }
        new UpgradeAsyncTask(receiptDBHelper, url) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                super.onPostExecute(success);
                mProgressBar.setVisibility(View.GONE);
                if (success) {
                    ots = detachedOts;
                    refresh(uri, hash, detachedOts, date);
                } else
                    Toast.makeText(ReceiptActivity.this, "File not stamped", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }


    public void refresh(final Uri uri, final Hash hash, final DetachedTimestampFile ots, final Long date){
        mDataset.put(getString(R.string.name),uri.getLastPathSegment());
        mDataset.put(getString(R.string.uri),uri.toString());
        mDataset.put(getString(R.string.hash), hash.toString());
        if (ots != null) {
            mDataset.put(getString(R.string.ots_proof), Receipt.bytesToHex(ots.serialize()));

            if (!ots.getTimestamp().isTimestampComplete())
                mDataset.put(getString(R.string.attestation), getString(R.string.pending_attestation));
            else if (date == null || date == 0)
                mDataset.put(getString(R.string.attestation), getString(R.string.pending_or_bad_attestation));
            else
                try {
                    //Thu May 28 2015 17:41:18 GMT+0200 (CEST)
                    final DateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
                    final Date netDate = new Date(date * 1000);
                    mDataset.put(getString(R.string.attestation), getString(R.string.bitcoin_attests) + " " + sdf.format(netDate));
                } catch (final Exception ex) {
                    mDataset.put(getString(R.string.attestation), getString(R.string.invalid_date));
                }
        } else
            mDataset.put(getString(R.string.ots_proof), getString(R.string.file_not_timestamped));
        mAdapter.notifyDataSetChanged();
    }

    public void onSharingClick() {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, ots.serialize());
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_proof_to)));
    }

    public void onInfoClick() {
        new AsyncTask<Void, Void, Boolean>() {
            String shortUrl = "";

            @Override
            protected Boolean doInBackground(final Void... params) {
                final String otsString = Receipt.bytesToHex(ots.serialize());
                final String url = "https://opentimestamps.org/info/?" + otsString;
                shortUrl = GoogleUrlShortener.shorten(url);
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                super.onPostExecute(success);
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(shortUrl));
                startActivity(intent);
            }
        }.execute();
    }

    public void onDownloadClick() {
        new Builder(this)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.file_download_alertdialog))
                .setPositiveButton(R.string.saving, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        onSavingClick();
                    }
                })
                .setNegativeButton(R.string.sharing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        onSharingClick();
                    }
                })
                .show();
    }

    public void onSavingClick() {
        if (!MainApplication.hasPermissions(this, WRITE_PERMISSIONS)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_PERMISSIONS[0]))
                showExplanation(this, getString(R.string.app_name), "Enable permissions to timestamp", WRITE_PERMISSIONS, PERMISSION_ALL);
            else
                ActivityCompat.requestPermissions(this, WRITE_PERMISSIONS, PERMISSION_ALL);
            return;
        }

        final String filename = Utils.bytesToHex(ots.fileDigest()) + ".ots";
        final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final String filepath = dir.getAbsolutePath() + "/" + filename;
        try {
            write(ots,filepath);
            Toast.makeText(this, getString(R.string.file_proof_saving) + " " + filepath, Toast.LENGTH_LONG).show();
        } catch (final IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.file_proof_saving_error), Toast.LENGTH_LONG).show();
        } catch (final Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.file_proof_saving_error), Toast.LENGTH_LONG).show();
        }
    }

    public static void write(final DetachedTimestampFile detached, final String filepath) throws IOException {
        final File file = new File(filepath);
        final FileOutputStream fos = new FileOutputStream(file);
        final StreamSerializationContext ssc = new StreamSerializationContext();
        detached.serialize(ssc);
        fos.write(ssc.getOutput());
        fos.close();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onSavingClick();
                    return;
                }
        }
        Toast.makeText(this, "Permissions not enabled", Toast.LENGTH_LONG).show();
    }
}