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
    public static String[] getMovieStringsFromJson(Context context, String movieJsonStr)
            throws JSONException {

        /* Movie information. Each movie's info is an element of the "results" array */
        final String TMBD_LIST = "results";

        final String TMBD_TITLE = "title";
//        final String TMBD_ID = "id";
        final String TMBD_POSTER = "poster_path";
        final String TMBD_MESSAGE_CODE = "cod";

        /* String array to hold each movie's String */
        String[] parsedMovieData = null;

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

        parsedMovieData = new String[movieArray.length()];

//        long localDate = System.currentTimeMillis();
//        long utcDate = SunshineDateUtils.getUTCDateFromLocal(localDate);
//        long startDay = SunshineDateUtils.normalizeDate(utcDate);

        for (int i = 0; i < movieArray.length(); i++) {
            String title;
            String posterUrl;
//            String highAndLow;
//
//            /* These are the values that will be collected */
//            long dateTimeMillis;
//            double high;
//            double low;
//            String description;

            /* Get the JSON object representing the day */
            JSONObject movieObject = movieArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
//            dateTimeMillis = startDay + SunshineDateUtils.DAY_IN_MILLIS * i;
//            date = SunshineDateUtils.getFriendlyDateString(context, dateTimeMillis, false);

            /*
             * Description is in a child array called "movie", which is 1 element long.
             * That element also contains a movie code.
             */
//            JSONObject movieObject =
//                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            title = movieObject.getString(TMBD_TITLE);

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary and is just a bad variable name.
             */
            posterUrl = movieObject.getString(TMBD_POSTER);
            Log.v(TAG, "posterUrl:" + posterUrl);
            parsedMovieData[i] = title + " - " + posterUrl;
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
