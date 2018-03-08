package com.muhammadmehar.mmmgweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.muhammadmehar.mmmgweather.sync.MMMGWeatherSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            if (findViewById(R.id.weather_detail_container) != null) {
                mTwoPane = true;
                if (savedInstanceState == null) {
                    Bundle args = new Bundle();
                    DetailFragment df = new DetailFragment();
                    df.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df).commit();
                }
            } else {
                mTwoPane = false;
            }

            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            forecastFragment.setUseTodayLayout(!mTwoPane);
            MMMGWeatherSyncAdapter.initializeSyncAdapter(this);
    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                AboutDialog about = new AboutDialog(this);
                about.setTitle("About");
                about.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(String date) {
        if (mTwoPane){
            Bundle args = new Bundle();
            args.putString( DetailActivity.DATE_KEY, date);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df).commit();

        }else {
            Intent intent = new Intent( this, DetailActivity.class);
            intent.putExtra(DetailActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }
}
