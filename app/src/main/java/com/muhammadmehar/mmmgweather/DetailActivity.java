package com.muhammadmehar.mmmgweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity {

    public final static String DATE_KEY = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null){
            Bundle args = new Bundle();
            args.putString(DATE_KEY, getIntent().getStringExtra(DATE_KEY));
            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction().add( R.id.weather_detail_container, df).commit();
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings_detail:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about_detail:
                AboutDialog about = new AboutDialog(this);
                about.setTitle("About");
                about.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
