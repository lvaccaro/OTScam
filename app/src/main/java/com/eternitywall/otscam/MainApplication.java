package com.eternitywall.otscam;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


public class MainApplication extends MultiDexApplication {
    public static final String[] READ_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE };
    public static final String[] WRITE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public static final int PERMISSION_ALL = 1;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // use job scheduler
            CameraJobService.startJob(this);
        } else {
            // use broadcast receiver
            CameraBroadcastReceiver.register(this);
        }
    }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static boolean hasPermissions(final Context context, final String... permissions) {
        if (context == null || permissions == null)
            return false;
        for (final String permission : permissions)
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    public static void showExplanation(final AppCompatActivity activity,
                                       final String title,
                                       final String message,
                                       final String[] permissions,
                                       final int permissionRequestCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        ActivityCompat.requestPermissions(activity, permissions, permissionRequestCode);
                    }
                }).create().show();
    }
}
