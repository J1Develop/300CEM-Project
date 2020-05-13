package com.example.movieshub.util;

public class Constant {
    public enum User{
        Register, Authenticate;
    }

    public static final String API_URL = "https://movie-ground-backend-302cem.herokuapp.com";

    public static final String API_URL_REGISTER = API_URL + "/user/register";
    public static final String API_URL_LOGIN = API_URL + "/user/authenticate";
    public static final String API_URL_UPDATE_USER = API_URL + "/user/edit";

    public static final String API_URL_READ_HISTORY = API_URL + "/history/view";
    public static final String API_URL_UPDATE_HISTORY = API_URL + "/history/insert";
    public static final String API_URL_DELETE_HISTORY = API_URL + "/history/delete";

    public static final String API_URL_READ_FAV = API_URL + "/favorite/view";
    public static final String API_URL_ADD_FAV = API_URL + "/favorite/insert";
    public static final String API_URL_DELETE_FAV = API_URL + "/favorite/delete";

    public static final String API_URL_READ_REVIEW = API_URL + "/review/view";
    public static final String API_URL_ADD_REVIEW = API_URL + "/review/insert";

    public static final String API_URL_READ_CINEMA = API_URL + "/cinema/view";

    public static final String NA_IMG_URL = "https://via.placeholder.com/300x450.png?text=Unavailable";

    public static final String KEY_USERNAME = "username";

    public static final String API_URL_MOVIE = "https://www.omdbapi.com";

    public static final String KEY_API_KEY = "apikey";
    public static final String VALUE_API_KEY = "a9f1d7ac";

    public static final String KEY_MEDIA_TYPE = "type";
    public static final String VALUE_MEDIA_TYPE = "movie";

    public static final String KEY_IMDB_ID = "i";

    public static final String KEY_KEYWORD = "s";

    public static final String KEY_PAGE_COUNT = "page";

    public static final String API_KEY_MAP_DIR = "https://www.google.com/maps/dir/";
}
