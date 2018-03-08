package com.muhammadmehar.mmmgweather.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Muhammad Mehar on 1/3/2017.
 */
public class WeatherContract {

    public static final String CONTENT_AUTHORITY = "com.muhammadmehar.mmmgweather";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String WEATHER_PATH = "weather";

    public static final String LOCATION_PATH = "location";

    public static final class LocationEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(LOCATION_PATH).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + LOCATION_PATH;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + LOCATION_PATH;

        public static final String TABLE_NAME = "location";

        public static final String COULUMN_CITY_NAME = "city";

        public static final String COULUMN_COUNTRY_ABBRE = "country";

        public static final String COULUMN_LON = "lon";

        public static final String COULUMN_LAT = "lat";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId( CONTENT_URI, id);
        }

    }

    public static final class WeatherEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(WEATHER_PATH).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH;

        public static final String TABLE_NAME = "weather";

        public static final String COULUMN_LOC_KEY = "location_id";

        public static final String COULUMN_WEATHER_ID = "weather_id";

        public static final String COULUMN_DATE_TEXT = "date";

        public static final String COULUMN_SHORT_DESC = "short_desc";

        public static final String COULUMN_MAX_TEMP = "max";

        public static final String COULUMN_MIN_TEMP ="min";

        public static final String COULUMN_HUMIDITY = "humidity";

        public static final String COULUMN_WIND_SPEED = "wind";

        public static final String COULUMN_DEGREES = "degrees";

        public static Uri buildWeatherUri(long id){
            return ContentUris.withAppendedId( CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithDate( String locationSetting, String date){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static Uri buildWeatherLocationWithStartDate( String locationSetting, String startDate){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendQueryParameter(COULUMN_DATE_TEXT, startDate).build();
        }

        public static String getLocationFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) { return uri.getPathSegments().get(2); }

        public static String getStartDateFromUri(Uri uri){
            return uri.getQueryParameter(COULUMN_DATE_TEXT);
        }

    }

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getDBDateString(Date date){
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.format(date);
    }

    public static Date getDateFromDB(String dateStr){
        SimpleDateFormat dbDFmt = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDFmt.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
