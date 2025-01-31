package com.example.movieshub.model;

public class SearchItem {
    private String title;
    private String year;
    private String imdbID;
    private String type;
    private String poster;

    public SearchItem(String title, String year, String imdbID, String type, String poster) {
        setTitle(title);
        setYear(year);
        setImdbID(imdbID);
        setType(type);
        setPoster(poster);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
}
