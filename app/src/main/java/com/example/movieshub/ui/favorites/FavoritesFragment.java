package com.example.movieshub.ui.favorites;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.movieshub.R;
import com.example.movieshub.activity.MovieDetailsActivity;
import com.example.movieshub.model.SearchItem;
import com.example.movieshub.util.Constant;
import com.example.movieshub.util.SearchItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FavoritesFragment extends Fragment {
    private SharedPreferences sharedPreferences;

    private OkHttpClient okHttpClient;

    private ProgressBar progressBar;
    private ListView listViewFav;

    private SearchItemAdapter searchItemAdapter;

    final ArrayList<SearchItem> searchItems = new ArrayList();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_pref), getContext().MODE_PRIVATE);

        okHttpClient = new OkHttpClient();

        progressBar = root.findViewById(R.id.progressBar);
        listViewFav = root.findViewById(R.id.list_view_fav);
        listViewFav.setOnItemClickListener((parent, view, position, id) -> {
            SearchItem searchItem = searchItems.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("title", searchItem.getTitle());
            bundle.putString("year", searchItem.getYear());
            bundle.putString("imdbID", searchItem.getImdbID());
            bundle.putString("type", searchItem.getType());
            bundle.putString("poster", searchItem.getPoster());

            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            intent.putExtra("bundle", bundle);

            startActivity(intent);
        });

        listViewFav.setOnItemLongClickListener((parent, view, position, id) -> {
            final SearchItem current = searchItems.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.dialog_title_remove_item, current.getTitle()))
                    .setPositiveButton(getString(R.string.dialog_btn_pos), (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        listViewFav.setVisibility(View.GONE);

                        removeFavorite(current.getImdbID());
                    })
                    .setNegativeButton(getString(R.string.dialog_btn_neg), (dialog, which) -> { }).show();
            return true;
        });

        requestFavorite();

        return root;
    }

    private void requestFavorite() {
        String identity = sharedPreferences.getString(getString(R.string.identity), null);

        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL_READ_FAV).newBuilder();
        builder.addQueryParameter(Constant.KEY_USERNAME, identity);

        final Request REQUEST = new Request.Builder().url(builder.build().toString()).build();

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listViewFav.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resString = response.body().string();

                try {
                    final JSONObject jsonObject = new JSONObject(resString);

                    if ("true".equals(jsonObject.getString("result"))) {
                        JSONArray favorite = jsonObject.getJSONArray("movie");

                        int len = favorite.length();

                        searchItems.clear();

                        for (int index = 0; index < len; index++) {
                            JSONObject current = (JSONObject) favorite.get(index);

                            String title = current.getString("title");
                            String year = current.getString("year");
                            String imdbID = current.getString("imdbID");
                            String type = current.getString("type");
                            String poster = current.getString("poster");

                            searchItems.add(new SearchItem(title, year, imdbID, type, poster));
                        }

                        Collections.reverse(searchItems);

                        getActivity().runOnUiThread(() -> {
                            searchItemAdapter = new SearchItemAdapter(getActivity(), R.layout.movie_list_item, searchItems);
                            listViewFav.setAdapter(searchItemAdapter);

                            progressBar.setVisibility(View.GONE);
                            listViewFav.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), searchItems.size() > 0? getString(R.string.toast_data_fetched) : getString(R.string.toast_no_movie_fav), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            listViewFav.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), getString(R.string.toast_no_movie_fav), Toast.LENGTH_LONG).show();
                        });
                    }

                } catch (JSONException jse) {
                    jse.printStackTrace();
                    Log.d("JSON Error", jse.getMessage());
                    Log.d("JSON Error", "Error to get JSON");
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        listViewFav.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void removeFavorite(String imdbID) {
        String identity = sharedPreferences.getString(getString(R.string.identity), null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", identity)
                    .put("imdbID", imdbID);
        } catch (JSONException jse) {
            jse.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        final Request request = new Request.Builder().url(Constant.API_URL_DELETE_FAV).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listViewFav.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                getActivity().runOnUiThread(() -> {

                    /*
                    After remove a favorite record from database,
                    refresh the list view by the request the favorite again.
                    */
                    requestFavorite();
                });
            }
        });
    }

}
