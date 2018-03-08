package com.muhammadmehar.mmmgweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Muhammad Mehar on 2/11/2017.
 */
public class MMMGWeatherSyncService extends Service {

    private static final Object mmmgSyncAdapterLock = new Object();
    private static MMMGWeatherSyncAdapter mmmgWeatherSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (mmmgSyncAdapterLock){
            if (mmmgWeatherSyncAdapter == null){
                mmmgWeatherSyncAdapter = new MMMGWeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mmmgWeatherSyncAdapter.getSyncAdapterBinder();
    }
}
