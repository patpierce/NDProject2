package com.example.android.pjpopmovies2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
//import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.pjpopmovies2.ReviewListRecyclerAdapter.ListItemClickListener;
import com.example.android.pjpopmovies2.data.FavoritesContract;
import com.example.android.pjpopmovies2.data.FavoritesDbHelper;
import com.example.android.pjpopmovies2.utilities.MovieJsonUtils;
import com.example.android.pjpopmovies2.utilities.NetworkUtils;

import java.net.URL;

public class DetailActivity extends AppCompatActivity
        implements ListItemClickListener {

//    private static final String TAG = DetailActivity.class.getSimpleName();

    private ReviewListRecyclerAdapter mReviewsAdapter;
    private RecyclerView mReviewsListRecView;
    private VideoListRecyclerAdapter mVideosAdapter;
    private RecyclerView mVideosListRecView;

    private SQLiteDatabase mDb;
    private final FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
    private Boolean favoriteMovie = false;

    private static String[][] reviewsData = null;
    private static String[][] videosData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intentThatStartedThisActivity = getIntent();

        TextView mDetailTitleView = findViewById(R.id.tv_detail_title);
        TextView mDetailRatingView = findViewById(R.id.tv_detail_rating);
        TextView mDetailSynopsisView = findViewById(R.id.tv_detail_synopsis);
        TextView mReleaseDateView = findViewById(R.id.tv_release_date);
        ImageView mDetailPosterView = findViewById(R.id.iv_detail_movie_poster);
        CheckBox mFavoriteStarView = findViewById(R.id.cb_favorite);
        Context context = mDetailPosterView.getContext();

        final String mMovieId;
        final String mTitle;
        final String mPosterUrl;
        final String mSynopsis;
        final String mRating;
        final String mReleaseDate;

        String posterBaseUrl = "https://image.tmdb.org/t/p/w342/";
        Bundle data = getIntent().getExtras();

        // Display the movie info that was passed from MainActivity
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movieentry")) {
                MovieEntry movie = data.getParcelable("movieentry");

                mMovieId = movie.getId();
                mTitle = movie.getTitle();
                mPosterUrl = movie.getPoster();
                String posterUrl = posterBaseUrl + mPosterUrl;
                mSynopsis = movie.getSynopsis();
                mRating = movie.getRating();
                String ratingMessage = "Rating: " + mRating;
                mReleaseDate = movie.getReleaseDate();

                Glide.with(context).load(posterUrl).into(mDetailPosterView);

                mDetailTitleView.setText(mTitle);
                mDetailSynopsisView.setText(mSynopsis);
                mDetailRatingView.setText(ratingMessage);
                mDetailSynopsisView.setText(mSynopsis);
                mReleaseDateView.setText(mReleaseDate);

                mDb = dbHelper.getWritableDatabase();
                Cursor fcursor = mDb.rawQuery(
                        "SELECT * FROM favorites WHERE movieId ='" + mMovieId + "'", null);
                if (fcursor.moveToFirst()) {
                    mFavoriteStarView.setChecked(true);
                    favoriteMovie = true;
//                    Log.d(TAG, "onCreate: fcursor in movetofirst");
                }
                fcursor.close();

                /*
                 * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
                 * do things like set the adapter of the Recyc/lerView and toggle the visibility.
                 */
                mReviewsListRecView = findViewById(R.id.rv_reviews);
                LinearLayoutManager rlayoutManager = new LinearLayoutManager(this);
                mReviewsListRecView.setLayoutManager(rlayoutManager);

                /*
                 * Use this setting to improve performance if you know that changes in content do not
                 * change the child layout size in the RecyclerView
                 */
                mReviewsListRecView.setHasFixedSize(true);

                // Pass in this as the ListItemClickListener to the ReviewListRecyclerAdapter constructor
                /*
                 * The ReviewListRecyclerAdapter is responsible for displaying each item in the list.
                 */
                mReviewsAdapter = new ReviewListRecyclerAdapter(this);
                mReviewsListRecView.setAdapter(mReviewsAdapter);

                loadReviewsData(mMovieId);

                /*
                 * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
                 * do things like set the adapter of the RecyclerView and toggle the visibility.
                 */
                mVideosListRecView = findViewById(R.id.rv_videos);
                LinearLayoutManager tlayoutManager = new LinearLayoutManager(this);
                mVideosListRecView.setLayoutManager(tlayoutManager);

                /*
                 * Use this setting to improve performance if you know that changes in content do not
                 * change the child layout size in the RecyclerView
                 */
                mVideosListRecView.setHasFixedSize(true);


                // Pass in this as the ListItemClickListener to the MovieListRecyclerAdapter constructor
                /*
                 * The VideoListRecyclerAdapter is responsible for displaying each item in the list.
                 */
                mVideosAdapter = new VideoListRecyclerAdapter(
                        new VideoListRecyclerAdapter.ListItemClickListener() {
                            @Override
                            public void onListItemClick(String[] videoInfo) {
                                openVideoUrl("https://www.youtube.com/watch?v=" + videoInfo[0]);
                            }
                        });
                mVideosListRecView.setAdapter(mVideosAdapter);

                loadVideosData(mMovieId);

                mFavoriteStarView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // if isChecked is true save info to database
//                            Log.d(TAG, "onCheckedChanged: star isChecked " + isChecked);
                            addAsFavorite(mMovieId, mTitle, mPosterUrl,
                                    mSynopsis, mRating, mReleaseDate,
                                    reviewsData, videosData);
                        } else {
                            // if isChecked is false remove row from database
//                            Log.d(TAG, "onCheckedChanged: star isChecked " + isChecked);
                            removeFavorite(mMovieId);

                        }
                    }
                });
            }
        }
    }

    /**
     * Query the mDb
     * @return Cursor containing the list of favorite moview
     */
    private Cursor getReviewsForFavoriteMovie(String movieId) {
        // query mDb passing in the table name and projection String []
        return mDb.query(
                FavoritesContract.ReviewsEntry.TABLE_NAME,
                null,
                FavoritesContract.ReviewsEntry.COLUMN_RV_MOVIEID + "=" + movieId,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Query the mDb
     * @return Cursor containing the list of favorite moview
     */
    private Cursor getVideosForFavoriteMovie(String movieId) {
        // query mDb passing in the table name and projection String []
        return mDb.query(
                FavoritesContract.VideosEntry.TABLE_NAME,
                null,
                FavoritesContract.VideosEntry.COLUMN_VD_MOVIEID + "=" + movieId,
                null,
                null,
                null,
                null
        );
    }

    private void addAsFavorite(String movieID, String title, String posterUrl,
                               String synopsis, String rating, String releaseDate,
                               String[][] reviews, String[][] videos) {

        favoriteMovie = true;

        ContentValues mcv = new ContentValues();
        ContentValues rcv = new ContentValues();
        ContentValues vcv = new ContentValues();

        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID, movieID);
        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_TITLE, title);
        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_POSTERURL, posterUrl);
        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_SYNOPSIS, synopsis);
        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_RATING, rating);
        mcv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_RELEASEDATE, releaseDate);

        mDb.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, mcv);

        if (reviews.length != 0) {
            for (int i = 0; i < reviews.length; i++) {
//                Log.d(TAG, "addAsFavorite: reviews row" + i);
                String reviewId = reviews[i][0];
                String author = reviews[i][1];
                String content = reviews[i][2];
                String url = reviews[i][3];

                rcv.put(FavoritesContract.ReviewsEntry.COLUMN_RV_MOVIEID, movieID);
                rcv.put(FavoritesContract.ReviewsEntry.COLUMN_RV_REVIEWID, reviewId);
                rcv.put(FavoritesContract.ReviewsEntry.COLUMN_RV_AUTHOR, author);
                rcv.put(FavoritesContract.ReviewsEntry.COLUMN_RV_CONTENT, content);
                rcv.put(FavoritesContract.ReviewsEntry.COLUMN_RV_URL, url);
//                Log.d(TAG, "addAsFavorite: " + i + ": " +reviews[i][1]);

                mDb.insert(FavoritesContract.ReviewsEntry.TABLE_NAME, null, rcv);

            }
        }
        if (videos.length != 0) {
            for (int i = 0; i < videos.length; i++) {
//                Log.d(TAG, "addAsFavorite: i " + i);
                String ytVideoKey = videos[i][0];
                String vidtype = videos[i][1];
                String vidtitle = videos[i][2];

//                Log.d(TAG, "addAsFavorite: ytVideoKey " + ytVideoKey);

                vcv.put(FavoritesContract.VideosEntry.COLUMN_VD_MOVIEID, movieID);
                vcv.put(FavoritesContract.VideosEntry.COLUMN_VD_YTVIDEOKEY, ytVideoKey);
                vcv.put(FavoritesContract.VideosEntry.COLUMN_VD_TYPE, vidtype);
                vcv.put(FavoritesContract.VideosEntry.COLUMN_VD_TITLE, vidtitle);

                mDb.insert(FavoritesContract.VideosEntry.TABLE_NAME, null, vcv);

            }
        }
    }

    private void removeFavorite(String movieID) {
        //  call mDb.delete to pass in the TABLE_NAME and the movieId to delete
        mDb.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID + "=" + movieID, null);
        mDb.delete(FavoritesContract.ReviewsEntry.TABLE_NAME,
                FavoritesContract.ReviewsEntry.COLUMN_RV_MOVIEID + "=" + movieID, null);
        mDb.delete(FavoritesContract.VideosEntry.TABLE_NAME,
                FavoritesContract.VideosEntry.COLUMN_VD_MOVIEID + "=" + movieID, null);
    }

    /**
     * This method gets the review data in the background.
     */
    private void loadReviewsData(String movieId) {
        new FetchReviewsTask().execute(movieId);
    }

    /**
     * This method gets the review data in the background.
     */
    private void loadVideosData(String movieId) {
        new FetchVideosTask().execute(movieId);
    }

    // Override ListItemClickListener's onListItemClick method

    /**
     * This is where we receive our callback from
     * {@link com.example.android.pjpopmovies2.ReviewListRecyclerAdapter.ListItemClickListener}
     * <p>
     * This callback is invoked when you click on an item in the list.
     *
     * @param reviewInfo Index in the array of movie info for the item that was clicked.
     */
    @Override
    public void onListItemClick(String[] reviewInfo) {
        Context context = this;

        String reviewId = reviewInfo[0];
        String author = reviewInfo[1];
        String content = reviewInfo[2];
        String url = reviewInfo[3];

//        Log.d(TAG, "onListItemClick: id " + reviewId);
//        Log.d(TAG, "onListItemClick: au " + author);
//        Log.d(TAG, "onListItemClick: cn " + content);
//        Log.d(TAG, "onListItemClick: ur " + url);

        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("reviewentry",
                new ReviewEntry(reviewId, author, content, url));
        openWebPage(url);
    }

    private class FetchReviewsTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = BuildConfig.TMDB_API_KEY;
            String movieId = params[0];
            // IF THE SORT METHOD IS FAVORITES, GET INFO FROM DATABASE
            if (favoriteMovie) {

                int cPos;
                Cursor rcursor = getReviewsForFavoriteMovie(movieId);
                if (rcursor.getCount() > 0) { // If rcursor has atleast one row
                    reviewsData = new String[rcursor.getCount()][4];
//                    Log.d(TAG, "doInBackground: rcursor.getCount() " + rcursor.getCount());

                    rcursor.moveToFirst();
                    do { // always prefer do while loop while you deal with database
                        cPos = rcursor.getPosition();
//                        Log.d(TAG, "get from rcursor: cPos " + cPos);
                        reviewsData[cPos][0] = rcursor.getString(rcursor.getColumnIndex("reviewId"));
                        reviewsData[cPos][1] = rcursor.getString(rcursor.getColumnIndex("author"));
                        reviewsData[cPos][2] = rcursor.getString(rcursor.getColumnIndex("content"));
                        reviewsData[cPos][3] = rcursor.getString(rcursor.getColumnIndex("url"));
                        rcursor.moveToNext();
                    } while (!rcursor.isAfterLast());
                    rcursor.close();
                    return reviewsData;
                } else {
                    reviewsData = new String[1][4];
                    reviewsData[0][2] = "No Reviews in Database";
                    return reviewsData;
                }
            } else {

                URL ReviewsRequestUrl = NetworkUtils.buildReviewsUrl(movieId, apiKey);
                try {
                    String jsonReviewResponse = NetworkUtils
                            .getResponseFromHttpUrl(ReviewsRequestUrl);
                    reviewsData = MovieJsonUtils
                            .getReviewStringsFromJson(DetailActivity.this, jsonReviewResponse);

//                Log.d(TAG, "doInBackground: " + ReviewsRequestUrl);
//                Log.d(TAG, "doInBackground: " + jsonReviewResponse);
//                Log.d(TAG, "doInBackground: 00 " + reviewsData[0][0]);
//                Log.d(TAG, "doInBackground: 01 " + reviewsData[0][1]);
//                Log.d(TAG, "doInBackground: 02 " + reviewsData[0][2]);
//                Log.d(TAG, "doInBackground: 03 " + reviewsData[0][3]);

                    return reviewsData;

                } catch (Exception e) {
                    e.printStackTrace();
                    reviewsData = new String[1][4];
                    reviewsData[0][2] = "No Reviews Found";
                    return reviewsData;
                }
            }
        }

        @Override
        protected void onPostExecute(String[][] reviewsData) {
            if (reviewsData != null) {
                mReviewsAdapter.setReviewData(reviewsData);
            }
        }
    }

    private class FetchVideosTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = BuildConfig.TMDB_API_KEY;
            String movieId = params[0];
            // IF THE SORT METHOD IS FAVORITES, GET INFO FROM DATABASE
            if (favoriteMovie) {

                int cPos;
                Cursor vcursor = getVideosForFavoriteMovie(movieId);
                if (vcursor.getCount() > 0) { // If vcursor has atleast one row
                    videosData = new String[vcursor.getCount()][3];
//                    Log.d(TAG, "doInBackground: vcursor.getCount() " + vcursor.getCount());

                    vcursor.moveToFirst();
                    do { // always prefer do while loop while you deal with database
                        cPos = vcursor.getPosition();
//                        Log.d(TAG, "get from vcursor: cPos " + cPos);
                        videosData[cPos][0] = vcursor.getString(vcursor.getColumnIndex("ytVideoKey"));
                        videosData[cPos][1] = vcursor.getString(vcursor.getColumnIndex("type"));
                        videosData[cPos][2] = vcursor.getString(vcursor.getColumnIndex("title"));
                        vcursor.moveToNext();
                    } while (!vcursor.isAfterLast());
                    vcursor.close();
                    return videosData;
                } else {
                    videosData = new String[1][4];
                    videosData[0][2] = "No Videos in Database";
                    return videosData;
                }
            } else {
                URL VideosRequestUrl = NetworkUtils.buildVideosUrl(movieId, apiKey);
                try {
                    String jsonVideoResponse = NetworkUtils
                            .getResponseFromHttpUrl(VideosRequestUrl);
                    videosData = MovieJsonUtils
                            .getVideoStringsFromJson(DetailActivity.this, jsonVideoResponse);

                    return videosData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(String[][] videosData) {
            if (videosData != null) {
                mVideosAdapter.setVideoData(videosData);
            }
        }
    }

    /**
     * This method fires off an implicit Intent to open a webpage.
     *
     * @param url Url of webpage to open.
     */
    private void openWebPage(String url) {

        Intent openlink = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Make sure there's an app available to launch
        if (openlink.resolveActivity(getPackageManager()) != null) {
            startActivity(openlink);
        }
    }

    /**
     * This method fires off an implicit Intent to open a Video in youtube,
     * falling back to browser if youtube is not installed.
     *
     * @param url Url of youtube video to open.
     */
    private void openVideoUrl(String url) {

        Intent yt_play = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Intent chooser = Intent.createChooser(yt_play, "Open With");

        // Make sure there's an app available to launch
        if (yt_play.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }
}

