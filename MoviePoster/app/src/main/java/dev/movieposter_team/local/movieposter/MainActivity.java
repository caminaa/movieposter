package dev.movieposter_team.local.movieposter;

/**
 * Created by Adrien on 12/03/2015.
 */
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {

    public ProgressDialog pDialog;

    // URL to get contacts JSON
    public static String url = "http://api.allocine.fr/rest/v3/movielist?partner=YW5kcm9pZC12M3M&count=25&filter=nowshowing&page=1&order=datedesc&format=json";

    // JSON Node names
    private static final String TAG_MOVIES = "movie";
    private static final String TAG_STATS = "statistics";
    private static final String TAG_TITLE = "title";
    //private static final String TAG_PRODUCTIONYEAR = "productionYear";
    private static final String TAG_SYNOPSIS = "synopsisShort";
    private static final String TAG_PRESS = "pressRating";


    // movies JSONArray
    public JSONArray movies = null;

    // Hashmap for ListView
    public ArrayList<HashMap<String, String>> moviesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        // Listview on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String title = ((TextView) view.findViewById(R.id.title))
                        .getText().toString();
                String synopsis = ((TextView) view.findViewById(R.id.synopsisShort))
                        .getText().toString();
                String press = ((TextView) view.findViewById(R.id.pressRating))
                        .getText().toString();

                // Starting single contact activity
                Intent in = new Intent(getApplicationContext(), SingleMovieActivity.class);
                in.putExtra(TAG_TITLE, title);
                in.putExtra(TAG_SYNOPSIS, synopsis);
                in.putExtra(TAG_PRESS, press);
                startActivity(in);

            }
        });

        // Calling async task to get json
        new GetMovies().execute();
    }
    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetMovies extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    movies = jsonObj.getJSONArray(TAG_MOVIES);

                    // looping through All Contacts
                    for (int i = 0; i < movies.length(); i++) {
                        JSONObject c = movies.getJSONObject(i);

                        String title = c.getString(TAG_TITLE);
                        String synopsisShort = c.getString(TAG_SYNOPSIS);



                        // Stats node is JSON Object
                        JSONObject statistics = c.getJSONObject(TAG_STATS);
                        String press = c.getString(TAG_PRESS);

                        // tmp hashmap for single contact
                        HashMap<String, String> movies = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        movies.put(TAG_TITLE, title);
                        movies.put(TAG_SYNOPSIS, synopsisShort);
                        movies.put(TAG_PRESS, press);


                        // adding contact to contact list
                        moviesList.add(movies);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, moviesList,
                    R.layout.list_item, new String[] { TAG_TITLE, TAG_SYNOPSIS,
                    TAG_PRESS }, new int[] { R.id.title,
                    R.id.synopsisShort, R.id.pressRating });

            setListAdapter(adapter);
        }

    }
}