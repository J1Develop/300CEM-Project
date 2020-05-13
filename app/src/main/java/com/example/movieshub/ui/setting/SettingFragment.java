package com.example.movieshub.ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.movieshub.R;
import com.example.movieshub.activity.LoginActivity;
import com.example.movieshub.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingFragment extends Fragment {

    private SharedPreferences sharedPreferences;

    private ListView list_view_setting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        list_view_setting = root.findViewById(R.id.list_view_setting);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.user_pref), getContext().MODE_PRIVATE);
        String identity = sharedPreferences.getString(getString(R.string.identity), null);

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_item_text_color_accent, getResources().getStringArray(R.array.setting));
        list_view_setting.setAdapter(adapter);

        list_view_setting.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    showChangePwdDialog(identity);
                    break;
                case 1:
                    showLogoutDialog(identity);
                    break;
            }
        });
        return root;
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getString(R.string.logged_in))
                .remove(getString(R.string.identity))
                .commit();

        Toast.makeText(getActivity(), getString(R.string.toast_logout), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);

        getActivity().finish();
    }

    private void showChangePwdDialog(String identity) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        EditText editTextPwd = new EditText(getContext());
        EditText editTextConfirm = new EditText(getContext());
        ProgressBar progressBar = new ProgressBar(getContext());

        LinearLayout.LayoutParams layoutParams
                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(50, 0, 50, 0);
        editTextPwd.setLayoutParams(layoutParams);
        editTextConfirm.setLayoutParams(layoutParams);

        editTextPwd.setHint(getString(R.string.hint_enter_pwd));
        editTextPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editTextConfirm.setHint(getString(R.string.hint_re_enter_pwd));
        editTextConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        progressBar.setVisibility(View.GONE);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(editTextPwd);
        linearLayout.addView(editTextConfirm);
        linearLayout.addView(progressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(linearLayout)
                .setTitle(getString(R.string.dialog_title_change_pwd))
                .setNegativeButton(getString(R.string.dialog_btn_neg), (dialog, which) -> {
                })
                .setPositiveButton(getString(R.string.dialog_btn_pos), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (TextUtils.getTrimmedLength(editTextPwd.getText().toString()) == 0)
                    editTextPwd.setError(getString(R.string.error_empty_field));
                else if (TextUtils.getTrimmedLength(editTextConfirm.getText().toString()) == 0)
                    editTextConfirm.setError(getString(R.string.error_empty_field));
                else if (!editTextConfirm.getText().toString().equals(editTextPwd.getText().toString())) {
                    editTextPwd.setError(getString(R.string.error_mismatch_field));
                    editTextConfirm.setError(getString(R.string.error_mismatch_field));
                } else {
                    editTextPwd.setVisibility(View.GONE);
                    editTextConfirm.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    OkHttpClient okHttpClient = new OkHttpClient();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", identity);
                        jsonObject.put("password", editTextPwd.getText().toString());
                    } catch (JSONException jse) {
                        jse.printStackTrace();
                    }

                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                    Request request = new Request.Builder().url(Constant.API_URL_UPDATE_USER).put(body).build();

                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i("IOException", e.getMessage());
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                                editTextPwd.setVisibility(View.VISIBLE);
                                editTextConfirm.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                alertDialog.dismiss();
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            int code = response.code();
                            getActivity().runOnUiThread(() -> {
                                switch (code) {
                                    case 201:
                                        Toast.makeText(getActivity(), getString(R.string.toast_new_pwd_re_login), Toast.LENGTH_SHORT).show();
                                        logout();
                                        break;
                                    case 500:
                                        Toast.makeText(getActivity(), getString(R.string.toast_failure), Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                editTextPwd.setVisibility(View.VISIBLE);
                                editTextConfirm.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                alertDialog.dismiss();
                            });
                        }
                    });
                }
            });
        });
        alertDialog.show();
    }

    private void showLogoutDialog(String identity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.dialog_title_logout_title, identity))
                .setPositiveButton(getString(R.string.dialog_btn_pos), (dialog, which) -> {
                    logout();
                })
                .setNegativeButton(getString(R.string.dialog_btn_neg), (dialog, which) -> {
                });

        builder.show();
    }
}
