package com.defunkt.Indoor;

import android.os.AsyncTask;
import android.util.Log;

import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;




/**
 * Created by blair on 10/20/17.
 */

public class LoadTask extends AsyncTask<Void, Void, Boolean> {

    String LOCATION_NAME = "North Foundation";

    @Override
    protected Boolean doInBackground(Void... voids) {
        return NavigineSDK.loadLocation(LOCATION_NAME, 30) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(result.booleanValue()){
            NavigineSDK.getNavigation().setMode(NavigationThread.MODE_NORMAL);
        }else{
            Log.d("Navigine_Init", "Failed to start.");
        }
    }
}
