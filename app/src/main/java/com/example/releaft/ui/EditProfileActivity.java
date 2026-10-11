
package com.example.releaft.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.releaft.R;
import com.example.releaft.data.ApiClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "ReLeafEditProfile";

    private EditText etEditEmail;
    private EditText etEditFullName;
    private EditText etEditBio;
    private EditText etEditCity;

    private TextView tvEditProfileStatus;
    private Button btnSaveProfile;
    private Button btnCancelEdit;

    private Call<ResponseBody> updateCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etEditEmail = findViewById(R.id.etEditEmail);
        etEditFullName = findViewById(R.id.etEditFullName);
        etEditBio = findViewById(R.id.etEditBio);
        etEditCity = findViewById(R.id.etEditCity);

        tvEditProfileStatus =
                findViewById(R.id.tvEditProfileStatus);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        etEditEmail.setText(
                getIntent().getStringExtra("email")
        );

        etEditFullName.setText(
                getIntent().getStringExtra("full_name")
        );

        etEditBio.setText(
                getIntent().getStringExtra("bio")
        );

        etEditCity.setText(
                getIntent().getStringExtra("city")
        );

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void saveProfile() {
        String fullName =
                etEditFullName.getText().toString().trim();

        String bio =
                etEditBio.getText().toString().trim();

        String city =
                etEditCity.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etEditFullName.setError("Full name is required");
            etEditFullName.requestFocus();
            return;
        }

        if (fullName.length() > 150) {
            etEditFullName.setError(
                    "Full name must be 150 characters or fewer"
            );
            return;
        }

        if (bio.length() > 500) {
            etEditBio.setError(
                    "Bio must be 500 characters or fewer"
            );
            return;
        }

        if (city.length() > 100) {
            etEditCity.setError(
                    "City must be 100 characters or fewer"
            );
            return;
        }

        String accessToken = getSharedPreferences(
                "ReLeafAuth",
                MODE_PRIVATE
        ).getString("access_token", null);

        if (accessToken == null || accessToken.isEmpty()) {
            tvEditProfileStatus.setText(
                    "Your session is missing. Please log in again."
            );
            return;
        }

        Map<String, String> profileData = new HashMap<>();
        profileData.put("full_name", fullName);
        profileData.put("bio", bio);
        profileData.put("city", city);

        btnSaveProfile.setEnabled(false);
        tvEditProfileStatus.setText("Saving your changes...");

        updateCall = ApiClient.getProfileUpdateApi()
                .updateProfile(
                        "Bearer " + accessToken,
                        profileData
                );

        updateCall.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnSaveProfile.setEnabled(true);

                if (response.isSuccessful()) {
                    tvEditProfileStatus.setText(
                            "Profile updated successfully!"
                    );

                    setResult(RESULT_OK);
                    finish();

                } else {
                    String message = "Could not update profile (HTTP "
                            + response.code() + ").";

                    try {
                        if (response.errorBody() != null) {
                            String body =
                                    response.errorBody().string();

                            Log.e(TAG, "Update error: " + body);

                            try {
                                JSONObject errorJson =
                                        new JSONObject(body);

                                if (errorJson.has("detail")) {
                                    message = errorJson.optString(
                                            "detail", message
                                    );
                                } else {
                                    message += " " + body;
                                }
                            } catch (Exception ignored) {
                                message += " " + body;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Reading error failed", e);
                    }

                    tvEditProfileStatus.setText(message);
                }
            }

            @Override
            public void onFailure(
                    Call<ResponseBody> call,
                    Throwable t
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnSaveProfile.setEnabled(true);

                tvEditProfileStatus.setText(
                        "Connection error: "
                                + t.getClass().getSimpleName()
                );

                Log.e(TAG, "Profile update request failed", t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (updateCall != null) {
            updateCall.cancel();
        }

        super.onDestroy();
    }
}
