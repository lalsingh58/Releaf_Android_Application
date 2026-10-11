
package com.example.releaft.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.releaft.MainActivity;
import com.example.releaft.R;
import com.example.releaft.data.ApiClient;
import com.example.releaft.data.RegisterRequest;
import com.example.releaft.data.TokenResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private Button btnRegister;
    private TextView tvRegisterStatus;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etRegisterUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvRegisterStatus = findViewById(R.id.tvRegisterStatus);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> register());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            ));
            finish();
        });
    }

    private void register() {
        String fullName = etUsername.getText().toString().trim();  // this field is now Full Name
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (fullName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            tvRegisterStatus.setText("Please fill in all fields.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvRegisterStatus.setText("Please enter a valid email address.");
            return;
        }

        if (password.length() < 8) {
            tvRegisterStatus.setText("Password must contain at least 8 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tvRegisterStatus.setText("Passwords do not match.");
            return;
        }

        btnRegister.setEnabled(false);
        tvRegisterStatus.setText("Creating your account...");

        RegisterRequest request = new RegisterRequest(fullName, email, password);

        ApiClient.getAuthApi()
                .register(request)
                .enqueue(new Callback<TokenResponse>() {

                    @Override
                    public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                        TokenResponse tokens = response.body();

                        if (response.isSuccessful()
                                && tokens != null
                                && tokens.getAccess() != null
                                && tokens.getRefresh() != null) {

                            getSharedPreferences("ReLeafAuth", MODE_PRIVATE)
                                    .edit()
                                    .putString("access_token", tokens.getAccess())
                                    .putString("refresh_token", tokens.getRefresh())
                                    .apply();

                            tvRegisterStatus.setText("Account created successfully!");

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            btnRegister.setEnabled(true);
                            tvRegisterStatus.setText("Registration failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenResponse> call, Throwable t) {
                        btnRegister.setEnabled(true);
                        tvRegisterStatus.setText("Cannot connect to server. Check the backend.");
                    }
                });
    }
}
