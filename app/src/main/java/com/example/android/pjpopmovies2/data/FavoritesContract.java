package com.example.android.pjpopmovies2.data;

import android.provider.BaseColumns;

public class FavoritesContract {

    public static final class FavoritesEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_MV_MOVIEID = "movieId";
        public static final String COLUMN_MV_TITLE = "title";
        public static final String COLUMN_MV_POSTERURL = "posterUrl";
        public static final String COLUMN_MV_SYNOPSIS = "synopsis";
        public static final String COLUMN_MV_RATING = "rating";
        public static final String COLUMN_MV_RELEASEDATE = "releaseDate";
    }

    public static final class ReviewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_RV_MOVIEID = "movieId";
        public static final String COLUMN_RV_REVIEWID = "reviewId";
        public static final String COLUMN_RV_AUTHOR = "author";
        public static final String COLUMN_RV_CONTENT = "content";
        public static final String COLUMN_RV_URL = "url";
    }

    public static final class VideosEntry implements BaseColumns {
        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_VD_MOVIEID = "movieId";
        public static final String COLUMN_VD_YTVIDEOKEY = "ytVideoKey";
        public static final String COLUMN_VD_TYPE = "type";
        public static final String COLUMN_VD_TITLE = "title";
    }
}

