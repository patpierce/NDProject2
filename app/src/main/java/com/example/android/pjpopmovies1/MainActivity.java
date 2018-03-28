package com.example.android.pjpopmovies1;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pjpopmovies1.MovieListRecyclerAdapter.ListItemClickListener;
import com.example.android.pjpopmovies1.utilities.MovieJsonUtils;
import com.example.android.pjpopmovies1.utilities.NetworkUtils;

import java.net.URL;

//import com.example.android.pjpopmovies1.data.MovieAppPreferences;

// Implement MovieListRecyclerAdapter.ListItemClickListener from the MainActivity
public class MainActivity extends AppCompatActivity
        implements ListItemClickListener {

    //    private static final int NUM_LIST_ITEMS = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private MovieListRecyclerAdapter mAdapter;
    private RecyclerView mMoviesListRecView;
    private Toast mToast;
    /* Create a Toast mToast to store the current Toast,
     * so we can cancel it if its still on screen if we get a new Toast.
     */
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
//    private String apiKey = getString(R.string.APIKEY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float dens = getResources().getDisplayMetrics().density;
        // Note, screenHeightDp isn't reliable
        // (it seems to be too small by the height of the status bar),
        // but we assume screenWidthDp is reliable.
        int screenWidthPx = (int) (config.screenWidthDp * dens);
//        int screenHeight = (int) (config.screenHeightDp * dens);
        int itemWidthPx = (int) (getResources().getDimension(R.dimen.movie_poster_width));
        // to get minColumnWidthPx, add poster size (itemWidthPx) from dimens file
        // to minMarginWidthPx of 6dp (*2 because left and right margins)
        int minMarginWidthPx = (int) (6 * dens);
        int minColumnWidthPx = itemWidthPx + (2 * minMarginWidthPx);
        int numberOfColumns = (screenWidthPx / minColumnWidthPx) + 1;
        int movieItemMarginPx = 0;

//        Log.d(TAG, "dens: " + dens);
//        Log.d(TAG, "itemWidthPx: " + itemWidthPx);
//        Log.d(TAG, "screenWidthPx: " + screenWidthPx);
//        Log.d(TAG, "minMarginWidthPx: " + minMarginWidthPx);
//        Log.d(TAG, "minItemWidthPx: " + minColumnWidthPx);

        while (movieItemMarginPx < minMarginWidthPx) {
//            Log.d(TAG, "numberOfColumns: " + numberOfColumns);
//            Log.d(TAG, "movieItemMarginPx: " + movieItemMarginPx);

            numberOfColumns--;
            movieItemMarginPx = (screenWidthPx - (itemWidthPx * numberOfColumns)) / ((numberOfColumns) * 2);
            Log.d(TAG, "numberOfColumns: " + numberOfColumns);
            Log.d(TAG, "movieItemMarginPx: " + movieItemMarginPx);

        }
        int vMarginInPx = (int) (12 * dens);

        Log.d(TAG, "dens: " + dens);
        Log.d(TAG, "itemWidthPx: " + itemWidthPx);
        Log.d(TAG, "screenWidthPx: " + screenWidthPx);
        Log.d(TAG, "minColumnWidthPx: " + minColumnWidthPx);
        Log.d(TAG, "numberOfColumns: " + numberOfColumns);
        Log.d(TAG, "movieItemMarginPx: " + movieItemMarginPx);
        Log.d(TAG, "vMarginInPx: " + vMarginInPx);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mMoviesListRecView = findViewById(R.id.rv_posters);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        // Set margins on recycle view elements to spread out the gaps evenly
        ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) mMoviesListRecView.getLayoutParams();
        marginLayoutParams.setMargins(movieItemMarginPx, vMarginInPx, movieItemMarginPx, vMarginInPx);
        mMoviesListRecView.setLayoutParams(marginLayoutParams);

        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        mMoviesListRecView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mMoviesListRecView.setHasFixedSize(true);

        // Pass in this as the ListItemClickListener to the MovieListRecyclerAdapter constructor
        /*
         * The MovieListRecyclerAdapter is responsible for displaying each item in the list.
         */
        mAdapter = new MovieListRecyclerAdapter(this);
        mMoviesListRecView.setAdapter(mAdapter);

        // "progress bar" cirle
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        loadMoviesData();

    }

    /**
     * This method gets sort order from preferences,
     * then gets the movie data in the background.
     */
    private void loadMoviesData() {
        showMoviesDataView();

//        String sortMethod = "popular";
        String sortMethod = "top_rated";
//        String sortMethod = AppPreferences.getPreferredSortMethod(this);
        new FetchMoviesTask().execute(sortMethod);
    }

    // Override ListItemClickListener's onListItemClick method

    /**
     * This is where we receive our callback from
     * {@link com.example.android.pjpopmovies1.MovieListRecyclerAdapter.ListItemClickListener}
     * <p>
     * This callback is invoked when you click on an item in the list.
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        // First, cancel the Toast if it isn't null
        /* so the new Toast shows immediately, instead of waiting
         * for other pending Toasts.
         */
        if (mToast != null) {
            mToast.cancel();
        }

        // Show a Toast when an item is clicked, displaying that item number that was clicked
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();
    }

    // Make the View for the movie data visible and hide the error message.
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mMoviesListRecView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mMoviesListRecView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_sort:
                // Pass in this as the ListItemClickListener to the MovieListRecyclerAdapter constructor
                mAdapter = new MovieListRecyclerAdapter(this);
                mMoviesListRecView.setAdapter(mAdapter);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = getString(R.string.APIKEY);
            String sortMethod = params[0];
            URL MoviesRequestUrl = NetworkUtils.buildUrl(sortMethod, apiKey);
            Log.d(TAG, "MoviesRequestUrl:" + MoviesRequestUrl);
            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl(MoviesRequestUrl);
                Log.d(TAG, "jsonMovieResponse:" + jsonMovieResponse);
                String[][] JsonMovieData = MovieJsonUtils
                        .getMovieStringsFromJson(MainActivity.this, jsonMovieResponse);

                return JsonMovieData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[][] moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesData != null) {
                Log.d(TAG, "in post execute, moviesData not eq null");
                showMoviesDataView();
                mAdapter.setMovieData(moviesData);
            } else {
                Log.d(TAG, "in post execute, moviesData is null");
                showErrorMessage();
            }
        }
    }
}