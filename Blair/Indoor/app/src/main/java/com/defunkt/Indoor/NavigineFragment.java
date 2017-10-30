package com.defunkt.Indoor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import java.util.Locale;

import static com.navigine.naviginesdk.NavigineSDK.TAG;

public class NavigineFragment extends Fragment {

    private NavigationThread mNavigation = null;
    private String USER_HASH = "3255-7212-207D-BFE1";
    private String LOCATION_NAME= "North Foundation";
    private int LOCATION_ID=2239;


    public static float DisplayWidthPx            = 0.0f;
    public static float DisplayHeightPx           = 0.0f;
    public static float DisplayWidthDp            = 0.0f;
    public static float DisplayHeightDp           = 0.0f;
    public static float DisplayDensity            = 0.0f;
    final static int permission = 1334;

    public static boolean PermissionLocation      = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigine, container, false);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initializes Navigine
        (new InitTask(getContext())).execute();

        //loads map file from server to the phone
        requestPerm();

    }

    public boolean initialize(Context context)
    {
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
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                DisplayWidthPx, DisplayHeightPx,
                DisplayWidthDp, DisplayHeightDp,
                DisplayDensity));

        return true;
    }

    class InitTask extends AsyncTask<Void, Void, Boolean>
    {
        private Context mContext  = null;
        private String  mErrorMsg = null;
        NavigineFragment mNav = new NavigineFragment();

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
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

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
        }
    }
}

