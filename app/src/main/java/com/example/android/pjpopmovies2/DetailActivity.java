package com.example.android.pjpopmovies2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.pjpopmovies2.utilities.MovieJsonUtils;
import com.example.android.pjpopmovies2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import com.example.android.pjpopmovies2.ReviewListRecyclerAdapter.ListItemClickListener;

public class DetailActivity  extends AppCompatActivity
        implements ListItemClickListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private ReviewListRecyclerAdapter mRevAdapter;
    private RecyclerView mReviewsListRecView;
//    private TextView mErrorMessageDisplay;
//    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intentThatStartedThisActivity = getIntent();

        TextView mDetailTitleView = findViewById(R.id.tv_detail_title);
        TextView mDetailRatingView = findViewById(R.id.tv_detail_rating);
        TextView mDetailPlotView = findViewById(R.id.tv_detail_synopsis);
        TextView mReleaseDateView = findViewById(R.id.tv_release_date);
        ImageView mDetailPosterView = findViewById(R.id.iv_detail_movie_poster);
        Context context = mDetailPosterView.getContext();

        String mMovieId;
        String mTitle;
        String mPosterUrl;
        String mPlot;
        String mRating;
        String mReleaseDate;

        String posterBaseUrl = "https://image.tmdb.org/t/p/w342/";
        Bundle data = getIntent().getExtras();

        // Display the movie info that was passed from MainActivity
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movieentry")) {
                MovieEntry movie = (MovieEntry) data.getParcelable("movieentry");

                mMovieId = movie.getId();
                mTitle = movie.getTitle();
                mPosterUrl = movie.getPoster();
                mPlot = movie.getPlot();
                mRating = movie.getRating();
                mReleaseDate = movie.getReleaseDate();

                String posterUrl = posterBaseUrl + mPosterUrl;
                Picasso.with(context).load(posterUrl).into(mDetailPosterView);

                mDetailTitleView.setText(mTitle);
                mDetailPlotView.setText(mPlot);
                mDetailRatingView.setText("Rating: " + mRating);
                mDetailPlotView.setText(mPlot);
                mReleaseDateView.setText(mReleaseDate);

                /*
                 * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
                 * do things like set the adapter of the RecyclerView and toggle the visibility.
                 */
                mReviewsListRecView = findViewById(R.id.rv_reviews);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                mReviewsListRecView.setLayoutManager(layoutManager);

                /*
                 * Use this setting to improve performance if you know that changes in content do not
                 * change the child layout size in the RecyclerView
                 */
                mReviewsListRecView.setHasFixedSize(true);

                // Pass in this as the ListItemClickListener to the MovieListRecyclerAdapter constructor
                /*
                 * The MovieListRecyclerAdapter is responsible for displaying each item in the list.
                 */
                mRevAdapter = new ReviewListRecyclerAdapter(this);
                mReviewsListRecView.setAdapter(mRevAdapter);

                loadReviewsData(mMovieId);
            }
        }
    }

    /**
     * This method gets the review data in the background.
     */
    private void loadReviewsData(String movieId) {
        new FetchReviewsTask().execute(movieId);
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
//        intentToStartDetailActivity.putExtra(getString(R.string.PLOTOVERVIEW), plotOverview);
//        intentToStartDetailActivity.putExtra(getString(R.string.RATING), rating);
        intentToStartDetailActivity.putExtra("reviewentry",
                new ReviewEntry(reviewId, author, content, url));
//        startActivity(intentToStartDetailActivity);
        openWebPage(url);
    }

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
                mRevAdapter.setReviewData(reviewsData);
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
        /*
         * We wanted to demonstrate the Uri.parse method because its usage occurs frequently. You
         * could have just as easily passed in a Uri as the parameter of this method.
         */
        Uri webpage = Uri.parse(url);
        /*
         * Here, we create the Intent with the action of ACTION_VIEW. This action allows the user
         * to view particular content. In this case, our webpage URL.
         */
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        /*
         * This is a check we perform with every implicit Intent that we launch. In some cases,
         * the device where this code is running might not have an Activity to perform the action
         * with the data we've specified. Without this check, in those cases your app would crash.
         */
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}

