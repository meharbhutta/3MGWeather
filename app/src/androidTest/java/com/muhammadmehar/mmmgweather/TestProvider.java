package com.muhammadmehar.mmmgweather;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.muhammadmehar.mmmgweather.data.WeatherContract.LocationEntry;
import com.muhammadmehar.mmmgweather.data.WeatherContract.WeatherEntry;
import com.muhammadmehar.mmmgweather.data.WeatherDBHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Muhammad Mehar on 1/9/2017.
 */
public class TestProvider extends AndroidTestCase {

    public void testDeleteDB() throws Throwable {

        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);

    }

    private String testCityName = "Multan";
    private String testDate = "23-Jun-2016";

    public void testGetType(){
        String type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherUri(123));
        assertEquals( WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        assertEquals( WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testCityName));
        assertEquals( WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithStartDate( testCityName, "23-Jul-2016"));
        assertEquals( WeatherEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate( testCityName, "23-Jul-2016"));
        assertEquals( WeatherEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        assertEquals( LocationEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(12));
        assertEquals( LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    private ContentValues getLocationContentValue(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationEntry.COULUMN_CITY_NAME, testCityName);
        contentValues.put(LocationEntry.COULUMN_COUNTRY_ABBRE, "PK");
        contentValues.put(LocationEntry.COULUMN_LAT, 90.101);
        contentValues.put(LocationEntry.COULUMN_LON, 101.23);

        return contentValues;

    }

    private ContentValues getWeatherContentValue(long locationRowID){

        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherEntry.COULUMN_WEATHER_ID, 321);
        contentValues.put(WeatherEntry.COULUMN_LOC_KEY, locationRowID);
        contentValues.put(WeatherEntry.COULUMN_DATE_TEXT, testDate);
        contentValues.put(WeatherEntry.COULUMN_SHORT_DESC, "Cloudy");
        contentValues.put(WeatherEntry.COULUMN_MAX_TEMP, 35.1);
        contentValues.put(WeatherEntry.COULUMN_MIN_TEMP, 12.5);
        contentValues.put(WeatherEntry.COULUMN_HUMIDITY, 65.123);
        contentValues.put(WeatherEntry.COULUMN_WIND_SPEED, 124.67);
        contentValues.put(WeatherEntry.COULUMN_DEGREES, 93.2);

        return contentValues;

    }

    private static void validateCursor( Cursor cursor, ContentValues contentValues){
        Set<Map.Entry< String, Object>> valueSets = contentValues.valueSet();
        for (Map.Entry< String, Object> entry : valueSets){

            String columnName = entry.getKey();
            int idx = cursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectedValue = entry.getValue().toString();
            assertEquals( expectedValue, cursor.getString(idx));

        }
    }

    public void testIRProvider() throws Throwable{

        Uri locationUri = mContext.getContentResolver().insert( LocationEntry.CONTENT_URI, getLocationContentValue());
        long locationRowID = ContentUris.parseId(locationUri);
        assertTrue(locationRowID != -1);

        Cursor  readDB = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (readDB.moveToFirst()){

            validateCursor( readDB, getLocationContentValue());

            Uri weatherUri = mContext.getContentResolver().insert( WeatherEntry.CONTENT_URI, getWeatherContentValue(locationRowID));

            readDB.close();

            readDB = mContext.getContentResolver().query(
                    WeatherEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            if (readDB.moveToFirst()){

                validateCursor( readDB, getWeatherContentValue(locationRowID));

            }else {
                fail("No Weather Data Returned!");
            }

        }else {
            fail("No Values Returned!");
        }

    }

}
