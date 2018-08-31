package com.example.android.pjpopmovies2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.pjpopmovies2.MovieListRecyclerAdapter.ListItemClickListener;
import com.example.android.pjpopmovies2.data.FavoritesContract;
import com.example.android.pjpopmovies2.data.FavoritesDbHelper;
import com.example.android.pjpopmovies2.utilities.MovieJsonUtils;
import com.example.android.pjpopmovies2.utilities.NetworkUtils;

import java.net.URL;


// Implement MovieListRecyclerAdapter.ListItemClickListener from the MainActivity
public class MainActivity extends AppCompatActivity
        implements ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MovieListRecyclerAdapter mAdapter;
    private RecyclerView mMoviesListRecView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private SQLiteDatabase mDb;

//    public static Picasso picassoWithCache;
//    File httpCacheDirectory = new File(getCacheDir(), "picasso-cache");
//    Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
//    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
//    picassoWithCache = new Picasso.Builder(this).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        float dens = getResources().getDisplayMetrics().density;
        // Note, screenHeightDp isn't reliable
        // (it seems to be too small by the height of the status bar),
        // but we assume screenWidthDp is reliable.
        int screenWidthDp = config.screenWidthDp;
        Log.d(TAG, "screenWidthDp: " + screenWidthDp);
        int screenWidthPx = (int) (screenWidthDp * dens);
        Log.d(TAG, "screenWidthPx: " + screenWidthPx);
//        int screenHeightPx = (int) (config.screenHeightDp * dens);
        int itemWidthPx = (int) (getResources().getDimension(R.dimen.movie_tile_width));
        // to get minColumnWidthPx, add poster size (itemWidthPx) from dimens file
        // to minMarginWidthPx of 6dp (*2 because left and right margins)
        int minMarginWidthPx = (int) (6 * dens);
        int minColumnWidthPx = itemWidthPx + (2 * minMarginWidthPx);
        int numberOfColumns = (screenWidthPx / minColumnWidthPx) + 1;
        int movieItemMarginPx = 0;
//        Log.d(TAG, "itemWidthPx: " + itemWidthPx);
//        Log.d(TAG, "minMarginWidthPx: " + minMarginWidthPx);
//        Log.d(TAG, "minColumnWidthPx: " + minColumnWidthPx);
//        Log.d(TAG, "numberOfColumns: " + numberOfColumns);

        while (movieItemMarginPx < minMarginWidthPx) {

            numberOfColumns--;
            movieItemMarginPx = (screenWidthPx - (itemWidthPx * numberOfColumns)) / ((numberOfColumns) * 2);

        }
        int vMarginInPx = (int) (12 * dens);
//        Log.d(TAG, "movieItemMarginPx: " + movieItemMarginPx);

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

        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        // Pass in this as the ListItemClickListener to the MovieListRecyclerAdapter constructor
        /*
         * The MovieListRecyclerAdapter is responsible for displaying each item in the list.
         */
        mAdapter = new MovieListRecyclerAdapter(this);
        mMoviesListRecView.setAdapter(mAdapter);

        // "progress bar" circle
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        String savedPrefSortOder =
                sharedPreferences.getString(getResources().getString(R.string.pref_sort_key),
                        getResources().getString(R.string.pref_sort_default));
        loadMoviesData(savedPrefSortOder);
    }

    /**
     * Query the mDb
     * @return Cursor containing the list of favorite moview
     */
    private Cursor getFavoriteMovies() {
        // query mDb passing in the table name and projection String [] order by COLUMN_MV_MOVIEID
        return mDb.query(
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FavoritesContract.FavoritesEntry.COLUMN_MV_MOVIEID
        );
    }

    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            String savedPrefSortOder =
                    sharedPreferences.getString(getResources().getString(R.string.pref_sort_key),
                            getResources().getString(R.string.pref_sort_default));
            loadMoviesData(savedPrefSortOder);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
//        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        String savedPrefSortOder =
                sharedPreferences.getString(getResources().getString(R.string.pref_sort_key),
                        getResources().getString(R.string.pref_sort_default));
        loadMoviesData(savedPrefSortOder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This method gets the movie data in the background.
     */
    private void loadMoviesData(String sortOrder) {
        showMoviesDataView();
        new FetchMoviesTask().execute(sortOrder);
    }

    // Override ListItemClickListener's onListItemClick method

    /**
     * This is where we receive our callback from
     * {@link com.example.android.pjpopmovies2.MovieListRecyclerAdapter.ListItemClickListener}
     * <p>
     * This callback is invoked when you click on an item in the list.
     *
     * @param movieInfo Index in the array of movie info for the item that was clicked.
     */

    @Override
    public void onListItemClick(String[] movieInfo) {
        Context context = this;

        // Now that I've added the parselables, can I use them here? Not completely sure how they work just yet.
        String movieId = movieInfo[0];
        String title = movieInfo[1];
        String posterUrl = movieInfo[2];
        String synopsis = movieInfo[3];
        String rating = movieInfo[4];
        String releaseDate = movieInfo[5];

        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movieentry", new MovieEntry(movieId, title, posterUrl, synopsis , rating, releaseDate));
        startActivity(intentToStartDetailActivity);
    }

    // Make the View for the movie data visible and hide the error message.
    private void showMoviesDataView() {
        /* hide the error message text view */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* show the movie data is recycler view */
        mMoviesListRecView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        /* hide  the movie data is recycler view */
        mMoviesListRecView.setVisibility(View.INVISIBLE);
        /* show the error message text view */
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
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[][] doInBackground(String... params) {

            String apiKey = BuildConfig.TMDB_API_KEY;
            String sortMethod = params[0];
//            Log.d(TAG, "doInBackground: sortMethod " + sortMethod);
            // IF THE SORT METHOD IS FAVORITES, GET INFO FROM DATABASE
            if ("favorites".equals(sortMethod)) {

                int cPos;
                Cursor cursor = getFavoriteMovies();
                if (cursor.getCount() > 0) { // If cursor has atleast one row
                    String[][] dbMoviesData = new String[cursor.getCount()][6];
//                    Log.d(TAG, "doInBackground: cursor.getCount() " + cursor.getCount());

                    cursor.moveToFirst();
                    do { // always prefer do while loop while you deal with database
                        cPos = cursor.getPosition();
//                        Log.d(TAG, "doInBackground: cPos " + cPos);
                        dbMoviesData[cPos][0] = cursor.getString(cursor.getColumnIndex("movieId"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][0] " + dbMoviesData[cPos][0]);
                        dbMoviesData[cPos][1] = cursor.getString(cursor.getColumnIndex("title"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][1] " + dbMoviesData[cPos][1]);
                        dbMoviesData[cPos][2] = cursor.getString(cursor.getColumnIndex("posterUrl"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][2] " + dbMoviesData[cPos][2]);
                        dbMoviesData[cPos][3] = cursor.getString(cursor.getColumnIndex("synopsis"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][3] " + dbMoviesData[cPos][3]);
                        dbMoviesData[cPos][4] = cursor.getString(cursor.getColumnIndex("rating"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][4] " + dbMoviesData[cPos][4]);
                        dbMoviesData[cPos][5] = cursor.getString(cursor.getColumnIndex("releaseDate"));
//                        Log.d(TAG, "doInBackground: dbMovieData[cPos][5] " + dbMoviesData[cPos][5]);
                        cursor.moveToNext();
                    } while (!cursor.isAfterLast());

                    return dbMoviesData;

                } else {
                    String[][] phMoviesData = new String[1][6]; // Dynamic string array

//                    Log.e("SQL Query Error", "Cursor has no data");
                    phMoviesData[0][0] = null;
                    phMoviesData[0][1] = "No Favorties in Database";
                    phMoviesData[0][2] = null;
                    phMoviesData[0][3] = "Use another sortlist and 'star' a favorite movie from its Details Screen";
                    phMoviesData[0][4] = null;
                    phMoviesData[0][5] = null;
                    return phMoviesData;

                }
            } else {
                URL MoviesRequestUrl = NetworkUtils.buildMainUrl(sortMethod, apiKey);
                try {
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl(MoviesRequestUrl);
                    String[][] JsonMoviesData = MovieJsonUtils
                            .getMovieStringsFromJson(MainActivity.this, jsonMovieResponse);

                    return JsonMoviesData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(String[][] moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesData != null) {
                showMoviesDataView();
                mAdapter.setMovieData(moviesData);
            } else {
                showErrorMessage();
            }
        }
    }
}