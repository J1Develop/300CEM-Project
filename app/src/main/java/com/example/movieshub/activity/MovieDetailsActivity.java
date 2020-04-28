package com.example.movieshub.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieshub.Constant;
import com.example.movieshub.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDetailsActivity extends AppCompatActivity {
    private OkHttpClient okHttpClient;

    private ImageView poster;
    private TextView rated, runtime, genre, plot, production, director, actors;
    private Button imdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        poster = findViewById(R.id.poster);
        rated = findViewById(R.id.text_view_rated);
        runtime = findViewById(R.id.text_view_runtime);
        genre = findViewById(R.id.text_view_genre);
        plot = findViewById(R.id.text_view_plot);
        production = findViewById(R.id.text_view_production);
        director = findViewById(R.id.text_view_director);
        actors = findViewById(R.id.text_view_actors);
        imdb = findViewById(R.id.btn_imdb);

        okHttpClient = new OkHttpClient();

        Intent intent = getIntent();
        final String imdbID = intent.getStringExtra("imdbID");
        getData(intent.getStringExtra("imdbID"));

        imdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("IMDB", "Clicked");

                String url = "googlechrome://navigate?url=" + "https://www.imdb.com/title/" + imdbID;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    Log.i("123", url);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.i("1", "1");
                    startActivity(intent);
                }
            }
        });
    }

    private void setResult(final String movieDetails) {
        if (!"".equals(movieDetails) && !(movieDetails == null)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject movieJsonObj = new JSONObject(movieDetails);
                        Log.i("JSON", movieJsonObj.toString());

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
                }
            });

        }
    }

    private void getData(String imdbID) {
        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL).newBuilder();
        builder.addQueryParameter(Constant.KEY_API_KEY, Constant.VALUE_API_KEY)
                .addQueryParameter(Constant.KEY_IMDB_ID, imdbID);

        final Request REQUEST = new Request
                .Builder()
                .url(builder.build().toString())
                .build();

        Log.i("URL", builder.build().toString());

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("On", "Failure");
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failure !", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    String resString = response.body().string();
                    Log.i("Data", resString);
                    setResult(resString);
                } catch (IOException ioe) {
                    Log.d("Error", "Error to get body");
                }
            }
        });
    }
}
