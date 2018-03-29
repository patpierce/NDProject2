package com.example.android.pjpopmovies1.utilities;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class MovieJsonUtils {

    /**
     * This method parses JSON from a web response and returns an 2d array of Strings
     * <p/>
     *
     * @param movieJsonStr JSON response from server
     * @return Array of Strings describing movie data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String[][] getMovieStringsFromJson(Context context, String movieJsonStr)
            throws JSONException {

        // In TMDB's returned json data, each movie's info is an element of the "results" array
        final String TMBD_LIST = "results";

        // keynames used in TMDB's json data
        final String TMBD_TITLE = "title";
        final String TMBD_POSTER = "poster_path";
        final String TMBD_OVERVIEW = "overview";
        final String TMBD_RATING = "vote_average";
        final String TMBD_MESSAGE_CODE = "cod";
//        final String TMBD_ID = "id";

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

            parsedMovieData[i][0] = title;
            parsedMovieData[i][1] = posterUrl;
            parsedMovieData[i][2] = plotOverview;
            parsedMovieData[i][3] = rating;
        }

        return parsedMovieData;
    }
}
