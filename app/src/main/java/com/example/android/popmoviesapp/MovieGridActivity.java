package com.example.android.popmoviesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.List;

public class MovieGridActivity extends AppCompatActivity {


    String movieTitle;
    String moviePlot;
    String movieRating;
    String releaseDate;
    String imageURL;
    String movieID;
//    String videoID;

    String movieLength;
    List<String> movieReviews;
    List<String> videoIDList;

    private TextView movieTitleView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieRatingView;
    private ImageView moviePosterView;
    private TextView movieLengthView;

    ArrayAdapter <String>reviewAdapter;


    private  LinearLayout buttonLayout;

    public static final String API_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get activity extra
        movieID = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        Log.v(this.getClass().getSimpleName(), "movieIDExtra" + movieID);


        //assign local variables to the text views
        movieTitleView =(TextView) findViewById(R.id.movieTitle);
        movieReleaseView = (TextView) findViewById(R.id.movieRelease);
        moviePlotView = (TextView) findViewById(R.id.moviePlot);
        movieRatingView = (TextView) findViewById(R.id.movieRating);
        moviePosterView = (ImageView)findViewById(R.id.posterImageView);
        movieLengthView = (TextView) findViewById(R.id.movieLength);

        movieReviews = new ArrayList<String>();
        videoIDList = new ArrayList<String>();

        //create array adapter
        reviewAdapter =
                new ArrayAdapter<String>(this,
                        R.layout.review_list_item,R.id.list_item_movieReview,
                        movieReviews);



        //bind adapter to listview
        ListView listView = (ListView) findViewById(R.id.listview_reviews);
                  listView.setAdapter(reviewAdapter);
//        listView.setAdapter(reviewAdapterUsers);


       buttonLayout = (LinearLayout)
        findViewById(R.id.trailerBtnLayout);


        //start movie task
        MovieDetailTask movieTask = new MovieDetailTask();
        movieTask.execute(movieID);
    }



    public class MovieDetailTask extends AsyncTask<String, String, String[]> {

        String movieID;

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            // contruct the movie api call

            movieID = params[0];

            //build uri to call api for all movie details
            Uri detailsBuiltUri = Uri.parse("http://api.themoviedb.org/3/movie/" + movieID + "?").buildUpon()
                    .appendQueryParameter("api_key", API_KEY).build();
            Log.v(getClass().getSimpleName(), detailsBuiltUri.toString());

            JSONObject movieDetailsString = getResponseFromUri(detailsBuiltUri);
            parseJsonString(movieDetailsString);

            //buil uri for video trailer clip and parse result
            Uri builtVideoUri = Uri.parse("http://api.themoviedb.org/3/movie/" + movieID + "/videos?").buildUpon()
                    .appendQueryParameter("api_key", API_KEY).build();
            JSONObject videoString = getResponseFromUri(builtVideoUri);
            parseJsonStringForVideo(videoString);

            //build uri for movie reviews and call Json parse on results
            Uri builtReviewUri = Uri.parse("http://api.themoviedb.org/3/movie/" + movieID + "/reviews?").buildUpon()
                    .appendQueryParameter("api_key", API_KEY).build();
            JSONObject reviewString = getResponseFromUri(builtReviewUri);
            parseJsonStringForReview(reviewString);


            return new String[0];
        }

        /**
         * Call the apo URI supplies and return json string
         * @param builtUri
         * @return
         */
        private JSONObject getResponseFromUri(Uri builtUri) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            JSONObject jsonResp = new JSONObject();

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

            try {
                 jsonResp = new JSONObject(movieJsonStr);
            } catch (JSONException e) {
            e.printStackTrace();
            }


            return jsonResp;
        }

        /**
         *
         * @param strings
         */
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            //want to trigger the activity updates here
            movieTitleView.append(movieTitle);
            movieReleaseView.append(releaseDate);
            movieRatingView.append(movieRating);
            movieRatingView.append(" / 10");
            movieLengthView.append(movieLength);
            moviePlotView.append(moviePlot);

            //set up the image view with the picassa

            Picasso.with(getApplicationContext()).load(imageURL).into(moviePosterView);

            for (int i = 0; i < videoIDList.size(); i++) {
                Button trailerButton = new Button(getApplicationContext());
//                trailerButton.setText(R.string.trailer_button);
                trailerButton.setText("Watch Trailer #"+ (i+1));
                trailerButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play24, 0, 0);
                trailerButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                buttonLayout.addView(trailerButton);

                trailerClickLister clickListen = new trailerClickLister(videoIDList.get(i));
                trailerButton.setOnClickListener(clickListen);
            }
            //want to trigget the activity updates here
     }

        /**
         * parse the Json reponse for the youtube trailer key
         * @param jsonResponse
         */
        private void parseJsonStringForVideo(JSONObject jsonResponse) {

            //clear all the movie ids and refresh the list
            try {

                JSONArray resultArray =     jsonResponse.getJSONArray("results");

            for(int i=0;i<resultArray.length();i++) {
                videoIDList.add(resultArray.getJSONObject(i).getString("key"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        /**
         * parse the Json reponse  for the user movie reviews
         * @param jsonResponse
         */
        private void parseJsonStringForReview(JSONObject jsonResponse) {

            //clear all the movie ids and refresh the list
            try {

                JSONArray resultArray =     jsonResponse.getJSONArray("results");

                for(int i=0; i< resultArray.length();i++){

                    String auth = resultArray.getJSONObject(i).getString("author");
                    if(auth==null) {
                        auth = "";
                    }
                    String content = resultArray.getJSONObject(i).getString("content");
                    if(content==null) {
                        content="";
                    }
                    movieReviews.add(content + "\n"+auth);
                }
//                reviewAdapter.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        /**
         * parse the Json reponse add image Urls to the adapter clas
         * @param forecastJson
         */
        private void parseJsonString(JSONObject forecastJson) {

            String IMAGE_PATH="http://image.tmdb.org/t/p/w185";


            //clear all the movie ids and refresh the list
            try {
//                JSONObject forecastJson = new JSONObject(jsonResponse);

                imageURL = IMAGE_PATH + forecastJson.getString("poster_path");
                movieTitle = forecastJson.getString("original_title");
                moviePlot = forecastJson.getString("overview");

                movieRating =  Double.toString(forecastJson.getDouble("vote_average"));
                releaseDate = forecastJson.getString("release_date");
                movieLength = forecastJson.getString("runtime");

                Log.v(getClass().getSimpleName(),imageURL);
                Log.v(getClass().getSimpleName(),movieTitle);
                Log.v(getClass().getSimpleName(),moviePlot);
                Log.v(getClass().getSimpleName(),movieRating);
                Log.v(getClass().getSimpleName(),releaseDate);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        /**
         * inner class to handle button listener
         */
        class trailerClickLister implements View.OnClickListener {

            String videoID;

            trailerClickLister(String id){
                super();
                videoID=id;
            }


            @Override
            public void onClick(View v) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoID)));
            }
        }
    }

}
