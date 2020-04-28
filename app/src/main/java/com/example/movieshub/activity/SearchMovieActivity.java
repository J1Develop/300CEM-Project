package com.example.movieshub.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieshub.Constant;
import com.example.movieshub.R;
import com.example.movieshub.model.SearchItem;
import com.example.movieshub.ui.home.SearchItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    private SearchView searchView;
    private Spinner spinnerSortBy, spinnerOrderBy;
    private Button btnApply;
    private TextView textViewSearchMsg;
    private ListView listViewMovies;
    private TextView textViewErrMsg;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        searchView = findViewById(R.id.search_bar);
        spinnerSortBy = findViewById(R.id.spinner_sort_by);
        spinnerOrderBy = findViewById(R.id.spinner_order_by);
        btnApply = findViewById(R.id.btn_filter_apply);
        listViewMovies = findViewById(R.id.list_view_movies);
        textViewErrMsg = findViewById(R.id.text_error_msg);
        textViewSearchMsg = findViewById(R.id.text_search_msg);

        button = new Button(this);
        button.setText("Load more");

        Intent intent = getIntent();
        keyword = intent.getStringExtra(INTENT_EXTRA_KEYWORD);
        getSupportActionBar().setTitle("Search of \"" + keyword + "\"");
        searchView.setQuery(keyword, false);

        okHttpClient = new OkHttpClient();

        ArrayAdapter<CharSequence> sortOptions = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sort_options, android.R.layout.simple_spinner_item);
        sortOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(sortOptions);

        ArrayAdapter<CharSequence> orderOptions = ArrayAdapter.createFromResource(getApplicationContext(), R.array.order_options, android.R.layout.simple_spinner_item);
        orderOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderBy.setAdapter(orderOptions);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Apply", "Clicked");
                sortSearchResult("Title", true);
            }
        });

        getMovies(currentPage);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("q", query);
                Log.d("k", keyword);
                if (TextUtils.getTrimmedLength(query) > 0
                        && query != null
                        && query != ""
                        && !query.equals(keyword)) {
                    Log.i("Search", "Submit");
                    keyword = query;
                    getSupportActionBar().setTitle("Search of \"" + keyword + "\"");
                    resetResult();
                    getMovies(currentPage);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        listViewMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), searchItems.get(position).getImdbID(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                intent.putExtra("imdbID", searchItems.get(position).getImdbID());

                startActivity(intent);
            }
        });

        listViewMovies.addFooterView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Button", "Clicked");
                if (currentPage <= totalPage) {
                    getMovies(currentPage);
                } else {
                    Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void sortSearchResult(final String sortBy, boolean isAscending) {
        Collections.sort(searchItems, new Comparator<SearchItem>() {
            @Override
            public int compare(SearchItem o1, SearchItem o2) {
                if ("Title".equals(sortBy))
                    return o1.getTitle().compareTo(o2.getTitle());
                else
                    return o1.getYear().compareTo(o2.getYear());
            }
        });
        for (SearchItem item : searchItems)
            Log.d("Sorted", item.getTitle());
    }

    private void resetResult() {
        searchItems.clear();
        currentPage = 1;
    }

    private void setResult(final String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            Log.i("Response", jsonObject.getString("Response"));

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

                runOnUiThread(new Runnable() {
                    @Override
                        public void run() {
                        textViewSearchMsg.setText(totalResults + " results of \"" + keyword + "\"");
                        searchItemAdapter = new SearchItemAdapter(getApplicationContext(), R.layout.movie_list_item, searchItems);

                        if (currentPage > 1) {
                            Parcelable parcelable = listViewMovies.onSaveInstanceState();
                            listViewMovies.setAdapter(searchItemAdapter);
                            listViewMovies.onRestoreInstanceState(parcelable);
                        } else {
                            listViewMovies.setAdapter(searchItemAdapter);
                        }
                        searchItemAdapter.notifyDataSetChanged();
                        currentPage++;
                    }
                });
            } else {
                final String error = jsonObject.getString("Error");
                Log.i("Error", error);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewSearchMsg.setVisibility(View.GONE);
                        listViewMovies.setEmptyView(textViewErrMsg);
                        textViewErrMsg.setText(error.toUpperCase());
                    }
                });
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            Log.d("JSON Error", jse.getMessage());
            Log.d("JSON Error", "Error to get JSON");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error to get JSON", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getMovies(int page) {
        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL).newBuilder();
        builder.addQueryParameter(Constant.KEY_API_KEY, Constant.VALUE_API_KEY)
                .addQueryParameter(Constant.KEY_MEDIA_TYPE, Constant.VALUE_MEDIA_TYPE)
                .addQueryParameter(Constant.KEY_KEYWORD, keyword)
                .addQueryParameter(Constant.KEY_PAGE_COUNT, String.valueOf(page));

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
