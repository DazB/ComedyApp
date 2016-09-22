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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // DazB Ticketmaster API key
    private static String apikey = System.getProperty("ticketmaster-api-key", "xoGWGgRDOLHGsutqGIk0YLGaNXaYhsAA");
    private static final String LAT = "53.9600";
    private static final String LONG = "-1.0873";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new TicketmasterGetEventsTask().execute();
    }

    /**
     * AsyncTask that handles the retrieving of events from Ticketmaster API
     * Params: ApiKey
     * Progress: Void
     * Result: List of events
     */
    class TicketmasterGetEventsTask extends AsyncTask<Void, Void, PagedResponse<Events>> {
        @Override
        protected PagedResponse<Events> doInBackground(Void... params) {
            // 1. Instantiate a DiscoveryApi client:
            DiscoveryApi discoveryApi = new DiscoveryApi(apikey);
            // 2. Make our first search
            PagedResponse<Events> response = null;
            try {
                response =
                        discoveryApi.searchEvents(new SearchEventsOperation().latlong(LAT, LONG) // Adding a location filter
                                .pageSize(50) // Asking for a maximum of 50 events per pages
                                // TODO: way to add filter for just comedy? Cos else we have to filter through response, which isn't ideal, since suppose response don't have comedy, we get no results on UI
                        );
            } catch (Exception e) {
                Log.d(TAG, "RetrieveDataTask, PagedResponse Error: ", e);
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
