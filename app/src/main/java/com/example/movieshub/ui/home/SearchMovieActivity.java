package com.example.movieshub.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.movieshub.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchMovieActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;

    final private String INTENT_EXTRA_KEYWORD = "keyword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        Intent intent = getIntent();
        String keyword = intent.getStringExtra(INTENT_EXTRA_KEYWORD);
        Toast.makeText(getApplicationContext(), keyword, Toast.LENGTH_SHORT).show();

        okHttpClient = new OkHttpClient();
        getSearchResult(keyword);
    }

    private void getSearchResult(String keyword) {
        final String URL = "http://www.omdbapi.com";

        final String API_KEY = "a9f1d7ac";

        HttpUrl.Builder builder = HttpUrl.parse(URL).newBuilder();

        builder.addQueryParameter("apikey", API_KEY)
                .addQueryParameter("t", "movie")
                .addQueryParameter("s", keyword);

        Log.i("URL", builder.build().toString());

        final Request REQUEST = new Request
                .Builder()
                .url(builder.build().toString())
                .build();

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getApplicationContext(), "Failure !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String jsonString = response.body().string();
                    Log.i("Data", jsonString);
                    JSONObject jsonObject = new JSONObject(jsonString);

                    Log.i("Response", jsonObject.getString("Response"));

                    if ("True".equals(jsonObject.getString("Response") )) {
                        Log.i("Data", jsonObject.getJSONArray("Search").toString());
                        Log.i("Count", jsonObject.getString("totalResults"));
                    } else {
                        Log.i("Response", "Is not true");
//                        Toast.makeText(getApplicationContext(), jsonObject.getString("Error"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException jse) {
                    jse.printStackTrace();
                    Log.d("JSON Error", jse.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error to get JSON", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException ioe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error to get body", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
