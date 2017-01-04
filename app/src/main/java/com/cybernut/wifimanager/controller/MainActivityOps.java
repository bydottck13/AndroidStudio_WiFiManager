package com.cybernut.wifimanager.controller;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cybernut.wifimanager.R;
import com.cybernut.wifimanager.view.MainActivity;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivityOps {
    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
            MainActivityOps.class.getSimpleName();

    WifiManager wifiManager;
    int size = 0;
    List<ScanResult> results;

    /**
     * Used to enable garbage collection.
     */
    protected WeakReference<MainActivity> mMainActivity;
    protected WeakReference<FloatingActionButton> mFab;

    /**
     * Id to identity ACCESS_COARSE_LOCATION permission request.
     */
    private static final int REQUEST_ACCESS_LOCATION = 101;

    public MainActivityOps(MainActivity mainActivity) {
        // Initialize the WeakReference.
        mMainActivity = new WeakReference<>(mainActivity);

        // Finish the initialization steps.
        initializeViewFields();
        initializeNonViewFields();
    }

    public void initializeViewFields() {
        Log.d(TAG, "initializeViewFields");
        // Get references to the UI components.
        mMainActivity.get().setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) mMainActivity.get().findViewById(R.id.toolbar);
        mMainActivity.get().setSupportActionBar(toolbar);

        populateAutoComplete();

        wifiManager = (WifiManager) mMainActivity.get().getSystemService(Context.WIFI_SERVICE);

        mMainActivity.get().registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                results = wifiManager.getScanResults();
                size = results.size();
                Log.d(TAG, "Acquire wifi "+size);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mFab = new WeakReference<>
                ((FloatingActionButton) mMainActivity.get().findViewById(R.id.fab));
        mFab.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.startScan();

                Snackbar.make(view, "Scanning...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                try {
                    size = size - 1;
                    while(size >= 0)
                    {
                        Log.d(TAG, results.get(size).SSID);
                        size--;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        });

        if(wifiManager.isWifiEnabled()==false)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity.get());
            dialog.setTitle("Remind");
            dialog.setMessage("Your Wi-Fi is disabled, enable it?");
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    wifiManager.setWifiEnabled(true);
                    Snackbar.make(mFab.get(),
                            "WiFi is disabled... making it enabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            });
            dialog.show();
        }

    }

    /**
     * (Re)initialize the non-view fields (e.g.,
     * GenericServiceConnection objects).
     */
    private void initializeNonViewFields() {
        Log.d(TAG, "initializeNonViewFields");
    }

    private void populateAutoComplete() {
        if (!mayRequestLocation()) {
            return;
        }

    }

    private boolean mayRequestLocation() {
        Log.d(TAG, "mayRequestCoarseLocation");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (mMainActivity.get().checkSelfPermission(ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (mMainActivity.get().
                shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "request permission");
            Snackbar.make(mFab.get(),
                    R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            mMainActivity.get().
                                    requestPermissions(new String[]{
                                                    ACCESS_COARSE_LOCATION},
                                            REQUEST_ACCESS_LOCATION);
                        }
                    });
        } else {
            Log.d(TAG, "Permission OK");
            mMainActivity.get().
                    requestPermissions(new String[]{
                                    ACCESS_COARSE_LOCATION},
                            REQUEST_ACCESS_LOCATION);
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialization steps.
     */
    public void onConfigurationChange(MainActivity mainActivity) {
        Log.d(TAG,
                "onConfigurationChange() called");
        // Reset the mActivity WeakReference.
        mMainActivity = new WeakReference<>(mainActivity);
        // (Re)initialize all the View fields.
        initializeViewFields();
    }

}
