package com.example.android.pjpopmovies1;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieEntry implements Parcelable {

    public static final Creator<MovieEntry> CREATOR = new Creator<MovieEntry>() {
        @Override
        public MovieEntry createFromParcel(Parcel in) {
            return new MovieEntry(in);
        }

        @Override
        public MovieEntry[] newArray(int size) {
            return new MovieEntry[size];
        }
    };

    private String movieId;
    private String title;
    private String posterUrl;
    private String plotOverview;
    private String rating;
    private String releaseDate;

    // Constructor
    public MovieEntry(String movieId, String title, String posterUrl, String plotOverview, String rating, String releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.plotOverview = plotOverview;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public String getId() {
        return movieId;
    }

    public void setId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return posterUrl;
    }

    public void setPoster(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPlot() {
        return plotOverview;
    }

    public void setPlot(String plotOverview) {
        this.plotOverview = plotOverview;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleasedate() {
        return releaseDate;
    }

    public void setReleasedate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    // Parcelling part
    protected MovieEntry(Parcel in) {
        this.movieId = in.readString();
        this.title = in.readString();
        this.posterUrl = in.readString();
        this.plotOverview = in.readString();
        this.rating = in.readString();
        this.releaseDate = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.movieId);
        dest.writeString(this.title);
        dest.writeString(this.posterUrl);
        dest.writeString(this.plotOverview);
        dest.writeString(this.rating);
        dest.writeString(this.releaseDate);
    }

    @Override
    public String toString() {
        return "MovieEntry{" +
                "movieId='" + movieId + '\'' +
                ", title='" + title + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", plotOverview='" + plotOverview + '\'' +
                ", rating='" + rating + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }
}

