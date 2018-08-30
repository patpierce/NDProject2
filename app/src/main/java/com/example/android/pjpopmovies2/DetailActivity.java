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
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.pjpopmovies2.data.FavoritesContract;
import com.example.android.pjpopmovies2.data.FavoritesDbHelper;
import com.example.android.pjpopmovies2.utilities.MovieJsonUtils;
import com.example.android.pjpopmovies2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import com.example.android.pjpopmovies2.ReviewListRecyclerAdapter.ListItemClickListener;

public class DetailActivity  extends AppCompatActivity
        implements ListItemClickListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private ReviewListRecyclerAdapter mReviewsAdapter;
    private RecyclerView mReviewsListRecView;
    private VideoListRecyclerAdapter mVideosAdapter;
    private RecyclerView mVideosListRecView;

    private SQLiteDatabase mDb;

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
                MovieEntry movie = (MovieEntry) data.getParcelable("movieentry");

                mMovieId = movie.getId();
                mTitle = movie.getTitle();
                mPosterUrl = movie.getPoster();
                mSynopsis = movie.getSynopsis();
                mRating = movie.getRating();
                mReleaseDate = movie.getReleaseDate();

                String posterUrl = posterBaseUrl + mPosterUrl;
                Picasso.with(context).load(posterUrl).into(mDetailPosterView);

                mDetailTitleView.setText(mTitle);
                mDetailSynopsisView.setText(mSynopsis);
                String mRatingMessage = "Rating: " + mRating;
                mDetailRatingView.setText(mRatingMessage);
                mDetailSynopsisView.setText(mSynopsis);
                mReleaseDateView.setText(mReleaseDate);

                FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
                mDb = dbHelper.getWritableDatabase();

//                Cursor cursor = checkFavoriteList(mMovieId);
                Cursor cursor = mDb.rawQuery(
                        "SELECT * FROM favorites WHERE movieId='" + mMovieId +"'", null);
                if(cursor.moveToFirst())
                {
                    mFavoriteStarView.setChecked(true);
                    Log.d(TAG, "onCreate: cursor in movetofirst");
//                    showMessage("Error", "Record exist");
                }
                else
                {
                    Log.d(TAG, "onCreate: cursor not movetofirst");
                    // Inserting record
                }

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
                        if(isChecked) {
                            // if isChecked is true save info to database
                            Log.d(TAG, "onCheckedChanged: star isChecked " + isChecked);
                            addAsFavorite(mMovieId, mTitle, mPosterUrl, mSynopsis, mRating, mReleaseDate);
                        }else{
                            // if isChecked is false remove row from database
                            Log.d(TAG, "onCheckedChanged: star isChecked " + isChecked);
                            removeFavorite(mMovieId);
//                            mAdapter.notifyDataSetChanged();

                        }
                    }
                });
            }
        }
    }

    private long addAsFavorite(String movieID, String title, String  posterUrl,
                               String  synopsis, String  rating, String releaseDate) {
        ContentValues cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID, movieID);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_TITLE, title);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_POSTERURL, posterUrl);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_SYNOPSIS, synopsis);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_RATING, rating);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_MV_RELEASEDATE, releaseDate);
        // COMPLETED (8) call insert to run an insert query on TABLE_NAME with the ContentValues created
        return mDb.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, cv);
    }

    private boolean removeFavorite(String movieID) {
        //  call mDb.delete to pass in the TABLE_NAME and the condition that WaitlistEntry._ID equals id
        return mDb.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID + "=" + movieID, null) > 0;
    }

    /**
     * Query the mDb and get all guests from the waitlist table
     *
     * @return Cursor containing the list of guests
     */

//    private Cursor checkFavoriteList(String movieId) {
//        String QUERYSTRING =
//                FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID + " = ?";
//        // query mDb passing in the table name and projection String [] order by COLUMN_MV_MOVIEID
//        return mDb.query(
//                FavoritesContract.FavoritesEntry.TABLE_NAME,
//                null,
//                QUERYSTRING,
//                new String [] { movieId },
//                null,
//                null,
//                FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID
//        );
//    }

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

        Log.d(TAG, "onListItemClick: id " + reviewId);
        Log.d(TAG, "onListItemClick: au " + author);
        Log.d(TAG, "onListItemClick: cn " + content);
        Log.d(TAG, "onListItemClick: ur " + url);

        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
//        intentToStartDetailActivity.putExtra(getString(R.string.TITLE), title);
//        intentToStartDetailActivity.putExtra(getString(R.string.POSTERURL), posterUrl);
//        intentToStartDetailActivity.putExtra(getString(R.string.SYNOPSIS), synopsis);
//        intentToStartDetailActivity.putExtra(getString(R.string.RATING), rating);
        intentToStartDetailActivity.putExtra("reviewentry",
                new ReviewEntry(reviewId, author, content, url));
//        startActivity(intentToStartDetailActivity);
        openWebPage(url);
    }

//    @Override
//    // Override ListItemClickListener's onListItemClick method
//    /**
//     * This is where we receive our callback from
//     * {@link com.example.android.pjpopmovies2.VideoListRecyclerAdapter.ListItemClickListener}
//     * <p>
//     * This callback is invoked when you click on an item in the list.
//     *
//     * @param ytVideoUrl is Youtube Url for the item that was clicked.
//     */
//    public void onListItemClick(String ytVideoUrl) {
//        Context context = this;
//
//        Log.d(TAG, "onListItemClick: key " + ytVideoUrl);
//
//        Class destinationClass = DetailActivity.class;
//        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
//        openVideoUrl(ytVideoUrl);
//    }

    public class FetchReviewsTask extends AsyncTask<String, Void, String[][]> {

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = BuildConfig.TMDB_API_KEY;
            String movieId = params[0];
            URL ReviewsRequestUrl = NetworkUtils.buildReviewsUrl(movieId, apiKey);
            try {
                String jsonReviewResponse = NetworkUtils
                        .getResponseFromHttpUrl(ReviewsRequestUrl);
                String[][] JsonReviewData = MovieJsonUtils
                        .getReviewStringsFromJson(DetailActivity.this, jsonReviewResponse);

                Log.d(TAG, "doInBackground: " + ReviewsRequestUrl);
                Log.d(TAG, "doInBackground: " + jsonReviewResponse);
                Log.d(TAG, "doInBackground: 00 " + JsonReviewData[0][0]);
                Log.d(TAG, "doInBackground: 01 " + JsonReviewData[0][1]);
                Log.d(TAG, "doInBackground: 02 " + JsonReviewData[0][2]);
                Log.d(TAG, "doInBackground: 03 " + JsonReviewData[0][3]);

                return JsonReviewData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[][] reviewsData) {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (reviewsData != null) {
//                showMoviesDataView();
                mReviewsAdapter.setReviewData(reviewsData);
//            } else {
//                showErrorMessage();
            }
        }
    }

    public class FetchVideosTask extends AsyncTask<String, Void, String[][]> {

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = BuildConfig.TMDB_API_KEY;
            String movieId = params[0];
            URL VideosRequestUrl = NetworkUtils.buildVideosUrl(movieId, apiKey);
            try {
                String jsonVideoResponse = NetworkUtils
                        .getResponseFromHttpUrl(VideosRequestUrl);
                String[][] JsonVideoData = MovieJsonUtils
                        .getVideoStringsFromJson(DetailActivity.this, jsonVideoResponse);

                Log.d(TAG, "doInBackground: " + VideosRequestUrl);
                Log.d(TAG, "doInBackground: " + jsonVideoResponse);
                Log.d(TAG, "doInBackground: 00 " + JsonVideoData[0][0]);
                Log.d(TAG, "doInBackground: 01 " + JsonVideoData[0][1]);
                Log.d(TAG, "doInBackground: 02 " + JsonVideoData[0][2]);

                return JsonVideoData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[][] videosData) {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (videosData != null) {
//                showMoviesDataView();
                mVideosAdapter.setVideoData(videosData);
//            } else {
//                showErrorMessage();
            }
        }
    }

    /**
     * This method fires off an implicit Intent to open a webpage.
     *
     * @param url Url of webpage to open. Should start with http:// or https:// as that is the
     *            scheme of the URI expected with this Intent according to the Common Intents page
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
     * @param url Url of youtube video to open. Should start with http:// or https:// as that is the
     *            scheme of the URI expected with this Intent according to the Common Intents page
     */
    private void openVideoUrl(String url) {

        Intent yt_play = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Intent chooser = Intent.createChooser(yt_play , "Open With");

        // Make sure there's an app available to launch
        if (yt_play .resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

}

