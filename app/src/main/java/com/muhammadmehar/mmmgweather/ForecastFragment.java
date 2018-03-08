package com.muhammadmehar.mmmgweather;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.muhammadmehar.mmmgweather.data.WeatherContract;

import java.util.Date;

/**
 * Created by Muhammad Mehar on 12/8/2016.
 */


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface Callback{
        public void onItemSelected(String date);
    }

    private ForecastAdapter mForecastAdapter;
    private int mPosition;
    private ListView mListView;
    private final String POSITION_KEY = "position";
    private boolean mUseTodayLayout;

    private String mLocation;
    private static final int FORECAST_LOADER = 0;


    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COULUMN_DATE_TEXT,
            WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COULUMN_CITY_NAME,
            WeatherContract.LocationEntry.COULUMN_LAT,
            WeatherContract.LocationEntry.COULUMN_LON
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_DATE_TEXT = 1;
    public static final int COL_SHORT_DESC = 2;
    public static final int COL_MAX_TEMP = 3;
    public static final int COL_MIN_TEMP = 4;
    public static final int COL_WEATHERID = 5;
    public static final int COL_CITY_NAME = 6;
    public static final int COL_LAT = 7;
    public static final int COL_LON = 8;

    public ForecastFragment(){

    }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ForecastAdapter( getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Cursor adapterCursor = mForecastAdapter.getCursor();
                        if (adapterCursor != null && adapterCursor.moveToPosition(i)) {
                            ((Callback) getActivity()).onItemSelected(adapterCursor.getString(COL_DATE_TEXT));
                        }
                        mPosition = i;
                    }
                }
        );

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)){
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader( FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate( R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_refresh){
//            updateWeather();
//            return true;
//        }
        if (item.getItemId() == R.id.open_map) {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !Utility.getPreferredLocation(getActivity()).equals(mLocation)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

//    private void updateWeather() {
//        MMMGWeatherSyncAdapter.syncImmediately(getActivity());
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String startDate = WeatherContract.getDBDateString(new Date());
        String sortOrder = WeatherContract.WeatherEntry.COULUMN_DATE_TEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate( mLocation, startDate);
        return new CursorLoader(
                getActivity(),
                weatherForLocation,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION){
            mListView.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    private void openPreferredLocationInMap(){

        if (mForecastAdapter != null){
            Cursor cursor = mForecastAdapter.getCursor();
            if (cursor != null){
                cursor.moveToPosition(0);
                String posLat = cursor.getString(COL_LAT);
                String posLong = cursor.getString(COL_LON);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(geoLocation);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(intent);
                }
            }
        }
    }
}
