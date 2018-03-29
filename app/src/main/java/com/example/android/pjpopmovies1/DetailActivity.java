package com.example.android.pjpopmovies1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intentThatStartedThisActivity = getIntent();

        String mTitle;
        TextView mDetailTitleView = findViewById(R.id.tv_detail_title);

        String mPlot;
        TextView mDetailRatingView = findViewById(R.id.tv_detail_rating);

        String mRating;
        TextView mDetailPlotView = findViewById(R.id.tv_detail_synopsis);

        String mPosterUrl;
        ImageView mDetailPosterView = findViewById(R.id.iv_detail_movie_poster);
        String posterBaseUrl = "https://image.tmdb.org/t/p/w342/";
        Context context = mDetailPosterView.getContext();

        // Display the movie info that was passed from MainActivity
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("TITLE")) {
                mTitle = intentThatStartedThisActivity.getStringExtra("TITLE");
                mDetailTitleView.setText(mTitle);
            }
            if (intentThatStartedThisActivity.hasExtra("POSTERURL")) {
                mPosterUrl = intentThatStartedThisActivity.getStringExtra("POSTERURL");
                String posterUrl = posterBaseUrl + mPosterUrl;
                Picasso.with(context).load(posterUrl).into(mDetailPosterView);
            }
            if (intentThatStartedThisActivity.hasExtra("PLOTOVERVIEW")) {
                mPlot = intentThatStartedThisActivity.getStringExtra("PLOTOVERVIEW");
                mDetailPlotView.setText(mPlot);
            }
            if (intentThatStartedThisActivity.hasExtra("RATING")) {
                mRating = "Rating: " +
                        intentThatStartedThisActivity.getStringExtra("RATING") + "/10";
                mDetailRatingView.setText(mRating);
            }
        }
    }
}

