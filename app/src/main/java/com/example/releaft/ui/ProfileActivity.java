
package com.example.releaft.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.releaft.R;
import com.example.releaft.data.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ReLeafProfile";
    private static final String PREFS_NAME = "ReLeafAuth";

    private TextView tvProfileStatus;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileUsername;
    private TextView tvProfileBio;
    private TextView tvProfileCity;

    private Button btnReloadProfile;
    private Button btnProfileBack;
    private Button btnEditProfile;

    private Call<ResponseBody> profileCall;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        registerEditProfileLauncher();
        setupButtons();
        loadProfile();
    }

    private void initializeViews() {
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvProfileCity = findViewById(R.id.tvProfileCity);

        btnReloadProfile = findViewById(R.id.btnReloadProfile);
        btnProfileBack = findViewById(R.id.btnProfileBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void registerEditProfileLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadProfile();
                    }
                }
        );
    }

    private void setupButtons() {
        btnReloadProfile.setOnClickListener(v -> loadProfile());

        btnProfileBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> openEditProfile());
    }

    private void openEditProfile() {
        Intent intent = new Intent(
                ProfileActivity.this,
                EditProfileActivity.class
        );

        intent.putExtra(
                "full_name",
                getDisplayValue(tvProfileName)
        );

        intent.putExtra(
                "email",
                getDisplayValue(tvProfileEmail)
        );

        intent.putExtra(
                "bio",
                getDisplayValue(tvProfileBio)
        );

        intent.putExtra(
                "city",
                getDisplayValue(tvProfileCity)
        );

        editProfileLauncher.launch(intent);
    }

    private String getDisplayValue(TextView textView) {
        String value = textView.getText().toString().trim();

        if (value.equals("Not provided") || value.equals("—")) {
            return "";
        }

        return value;
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        String accessToken = prefs.getString("access_token", null);

        if (accessToken == null || accessToken.isEmpty()) {
            tvProfileStatus.setText(
                    "Please log in to view your profile."
            );
            return;
        }

        if (profileCall != null) {
            profileCall.cancel();
        }

        btnReloadProfile.setEnabled(false);
        tvProfileStatus.setText("Loading profile...");

        profileCall = ApiClient.getProfileApi().getMyProfile(
                "Bearer " + accessToken
        );

        profileCall.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnReloadProfile.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject object = new JSONObject(json);
                        JSONObject data = findProfileObject(object);

                        tvProfileName.setText(
                                value(data, "full_name", "fullName", "name")
                        );

                        tvProfileEmail.setText(
                                value(data, "email", "email_address")
                        );

                        tvProfileUsername.setText(
                                value(data, "username", "user_name")
                        );

                        tvProfileBio.setText(
                                value(data, "bio", "about")
                        );

                        tvProfileCity.setText(
                                value(data, "city", "location")
                        );

                        tvProfileStatus.setText(
                                "Profile loaded successfully."
                        );

                    } catch (Exception e) {
                        tvProfileStatus.setText(
                                "Could not read the profile response."
                        );

                        Log.e(TAG, "Profile JSON parsing failed", e);
                    }

                } else {
                    String error = "Unable to load profile (HTTP "
                            + response.code() + ").";

                    try {
                        if (response.errorBody() != null) {
                            error += " " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read error response", e);
                    }

                    tvProfileStatus.setText(error);
                    Log.e(TAG, error);
                }
            }

            @Override
            public void onFailure(
                    Call<ResponseBody> call,
                    Throwable t
            ) {
                if (isFinishing() || isDestroyed() || call.isCanceled()) {
                    return;
                }

                btnReloadProfile.setEnabled(true);

                tvProfileStatus.setText(
                        "Connection error: "
                                + t.getClass().getSimpleName()
                );

                Log.e(TAG, "Profile request failed", t);
            }
        });
    }

    private JSONObject findProfileObject(JSONObject object) {
        if (object.has("full_name")
                || object.has("fullName")
                || object.has("email")
                || object.has("username")) {
            return object;
        }

        String[] wrappers = {"data", "profile", "user", "results"};

        for (String key : wrappers) {
            Object nested = object.opt(key);

            if (nested instanceof JSONObject) {
                return findProfileObject((JSONObject) nested);
            }

            if (nested instanceof JSONArray) {
                JSONArray array = (JSONArray) nested;

                if (array.length() > 0
                        && array.opt(0) instanceof JSONObject) {
                    return findProfileObject(array.optJSONObject(0));
                }
            }
        }

        return object;
    }

    private String value(JSONObject object, String... keys) {
        for (String key : keys) {
            Object result = object.opt(key);

            if (result != null && !JSONObject.NULL.equals(result)) {
                String text = String.valueOf(result).trim();

                if (!text.isEmpty()) {
                    return text;
                }
            }
        }

        return "Not provided";
    }

    @Override
    protected void onDestroy() {
        if (profileCall != null) {
            profileCall.cancel();
        }

        super.onDestroy();
    }
}
