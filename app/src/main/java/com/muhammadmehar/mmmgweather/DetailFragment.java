package com.muhammadmehar.mmmgweather;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muhammadmehar.mmmgweather.data.WeatherContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private final String shareTag = "#3MGWeather (http://3mgweather.blogspot.com)";
    private String mLocation;
    private final String LOCATION_KEY = "location";

    public ImageView iconView;
    public TextView day;
    public TextView date;
    public TextView high;
    public TextView low;
    public TextView forecast;
    public TextView humidity;
    public TextView wind;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.icon_imageView);
        day = (TextView) rootView.findViewById(R.id.day_textview);
        date = (TextView) rootView.findViewById(R.id.date_textview);
        high = (TextView) rootView.findViewById(R.id.high_textview);
        low = (TextView) rootView.findViewById(R.id.low_textview);
        forecast = (TextView) rootView.findViewById(R.id.forecast_textview);
        humidity = (TextView) rootView.findViewById(R.id.humidity_textview);
        wind = (TextView) rootView.findViewById(R.id.wind_textview);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString( LOCATION_KEY, mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY) && mLocation != null && mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Bundle arguments = getArguments();
        String dateStr = arguments.getString(DetailActivity.DATE_KEY);

        String[] projection={
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COULUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COULUMN_DATE_TEXT,
                WeatherContract.WeatherEntry.COULUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COULUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COULUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COULUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COULUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COULUMN_DEGREES,
                WeatherContract.LocationEntry.COULUMN_CITY_NAME
        };

        mLocation = Utility.getPreferredLocation(getActivity());

        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate( mLocation, dateStr);
        return new CursorLoader(getActivity(),
                uri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_WEATHER_ID));

            String dateText = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_DATE_TEXT));

            iconView.setImageResource(Utility.getResourseId( weatherId, 1, dateText, getActivity()));

            day.setText(Utility.getFriendlyDayString(getActivity(), dateText).split(",")[0]);

            date.setText(Utility.getFormattedMonthDay(dateText));

            String forecastText = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_SHORT_DESC));
            forecast.setText(forecastText);

            iconView.setContentDescription(forecastText);

            float highTemp = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_MAX_TEMP));
            high.setText(getString(R.string.format_temp_String, highTemp));

            float lowTemp = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_MIN_TEMP));
            low.setText(getString(R.string.format_temp_String, lowTemp));

            float humiditi = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_HUMIDITY));
            String hum = "Humidity: " + (int) humiditi + "%";
            humidity.setText(hum);

            float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_WIND_SPEED));
            float deg = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COULUMN_DEGREES));
            wind.setText(getString(R.string.format_wind_String, windSpeed, Utility.getDirection(deg)));

        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        loader.reset();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);
        final ShareActionProvider mShareForecast = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
        if (mShareForecast != null){

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mShareForecast.setShareIntent(createShareForecastIntent());
                }
            }, 100);

        }
    }


    private Intent createShareForecastIntent(){
        return new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, String.format("%s, %s - %s - %s/%s (%s) %s", day.getText(), date.getText(), forecast.getText(), high.getText(), low.getText(), mLocation, shareTag));

    }

}
