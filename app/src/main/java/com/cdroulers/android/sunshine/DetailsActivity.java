package com.cdroulers.android.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ShareActionProvider;
import android.widget.TextView;


public class DetailsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent detailsIntent = new Intent(this, SettingsActivity.class);
            startActivity(detailsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String LogTag = MainActivity.LogTag + "-Details";

        private String forecastString;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_details_fragment, menu);

            ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();

            shareActionProvider.setShareIntent(getDefaultShareIntent());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_details, container, false);

            Intent intent = getActivity().getIntent();
            forecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView forecastText = (TextView) rootView.findViewById(R.id.details_forecast);
            forecastText.setText(forecastString);

            return rootView;
        }

        private Intent getDefaultShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "OH GOD THE WEATHER");
            shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + " #SunshineApp");
            return shareIntent;
        }
    }
}
