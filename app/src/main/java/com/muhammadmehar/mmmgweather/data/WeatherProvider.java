package com.muhammadmehar.mmmgweather.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.muhammadmehar.mmmgweather.data.WeatherContract.CONTENT_AUTHORITY;
import static com.muhammadmehar.mmmgweather.data.WeatherContract.LOCATION_PATH;
import static com.muhammadmehar.mmmgweather.data.WeatherContract.LocationEntry;
import static com.muhammadmehar.mmmgweather.data.WeatherContract.WEATHER_PATH;
import static com.muhammadmehar.mmmgweather.data.WeatherContract.WeatherEntry;

/**
 * Created by Muhammad Mehar on 1/11/2017.
 */
public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_LOCATION = 101;
    private static final int WEATHER_LOCATION_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private WeatherDBHelper mWeatherDBHelper;

    private static UriMatcher buildUriMatcher(){

        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        uriMatcher.addURI( authority, WEATHER_PATH, WEATHER);
        uriMatcher.addURI( authority, WEATHER_PATH + "/*", WEATHER_LOCATION);
        uriMatcher.addURI( authority, WEATHER_PATH + "/*/*", WEATHER_LOCATION_DATE);
        uriMatcher.addURI( authority, LOCATION_PATH, LOCATION);
        uriMatcher.addURI( authority, LOCATION_PATH + "/#", LOCATION_ID);

        return uriMatcher;
    }

    UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {

        mWeatherDBHelper = new WeatherDBHelper(getContext());

        return true;
    }

    private static SQLiteQueryBuilder buildSQLiteQuery(){
        final SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(WeatherEntry.TABLE_NAME
                + " INNER JOIN "
                + LocationEntry.TABLE_NAME
                + " ON "
                + WeatherEntry.TABLE_NAME
                + "."
                + WeatherEntry.COULUMN_LOC_KEY
                + " = "
                + LocationEntry.TABLE_NAME
                + "."
                + LocationEntry._ID);
        return sqLiteQueryBuilder;
    }

    private static final String sqliteQueryLocation =
            LocationEntry.TABLE_NAME + "."
            + LocationEntry.COULUMN_CITY_NAME + " = ? ";

    private static final String sqliteQueryLocationStartDate =
            LocationEntry.TABLE_NAME + "."
            + LocationEntry.COULUMN_CITY_NAME + " = ? AND "
            + WeatherEntry.COULUMN_DATE_TEXT + " >= ? ";

    private static final String sqliteQueryLocationDate =
            LocationEntry.TABLE_NAME + "."
            + LocationEntry.COULUMN_CITY_NAME + " = ? AND "
            + WeatherEntry.COULUMN_DATE_TEXT + " = ? ";

    private Cursor WeatherQueryBuilder( Uri uri, String[] projection, String sortOrder){
        String location = WeatherEntry.getLocationFromUri(uri);
        String startDate = WeatherEntry.getStartDateFromUri(uri);
        String[] selectionArgs;
        String selection;

        if(startDate == null){
            selectionArgs = new String[]{location};
            selection = sqliteQueryLocation;
        }else{
            selectionArgs = new String[]{location,startDate};
            selection = sqliteQueryLocationStartDate;
        }
        return buildSQLiteQuery().query(
                mWeatherDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor WeatherQueryBuilderDate( Uri uri, String[] projection, String sortOrder){
        String date = WeatherEntry.getDateFromUri(uri);
        return buildSQLiteQuery().query(
                mWeatherDBHelper.getReadableDatabase(),
                projection,
                sqliteQueryLocationDate,
                new String[]{ WeatherEntry.getLocationFromUri(uri), date},
                null,
                null,
                sortOrder);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)){
            case WEATHER:
                retCursor = mWeatherDBHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case WEATHER_LOCATION:
                retCursor = WeatherQueryBuilder( uri, projection, sortOrder);
                break;
            case WEATHER_LOCATION_DATE:
                retCursor = WeatherQueryBuilderDate( uri, projection, sortOrder);
                break;
            case LOCATION:
                retCursor = mWeatherDBHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case LOCATION_ID:
                retCursor = mWeatherDBHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        LocationEntry._ID + "=" + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri( getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch(sUriMatcher.match(uri)){
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_LOCATION_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        Uri retUri;
        switch (sUriMatcher.match(uri)){
            case WEATHER:
                long weather_id = mWeatherDBHelper.getWritableDatabase().insert( WeatherEntry.TABLE_NAME, null, contentValues);
                if (weather_id > 0){
                    retUri = WeatherEntry.buildWeatherUri(weather_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into uri: " + uri);
                }
                break;
            case LOCATION:
                long location_id = mWeatherDBHelper.getWritableDatabase().insert( LocationEntry.TABLE_NAME, null, contentValues);
                if (location_id > 0){
                    retUri = LocationEntry.buildLocationUri(location_id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into uri: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int retInt;
        switch (sUriMatcher.match(uri)){
            case WEATHER:
                retInt = mWeatherDBHelper.getWritableDatabase().delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                retInt = mWeatherDBHelper.getWritableDatabase().delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (selection == null || retInt != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return retInt;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int retInt;
        switch (sUriMatcher.match(uri)){
            case WEATHER:
                retInt = mWeatherDBHelper.getWritableDatabase().update(WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case LOCATION:
                retInt = mWeatherDBHelper.getWritableDatabase().update(LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (selection == null || retInt != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return retInt;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mWeatherDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case WEATHER:
                db.beginTransaction();
                int retCount = 0;
                try{
                    for (ContentValues contentValues : values){
                        long id = db.insert( WeatherEntry.TABLE_NAME, null, contentValues);
                        if (id != -1){
                            retCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return retCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}


