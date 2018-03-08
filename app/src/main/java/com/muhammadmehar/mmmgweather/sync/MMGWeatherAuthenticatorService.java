package com.muhammadmehar.mmmgweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Muhammad Mehar on 2/11/2017.
 */
public class MMGWeatherAuthenticatorService extends Service {

    private MMMGWeatherAuthenticator mmmgWeatherAuthenticator;

    @Override
    public void onCreate() {
        mmmgWeatherAuthenticator = new MMMGWeatherAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mmmgWeatherAuthenticator.getIBinder();
    }

}
