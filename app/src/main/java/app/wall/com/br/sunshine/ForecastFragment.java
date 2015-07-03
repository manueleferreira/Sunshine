package app.wall.com.br.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if( id == R.id.action_refresh )
        {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefLocation = prefs.getString( getString( R.string.pref_location_key ),
                getString(R.string.pref_location_default) );
        String prefUnit = prefs.getString( getString( R.string.pref_unit_key ), getString( R.string.pref_unit_label ) );

        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute(prefLocation, prefUnit);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // dummy data
        final List<String> weekForecast = new ArrayList<String>();

        // adapter
       mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview,
                        weekForecast
                );

        final ListView listView = (ListView) rootView.findViewById(R.id.list_item_forecast_textview);
        listView.setAdapter(mForecastAdapter);
        final AdapterView.OnItemClickListener adapterView = new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //final Context context = view.getContext();
                final Context context = getActivity();
                final String forecastItem = mForecastAdapter.getItem(position);


                Intent showDetailActivityIntent = new Intent(context, DetailActivity.class);
                showDetailActivityIntent.putExtra( Intent.EXTRA_TEXT, forecastItem );
                startActivity(showDetailActivityIntent);
            }
        };
        listView.setOnItemClickListener( adapterView );

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] result) {
            if( result != null )
            {
                mForecastAdapter.clear();
                for(String dateForecastStr : result)
                {
                    mForecastAdapter.add(dateForecastStr);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length == 0)
            {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            final String mode = "json";
            final int cnt = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builder = Uri.parse(FORECAST_BASE_URL).buildUpon().
                        appendQueryParameter(QUERY_PARAM, params[0]).
                        appendQueryParameter(FORMAT_PARAM, mode).
                        appendQueryParameter(UNITS_PARAM, params[1]).
                        appendQueryParameter(DAYS_PARAM, Integer.toString( cnt ) ).build();
                URL url = new URL( builder.toString() );

                Log.v( LOG_TAG, "Built URI: " + builder.toString() );

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try
            {
                final WeatherDataParser parser = new WeatherDataParser();
                final String[] valueParsedArray = parser.getWeatherDataFromJson( forecastJsonStr, cnt );
                return valueParsedArray;
            }
            catch( JSONException e)
            {
                Log.e( LOG_TAG, e.getMessage(), e );
                e.printStackTrace();
            }

            return null;
        }
    }

}