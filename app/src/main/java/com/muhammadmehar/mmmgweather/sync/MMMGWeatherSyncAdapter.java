package com.muhammadmehar.mmmgweather.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.muhammadmehar.mmmgweather.MainActivity;
import com.muhammadmehar.mmmgweather.R;
import com.muhammadmehar.mmmgweather.Utility;
import com.muhammadmehar.mmmgweather.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Muhammad Mehar on 2/11/2017.
 */
public class MMMGWeatherSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = MMMGWeatherSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String[] NOTIFIY_PROJECTION = {
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP
    };

    private static final int WEATHER_NOTIFICATION_ID = 3004;
    private static final long DAYS_IN_MILLIS = 24 * 60 * 60 * 1000;

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHOR_DESC = 1;
    private static final int INDEX_MAX = 2;
    private static final int INDEX_MIN =3;

    public MMMGWeatherSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        class FetchData{
            public String fetchDataFromServer(Uri uri){
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(uri.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null){
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null ){
                        buffer.append(line).append("\n");
                    }
                    if (buffer.length() == 0){
                        return null;
                    }
                    return buffer.toString();

                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);
                }
                finally {
                    if (urlConnection != null){
                        urlConnection.disconnect();
                    }
                    if (reader != null){
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
                return null;
            }
            public String[] getCityCountryName(String jsonStr){
                if (jsonStr == null){ return null;}
                String[] result = new String[2];
                try {
                    JSONObject locationJsonArray = new JSONObject(jsonStr).getJSONArray("results").getJSONObject(0);
                    JSONArray addressComp = locationJsonArray.getJSONArray("address_components");
                    for(int i=0; i < addressComp.length(); i++) {
                        if (addressComp.getJSONObject(i).getJSONArray("types").getString(0).equals("locality")) {
                            result[0] = addressComp.getJSONObject(i).getString("long_name");
                        } else if (addressComp.getJSONObject(i).getJSONArray("types").getString(0).equals("country")){
                            result[1] = addressComp.getJSONObject(i).getString("long_name");
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                return result;
            }
        }

        double latitude = 0;
        double longitude = 0;

        long locationId = 0;


        String locationQuery = Utility.getPreferredLocation(getContext());

        if (locationQuery != null){

            Uri baseUri = Uri.parse("https://maps.googleapis.com/maps/api/geocode/json?").buildUpon().appendQueryParameter("key", "AIzaSyBYQCDFCZeXbfmWTLUCrwi_fZBdrp7sv-c").appendQueryParameter("address", locationQuery.replaceAll(" ", "%20")).build();
            String[] str = new FetchData().getCityCountryName(new FetchData().fetchDataFromServer(baseUri));
            if (str == null){return;}

            try {
                JSONObject locationJsonArray = new JSONObject(new FetchData().fetchDataFromServer(baseUri)).getJSONArray("results").getJSONObject(0);
                latitude = locationJsonArray.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                longitude = locationJsonArray.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            locationId = addLocation( str[0], str[1], latitude, longitude);

        }

        //<!-- TODO: Add API Keys below here -->
        String apixuAPIKey = "Your apixu api key";
        String wundergroundAPIKey = "Your wunderground api key";

        try {

            if (latitude == 0 && longitude == 0){return;}

            Uri uriCurrent = Uri.parse("http://api.apixu.com/v1/forecast.json?")
                    .buildUpon()
                    .appendQueryParameter("key", apixuAPIKey)
                    .appendQueryParameter("q", latitude + "," + longitude)
                    .appendQueryParameter("day", "10")
                    .build();


            Uri uri = Uri.parse("http://api.wunderground.com/api/" + wundergroundAPIKey + "/forecast10day/q")
                    .buildUpon()
                    .appendPath(latitude + "," + longitude + ".json")
                    .build();

            JSONObject forecastJsonObject = new JSONObject(new FetchData().fetchDataFromServer(uri)).getJSONObject("forecast").getJSONObject("simpleforecast");
            JSONArray weatherArray = forecastJsonObject.getJSONArray("forecastday");
            JSONObject currentJsonObject = null;
            Vector<ContentValues> cVector = new Vector<>();



            for (int i = 0; i < weatherArray.length(); i++){
                ContentValues contentValues = new ContentValues();
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_LOC_KEY, locationId);
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_WEATHER_ID, Utility.getWeatherID(weatherArray.getJSONObject(i).getString("icon")));
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_DATE_TEXT, WeatherContract.getDBDateString(new Date(weatherArray.getJSONObject(i).getJSONObject("date").getInt("epoch") * 1000L)));
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_SHORT_DESC, weatherArray.getJSONObject(i).getString("conditions"));
                if (weatherArray.getJSONObject(i).getJSONObject("high").getString("celsius").isEmpty()){
                    if (currentJsonObject == null) {
                        currentJsonObject = new JSONObject(new FetchData().fetchDataFromServer(uriCurrent)).getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day");
                    }
                    contentValues.put(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP, currentJsonObject.getDouble("maxtemp_c"));
                }
                else {
                    contentValues.put(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP, weatherArray.getJSONObject(i).getJSONObject("high").getDouble("celsius"));
                }
                if (weatherArray.getJSONObject(i).getJSONObject("low").getString("celsius").isEmpty()){
                    if (currentJsonObject == null) {
                        currentJsonObject = new JSONObject(new FetchData().fetchDataFromServer(uriCurrent)).getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(i).getJSONObject("day");
                    }
                    contentValues.put(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP, currentJsonObject.getDouble("mintemp_c"));
                }
                else {
                    contentValues.put(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP, weatherArray.getJSONObject(i).getJSONObject("low").getDouble("celsius"));
                }
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_HUMIDITY, weatherArray.getJSONObject(i).getDouble("avehumidity"));
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_WIND_SPEED, weatherArray.getJSONObject(i).getJSONObject("avewind").getDouble("kph"));
                contentValues.put( WeatherContract.WeatherEntry.COULUMN_DEGREES, weatherArray.getJSONObject(i).getJSONObject("avewind").getDouble("degrees"));

                cVector.add(contentValues);
            }
            if (cVector.size() > 0){
                ContentValues[] cArray = new ContentValues[cVector.size()];
                cVector.toArray(cArray);
                getContext().getContentResolver().bulkInsert( WeatherContract.WeatherEntry.CONTENT_URI, cArray);
                Calendar calendar = Calendar.getInstance();
                calendar.add( Calendar.DATE, -1);
                String yesterdayDate = WeatherContract.getDBDateString(calendar.getTime());
                getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.COULUMN_DATE_TEXT + "<= ?",
                        new String[]{yesterdayDate});
                notifiyWeather();
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    private long addLocation( String cityName, String country, double lat, double lon){
        Cursor cursor = getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COULUMN_CITY_NAME + " = ? ",
                new String[]{cityName},
                null
        );
        if (cursor.moveToFirst()){
            return cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
        }else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(WeatherContract.LocationEntry.COULUMN_CITY_NAME, cityName);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_COUNTRY_ABBRE, country);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_LAT, lat);
            contentValues.put(WeatherContract.LocationEntry.COULUMN_LON, lon);
            return ContentUris.parseId(getContext().getContentResolver().insert( WeatherContract.LocationEntry.CONTENT_URI, contentValues));
        }
    }

    private void notifiyWeather(){
        Context context = getContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotifications = preferences.getBoolean( context.getString(R.string.pref_enable_notifications_key), Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
        if (displayNotifications){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, 3);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            long lastSync = preferences.getLong( context.getString(R.string.pref_last_notification), 0);

            if (cal.getTimeInMillis() - lastSync >= DAYS_IN_MILLIS) {
                String locationQuery = Utility.getPreferredLocation(context);
                String datestr = WeatherContract.getDBDateString(new Date());
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate( locationQuery, datestr);
                Cursor cursor = context.getContentResolver().query( weatherUri, NOTIFIY_PROJECTION, WeatherContract.WeatherEntry.COULUMN_DATE_TEXT + " = ? ", new String[]{datestr}, null);
                if (cursor.moveToFirst()) {
                    int iconID = Utility.getResourseId( cursor.getInt(INDEX_WEATHER_ID), 1, datestr, context);
                    String title = context.getString(R.string.app_name);
                    String contentText = context.getString(R.string.format_notification,
                            cursor.getString(INDEX_SHOR_DESC),
                            Utility.formatTemperature(cursor.getFloat(INDEX_MAX), context),
                            Utility.formatTemperature(cursor.getFloat(INDEX_MIN), context));

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setSmallIcon(iconID)
                            .setContentTitle(title)
                            .setContentText(contentText);

                    Intent resultIntent = new Intent(context, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(resultPendingIntent);

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong( context.getString(R.string.pref_last_notification), cal.getTimeInMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }

    public static void syncImmediately(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);

    }

    public static Account getSyncAccount(Context context){
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account( context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (accountManager.getPassword(newAccount) == null){
            if (!accountManager.addAccountExplicitly( newAccount, null, null)){
                return null;
            }
           onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync( Context context, int synInterval,int flexTime){
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        //only applicable if sdk version is greater than 19
        if (account != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SyncRequest request = new SyncRequest.Builder()
                        .syncPeriodic(synInterval, flexTime)
                        .setSyncAdapter(account, authority).build();
                ContentResolver.requestSync(request);
            } else {
                ContentResolver.addPeriodicSync(account, authority, new Bundle(), synInterval);
            }
        }
    }

    private static void onAccountCreated(Account newAccount, Context context){
        MMMGWeatherSyncAdapter.configurePeriodicSync( context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically( newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);

    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }
}
