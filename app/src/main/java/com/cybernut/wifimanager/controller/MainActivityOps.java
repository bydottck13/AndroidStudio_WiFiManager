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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cybernut.wifimanager.R;
import com.cybernut.wifimanager.model.WiFiList;
import com.cybernut.wifimanager.view.MainActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivityOps {
    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
            MainActivityOps.class.getSimpleName();

    private WifiManager wifiManager;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<HashMap<String, String>> mArrayList = new ArrayList<>();
    private SimpleAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;

    /**
     * Used to enable garbage collection.
     */
    private WeakReference<MainActivity> mMainActivity;
    private WeakReference<FloatingActionButton> mFab;
    private WeakReference<TextView> mTextView;

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

    private void initializeViewFields() {
        Log.d(TAG, "initializeViewFields");
        // Get references to the UI components.
        mMainActivity.get().setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) mMainActivity.get().findViewById(R.id.toolbar);
        mMainActivity.get().setSupportActionBar(toolbar);
        mTextView = new WeakReference<>
                ((TextView) mMainActivity.get().findViewById(R.id.textView2));
        WeakReference<ListView> mListView = new WeakReference<>
                ((ListView) mMainActivity.get().findViewById(R.id.listView1));

        populateAutoComplete();

        wifiManager = (WifiManager) mMainActivity.get().getSystemService(Context.WIFI_SERVICE);
        mAdapter = new SimpleAdapter(mMainActivity.get(), mArrayList, R.layout.list_wifi,
                new String[] {"ssid", "power", "freq"}, new int[] {R.id.ssid, R.id.power, R.id.freq});
        mListView.get().setAdapter(mAdapter);

        mFab = new WeakReference<>
                ((FloatingActionButton) mMainActivity.get().findViewById(R.id.fab));
        mFab.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.startScan();

                Snackbar.make(view, "Scanning...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if(!wifiManager.isWifiEnabled())
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

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                results = wifiManager.getScanResults();
                size = results.size();
                Log.d(TAG, "Acquire wifi "+size);
                mArrayList.clear();

                for(int i=0; i<size; i++) {
                    //Log.d(TAG, results.get(i).SSID);
                    //Log.d(TAG, results.get(i).level+" dBm");
                    //Log.d(TAG, "freq "+results.get(i).frequency);

                    HashMap<String, String> item = new HashMap<>();
                    item.put("ssid", results.get(i).SSID);
                    item.put("power", results.get(i).level+" dBm");
                    String wifichn = WiFiList.WIFI_CHANNELS.containsKey(
                            Integer.toString(results.get(i).frequency))?
                            WiFiList.WIFI_CHANNELS.
                                    get(Integer.toString(results.get(i).frequency)):"5G";
                    item.put("freq", wifichn);
                    mArrayList.add(item);
                }

                // Sort by power
                Collections.sort(mArrayList, new Comparator<HashMap<String, String>>() {

                    @Override
                    public int compare(HashMap<String, String> lhs,
                                       HashMap<String, String> rhs) {
                        // TODO Auto-generated method stub
                        return (lhs.get("power")).compareTo(rhs.get("power"));
                    }
                });

                if(size > 0) {
                    mTextView.get().setText(mArrayList.get(0).get("ssid"));
                }
                mAdapter.notifyDataSetChanged();
            }
        };
        mMainActivity.get().registerReceiver(mBroadcastReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if(size > 0) {
            mTextView.get().setText(mArrayList.get(0).get("ssid"));
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
        Log.d(TAG, "mayRequestLocation");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        Log.d(TAG, "newer than M");
        if (mMainActivity.get().checkSelfPermission(ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.d(TAG, "no permission");

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

    public void unregisterReceiverAndDestroy() {
        Log.d(TAG, "go to unregisterReceiverAndDestroy");
        if(mBroadcastReceiver!=null) {
            mMainActivity.get().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

}
