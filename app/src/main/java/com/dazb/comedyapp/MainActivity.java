package com.dazb.comedyapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.ticketmaster.api.discovery.DiscoveryApi;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Attraction;
import com.ticketmaster.discovery.model.Classification;
import com.ticketmaster.discovery.model.Date;
import com.ticketmaster.discovery.model.Date.Start;
import com.ticketmaster.discovery.model.Event;
import com.ticketmaster.discovery.model.Events;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    // Debug log TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    // DazB Ticketmaster API key
    private static String apikey = System.getProperty("ticketmaster-api-key", "xoGWGgRDOLHGsutqGIk0YLGaNXaYhsAA");

    // ENTS24 API keys
    private static String entsClientID = "91fdea0e6e8de74094ad0495f34ba081903e8bea";
    private static String entsClientSecret = "bfd40f1dcb565e9a0e206395c7ae7c6f108cd26a";
    private static String entsUsername = "dazbahri@hotmail.co.uk";
    private static String entsPassword = "ShitPissFuckCunt";
    private String entsToken;


    // Instantiate a DiscoveryApi client:
    DiscoveryApi discoveryApi = new DiscoveryApi(apikey);

    // York
    private static final String LAT = "53.9600";
    private static final String LONG = "-1.0873";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new EntsGetEventsTask().execute();
        new TicketmasterGetEventsTask().execute();
    }

    /**
     * AsyncTask that handles the retrieving of events from Ticketmaster API
     * Params: Void
     * Progress: Void
     * Result: List of events
     */
    class TicketmasterGetEventsTask extends AsyncTask<Void, Void, PagedResponse<Events>> {
        @Override
        protected PagedResponse<Events> doInBackground(Void... params) {
            // Make our first search
            PagedResponse<Events> response = null;
            try {
                response =
                        discoveryApi.searchEvents(new SearchEventsOperation().latlong(LAT, LONG) // Adding a location filter
                                .pageSize(50) // Asking for a maximum of 50 events per pages
                                // TODO: way to add filter for just comedy? Cos else we have to filter through response, which isn't ideal, since suppose response don't have comedy, we get no results on UI
                        );
            } catch (Exception e) {
                Log.d(TAG, "TicketmasterGetEventsTask, PagedResponse Error: ", e);
            }
            // If we get some sort of response, return it. Else, return null
            if (response != null) {
                Log.d(TAG, response.toString());
                Log.d(TAG, response.getContent().getEvents().toString());
                return response;
            }
            return null;
        }

        /**
         * Once we get a response from our search, update the UI with the results
         * @param response events returned from doInBackground
         */
        @Override
        protected void onPostExecute(PagedResponse<Events> response) {
            TextView events = (TextView)findViewById(R.id.textView);
            events.setMovementMethod(new ScrollingMovementMethod()); // Make shit scroll

            if (response != null) {
                // Go through every event returned
                for(com.ticketmaster.discovery.model.Event event : response.getContent().getEvents()) {
                    // If event classified as comedy, update UI with the event name
                    // TODO: as discussed above, not ideal. Really we want only comedy events returned from Ticketmaster, not any old event
                    if (event.getClassifications().get(0).getGenre().getName().toLowerCase().contains("comedy")) {
                        events.append(event.getName() + "\r\n");
                    }
                }
            }
        }
    }

    /**
     * AsyncTask that handles the retrieving of events from Ents24 API
     * Params: Void
     * Progress: Void
     * Result: Void
     */
    class EntsGetEventsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Create connection
                HttpURLConnection entsConnection = (HttpURLConnection) new URL("https://api.ents24.com/auth/login").openConnection();
                entsConnection.setRequestMethod("POST");
                entsConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                entsConnection.setUseCaches(false);
                entsConnection.setDoOutput(true);

                // Send request
                DataOutputStream wr = new DataOutputStream (
                        entsConnection.getOutputStream());
                wr.writeBytes("client_id="+ entsClientID + "&client_secret=" + entsClientSecret + "&username=" + entsUsername + "&password=" + entsPassword);
                wr.close();

                //Get Response
                int statusCode = entsConnection.getResponseCode();
                StringBuilder response = new StringBuilder();
                // Check request codes for Bad request
                if (statusCode >= 200 && statusCode < 400) {
                    // Good request
                    InputStream is = entsConnection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();

                    // Get token
                    JSONObject responseJSON = new JSONObject(response.toString());
                    if (responseJSON.getString("access_token") != null) {
                        entsToken = responseJSON.getString("access_token");
                    }
                    else {
                        entsToken = "";
                    }

                }
                else {
                    // Bad request
                    InputStream is = entsConnection.getErrorStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                }

                Log.d(TAG, response.toString());

            }
            catch (Exception e) {
                Log.d(TAG, "ENTS24 connection error: ", e);
            }

            return null;
        }

    }
}
