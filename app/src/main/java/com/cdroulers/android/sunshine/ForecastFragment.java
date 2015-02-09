package com.cdroulers.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.cdroulers.android.sunshine.data.WeatherContract;
import com.cdroulers.android.sunshine.data.WeatherContract.WeatherEntry;
import com.cdroulers.android.sunshine.data.WeatherContract.LocationEntry;
import com.cdroulers.android.sunshine.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Date;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    private SimpleCursorAdapter forecastsAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(SettingsManager.getLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        String location = SettingsManager.getLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastsAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                // the column names to use to fill the textviews
                new String[]{
                        WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{
                        R.id.list_item_forecast_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_forecast_high_textview,
                        R.id.list_item_forecast_low_textview
                },
                0
        );
        forecastsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = SettingsManager.isMetric(getActivity());
                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP: {
                        // we have to do some formatting and possibly a conversion
                        ((TextView) view).setText(SettingsManager.formatTemperature(cursor.getDouble(columnIndex), isMetric));
                        return true;
                    }
                    case COL_WEATHER_DATE: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(SettingsManager.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);

        forecastListView.setAdapter(forecastsAdapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                String forecast = cursor.getString(COL_WEATHER_DATE) + cursor.getString(COL_WEATHER_DESC) + cursor.getDouble(COL_WEATHER_MAX_TEMP) + cursor.getDouble(COL_WEATHER_MIN_TEMP);
                Intent detailsIntent = new Intent(getActivity(), DetailsActivity.class);
                detailsIntent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailsIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = SettingsManager.getLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastsAdapter.swapCursor(null);
    }
}
