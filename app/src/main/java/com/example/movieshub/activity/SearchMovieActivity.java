package com.example.movieshub.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieshub.util.Constant;
import com.example.movieshub.R;
import com.example.movieshub.model.SearchItem;
import com.example.movieshub.util.SearchItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchMovieActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;

    private final String INTENT_EXTRA_KEYWORD = "keyword";

    private int currentPage = 1;

    private int totalPage;

    private String keyword;

    private ArrayList<SearchItem> searchItems = new ArrayList<>();

    private SearchItemAdapter searchItemAdapter;

    private ProgressBar progressBar;

    private LinearLayout linearLayout;

    private SearchView searchView;
    private TextView textViewSearchMsg;
    private ListView listViewMovies;
    private TextView textViewErrMsg;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.layout_content);

        searchView = findViewById(R.id.search_bar);
        listViewMovies = findViewById(R.id.list_view_movies);
        textViewErrMsg = findViewById(R.id.text_error_msg);
        textViewSearchMsg = findViewById(R.id.text_search_msg);

        button = new Button(this);
        button.setText(getString(R.string.msg_load_more_data));

        Intent intent = getIntent();
        keyword = intent.getStringExtra(INTENT_EXTRA_KEYWORD);
        getSupportActionBar().setTitle(getString(R.string.title_search_info, keyword));
        searchView.setQuery(keyword, false);

        okHttpClient = new OkHttpClient();

        requestMovies(currentPage);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.getTrimmedLength(query) > 0 && !query.equals(keyword)) {
                    keyword = query;
                    getSupportActionBar().setTitle(getString(R.string.title_search_info, keyword));
                    initSearchState();
                    requestMovies(currentPage);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        listViewMovies.setOnItemClickListener((parent, view, position, id) -> {
            SearchItem searchItem = searchItems.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("title", searchItem.getTitle());
            bundle.putString("year", searchItem.getYear());
            bundle.putString("imdbID", searchItem.getImdbID());
            bundle.putString("type", searchItem.getType());
            bundle.putString("poster", searchItem.getPoster());

            Intent intent1 = new Intent(SearchMovieActivity.this, MovieDetailsActivity.class);
            intent1.putExtra("bundle", bundle);

            startActivity(intent1);
        });

        listViewMovies.addFooterView(button);

        button.setOnClickListener(v -> {
            if (!isLastPage()) {
                requestMovies(currentPage);
            } else {
                Toast.makeText(SearchMovieActivity.this, getString(R.string.end_of_search_msg), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean isLastPage() {
        return currentPage == totalPage;
    }

    private void initSearchState() {
        searchItems.clear();
        currentPage = 1;
    }

    private void showMovies(final String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            boolean isSuccess = "True".equals(jsonObject.getString("Response"));
            if (isSuccess) {
                JSONArray movies = jsonObject.getJSONArray("Search");

                final String totalResults = jsonObject.getString("totalResults");
                totalPage = Integer.parseInt(totalResults) / 10 + 1;

                int len = movies.length();
                for (int index = 0; index < len; index++) {
                    JSONObject movie = (JSONObject) movies.get(index);
                    String title = movie.getString("Title");
                    String year = movie.getString("Year");
                    String imdbID = movie.getString("imdbID");
                    String type = movie.getString("Type");
                    String poster = movie.getString("Poster");

                    searchItems.add(new SearchItem(title, year, imdbID, type, poster));
                }

                runOnUiThread(() -> {
                    textViewSearchMsg.setText(getString(R.string.title_search_count, totalResults, keyword));
                    searchItemAdapter = new SearchItemAdapter(SearchMovieActivity.this, R.layout.movie_list_item, searchItems);

                    if (isLastPage())
                        button.setEnabled(false);

                    if (currentPage > 1) {
                        Parcelable parcelable = listViewMovies.onSaveInstanceState();
                        listViewMovies.setAdapter(searchItemAdapter);
                        listViewMovies.onRestoreInstanceState(parcelable);
                    } else {
                        listViewMovies.setAdapter(searchItemAdapter);
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    searchItemAdapter.notifyDataSetChanged();
                    currentPage++;
                    Toast.makeText(SearchMovieActivity.this, getString(R.string.toast_data_fetched), Toast.LENGTH_SHORT).show();
                });
            } else {
                final String error = jsonObject.getString("Error");
                Log.i("Error", error);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    textViewSearchMsg.setVisibility(View.GONE);
                    listViewMovies.setEmptyView(textViewErrMsg);
                    textViewErrMsg.setText(error.toUpperCase());
                });
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            Log.d("JSON Error", jse.getMessage());
            Log.d("JSON Error", "Error to get JSON");
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                Toast.makeText(SearchMovieActivity.this, getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void requestMovies(int page) {
        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL_MOVIE).newBuilder();
        builder.addQueryParameter(Constant.KEY_API_KEY, Constant.VALUE_API_KEY)
                .addQueryParameter(Constant.KEY_MEDIA_TYPE, Constant.VALUE_MEDIA_TYPE)
                .addQueryParameter(Constant.KEY_KEYWORD, keyword)
                .addQueryParameter(Constant.KEY_PAGE_COUNT, String.valueOf(page));

        final Request REQUEST = new Request
                .Builder()
                .url(builder.build().toString())
                .build();

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(SearchMovieActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                try {
                    String resString = response.body().string();
                    showMovies(resString);
                } catch (IOException ioe) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(SearchMovieActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
