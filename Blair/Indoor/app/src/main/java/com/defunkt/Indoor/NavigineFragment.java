package com.defunkt.Indoor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.LocationView;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.SubLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.navigine.naviginesdk.NavigineSDK.TAG;

public class NavigineFragment extends Fragment {

    private static NavigationThread mNavigation = null;
    private static String USER_HASH = "3255-7212-207D-BFE1";
    private String LOCATION_NAME = "N Foundation";
    private int LOCATION_ID =2267;
    private final int permission = 1334;
    private static float displayWidthPx;
    private static float displayHeightPx;
    private static float displayWidthDp;
    private static float displayHeightDp;
    private static float displayDensity;
    private LocationView mLocationView;
    private Location mLocation;
    private SubLocation subLoc;
    private boolean mMapLoaded;
    private int mCurrentSubLocationIndex;
    private android.os.Handler mHandler = new android.os.Handler();
    private long mErrorMessageTime = 0;
    private DeviceInfo mDeviceInfo;
    private final int ERROR_MESSAGE_TIMEOUT = 5000;
    private NavigineFragment mNav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigine, container, false);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNav = new NavigineFragment();

        checkPermissions();

        //Loads map from server
        (new LoadTask()).execute();

        //initializes locationview...it's what views the map
        mLocationView = getActivity().findViewById(R.id.navigation_location_view);

        mLocationView.setBackgroundColor(0xffebebeb);

        //setting up the listeners for the mapview
        mLocationView.setListener(
                new LocationView.Listener(){
                    @Override public void onClick(float x, float y){handleClick(x, y);}
                    @Override public void onLongClick(float x, float y){handleOnLockClick(x, y);}
                    @Override public void onDoubleClick(float x, float y){}
                    @Override public void onZoom(float ratio) {}
                    @Override public void onScroll(float x, float y){}
                    @Override public void onDraw(Canvas canvas){


                    }
                }
        );

        loadMap();
        loadSubLocation(mCurrentSubLocationIndex);
    }

    private void handleOnLockClick(float x, float y) {
        //TODO: see if you want to implement this, might not.

    }

    private void handleClick(float x, float y) {
        //TODO: set this up so the user can pick a point they want to travel to.
    }

    public boolean checkPermissions(){

        int permissionFineLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionAccessNetworkState = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE);
        int permissionAccessWifiState = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_WIFI_STATE);
        int permissionBluetooth = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH);
        int permissionBluetoothAdmin = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADMIN);
        int permissionInternet = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.INTERNET);
        int permissionBootComplete = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_BOOT_COMPLETED);
        int permissionRead = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionPhoneState = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE);

        List<String> permissionList = new ArrayList<>();

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissionAccessNetworkState != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (permissionAccessWifiState != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        if (permissionBluetooth != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH);
        }

        if (permissionBluetoothAdmin != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (permissionInternet != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.INTERNET);
        }

        if (permissionBootComplete != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        }

        if (permissionRead != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissionWrite != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionPhoneState != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!permissionList.isEmpty()){
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), permission);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == permission) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Fine location granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Coarse location granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.ACCESS_NETWORK_STATE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Network state granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.ACCESS_WIFI_STATE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Wifi state granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.BLUETOOTH)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Bluetooth granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.BLUETOOTH_ADMIN)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Bluetooth admin granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.INTERNET)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Internet granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.RECEIVE_BOOT_COMPLETED)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Boot completed granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Read storage granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Write storage granted");
                        }
                    }

                    else if (permissions[i].equals(Manifest.permission.READ_PHONE_STATE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "Phone state granted");
                        }
                    }
                }
            }
        }
    }

    //initializes the NavigineSDK. Initialized inside the MainActivity using InitTask
    //has to initialize in main or else it will initialize late and it will not be able to load the map.
    public static boolean initialize(Context context){
        NavigineSDK.setParameter(context, "debug_level", 2);
        NavigineSDK.setParameter(context, "apply_server_config_enabled",  false);
        NavigineSDK.setParameter(context, "actions_updates_enabled",      false);
        NavigineSDK.setParameter(context, "location_updates_enabled",     true);
        NavigineSDK.setParameter(context, "location_loader_timeout",      60);
        NavigineSDK.setParameter(context, "location_update_timeout",      300);
        NavigineSDK.setParameter(context, "location_retry_timeout",       300);
        NavigineSDK.setParameter(context, "post_beacons_enabled",         true);
        NavigineSDK.setParameter(context, "post_messages_enabled",        true);
        if (!NavigineSDK.initialize(context, USER_HASH, "https://api.navigine.com"))
            return false;

        mNavigation = NavigineSDK.getNavigation();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayWidthPx  = displayMetrics.widthPixels;
        displayHeightPx = displayMetrics.heightPixels;
        displayDensity  = displayMetrics.density;
        displayWidthDp  = displayWidthPx  / displayDensity;
        displayHeightDp = displayHeightPx / displayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                displayWidthPx, displayHeightPx,
                displayWidthDp, displayHeightDp,
                displayDensity));

        return true;
    }

    //loads the map
    private boolean loadMap(){
        if (mMapLoaded){
            return false;
        }

        mMapLoaded = true;

        if (mNavigation == null) {
            Log.e(TAG, "Can't load map! Navigine SDK is not available!");
            return false;
        }

        mLocation = mNavigation.getLocation();
        mCurrentSubLocationIndex = -1;

        //checks if there's a location available
        if (mLocation == null) {
            Log.e(TAG, "Loading map failed: no location");
            return false;
        }

        //checks to see if there's a sublocation
        if (mLocation.subLocations.size() == 0) {
            Log.e(TAG, "Loading map failed: no sublocations");
            mLocation = null;
            return false;
        }

        //loads the sublocation if there, if not it outputs an error in the logcat
        if (!loadSubLocation(0)) {
            Log.e(TAG, "Loading map failed: unable to load default sublocation");
            mLocation = null;
            return false;
        }

        mHandler.post(mRunnable);
        //sets the mode to normal. Normal mode continuously scans your location
        mNavigation.setMode(NavigationThread.MODE_NORMAL);
        return true;
    }

    final Runnable mRunnable =
            new Runnable()
            {
                public void run()
                {
                    if (mNavigation == null)
                    {
                        Log.d(TAG, "Sorry, navigation is not supported on your device!");
                        return;
                    }

                    final long timeNow = NavigineSDK.currentTimeMillis();

                    if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT)
                    {
                        mErrorMessageTime = 0;
                    }

                    // Check if location is loaded
                    if (mLocation == null || mCurrentSubLocationIndex < 0)
                        return;

                    // Get current sublocation displayed
                    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

                    // Start navigation if necessary
                    if (mNavigation.getMode() == NavigationThread.MODE_IDLE)
                        mNavigation.setMode(NavigationThread.MODE_NORMAL);

                    // Get device info from NavigationThread
                    mDeviceInfo = mNavigation.getDeviceInfo();

                    if (mDeviceInfo.errorCode == 0)
                    {
                        mErrorMessageTime = 0;
                        //##
                    }

                    // This causes map redrawing
                    mLocationView.redraw();
                }
            };

    //loads the subloacation
    private boolean loadSubLocation(int index) {
        //checks to see if there's an instance of the navigation thread
        if (mNavigation == null) {
            return false;
        }

        //Checks to see if there's a location || if the CurrentLocationIndex is < 0 || if the sublocation is lt or eq to the CurrentLocationIndex
        if (mLocation == null || index < 0 || index >= mLocation.subLocations.size()) {
            return false;
        }

        //displays the current sublocation
        SubLocation subLoc = mLocation.subLocations.get(index);
        Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.name, subLoc.width, subLoc.height));

        //error checking for subloc width and height
        if (subLoc.width < 1.0f || subLoc.height < 1.0f) {
            Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.width, subLoc.height));
            return false;
        }

        //If it can't load the img
        if (!mLocationView.loadSubLocation(subLoc)) {
            Log.e(TAG, "Loading sublocation failed: invalid image");
            return false;
        }

        mCurrentSubLocationIndex = index;

        mHandler.post(mRunnable);
        return true;
    }

    class LoadTask extends AsyncTask<Void, Void, Boolean> {
        @Override protected Boolean doInBackground(Void... params){
            return NavigineSDK.loadLocation(LOCATION_ID, 30) ?
                    Boolean.TRUE : Boolean.FALSE;
        }

        @Override protected void onPostExecute(Boolean result){
            if (result.booleanValue()){
                // Location is successully loaded
                NavigineSDK.getNavigation().setMode(NavigationThread.MODE_NORMAL);

            }else{
                // Error downloading location
                Log.d(TAG, "Error downloading location!");

            }
        }
    }
}

