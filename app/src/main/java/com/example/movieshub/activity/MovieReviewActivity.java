package com.example.movieshub.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.movieshub.R;
import com.example.movieshub.model.Review;
import com.example.movieshub.util.Constant;
import com.example.movieshub.util.ReviewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MovieReviewActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    private OkHttpClient okHttpClient;

    private LinearLayout linearLayout;

    private ProgressBar progressBar;
    private ListView listViewReview;
    private EditText editTextReview;
    private Button btnSubmit;

    private List<Review> reviewList = new ArrayList<>();

    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_review);

        linearLayout = findViewById(R.id.layout_review_content);

        progressBar = findViewById(R.id.progressBar);
        listViewReview = findViewById(R.id.list_view_review);
        editTextReview = findViewById(R.id.edit_text_review);
        btnSubmit = findViewById(R.id.btn_submit);

        okHttpClient = new OkHttpClient();

        sharedPreferences = getSharedPreferences(getString(R.string.user_pref), MODE_PRIVATE);
        final String identity = sharedPreferences.getString(getString(R.string.identity), null);

        Intent intent = getIntent();
        final Bundle bundle = intent.getBundleExtra("bundle");

        getSupportActionBar().setTitle(getString(R.string.title_review, bundle.getString("title")));

        btnSubmit.setOnClickListener(v -> {
            String review = editTextReview.getText().toString();
            if (TextUtils.getTrimmedLength(review) > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MovieReviewActivity.this);
                builder.setTitle(getString(R.string.dialog_title_post_review))
                        .setMessage(getString(R.string.dialog_msg_post_review))
                        .setIcon(R.drawable.baseline_comment_24)
                        .setPositiveButton(getString(R.string.dialog_btn_pos), (dialog, which) -> {
                            createReview(bundle.getString("imdbID"), identity, editTextReview.getText().toString());
                            editTextReview.getText().clear();
                            editTextReview.clearFocus();
                            InputMethodManager imm = (InputMethodManager) MovieReviewActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        })
                        .setNegativeButton(getString(R.string.dialog_btn_neg), (dialog, which) -> { })
                        .show();
            } else {
                editTextReview.setError(getString(R.string.error_empty_field));
            }
        });

        requestReview(bundle.getString("imdbID"));
    }

    private void requestReview(final String imdbID) {
        HttpUrl.Builder builder = HttpUrl.parse(Constant.API_URL_READ_REVIEW).newBuilder();
        builder.addQueryParameter("imdbID", imdbID);

        final Request REQUEST = new Request.Builder().url(builder.build().toString()).build();

        okHttpClient.newCall(REQUEST).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("On", "Failure");
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resString = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(resString);
                    if ("true".equals(jsonObject.getString("result"))) {
                        JSONArray review = jsonObject.getJSONArray("review");

                        int len = review.length();
                        reviewList.clear();

                        for (int index = 0; index < len; index++) {
                            JSONObject current = (JSONObject) review.get(index);

                            String ownership = current.getString("ownership");
                            String content = current.getString("content");
                            String date = current.getString("date");

                            reviewList.add(new Review(imdbID, ownership, content, date));
                        }

                        Collections.reverse(reviewList);

                        runOnUiThread(() -> {
                            reviewAdapter = new ReviewAdapter(MovieReviewActivity.this, R.layout.review_list_item, reviewList);
                            listViewReview.setAdapter(reviewAdapter);
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(MovieReviewActivity.this, reviewList.size() > 0? getString(R.string.toast_data_fetched) : getString(R.string.toast_no_movie_cmt), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_no_movie_cmt), Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSON Error", e.getMessage());
                    Log.d("JSON Error", "Error to get JSON");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_json_err), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void createReview(final String imdbID, String ownership, String content) {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        JSONObject jsonObject = new JSONObject(),
                review = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        try {
            review.put("ownership", ownership)
                    .put("content", content)
                    .put("date", simpleDateFormat.format(today));

            jsonArray.put(review);

            jsonObject.put("imdbID", imdbID)
                    .put("review", jsonArray);
        } catch (JSONException jse) {
            jse.printStackTrace();
            Log.d("JSON Error", jse.getMessage());
            Log.d("JSON Error", "Error to save review");
        }

        final RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        Request request = new Request.Builder().url(Constant.API_URL_ADD_REVIEW).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                final int code = response.code();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (code) {
                            case 201:
                                Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_comment_success), Toast.LENGTH_SHORT).show();
                                break;
                            case 500:
                                Toast.makeText(MovieReviewActivity.this, getString(R.string.toast_comment_failure), Toast.LENGTH_SHORT).show();
                                break;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);

                        requestReview(imdbID);
                    }
                });
            }
        });
    }
}
