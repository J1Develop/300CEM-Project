package com.example.movieshub.util;

public class Constant {
    public enum User{
        Register, Authenticate;
    }

    public static final String API_URL_REGISTER = "https://movie-ground-backend-302cem.herokuapp.com/user/register";
    public static final String API_URL_LOGIN = "https://movie-ground-backend-302cem.herokuapp.com/user/authenticate";
    public static final String API_URL_MOVIE = "https://www.omdbapi.com";

    public static final String KEY_API_KEY = "apikey";
    public static final String VALUE_API_KEY = "a9f1d7ac";

    public static final String KEY_MEDIA_TYPE = "type";
    public static final String VALUE_MEDIA_TYPE = "movie";

    public static final String KEY_IMDB_ID = "i";

    public static final String KEY_KEYWORD = "s";

    public static final String KEY_PAGE_COUNT = "page";

    public static final String OBJECT_NAME_RES = "Response";
    public static final String OBJECT_NAME_ERR = "Error";
    public static final String ARRAY_NAME_SEARCH = "Search";

    public static final String NA_IMG_URL = "https://via.placeholder.com/300x450.png?text=Unavailable";
}
