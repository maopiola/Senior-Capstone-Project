package com.defunkt.Indoor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
<<<<<<< HEAD
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
=======
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
<<<<<<< HEAD

import com.navigine.naviginesdk.LocationView;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import java.util.ArrayList;
import java.util.List;
=======
import android.widget.Toast;

import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
import java.util.Locale;

import static com.navigine.naviginesdk.NavigineSDK.TAG;

public class NavigineFragment extends Fragment {

    private NavigationThread mNavigation = null;
    private String USER_HASH = "3255-7212-207D-BFE1";
<<<<<<< HEAD
    private String LOCATION_NAME = "N Foundation";
    private int LOCATION_ID =2267;
    private final int permission = 1334;
    private float displayWidthPx;
    private float displayHeightPx;
    private float displayWidthDp;
    private float displayHeightDp;
    private float displayDensity;
    private LocationView mLocationView;
=======
    private String LOCATION_NAME= "North Foundation";
    private int LOCATION_ID=2239;


    public static float DisplayWidthPx            = 0.0f;
    public static float DisplayHeightPx           = 0.0f;
    public static float DisplayWidthDp            = 0.0f;
    public static float DisplayHeightDp           = 0.0f;
    public static float DisplayDensity            = 0.0f;
    final static int permission = 1334;

    public static boolean PermissionLocation      = false;

>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigine, container, false);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

<<<<<<< HEAD
        //initializes locationview...it's what views the map
        mLocationView = getActivity().findViewById(R.id.navigation_location_view);

        //checks permissions needed
        checkPermissions();

        //initializes Navigine
        (new InitTask(getContext())).execute();

        //loads map
        (new LoadTask()).execute();

        //starts navigation
        startNavigation();

    }

    private boolean checkPermissions(){

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

    public boolean initialize(Context context){
=======
        //initializes Navigine
        (new InitTask(getContext())).execute();

        //loads map file from server to the phone
        requestPerm();

    }

    public boolean initialize(Context context)
    {
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
        NavigineSDK.setParameter(context, "debug_level", 2);
        NavigineSDK.setParameter(context, "apply_server_config_enabled",  false);
        NavigineSDK.setParameter(context, "actions_updates_enabled",      false);
        NavigineSDK.setParameter(context, "location_updates_enabled",     true);
        NavigineSDK.setParameter(context, "location_loader_timeout",      60);
        NavigineSDK.setParameter(context, "location_update_timeout",      300);
        NavigineSDK.setParameter(context, "location_retry_timeout",       300);
        NavigineSDK.setParameter(context, "post_beacons_enabled",         true);
        NavigineSDK.setParameter(context, "post_messages_enabled",        true);
        if (!NavigineSDK.initialize(context, USER_HASH, null))
            return false;

        mNavigation = NavigineSDK.getNavigation();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
<<<<<<< HEAD
        displayWidthPx  = displayMetrics.widthPixels;
        displayHeightPx = displayMetrics.heightPixels;
        displayDensity  = displayMetrics.density;
        displayWidthDp  = displayWidthPx  / displayDensity;
        displayHeightDp = displayHeightPx / displayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                displayWidthPx, displayHeightPx,
                displayWidthDp, displayHeightDp,
                displayDensity));
=======
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                DisplayWidthPx, DisplayHeightPx,
                DisplayWidthDp, DisplayHeightDp,
                DisplayDensity));
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06

        return true;
    }

<<<<<<< HEAD
    class InitTask extends AsyncTask<Void, Void, Boolean>{
=======
    class InitTask extends AsyncTask<Void, Void, Boolean>
    {
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
        private Context mContext  = null;
        private String  mErrorMsg = null;
        NavigineFragment mNav = new NavigineFragment();

<<<<<<< HEAD
        public InitTask(Context context){
            mContext = context.getApplicationContext();
        }

        @Override protected Boolean doInBackground(Void... params){
            try { Thread.sleep(1000); } catch ( Throwable e) { }
            if (!mNav.initialize(getActivity().getApplicationContext())){
                mErrorMsg = "Error downloading location Navigine!";
                return Boolean.FALSE;
            }
            Log.d(TAG, "Initialized!");

            if (!NavigineSDK.loadLocation(LOCATION_ID, 30)){
                mErrorMsg = "Error downloading location 'Navigine!";
=======
        public InitTask(Context context)
        {
            mContext = context.getApplicationContext();
        }

        @Override protected Boolean doInBackground(Void... params)
        {
            try { Thread.sleep(1000); } catch ( Throwable e) { }
            if (!mNav.initialize(getActivity().getApplicationContext()))
            {
                mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d(TAG, "Initialized!");
            if (!NavigineSDK.loadLocation(LOCATION_ID, 30))
            {
                mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
<<<<<<< HEAD
    }

    class LoadTask extends AsyncTask<Void, Void, Boolean>{
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

    public void startNavigation(){
        String locationFile = NavigineSDK.getLocationFile(LOCATION_NAME);
        mNavigation = NavigineSDK.getNavigation();
        if (mNavigation != null && mNavigation.loadLocation(locationFile)){
            mNavigation.setMode(NavigationThread.MODE_NORMAL);
=======

        @Override protected void onPostExecute(Boolean result)
        {
            if (result.booleanValue())
            {
                // Starting main activity
            }
            else
            {
                Log.d(TAG, mErrorMsg);
            }
        }
    }

    public void requestPerm(){
        if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
            class LoadTask extends AsyncTask<Void, Void, Boolean>
            {
                @Override protected Boolean doInBackground(Void... params)
                {
                    return NavigineSDK.loadLocation(LOCATION_ID, 30) ?
                            Boolean.TRUE : Boolean.FALSE;
                }

                @Override protected void onPostExecute(Boolean result)
                {
                    if (result.booleanValue())
                    {
                        // Location is successully loaded
                        // Do whatever you want here, e.g. you can start navigation
                        NavigineSDK.getNavigation().setMode(NavigationThread.MODE_NORMAL);

                    }
                    else
                    {
                        // Error downloading location
                        // Try again later or contact technical support
                        Log.d(TAG, "Error downloading location!");
                    }
                }
            }

        }else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permission);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            requestPerm();

        }else{
            Toast.makeText(getActivity().getApplicationContext(), "This app requires location permission", Toast.LENGTH_SHORT).show();
>>>>>>> e9a30b36220d4d726680a2c4d2714b15bd160a06
        }
    }
}

