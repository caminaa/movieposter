package dev.movieposter_team.local.movieposter;

/**
 * Created by Adrien on 12/03/2015.
 */
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
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
    private static final String TAG_POSTER = "poster";
    private static final String TAG_HREF = "href";
    private static final String TAG_IMAGE = "image";



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


    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

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

                    //get feed
                    JSONObject feedObj = jsonObj.getJSONObject("feed");


                    // Getting JSON Array node
                    movies = feedObj.getJSONArray(TAG_MOVIES);

                    //ImageView[] imageViewTab = new ImageView[movies.length()];

                    // looping through All Contacts
                    for (int i = 0; i < movies.length(); i++) {

                        //imageViewTab[i] = new ImageView(MainActivity.this);

                        JSONObject c = movies.getJSONObject(i);

                        String title = c.getString(TAG_TITLE);
                        String synopsisShort = c.getString(TAG_SYNOPSIS);

                       // JSONObject posterObj = jsonObj.getJSONObject(TAG_POSTER);
                        //String href = posterObj.getString(TAG_HREF);


                       // (new ImageLoadTask(href, imageViewTab[i])).execute();

                        // Stats node is JSON Object
                        //JSONObject statistics = c.getJSONObject(TAG_STATS);
                        //String press = c.getString(TAG_PRESS);

                        // tmp hashmap for single contact
                        HashMap<String, String> movies = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        movies.put(TAG_TITLE, title);
                        movies.put(TAG_SYNOPSIS, synopsisShort);
                        //movies.put(TAG_PRESS, press);


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
                    R.layout.list_item, new String[] { TAG_TITLE, TAG_SYNOPSIS },
                    new int[] { R.id.title, R.id.synopsisShort });

            setListAdapter(adapter);
        }

    }
}