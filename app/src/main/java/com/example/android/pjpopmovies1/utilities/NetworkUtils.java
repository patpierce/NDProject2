package com.example.android.pjpopmovies1.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

//    private static Context mContext;
//    public NetworkUtils() {
//    }
//    public NetworkUtils(Context context) {
//        mContext = context;
//    }

    //    Working with the themoviedb.org API
//    request data from the /movie/popular and /movie/top_rated endpoints
//    URL parameter like so: http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
    private static final String TMDB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String API_KEY_PARAM = "api_key";
//    public static String apiKey = mContext.getResources().getString(R.string.APIKEY);

//    For Images:
//    URL constructed using 3 parts:
//            1) The base URL will look like: http://image.tmdb.org/t/p/.
//            2) Then you will need a ‘size’, which will be one of the following:
//            "w92", "w154", "w185", "w342", "w500", "w780", or "original".
//    For most phones we recommend using "w185".
//            3) And finally the poster path returned by the query,
//    in this case "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"
//    Combining these three parts gives us a final url of
//        http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
//    This is also explained explicitly in the API documentation for /configuration.
//    private static final String TMDB_IMG_BASE_URL = "http://image.tmdb.org/t/p/";


    public static URL buildUrl(String sortOrder, String apiKey) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(sortOrder)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }


}
