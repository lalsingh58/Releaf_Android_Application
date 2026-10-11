
package com.example.releaft.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.releaft.data.LoginRequest;
import com.example.releaft.MainActivity;
import com.example.releaft.R;
import com.example.releaft.data.TokenResponse;
import com.example.releaft.data.ApiClient;
import com.example.releaft.data.AuthApi;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "ReLeafLogin";
    private static final String PREFS_NAME = "ReLeafAuth";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvLoginStatus;
    private TextView tvGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedToken = prefs.getString("access_token", null);

        if (savedToken != null && !savedToken.isEmpty()) {
            openDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvLoginStatus = findViewById(R.id.tvLoginStatus);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> login());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });
    }

    private void login() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            tvLoginStatus.setText("Enter your email and password.");
            return;
        }

        btnLogin.setEnabled(false);
        tvLoginStatus.setText("Signing in...");

        AuthApi api = ApiClient.getAuthApi();

        api.login(new LoginRequest(email, password))
                .enqueue(new Callback<TokenResponse>() {

                    @Override
                    public void onResponse(
                            Call<TokenResponse> call,
                            Response<TokenResponse> response
                    ) {
                        btnLogin.setEnabled(true);

                        if (response.isSuccessful()) {
                            TokenResponse tokens = response.body();

                            if (tokens != null
                                    && tokens.getAccess() != null
                                    && !tokens.getAccess().isEmpty()) {

                                SharedPreferences prefs =
                                        getSharedPreferences(
                                                PREFS_NAME,
                                                MODE_PRIVATE
                                        );

                                SharedPreferences.Editor editor =
                                        prefs.edit();

                                editor.putString(
                                        "access_token",
                                        tokens.getAccess()
                                );

                                if (tokens.getRefresh() != null) {
                                    editor.putString(
                                            "refresh_token",
                                            tokens.getRefresh()
                                    );
                                }

                                editor.apply();

                                openDashboard();
                            } else {
                                tvLoginStatus.setText(
                                        "Login response did not contain an access token."
                                );
                                Log.e(TAG, "Successful response missing access token");
                            }
                        } else {
                            String errorMessage =
                                    readErrorMessage(response);

                            tvLoginStatus.setText(errorMessage);

                            Log.e(
                                    TAG,
                                    "Login failed: HTTP " + response.code()
                                            + " - " + errorMessage
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TokenResponse> call,
                            Throwable t
                    ) {
                        btnLogin.setEnabled(true);

                        String message = t.getMessage();

                        tvLoginStatus.setText(
                                "Connection error: "
                                        + t.getClass().getSimpleName()
                                        + (message != null
                                        ? " - " + message : "")
                        );

                        Log.e(TAG, "Login network request failed", t);
                    }
                });
    }

    private String readErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();

                Log.e(TAG, "Django error response: " + body);

                JSONObject json = new JSONObject(body);

                if (json.has("detail")) {
                    return json.optString("detail", "Login failed.");
                }

                if (json.has("message")) {
                    return json.optString("message", "Login failed.");
                }

                if (json.has("error")) {
                    Object error = json.get("error");

                    if (error instanceof JSONObject) {
                        JSONObject errorObject = (JSONObject) error;
                        return errorObject.optString(
                                "message",
                                "Login failed."
                        );
                    }

                    return error.toString();
                }

                return "Login failed (HTTP "
                        + response.code() + "): " + body;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not read login error", e);
        }

        return "Login failed (HTTP " + response.code() + ").";
    }

    private void openDashboard() {
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }
}
