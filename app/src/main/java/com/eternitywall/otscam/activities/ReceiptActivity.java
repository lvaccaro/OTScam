package com.eternitywall.otscam.activities;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eternitywall.otscam.GoogleUrlShortener;
import com.eternitywall.otscam.R;
import com.eternitywall.otscam.adapters.ItemAdapter;
import com.eternitywall.otscam.dbhelpers.ReceiptDBHelper;
import com.eternitywall.otscam.models.Receipt;
import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.op.OpSHA256;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class ReceiptActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private LinkedHashMap<String,String> mDataset;
    private RecyclerView.LayoutManager mLayoutManager;
    ProgressBar mProgressBar;

    ReceiptDBHelper receiptDBHelper;
    ContentResolver mContentResolver;
    DetachedTimestampFile ots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.divider_grey);
        horizontalDecoration.setDrawable(horizontalDivider);
        mRecyclerView.addItemDecoration(horizontalDecoration);

        // Specify and fill the adapter
        mDataset = new LinkedHashMap<>();
        mAdapter = new ItemAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Set button
        findViewById(R.id.btnInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInfoClick();
            }
        });
        findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownloadClick();
            }
        });
        // Check DB
        receiptDBHelper = new ReceiptDBHelper(this);

        // Init content
        mContentResolver = getContentResolver();

        // Get intent file
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
                String scheme = uri.getScheme();
                if (scheme.equals("content")) {
                    /*String mimeType = intent.getType();
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    refresh(filePath);*/
                    load(uri);
                } else {
                    load(uri);
                }
            }
        }
    }



    private void load (final Uri uri) {

        new AsyncTask<Void, Void, Boolean>() {
            DetachedTimestampFile detachedOts;
            DetachedTimestampFile detached;
            Long date;
            Hash hash;

            @Override
            protected Boolean doInBackground(Void... params) {

                // Build hash & digest
                try {
                    InputStream fileInputStream = mContentResolver.openInputStream(uri);
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

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);

            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                mProgressBar.setVisibility(View.GONE);

                if(success==true) {
                    ots = detachedOts;
                    refresh(uri, hash, detachedOts, date);
                    return;
                }

                Toast.makeText(ReceiptActivity.this, "File not stamped",Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void refresh(Uri uri, Hash hash, DetachedTimestampFile ots, Long date){
        mDataset.put(getString(R.string.name),uri.getLastPathSegment());
        mDataset.put(getString(R.string.uri),uri.toString());
        mDataset.put(getString(R.string.hash), hash.toString());
        if(ots == null){
            mDataset.put(getString(R.string.ots_proof), getString(R.string.file_not_timestamped));
        } else {
            mDataset.put(getString(R.string.ots_proof), Receipt.bytesToHex(ots.serialize()));

            if (date == null || date == 0) {
                mDataset.put(getString(R.string.attestation), getString(R.string.pending_or_bad_attestation));
            } else {
                try {
                    //Thu May 28 2015 17:41:18 GMT+0200 (CEST)
                    DateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
                    Date netDate = new Date(date * 1000);
                    mDataset.put(getString(R.string.attestation), getString(R.string.bitcoin_attests) + " " + sdf.format(netDate));
                } catch (Exception ex) {
                    mDataset.put(getString(R.string.attestation), getString(R.string.invalid_date));
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void onSharingClick() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, ots.serialize());
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_proof_to)));
    }

    public void onInfoClick() {
        new AsyncTask<Void, Void, Boolean>() {
            String shortUrl = "";

            @Override
            protected Boolean doInBackground(Void... params) {
                String otsString = Receipt.bytesToHex(ots.serialize());
                String url = "https://opentimestamps.org/info/?";
                url += otsString;
                shortUrl = GoogleUrlShortener.shorten(url);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(shortUrl));
                startActivity(i);
            }
        }.execute();
    }


    public void onDownloadClick() {
        new AlertDialog.Builder(ReceiptActivity.this)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.file_download_alertdialog))
                .setPositiveButton(R.string.saving, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onSavingClick();
                    }
                })
                .setNegativeButton(R.string.sharing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onSharingClick();
                    }
                })
                .show();
    }

    public void onSavingClick() {

        String filename = Utils.bytesToHex(ots.fileDigest())+".ots";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filepath = dir.getAbsolutePath()+"/"+filename;
        try {
            write(ots,filepath);
            Toast.makeText(this,getString(R.string.file_proof_saving)+" "+filepath,Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.file_proof_saving_error),Toast.LENGTH_LONG).show();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this,getString(R.string.file_proof_saving_error),Toast.LENGTH_LONG).show();
        }
    }

    public static void write(DetachedTimestampFile detached, String filepath) throws IOException, NoSuchAlgorithmException {

        File file = new File(filepath);
        FileOutputStream fos = new FileOutputStream(file);

        StreamSerializationContext ssc = new StreamSerializationContext();
        detached.serialize(ssc);

        fos.write(ssc.getOutput());
        fos.close();
    }
}
