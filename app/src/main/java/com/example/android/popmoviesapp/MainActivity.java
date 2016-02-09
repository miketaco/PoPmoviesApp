package com.example.android.popmoviesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.android.popmoviesapp.data.MovieDbHelper;
import com.example.android.popmoviesapp.data.MovieDetailContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter <String>movieAdapter;
    ImageAdapter posterAdapter;
    ArrayList<String> movieIds;
    public static final String API_KEY = BuildConfig.MOVIE_DB_API_KEY;

    //local db Helper
    private MovieDbHelper dbHelper;

    private String selectedSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        movieIds = new ArrayList<String>();

        setContentView(R.layout.activity_main);

        //set icon on toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.pop_launcher);

        //init movie adapter
        movieAdapter = new ArrayAdapter<String>(getApplicationContext() ,R.layout.content_main , new ArrayList<String>());
        //init poster adapter
        posterAdapter = new ImageAdapter(this);
        //init dbHelper
        dbHelper = new MovieDbHelper(getApplicationContext());


        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(posterAdapter);

        Spinner spinner = (Spinner) findViewById(R.id.movie_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //set the spinner activity
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        selectedSort = spinner.getSelectedItem().toString();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
        //                Toast.makeText(MainActivity.this, "" + movieIds.get(position),
        //                        Toast.LENGTH_SHORT).show();

                Log.v(this.getClass().getSimpleName(),"movieID="+movieIds.get(position));

                Intent intent = new Intent(parent.getContext(), MovieGridActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,  movieIds.get(position));
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(selectedSort.equals("Favorites")) {
            setUpFavoritesFromDB();
            posterAdapter.notifyDataSetChanged();
        }
        Log.v(this.getClass().getSimpleName(), "onResume selected="+selectedSort);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        // here we want to call the task to call the api and setup images

//        imageDownTask.execute("popularity.desc", "", null);
    }


    /**
     * helper method to make db read to get all favorite movies
     * and assign values to local variable
     */
    private void setUpFavoritesFromDB() {

        movieIds.clear();;
        posterAdapter.clearItems();

        //read the db to look for movie record
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] projection = {
                MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE};

        Cursor cursor = db.query(
                MovieDetailContract.MovieEntry.TABLE_NAME,  // The table to query
                null,                               // The columns to return null for all
                MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE + "='true'",                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );



        if(cursor!=null) {

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {


                Log.v(getClass().getSimpleName(),cursor.getString(cursor.getColumnIndex(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_NAME)));
                Log.v(getClass().getSimpleName(),cursor.getString(cursor.getColumnIndex(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID)));
                Log.v(getClass().getSimpleName(),cursor.getString(cursor.getColumnIndex(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_IMAGE)));

                movieIds.add(cursor.getString(cursor.getColumnIndex(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID)));
                posterAdapter.addItem(cursor.getString(cursor.getColumnIndex(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_IMAGE)));

                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

    }


    /**
     * inner class the handle the spinner actions
     */
    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            PosterImageTask imageDownTask= new PosterImageTask();

            selectedSort = (String)parent.getItemAtPosition(pos);

            Log.v(getClass().getSimpleName(), selectedSort);

            imageDownTask.execute(selectedSort, "", null);

//            if(pos==0) {
//                imageDownTask.execute("popularity.desc", "", null);
//            }else if(pos==1) {
//                imageDownTask.execute("vote_count.desc", "", null);
//            }else if(pos==2){
//                imageDownTask.execute("now_playing", "", null);
//            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public class ImageAdapter extends BaseAdapter {
         private Context mContext;
         private ArrayList<String>imageUrls;


        public ImageAdapter(Context c) {
            mContext = c;
            imageUrls = new ArrayList<String>();
        }

        public int getCount() {
            return imageUrls.size();
        }

        public void clearItems(){
            imageUrls.clear();
        }

        public String getItem(int position) {
            return imageUrls.get(position);
        }

        public void addItem(String imageURL){
            imageUrls.add(imageURL);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
//                imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                imageView.setPadding(2, 2, 2, 2);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }

            String url = getItem(position);

            Picasso.with(mContext).load(url).into(imageView);



            return imageView;
        }


    }



    public class PosterImageTask extends AsyncTask<String, String, String[]> {

        String sortOrder;

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            // contruct the movie api call

            sortOrder = params[0];


//            http://api.themoviedb.org/3/movie/now_playing
            Uri builtUri;

            if(sortOrder.equals("Now Playing")) {

                builtUri = Uri.parse("http://api.themoviedb.org/3/movie/now_playing").
                        buildUpon().appendQueryParameter("api_key", API_KEY).build();

            }else if(sortOrder.equals("Most Popular")){

                builtUri = Uri.parse("http://api.themoviedb.org/3/discover/movie?").
                        buildUpon().appendQueryParameter("sort_by", "popularity.desc")
                        .appendQueryParameter("api_key", API_KEY).build();
            } else if(sortOrder.equals("Favorites")){

                //the favorites option can read all the movie data from the db
                setUpFavoritesFromDB();

                return new String[0];
            } else {
                builtUri = Uri.parse("http://api.themoviedb.org/3/discover/movie?").
                        buildUpon().appendQueryParameter("sort_by", "vote_count.desc")
                        .appendQueryParameter("api_key", API_KEY).build();
            }


            Log.v(getClass().getSimpleName(), builtUri.toString());

            try {
            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                    return null;
                }
                movieJsonStr = buffer.toString();

                parseJsonString(movieJsonStr);

            }catch(IOException ioe) {
                ioe.printStackTrace();
            }            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(getClass().getName(), "Error closing stream", e);
                    }
                }
            }

            return new String[0];
        }


        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            posterAdapter.notifyDataSetChanged();
        }

        /**
         * parse the Json reponse add image Urls to the adapter clas
         * @param jsonResponse
         */
        private void parseJsonString(String jsonResponse) {

            String IMAGE_URL="http://image.tmdb.org/t/p/w185";

            //clear all the movie ids and refresh the list
            movieIds.clear();
            posterAdapter.clearItems();
            try {
            JSONObject forecastJson = new JSONObject(jsonResponse);
            JSONArray movieArray = forecastJson.getJSONArray("results");

            //iterate results objects
            for(int i=0; i<movieArray.length();i++){

                JSONObject movieObj =
                movieArray.getJSONObject(i);
                //construct image URL
                String imagePath = IMAGE_URL+ movieObj.getString("poster_path");

                Log.v(getClass().getSimpleName(),imagePath);
                //add imagepath to the image adapterclass
                posterAdapter.addItem(imagePath);
                //add id to the movieID array
                movieIds.add( movieObj.getString("id"));
            }



            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
     }
}
