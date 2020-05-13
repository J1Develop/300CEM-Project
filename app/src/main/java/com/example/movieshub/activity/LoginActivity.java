package com.example.movieshub.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieshub.R;
import com.example.movieshub.biometric.BiometricBuilder;
import com.example.movieshub.biometric.BiometricCallback;
import com.example.movieshub.biometric.BiometricManager;
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

public class LoginActivity extends AppCompatActivity implements BiometricCallback {

    private SharedPreferences sharedPreferences;

    private OkHttpClient okHttpClient = new OkHttpClient();

    private BiometricManager biometricManager;

    private EditText txtUname, txtPwd;

    private Button btnSignIn, btnSignUp, btnBioLogin;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUname = findViewById(R.id.edit_txt_username);
        txtPwd = findViewById(R.id.edit_txt_pwd);

        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnBioLogin = findViewById(R.id.btn_bio_login);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_login));

        sharedPreferences = getSharedPreferences(getString(R.string.user_pref), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.logged_in), false))
            startMainActivity(sharedPreferences.getString(getString(R.string.identity), null));

        btnSignIn.setOnClickListener(v -> {
            if (validField(txtUname) && validField(txtPwd))
                progressBar.setVisibility(View.VISIBLE);
                authenticate(Constant.User.Authenticate, txtUname.getText().toString(), txtPwd.getText().toString());
        });

        btnSignUp.setOnClickListener(v -> {
            if (validField(txtUname) && validField(txtPwd))
                progressBar.setVisibility(View.VISIBLE);
                authenticate(Constant.User.Register, txtUname.getText().toString(), txtPwd.getText().toString());
        });

        btnBioLogin.setOnClickListener(v -> {
            BiometricBuilder biometricBuilder = new BiometricBuilder(LoginActivity.this);
            biometricBuilder
                    .setTitle(getString(R.string.biometric_title))
                    .setSubtitle(getString(R.string.biometric_subtitle))
                    .setDescription(getString(R.string.biometric_description))
                    .setNegativeButtonText(getString(R.string.biometric_negative_button_text));

            biometricManager = biometricBuilder.build();
            biometricManager.authenticate(LoginActivity.this);
        });
    }

    @Override
    public void onSdkVersionNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_sdk_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_hardware_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_fingerprint_not_available), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_permission_not_granted), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationCancelled() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show();
        biometricManager.cancelAuthentication();
    }

    @Override
    public void onAuthenticationSuccessful() {
        startMainActivity();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
    }

    private boolean validField(TextView target) {
        boolean isValid = TextUtils.getTrimmedLength(target.getText().toString()) > 0;
        if (!isValid)
            target.setError(getString(R.string.error_empty_field));
        return isValid;
    }

    private void startMainActivity() {
        startMainActivity("user001");
    }

    private void startMainActivity(String identity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.logged_in), true)
                .putString(getString(R.string.identity), identity)
                .commit();

        Toast.makeText(getApplicationContext(), getString(R.string.biometric_success, identity), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void authenticate(final Constant.User type, String username, String pwd) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", pwd);
        } catch (JSONException jse)  {
            jse.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        String url = null;

        switch (type) {
            case Register:
                url = Constant.API_URL_REGISTER;
                break;
            case Authenticate:
                url = Constant.API_URL_LOGIN;
                break;
            default:
                throw new NullPointerException();
        }

        Request request = new Request.Builder().url(url).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("IOException", e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, getString(R.string.toast_failure), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resString = response.body().string();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject result = new JSONObject(resString);

                        if (result.getBoolean("result")) {
                            switch (type) {
                                case Register:
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_success), Toast.LENGTH_LONG).show();
                                    break;
                                case Authenticate:
                                    String identity = result.getString(getString(R.string.identity));
                                    startMainActivity(identity);
                                    break;
                            }
                        } else {
                            switch (type) {
                                case Register:
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_failure_username), Toast.LENGTH_LONG).show();
                                    break;
                                case Authenticate:
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Error", e.getMessage());
                    }
                });
            }
        });
    }
}
