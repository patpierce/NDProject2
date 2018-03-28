package com.example.android.pjpopmovies1.utilities;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by pp2731 on 3/25/2018.
 */

public class MovieJsonUtils {

    private static final String TAG = MovieJsonUtils.class.getSimpleName();

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullMovieDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param movieJsonStr JSON response from server
     * @return Array of Strings describing movie data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String[][] getMovieStringsFromJson(Context context, String movieJsonStr)
            throws JSONException {

        // In TMDB's returned json data, each movie's info is an element of the "results" array
        final String TMBD_LIST = "results";

//        final String TMBD_ID = "id";
        // keynames used in TMDB's json data
        final String TMBD_TITLE = "title";
        final String TMBD_POSTER = "poster_path";
        final String TMBD_OVERVIEW = "overview";
        final String TMBD_RATING = "vote_average";
        final String TMBD_MESSAGE_CODE = "cod";


//        String[][] parsedMovieData = null;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMBD_MESSAGE_CODE)) {
            int errorCode = movieJson.getInt(TMBD_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(TMBD_LIST);

        /* Two dimensional String array to hold each movie's parsed attributes */
        String parsedMovieData[][];
        parsedMovieData = new String[movieArray.length()][4];

        for (int i = 0; i < movieArray.length(); i++) {
            String title;
            String posterUrl;
            String plotOverview;
            String rating;

            /* Get the JSON object representing the individual movie */
            JSONObject movieObject = movieArray.getJSONObject(i);

            title = movieObject.getString(TMBD_TITLE);
            posterUrl = movieObject.getString(TMBD_POSTER);
            plotOverview = movieObject.getString(TMBD_OVERVIEW);
            rating = movieObject.getString(TMBD_RATING);

            Log.v(TAG, "posterUrl:" + posterUrl);
            parsedMovieData[i][0] = title;
            parsedMovieData[i][1] = posterUrl;
            parsedMovieData[i][2] = plotOverview;
            parsedMovieData[i][3] = rating;
        }

        return parsedMovieData;
    }

//    /**
//     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
//     *
//     * @param context         An application context, such as a service or activity context.
//     * @param forecastJsonStr The JSON to parse into ContentValues.
//     *
//     * @return An array of ContentValues parsed from the JSON.
//     */
//    public static ContentValues[] getFullWeatherDataFromJson(Context context, String forecastJsonStr) {
//        /** This will be implemented in a future lesson **/
//        return null;
//    }
}
