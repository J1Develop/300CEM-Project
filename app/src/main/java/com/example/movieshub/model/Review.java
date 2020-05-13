package com.example.movieshub.model;

public class Review {
    private String imdb;
    private String ownsership;
    private String content;
    private String createDate;

    public Review(String imdb, String ownsership, String content, String createDate) {
        this.imdb = imdb;
        this.ownsership = ownsership;
        this.content = content;
        this.createDate = createDate;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getOwnsership() {
        return ownsership;
    }

    public void setOwnsership(String ownsership) {
        this.ownsership = ownsership;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
