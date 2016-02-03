package com.example.android.popmoviesapp.data;

import android.provider.BaseColumns;

/**
 * Created by mtacopino on 2/2/2016.
 */
public class MovieDetailContract {

    public MovieDetailContract(){
    }

    /* Inner class that defines the table contents */
    public static abstract class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_MOVIE_NAME = "movie_name";
        public static final String COLUMN_NAME_MOVIE_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_DESCRIPTION = "desc";
        public static final String COLUMN_NAME_MOVIE_RUN_TIME = "runtime";
        public static final String COLUMN_NAME_MOVIE_RATING = "rating";
        public static final String COLUMN_NAME_MOVIE_IMAGE = "image_path";
        public static final String COLUMN_NAME_MOVIE_ISFAVORITE = "isfavorite";

    }

    public static abstract class MovieVideoEntry implements BaseColumns {
        public static final String TABLE_NAME = "video";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_MOVIE_VIDEOID = "videoid";
    }

    public static abstract class MovieReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";
        public static final String COLUMN_NAME_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME_MOVIE_REVIEW_USER = "user";
        public static final String COLUMN_NAME_MOVIE_REVIEW = "review";
    }


}
