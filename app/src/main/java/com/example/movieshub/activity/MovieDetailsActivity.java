package com.example.movieshub.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieshub.util.Constant;
import com.example.movieshub.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MovieDetailsActivity extends AppCompatActivity {
    private OkHttpClient okHttpClient;

    private ProgressBar progressBar;

    private LinearLayout linearLayout;

    private ImageView poster;
    private TextView rated, runtime, genre, plot, production, director, actors;
    private Button imdb, favorite, review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layout_data);

        poster = findViewById(R.id.poster);
        rated = findViewById(R.id.text_view_rated);
        runtime = findViewById(R.id.text_view_runtime);
        genre = findViewById(R.id.text_view_genre);
        plot = findViewById(R.id.text_view_plot);
        production = findViewById(R.id.text_view_production);
        director = findViewById(R.id.text_view_director);
        actors = findViewById(R.id.text_view_actors);
        imdb = findViewById(R.id.btn_imdb);
        favorite = findViewById(R.id.btn_favorite);
        review = findViewById(R.id.btn_review);

        okHttpClient = new OkHttpClient();

        Intent intent = getIntent();
        final Bundle bundle = intent.getBundleExtra("bundle");

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.user_pref), MODE_PRIVATE);
        final String identity = sharedPreferences.getString(getString(R.string.identity), null);
        if (identity != null) {
            createHistory(identity, bundle);
        }

        requestMovieDetails(bundle.getString("imdbID"));

        imdb.setOnClickListener(v -> {
            String url = "googlechrome://navigate?url=" + "https://www.imdb.com/title/" + bundle.getString("imdbID");
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent1);
            } catch (ActivityNotFoundException e) {
                startActivity(intent1);
            }
        });

        favorite.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MovieDetailsActivity.this);
            builder.setMessage(getString(R.string.alert_add_fav))
                    .setPositiveButton(getString(R.string.dialog_btn_pos), (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);
                        createFav(identity, bundle);
                    })
                    .setNegativeButton(getString(R.string.dialog_btn_neg), (dialog, which) -> { }).show();
        });

        review.setOnClickListener(v -> {
            Intent intent12 = new Intent(MovieDetailsActivity.this, MovieReviewActivity.class);
            intent12.putExtra("bundle", bundle);
            startActivity(intent12);
        });
    }

    private void showMovieDetails(final String movieDetails) {
        if (TextUtils.getTrimmedLength(movieDetails) > 0) {
            runOnUiThread(() -> {
                try {
                    JSONObject movieJsonObj = new JSONObject(movieDetails);

                    getSupportActionBar().setTitle(movieJsonObj.getString("Title") + " (" + movieJsonObj.getString("Year") + ")");

                    boolean isImageUrlAvailable = !"N/A".equals(movieJsonObj.getString("Poster"));
                    Picasso.get()
                            .load(isImageUrlAvailable ? movieJsonObj.getString("Poster") : Constant.NA_IMG_URL)
                            .into(poster);

                    rated.setText(movieJsonObj.getString("Rated"));
                    runtime.setText(movieJsonObj.getString("Runtime"));
                    genre.setText(movieJsonObj.getString("Genre"));
                    plot.setText(movieJsonObj.getString("Plot"));
                    production.setText(movieJsonObj.getString("Production"));
                    director.setText(movieJsonObj.getString("Director"));
                    actors.setText(movieJsonObj.getString("Actors"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                Toast.makeText(MovieDetailsActivity.this, getString(R.string.toast_data_fetched), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void requestMovieDetails(String imdbID) {
        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL_MOVIE).newBuilder();
        builder.addQueryParameter(Constant.KEY_API_KEY, Constant.VALUE_API_KEY)
                .addQueryParameter(Constant.KEY_IMDB_ID, imdbID);

        final Request REQUEST = new Request
                .Builder()
                .url(builder.build().toString())
                .build();

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("On", "Failure");
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(MovieDetailsActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    String resString = response.body().string();
                    showMovieDetails(resString);
                } catch (IOException ioe) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(MovieDetailsActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private JSONObject movieToJson(String username, Bundle bundle) {
        JSONObject jsonObject = new JSONObject(),
                movie = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        try {
            movie.put("title", bundle.getString("title"))
                    .put("year", bundle.getString("year"))
                    .put("imdbID", bundle.getString("imdbID"))
                    .put("type", bundle.getString("type"))
                    .put("poster", bundle.getString("poster"));

            jsonArray.put(movie);

            jsonObject.put("username", username)
                    .put("movie", jsonArray);
        } catch (JSONException jse) {
            jse.printStackTrace();
            Log.d("JSON Error", jse.getMessage());
            Log.d("JSON Error", "Error to save movie");
        }

        return jsonObject;
    }

    private void createHistory(String username, Bundle bundle) {
        JSONObject jsonObject = movieToJson(username, bundle);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        Request request = new Request.Builder().url(Constant.API_URL_UPDATE_HISTORY).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MovieDetailsActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) { }
        });
    }

    private void createFav(String username, Bundle bundle) {
        JSONObject jsonObject = movieToJson(username, bundle);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        Request request = new Request.Builder().url(Constant.API_URL_ADD_FAV).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(MovieDetailsActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                final int code = response.code();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(MovieDetailsActivity.this, code == 201? getString(R.string.toast_add_fav_success) : getString(R.string.toast_add_fav_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
