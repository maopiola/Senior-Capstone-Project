package com.defunkt.Indoor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import com.navigine.naviginesdk.LocationPoint;
import com.navigine.naviginesdk.LocationView;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.RoutePath;
import com.navigine.naviginesdk.SubLocation;
import com.navigine.naviginesdk.Venue;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.navigine.naviginesdk.NavigineSDK.TAG;

public class NavigineFragment extends Fragment {

    //initilization of everything
    private static NavigationThread mNavigation = null;
    private static String USER_HASH = "3255-7212-207D-BFE1";
    private int LOCATION_ID =2267;
    private final int permission = 1334;
    private LocationView mLocationView;
    private Location mLocation;
    private SubLocation subLoc;
    private boolean mMap;
    private int mCurrentSubLocationIndex;
    private android.os.Handler mHandler = new android.os.Handler();
    private DeviceInfo mDeviceInfo;
    private Bitmap venueBitmap;
    public static float  displayDensity;
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();
    private static final int UPDATE_TIMEOUT = 100;  // milliseconds
    private LocationPoint mPinPoint;
    private LocationPoint mTargetPoint;
    private RectF mPinPointRect;
    private Venue mTargetVenue;
    private Venue mSelectedVenue;
    private RectF mSelectedVenueRect;
    private View mBackView;

    @Override
    //loads all of this into our activity navigation drawer frame
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigine, container, false);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("OU Indoors: Maps");

        //grabs the grizzly head resource
        venueBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grizzly);

        mBackView = getActivity().findViewById(R.id.back_view);
        mBackView.setVisibility(View.INVISIBLE);

        //checks the permissions granted, obviously
        checkPermissions();

        //Loads map from server
        (new LoadTask()).execute();

        //initializes locationview...it's what views the map
        mLocationView = getActivity().findViewById(R.id.navigation_location_view);

        //setting up the listeners for the mapview
        mLocationView.setListener(
                new LocationView.Listener(){

                    //will handle Dijkstra point placement
                    @Override public void onClick(float x, float y){handleClick(x, y);}

                    //may make this display the names of the venues
                    @Override public void onLongClick(float x, float y){handleOnLockClick(x, y);}

                    @Override public void onDoubleClick(float x, float y){}

                    //this is unneccessary since it is handled but the android OS
                    @Override public void onZoom(float ratio) {}

                    //this is also handled by android, might want to implement a different way
                    @Override public void onScroll(float x, float y){}

                    //this will handle drawing the route of the Dijkstra, venues, points
                    @Override public void onDraw(Canvas canvas){

                        drawPoints(canvas);
                        drawVenues(canvas);
                        drawDevice(canvas);

                    }
                }
        );

        loadMap();

        // Starting interface updates
        mTimerTask = new TimerTask()
        {
            @Override public void run()
            {
                mHandler.post(mRunnable);
            }
        };
        mTimer.schedule(mTimerTask, UPDATE_TIMEOUT, UPDATE_TIMEOUT);
    }

    private void drawVenues(Canvas canvas) {

        if (mLocation == null || mCurrentSubLocationIndex < 0) {
            return;
        }

        final float venueSize = 0.1f * displayDensity;

        Paint paint = new Paint();

        for(int i = 0; i < subLoc.venues.size(); ++i) {

            Venue venue = subLoc.venues.get(i);

            if (venue.subLocation != subLoc.id) {
                continue;

            }

            final PointF P = mLocationView.getScreenCoordinates(venue.x, venue.y);
            final float x0 = P.x - venueSize/2;
            final float y0 = P.y - venueSize/2;
            final float x1 = P.x + venueSize/2;
            final float y1 = P.y + venueSize/2;
            canvas.drawBitmap(venueBitmap, null, new RectF(x0, y0, x1, y1), paint);
        }
    }

    /*private Venue getVenue(float x, float y){

        Venue v0 = null;
        float d0 = 1000.0f;

        for(int i = 0; i < subLoc.venues.size(); ++i){

            Venue venue = subLoc.venues.get(i);
            if (venue.subLocation != subLoc.id)
                continue;
            PointF P = mLocationView.getScreenCoordinates(venue.x, venue.y);
            float d = Math.abs(x - P.x) + Math.abs(y - P.y);
            if (d < 30.0f * displayDensity && d < d0){
                v0 = new Venue(venue);
                d0 = d;
            }
        }

        return v0;
    }*/

    /*private void cancelVenue(){

        mSelectedVenue = null;
        mHandler.post(mRunnable);
    }*/

    private void makePin(PointF P) {

        if (mTargetPoint != null || mTargetVenue != null) {
            Log.e(TAG, "Unable to make route. You must cancel previous route first.");
            return;
        }

        if (mDeviceInfo.errorCode != 0) {
            Log.e(TAG, "Unable to make route. Navigation is unavailable.");
            return;
        }

        mPinPoint = new LocationPoint(mLocation.id, subLoc.id, P.x, P.y);
        mPinPointRect = new RectF();
        mHandler.post(mRunnable);
    }

    private void cancelPin(){

        mPinPoint = null;
        mPinPointRect = null;
        mHandler.post(mRunnable);
    }

    private void drawPoints(Canvas canvas) {

        final int solidColor  = Color.argb(255, 64, 163, 205);  // Light-blue color
        final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
        final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
        final float dp        = displayDensity;
        final float textSize  = 0.05f * dp;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Draws pinpoint
        if (mPinPoint != null && mPinPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
            final float tRadius = 0.05f * dp;

            //make route bar
            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(1 * dp);
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            //pinpoint
            paint.setColor(solidColor);
            paint.setStrokeWidth(0);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, 0.5f*tRadius, paint);

            final String text = "Make route";
            final float textWidth = paint.measureText(text);
            final float h  = 0.5f * dp;
            final float w  = Math.max(1 * dp, textWidth + h/2);
            final float x0 = T.x;
            final float y0 = T.y - 0.05f * dp;

            mPinPointRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);

            paint.setColor(solidColor);
            canvas.drawRoundRect(mPinPointRect, h/2, h/2, paint);

            paint.setARGB(255, 255, 255, 255);
            canvas.drawText(text, x0 - textWidth/2, y0 + textSize/4, paint);
        }

        //draws target point
        if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.id) {
            final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
            final float tRadius = 0.025f * dp;

            paint.setARGB(255, 0, 0, 0);
            paint.setStrokeWidth(0.25f * dp);
            //T is the coordinates of the point that we are making
            canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

            paint.setColor(solidColor);
            canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
        }
    }

    private void drawDevice(Canvas canvas){

        if (mLocation == null || mCurrentSubLocationIndex < 0){
            return;
        }

        if (mDeviceInfo.errorCode != 0){
            return;
        }

        if (subLoc == null){
            return;
        }

        final int solidColor  = Color.argb(255, 64,  163, 205); // Light-blue color

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        if (mDeviceInfo.paths != null && mDeviceInfo.paths.size() > 0){
            RoutePath path = mDeviceInfo.paths.get(0);
            if (path.points.size() >= 2){
                paint.setColor(solidColor);

                for(int i = 1; i < path.points.size(); ++i){
                    LocationPoint P = path.points.get(i-1);
                    LocationPoint Q = path.points.get(i);

                    paint.setStrokeWidth(0.05f*displayDensity);
                    PointF P1 = mLocationView.getScreenCoordinates(P);
                    PointF Q1 = mLocationView.getAbsCoordinates(Q);
                    canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
                }
            }
        }

        paint.setStrokeCap(Paint.Cap.BUTT);

        final float x = mDeviceInfo.x;
        final float y = mDeviceInfo.y;
        final float r = mDeviceInfo.r;
        final float radius = .2f *displayDensity;

        PointF O = mLocationView.getScreenCoordinates(x, y);

        paint.setColor(solidColor);
        canvas.drawCircle(O.x, O.y, radius, paint);

    }

    public void onMakeRoute(View v) {
        if (mNavigation == null)
            return;

        if (mPinPoint == null)
            return;

        mTargetPoint  = mPinPoint;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;

        mNavigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.VISIBLE);
        mHandler.post(mRunnable);
    }

    public void onCancelRoute(View v)
    {
        if (mNavigation == null)
            return;

        mTargetPoint  = null;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;

        mNavigation.cancelTargets();
        mBackView.setVisibility(View.GONE);
        mHandler.post(mRunnable);
    }

    private void handleOnLockClick(float x, float y) {

        makePin(mLocationView.getAbsCoordinates(x, y));
        //cancelVenue();
    }

    private void handleClick(float x, float y) {

        if (mPinPoint != null) {
            if (mPinPointRect != null && mPinPointRect.contains(x, y)) {
                mTargetPoint  = mPinPoint;
                mTargetVenue  = null;
                mPinPoint     = null;
                mPinPointRect = null;
                mNavigation.setTarget(mTargetPoint);
                mBackView.setVisibility(View.VISIBLE);
                return;
            }
            cancelPin();
            return;
        }

        if (mSelectedVenue != null) {
            if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y)) {
                mTargetVenue = mSelectedVenue;
                mTargetPoint = null;
                mNavigation.setTarget(new LocationPoint(mLocation.id, subLoc.id, mTargetVenue.x, mTargetVenue.y));
                mBackView.setVisibility(View.VISIBLE);
            }
            //cancelVenue();
            return;
        }

        //Venue selection
       // mSelectedVenue = getVenue(x, y);
        mSelectedVenueRect = new RectF();

        mHandler.post(mRunnable);
    }



    //grabs all of the permissions that we need to run the app
    public boolean checkPermissions(){

        //initialization of all the permissions
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

        //permission list
        List<String> permissionList = new ArrayList<>();

        //checks to see if the permission is already granted. If it's not add it to list
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

        //checks to see if the list is empty. If it's not empty it requests those permissions
        if (!permissionList.isEmpty()){
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), permission);
            return false;
        }
        return true;
    }

    @Override
    //handles the requests of the permissions
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //loops through the permissions list and grants the permissions
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

        //initializing needed parameters
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

        //get parameters of the phone's screen
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        displayDensity = metrics.densityDpi;

        //gets the navigation thread instance
        mNavigation = NavigineSDK.getNavigation();

        return true;
    }

    //loads the map
    private boolean loadMap(){
        //checks if map already loaded
        if (mMap){
            return false;
        }

        mMap = true;

        //gets the location from the navigation thread
        mLocation = mNavigation.getLocation();
        mCurrentSubLocationIndex = -1;

        //failure upon initialization
        if (mNavigation == null) {
            Log.e(TAG, "Navigine SDK did not initialize. Can't load the map.");
            return false;
        }

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

        //updates UI with separate thread
        mHandler.post(mRunnable);

        //sets the mode to normal. Normal mode continuously scans your location
        mNavigation.setMode(NavigationThread.MODE_NORMAL);
        return true;
    }

    //loads the subloacation
    private boolean loadSubLocation(int index) {

        //displays the current sublocation
        subLoc = mLocation.subLocations.get(index);

        //checks to see if there's an instance of the navigation thread
        if (mNavigation == null) {
            return false;
        }

        //Checks to see if there's a location in navi thread || if the CurrentLocationIndex is < 0 then no current location || if the sublocation is lt or eq to the CurrentLocationIndex no sublocation
        if (mLocation == null || index < 0 || index >= mLocation.subLocations.size()) {
            return false;
        }

        //error checking for subloc width and height
        if (subLoc.width < 1.0f || subLoc.height < 1.0f) {
            Log.e(TAG, "Loading sublocation failed, invalid size.");
            return false;
        }

        //if it can't load the img
        if (!mLocationView.loadSubLocation(subLoc)) {
            Log.e(TAG, "Loading sublocation failed, invalid image");
            return false;
        }

        //sets the index of the current sublocation
        mCurrentSubLocationIndex = index;

        //seperate thread to load sublocation to UI
        //Note: thread() does not update the UI only the Handler does
        mHandler.post(mRunnable);
        return true;
    }

    final Runnable mRunnable =
            new Runnable() {
        public void run() {
            if (mNavigation == null) {
                Log.d(TAG, "Navigation is not supported on this device.");
                return;
            }

            //check in the location is loaded
            if (mLocation == null || mCurrentSubLocationIndex < 0) {
                Log.e(TAG, "The location is unable to be loaded.");
                return;
            }

            //gets the current sublocation
            SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

            //Starts navigation
            if (mNavigation.getMode() == NavigationThread.MODE_IDLE) {
                mNavigation.setMode(NavigationThread.MODE_NORMAL);

            }

            if (mTargetPoint != null || mTargetVenue != null)
                mBackView.setVisibility(View.VISIBLE);
            else
                mBackView.setVisibility(View.GONE);

            //gets the device info from the navigation thread.
            mDeviceInfo = mNavigation.getDeviceInfo();

            //redraws the map
            mLocationView.redraw();
        }
    };

    //load the location into the SDK
    class LoadTask extends AsyncTask<Void, Void, Boolean> {
        @Override protected Boolean doInBackground(Void... params){
            return NavigineSDK.loadLocation(LOCATION_ID, 30) ?
                    Boolean.TRUE : Boolean.FALSE;
        }

        @Override protected void onPostExecute(Boolean result){
            if (result.booleanValue()){

                //starts navigation if successful
                NavigineSDK.getNavigation().setMode(NavigationThread.MODE_NORMAL);

            }

            else{

                //error downloading location
                Log.d(TAG, "Error downloading location.");

            }
        }
    }
}

