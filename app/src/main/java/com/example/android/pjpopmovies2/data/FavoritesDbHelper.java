package com.example.android.pjpopmovies2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pjpopmovies2.data.FavoritesContract.*;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "favorites.db";
    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;
    //  Constructortakes a context and calls the parent constructor
    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create an String that will create the favorites table
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID + " INTEGER," +
                FavoritesEntry.COLUMN_MV_MOVIEID + " TEXT NOT NULL, " +
                FavoritesEntry.COLUMN_MV_TITLE + " TEXT NOT NULL, " +
                FavoritesEntry.COLUMN_MV_POSTERURL + ", " +
                FavoritesEntry.COLUMN_MV_SYNOPSIS + ", " +
                FavoritesEntry.COLUMN_MV_RATING + ", " +
                FavoritesEntry.COLUMN_MV_RELEASEDATE + ", " +
                "PRIMARY KEY (" + FavoritesEntry.COLUMN_MV_MOVIEID +  ")" +
                "); ";
        // Create an String that will create the reviews table
        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +
                ReviewsEntry._ID + " INTEGER," +
                ReviewsEntry.COLUMN_RV_MOVIEID + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_RV_REVIEWID + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_RV_AUTHOR + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_RV_CONTENT + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_RV_URL + ", " +
                "PRIMARY KEY (" +
                ReviewsEntry.COLUMN_RV_MOVIEID + ", " + ReviewsEntry.COLUMN_RV_REVIEWID + ")" +
                "); ";
        // Create String that will create the videos table
        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + VideosEntry.TABLE_NAME + " (" +
                VideosEntry._ID + " INTEGER," +
                VideosEntry.COLUMN_VD_MOVIEID + " TEXT NOT NULL, " +
                VideosEntry.COLUMN_VD_YTVIDEOKEY + " TEXT NOT NULL, " +
                VideosEntry.COLUMN_VD_TYPE + ", " +
                VideosEntry.COLUMN_VD_TITLE + ", " +
                "PRIMARY KEY (" +
                VideosEntry.COLUMN_VD_MOVIEID + ", " + VideosEntry.COLUMN_VD_YTVIDEOKEY + ")" +
                "); ";
        // Execute the query by calling execSQL on sqLiteDatabase
        //  and pass the string query SQL_CREATE_FAVORITES_TABLE
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // drop table,  then re-create
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VideosEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
