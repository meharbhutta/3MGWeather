package com.muhammadmehar.mmmgweather;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.muhammadmehar.mmmgweather.data.WeatherContract.LocationEntry;
import com.muhammadmehar.mmmgweather.data.WeatherContract.WeatherEntry;
import com.muhammadmehar.mmmgweather.data.WeatherDBHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Muhammad Mehar on 1/9/2017.
 */
public class TestDB extends AndroidTestCase {

    public void testCreateDB() throws Throwable {

        mContext.deleteDatabase(WeatherDBHelper.DATABASE_NAME);
        SQLiteDatabase writableDatabase = new WeatherDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, writableDatabase.isOpen());
        writableDatabase.close();

    }

    private String testCityName = "Multan";

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
        contentValues.put(WeatherEntry.COULUMN_DATE_TEXT, "23-Jul-2016");
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

    public void testIRDB() throws Throwable{

        SQLiteDatabase writableDatabase = new WeatherDBHelper(this.mContext).getWritableDatabase();

        long locationRowID = writableDatabase.insert( LocationEntry.TABLE_NAME, null, getLocationContentValue());
        assertTrue(locationRowID != -1);

        Cursor readDB = writableDatabase.query(
                LocationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
                );

        if (readDB.moveToFirst()){

            validateCursor( readDB, getLocationContentValue());

            long weatherRowID = writableDatabase.insert(WeatherEntry.TABLE_NAME, null, getWeatherContentValue(locationRowID));

            assertTrue(weatherRowID != -1);

            readDB.close();
            readDB = writableDatabase.query(
                    WeatherEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

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
