package layout;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popmoviesapp.BuildConfig;
import com.example.android.popmoviesapp.R;
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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailsFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "MOVIEID";

    String movieTitle;
    String moviePlot;
    String movieRating;
    String releaseDate;
    String imageURL;
    String movieID;
    boolean isFavorite;

    String movieLength;
    List<String> movieReviews;
    List<String> videoIDList;

    private TextView movieTitleView;
    private TextView movieReleaseView;
    private TextView moviePlotView;
    private TextView movieRatingView;
    private ImageView moviePosterView;
    private TextView movieLengthView;
    private Button favoriteButton;

    ArrayAdapter<String> reviewAdapter;
    private LinearLayout buttonLayout;

    private MovieDbHelper dbHelper;
    private UpdateFavoriteTask favTask;

    // ADD API KEY
    public static final String API_KEY = BuildConfig.MOVIE_DB_API_KEY;


    private OnFragmentInteractionListener mListener;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MovieDetailsFragment.
     */
    public static MovieDetailsFragment newInstance(String param1, String param2) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieID = getArguments().getString(ARG_PARAM1);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //if the app just opened there is no movie selected just show blank view
        if(movieID==null) {

            View view = new LinearLayout(getActivity());
                   view.setBackgroundColor(getResources().getColor(R.color.background));

            return view;
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        Log.v(this.getClass().getSimpleName(), "movieIDExtra" + movieID);


        //assign local variables to the text views
        movieTitleView =(TextView) rootView.findViewById(R.id.movieTitle);
        movieReleaseView = (TextView) rootView.findViewById(R.id.movieRelease);
        moviePlotView = (TextView) rootView.findViewById(R.id.moviePlot);
        movieRatingView = (TextView) rootView.findViewById(R.id.movieRating);
        moviePosterView = (ImageView)rootView.findViewById(R.id.posterImageView);
        movieLengthView = (TextView) rootView.findViewById(R.id.movieLength);
        favoriteButton = (Button) rootView.findViewById(R.id.favoriteStarBtn);

        movieReviews = new ArrayList<String>();
        videoIDList = new ArrayList<String>();

        dbHelper = new MovieDbHelper(getActivity());


        //create array adapter
        reviewAdapter =
                new ArrayAdapter<String>(getActivity(),
                        R.layout.review_list_item,R.id.list_item_movieReview,
                        movieReviews);



        //bind adapter to listview
        ListView listView = (ListView) rootView.findViewById(R.id.listview_reviews);
        listView.setAdapter(reviewAdapter);


        buttonLayout = (LinearLayout)
                rootView.findViewById(R.id.trailerBtnLayout);

        if(movieID!=null) {
            //start movie task
            MovieDetailTask movieTask = new MovieDetailTask();
            movieTask.execute(movieID);
        }


        //onlick listener for the favorites button
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isFavorite = !isFavorite;

                favTask = new UpdateFavoriteTask();

                if (isFavorite) {
                    favoriteButton.setBackgroundResource(R.drawable.starfilled_48);
                    favTask.execute("true");

                    Toast toast = Toast.makeText(getActivity(), movieTitle + " added to your favorites!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    favoriteButton.setBackgroundResource(R.drawable.outlinedstar_48);
                    favTask.execute("false");
                }
            }
        });



        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public class UpdateFavoriteTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            //create content values from the values stored from api call
            ContentValues values = new ContentValues();
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID, movieID);
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_NAME, movieTitle);

            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE, params[0]);

            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_DESCRIPTION, moviePlot);
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_IMAGE, imageURL);
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RATING, movieRating);
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RELEASE_DATE, releaseDate);
            values.put(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RUN_TIME, movieLength);


            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    MovieDetailContract.MovieEntry.TABLE_NAME,
                    "null",
                    values);


            return null;
        }


    }

    /**
     * MovieDetailTask inner class
     */
    public class MovieDetailTask extends AsyncTask<String, String, String[]> {

        String movieID;

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;
            // contruct the movie api call

            movieID = params[0];

            //if we found the movie in db we dont need to call api
            if(!lookUpMovieInDB(movieID)) {

                //build uri to call api for all movie details
                Uri detailsBuiltUri = Uri.parse("http://api.themoviedb.org/3/movie/" + movieID + "?").buildUpon()
                        .appendQueryParameter("api_key", API_KEY).build();

                JSONObject movieDetailsString = getResponseFromUri(detailsBuiltUri);
                parseJsonStringDetails(movieDetailsString);
            }


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
         * helper method to lookup the movie and take action if found.
         */
        private boolean lookUpMovieInDB(String id){
            boolean isFound = false;

            //read the db to look for movie record
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String[] projection = {
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE};

            Cursor cursor = db.query(
                    MovieDetailContract.MovieEntry.TABLE_NAME,  // The table to query
                    null,                               // The columns to return
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID + "=" + id,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );

            //go to the first element of the cursor and get isFavorite column
            if (cursor.moveToFirst()) {
                String favoriteString = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE));

                //we found the movie in the DB lets set the fields so we do not have to cann api


                Log.v(getClass().getSimpleName(),"Movie found in DB loading details");
                imageURL = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_IMAGE));
                movieTitle = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_NAME));
                moviePlot = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_DESCRIPTION));
                movieRating =  cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RATING));
                releaseDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RELEASE_DATE));
                movieLength = cursor.getString(
                        cursor.getColumnIndexOrThrow(MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RUN_TIME));


                if(favoriteString.equals("true")){
                    isFavorite=true;
                    isFound = true;

                }

                Log.v(getClass().getSimpleName(),"Read from database isFavorite = "+favoriteString);
            }

            //close cursor and db connection
            cursor.close();
            db.close();

            return isFound;
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
         *  post execute method to set all text views to retrieved values
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
            movieLengthView.append(" mins");
            moviePlotView.append(moviePlot);

            //set up the image view with the picassa

            Picasso.with(getActivity()).load(imageURL).into(moviePosterView);

            //change favorite image if we found the flag set for this movie
            if (isFavorite) {
                favoriteButton.setBackgroundResource(R.drawable.starfilled_48);
            }



            for (int i = 0; i < videoIDList.size(); i++) {
                Button trailerButton = new Button(getActivity());
//                trailerButton.setText(R.string.trailer_button);
                if(videoIDList.size()==1){
                    //if there is only 1 video dont add extra text
                    trailerButton.setText("Watch Trailer");
                }else {
                    trailerButton.setText("Watch Trailer #" + (i + 1));
                }
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
        private void parseJsonStringDetails(JSONObject forecastJson) {

            String IMAGE_PATH="http://image.tmdb.org/t/p/w185";


            //clear all the movie ids and refresh the list
            try {

                imageURL = IMAGE_PATH + forecastJson.getString("poster_path");
                movieTitle = forecastJson.getString("original_title");
                moviePlot = forecastJson.getString("overview");

                movieRating =  Double.toString(forecastJson.getDouble("vote_average"));
                releaseDate = forecastJson.getString("release_date");
                movieLength = forecastJson.getString("runtime");

//                Log.v(getClass().getSimpleName(),imageURL);
//                Log.v(getClass().getSimpleName(),movieTitle);
//                Log.v(getClass().getSimpleName(),moviePlot);
//                Log.v(getClass().getSimpleName(),movieRating);
//                Log.v(getClass().getSimpleName(),releaseDate);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        /**
         * inner class to handle button listener for playing trailer intent
         */
        class trailerClickLister implements View.OnClickListener {

            String videoID;

            trailerClickLister(String id){
                super();
                videoID=id;
            }


            @Override
            public void onClick(View v) {

                //implementation to fall back on web browser if youtube not available
                try{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoID));
                    startActivity(intent);
                }catch (ActivityNotFoundException ex){
                    Intent intent=new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v="+videoID));
                    startActivity(intent);
                }
            }
        }
    }
}
