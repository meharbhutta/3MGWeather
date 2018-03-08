package com.muhammadmehar.mmmgweather.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.muhammadmehar.mmmgweather.data.WeatherContract.LocationEntry;
import com.muhammadmehar.mmmgweather.data.WeatherContract.WeatherEntry;

/**
 * Created by Muhammad Mehar on 1/8/2017.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "weather.db";
    public static int DATABASE_VER = 1;

    public WeatherDBHelper(Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WEATHER_TABLE_QUERY =
                "CREATE TABLE "
                        + WeatherEntry.TABLE_NAME
                        + "("
                        + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + WeatherEntry.COULUMN_WEATHER_ID + " INTEGER NOT NULL,"
                        + WeatherEntry.COULUMN_LOC_KEY + " INTEGER NOT NULL,"
                        + WeatherEntry.COULUMN_DATE_TEXT + " TEXT NOT NULL,"
                        + WeatherEntry.COULUMN_SHORT_DESC + " TEXT NOT NULL,"
                        + WeatherEntry.COULUMN_MAX_TEMP + " REAL NOT NULL,"
                        + WeatherEntry.COULUMN_MIN_TEMP + " REAL NOT NULL,"
                        + WeatherEntry.COULUMN_HUMIDITY + " REAL NOT NULL,"
                        + WeatherEntry.COULUMN_WIND_SPEED + " REAL NOT NULL,"
                        + WeatherEntry.COULUMN_DEGREES + " TEXT NOT NULL,"
                        + "FOREIGN KEY (" + WeatherEntry.COULUMN_LOC_KEY + ") REFERENCES "
                        + LocationEntry.TABLE_NAME + " ("+ LocationEntry._ID +"),"
                        + "UNIQUE (" + WeatherEntry.COULUMN_DATE_TEXT + ", "
                        + WeatherEntry.COULUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_LOCATION_TABLE_QUERY =
                "CREATE TABLE "
                        + LocationEntry.TABLE_NAME
                        + "("
                        + LocationEntry._ID + " INTEGER PRIMARY KEY, "
                        + LocationEntry.COULUMN_CITY_NAME + " TEXT UNIQUE NOT NULL, "
                        + LocationEntry.COULUMN_COUNTRY_ABBRE + " TEXT NOT NULL, "
                        + LocationEntry.COULUMN_LAT + " REAL NOT NULL, "
                        + LocationEntry.COULUMN_LON + " REAL NOT NULL, "
                        + "UNIQUE (" + LocationEntry.COULUMN_CITY_NAME + ") ON CONFLICT IGNORE"
                        + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE_QUERY);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);

    }

}
