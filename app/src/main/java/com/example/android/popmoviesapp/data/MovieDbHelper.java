package com.example.android.popmoviesapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mtacopino on 2/2/2016.
 */
public class MovieDbHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "popmovie.db";

    // create statement for movie details
    private static final String SQL_CREATE_MOVIE_TABLE =
            "CREATE TABLE " + MovieDetailContract.MovieEntry.TABLE_NAME + " (" +
                    MovieDetailContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID + " INTEGER NOT NULL ,"+
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_NAME + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_DESCRIPTION + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RELEASE_DATE + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RATING + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_RUN_TIME + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ISFAVORITE + " TEXT , " +
                    MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_IMAGE + " TEXT )" +
                    //create unique key to guarentee only one record per movie
                    " UNIQUE (" + MovieDetailContract.MovieEntry.COLUMN_NAME_MOVIE_ID
                    + ") ON CONFLICT REPLACE);";;

    // statement to create video table
    private static final String SQL_CREATE_MOVIE_VIDEO_TABLE =
            "CREATE TABLE " + MovieDetailContract.MovieVideoEntry.TABLE_NAME + " (" +
                    MovieDetailContract.MovieVideoEntry._ID + " INTEGER PRIMARY KEY," +
                    MovieDetailContract.MovieVideoEntry.COLUMN_NAME_MOVIE_ID + " INTEGER NOT NULL ,"+
                    MovieDetailContract.MovieVideoEntry.COLUMN_NAME_MOVIE_VIDEOID + " INTEGER NOT NULL );";

    //statement to create the review table
    private static final String SQL_CREATE_MOVIE_REVIEW_TABLE =
            "CREATE TABLE " + MovieDetailContract.MovieReviewEntry.TABLE_NAME + " (" +
                    MovieDetailContract.MovieReviewEntry._ID + " INTEGER PRIMARY KEY," +
                    MovieDetailContract.MovieReviewEntry.COLUMN_NAME_MOVIE_ID + " INTEGER NOT NULL ,"+
                    MovieDetailContract.MovieReviewEntry.COLUMN_NAME_MOVIE_REVIEW + " TEXT ,"+
                    MovieDetailContract.MovieReviewEntry.COLUMN_NAME_MOVIE_REVIEW_USER + " TEXT );";

    private static final String SQL_DROP_MOVIE_TABLE =
            "DROP TABLE IF EXISTS " + MovieDetailContract.MovieEntry.TABLE_NAME;
    private static final String SQL_DROP_VIDEO_TABLE =
            "DROP TABLE IF EXISTS " + MovieDetailContract.MovieVideoEntry.TABLE_NAME;
    private static final String SQL_DROP_REVIEW_TABLE =
            "DROP TABLE IF EXISTS " + MovieDetailContract.MovieReviewEntry.TABLE_NAME;


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DROP_MOVIE_TABLE);
        onCreate(sqLiteDatabase);
    }


}
