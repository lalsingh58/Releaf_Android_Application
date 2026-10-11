
package com.example.releaft;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.releaft.data.ApiClient;
import com.example.releaft.ui.LoginActivity;
import com.example.releaft.ui.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ReLeafAQI";
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private TextView tvAqi;
    private TextView tvAqiCategory;
    private TextView tvLocation;
    private TextView tvChallenge;
    private TextView tvPoints;
    private TextView tvStreak;

    private MaterialButton btnRefreshAqi;
    private MaterialButton btnPlant;
    private MaterialButton btnWater;
    private MaterialButton btnLogout;

    private BottomNavigationView bottomNavigation;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Call<ResponseBody> aqiCall;

    private final ExecutorService geocoderExecutor =
            Executors.newSingleThreadExecutor();

    private boolean waitingForLocation = false;
    private boolean aqiRequestStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadMyStats();

        locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        setupButtons();
        setupNavigation();
        loadLiveAqi();
    }

    private void initializeViews() {
        tvAqi = findViewById(R.id.tvAqi);
        tvAqiCategory = findViewById(R.id.tvAqiCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvChallenge = findViewById(R.id.tvChallenge);
        tvPoints = findViewById(R.id.tvPoints);
        tvStreak = findViewById(R.id.tvStreak);

        btnRefreshAqi = findViewById(R.id.btnRefreshAqi);
        btnPlant = findViewById(R.id.btnPlant);
        btnWater = findViewById(R.id.btnWater);
        btnLogout = findViewById(R.id.btnLogout);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadMyStats() {
        android.content.SharedPreferences preferences =
                getSharedPreferences("ReLeafAuth", MODE_PRIVATE);

        String accessToken =
                preferences.getString("access_token", null);

        if (accessToken == null || accessToken.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Please log in to view your statistics",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ApiClient.getStatsApi()
                .getMyStats("Bearer " + accessToken)
                .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {

                    @Override
                    public void onResponse(
                            retrofit2.Call<okhttp3.ResponseBody> call,
                            retrofit2.Response<okhttp3.ResponseBody> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Unable to load your statistics",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        try {
                            String json = response.body().string();
                            org.json.JSONObject stats =
                                    new org.json.JSONObject(json);

                            int points = stats.optInt("points", 0);
                            int streak = stats.optInt("current_streak", 0);
                            int level = stats.optInt("level", 1);
                            int rank = stats.optInt("rank", 0);

                            tvPoints.setText("Points: " + points);
                            tvStreak.setText("Streak: " + streak + " days");

                            android.util.Log.d(
                                    "ReLeafStats",
                                    "Level: " + level + ", Rank: " + rank
                            );

                        } catch (Exception e) {
                            android.util.Log.e(
                                    "ReLeafStats",
                                    "Could not parse statistics",
                                    e
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<okhttp3.ResponseBody> call,
                            Throwable t
                    ) {
                        if (!isFinishing() && !isDestroyed()) {
                            android.util.Log.e(
                                    "ReLeafStats",
                                    "Statistics request failed",
                                    t
                            );

                            Toast.makeText(
                                    MainActivity.this,
                                    "Could not connect to the server",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private void setupButtons() {
        btnRefreshAqi.setOnClickListener(v -> loadLiveAqi());

        btnPlant.setOnClickListener(v -> openAction("plant"));
        btnWater.setOnClickListener(v -> openAction("water"));

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(
                "ReLeafAuth",
                MODE_PRIVATE
        );

        prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .apply();

        Intent intent = new Intent(
                MainActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void openAction(String actionType) {
        Intent intent = new Intent(
                MainActivity.this,
                com.example.releaft.ui.SubmitActionActivity.class
        );

        intent.putExtra("action_type", actionType);
        startActivity(intent);
    }

    private void setupNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(
                        MainActivity.this,
                        com.example.releaft.ui.MyActionsActivity.class
                ));
                return true;
            } else if (id == R.id.nav_leaderboard) {
                startActivity(new Intent(
                        MainActivity.this,
                        com.example.releaft.ui.LeaderboardActivity.class
                ));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(
                        MainActivity.this,
                        ProfileActivity.class
                ));
                return true;
            }

            return false;
        });
    }

    private void showComingSoon(String screen) {
        Toast.makeText(
                this,
                screen + " screen will be implemented next.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadLiveAqi() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            tvAqi.setText("--");
            tvAqiCategory.setText("Location permission needed");
            tvLocation.setText("Allow location to check local AQI");
            tvChallenge.setText(
                    "Allow location permission to retrieve your area's live air quality."
            );

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        stopLocationUpdates();
        waitingForLocation = false;
        aqiRequestStarted = false;

        if (aqiCall != null) {
            aqiCall.cancel();
        }

        btnRefreshAqi.setEnabled(false);
        tvAqi.setText("--");
        tvAqiCategory.setText("Loading live AQI...");
        tvLocation.setText("Finding your location...");
        tvChallenge.setText("Checking current air quality...");

        findLocation();
    }

    private void findLocation() {
        if (locationManager == null) {
            showLocationError("Location service is unavailable.");
            return;
        }

        try {
            Location recent = null;

            String[] providers = {
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.GPS_PROVIDER
            };

            for (String provider : providers) {
                if (!locationManager.isProviderEnabled(provider)) {
                    continue;
                }

                Location candidate =
                        locationManager.getLastKnownLocation(provider);

                if (candidate != null) {
                    long age = System.currentTimeMillis()
                            - candidate.getTime();

                    if (age >= 0 && age < 120000
                            && (recent == null
                            || candidate.getTime() > recent.getTime())) {
                        recent = candidate;
                    }
                }
            }

            if (recent != null) {
                fetchAqi(
                        recent.getLatitude(),
                        recent.getLongitude()
                );
            } else {
                requestLocationUpdates();
            }

        } catch (SecurityException e) {
            showLocationError("Location permission is required.");
            Log.e(TAG, "Location permission error", e);
        }
    }

    private void requestLocationUpdates() {
        if (waitingForLocation) {
            return;
        }

        waitingForLocation = true;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                stopLocationUpdates();

                fetchAqi(
                        location.getLatitude(),
                        location.getLongitude()
                );
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        try {
            boolean requested = false;

            String[] providers = {
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.GPS_PROVIDER
            };

            for (String provider : providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                            provider,
                            1000,
                            0,
                            locationListener
                    );
                    requested = true;
                }
            }

            if (!requested) {
                showLocationError(
                        "Turn on your phone's location service and refresh."
                );
            }

        } catch (SecurityException e) {
            showLocationError("Location permission is required.");
            Log.e(TAG, "Could not request location", e);
        }
    }

    private void fetchAqi(double latitude, double longitude) {
        if (aqiRequestStarted) {
            return;
        }

        aqiRequestStarted = true;
        waitingForLocation = false;

        tvLocation.setText(String.format(
                Locale.getDefault(),
                "Finding city · %.4f, %.4f",
                latitude,
                longitude
        ));

        resolveCityName(latitude, longitude);

        aqiCall = ApiClient.getAqiApi().getAqi(
                latitude,
                longitude
        );

        aqiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnRefreshAqi.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject root = new JSONObject(
                                response.body().string()
                        );

                        displayAqi(root);

                    } catch (Exception e) {
                        showAqiError("Could not read AQI data.");
                        Log.e(TAG, "AQI parsing failed", e);
                    }

                } else {
                    String errorMessage = "AQI request failed (HTTP "
                            + response.code() + ").";

                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " "
                                    + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read AQI error", e);
                    }

                    showAqiError(errorMessage);
                    Log.e(TAG, errorMessage);
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

                btnRefreshAqi.setEnabled(true);
                showAqiError(
                        "Connection failed. Check Django and USB connection."
                );
                Log.e(TAG, "AQI network failure", t);
            }
        });
    }

    private void resolveCityName(double latitude, double longitude) {
        String coordinates = String.format(
                Locale.getDefault(),
                "%.4f, %.4f",
                latitude,
                longitude
        );

        geocoderExecutor.execute(() -> {
            String city = "";

            try {
                Geocoder geocoder = new Geocoder(
                        getApplicationContext(),
                        Locale.getDefault()
                );

                List<Address> addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                );

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    String subLocality = address.getSubLocality();
                    String locality = address.getLocality();
                    String subAdmin = address.getSubAdminArea();
                    String admin = address.getAdminArea();

                    if (subLocality != null && !subLocality.trim().isEmpty()) {
                        city = subLocality.trim();
                    }

                    String mainCity = locality;

                    if (mainCity == null || mainCity.trim().isEmpty()) {
                        mainCity = subAdmin;
                    }

                    if (mainCity == null || mainCity.trim().isEmpty()) {
                        mainCity = admin;
                    }

                    if (mainCity != null && !mainCity.trim().isEmpty()) {
                        mainCity = mainCity.trim();

                        if (city.isEmpty()) {
                            city = mainCity;
                        } else if (!city.equalsIgnoreCase(mainCity)) {
                            city += ", " + mainCity;
                        }
                    }
                }

            } catch (Exception e) {
                Log.w(TAG, "City lookup failed", e);
            }

            final String resolvedCity = city;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                String displayLocation;

                if (resolvedCity.isEmpty()) {
                    displayLocation = coordinates;
                } else {
                    displayLocation = resolvedCity + " · " + coordinates;
                }

                tvLocation.setText(displayLocation);
            });
        });
    }

    private void displayAqi(JSONObject root) {
        JSONObject challenge = root.optJSONObject("challenge");

        double usAqi = root.optDouble("us_aqi", -1);

        if (usAqi >= 0) {
            int roundedAqi = (int) Math.round(usAqi);
            tvAqi.setText(String.valueOf(roundedAqi));
            updateAqiColor(roundedAqi);
        } else {
            tvAqi.setText("--");
            tvAqiCategory.setText("AQI unavailable");
        }

        String category = root.optString("category", "");

        if (!category.isEmpty()) {
            tvAqiCategory.setText(category);
        }

        if (challenge != null) {
            tvChallenge.setText(challenge.optString(
                    "message",
                    "Live air quality updated successfully."
            ));
        } else {
            tvChallenge.setText(
                    "Live air quality updated successfully."
            );
        }

        Log.d(TAG, "AQI response received: " + root);
    }

    private void updateAqiColor(int aqi) {
        int color;

        if (aqi <= 50) {
            color = 0xFF238B57;
        } else if (aqi <= 100) {
            color = 0xFFCA8A04;
        } else if (aqi <= 150) {
            color = 0xFFEA580C;
        } else if (aqi <= 200) {
            color = 0xFFDC2626;
        } else if (aqi <= 300) {
            color = 0xFF7E22CE;
        } else {
            color = 0xFF7F1D1D;
        }

        tvAqi.setTextColor(color);
        tvAqiCategory.setTextColor(color);
    }

    private void showLocationError(String message) {
        waitingForLocation = false;
        btnRefreshAqi.setEnabled(true);
        tvAqiCategory.setText("Location unavailable");
        tvLocation.setText("Could not detect your location");
        tvChallenge.setText(message);

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void showAqiError(String message) {
        tvAqi.setText("--");
        tvAqiCategory.setText("AQI unavailable");
        tvChallenge.setText(message);
    }

    private void stopLocationUpdates() {
        waitingForLocation = false;

        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.w(TAG, "Could not stop location updates", e);
            }

            locationListener = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean granted = false;

            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (granted) {
                loadLiveAqi();
            } else {
                showLocationError(
                        "Location permission denied. Allow it in app settings."
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();

        if (aqiCall != null) {
            aqiCall.cancel();
        }

        geocoderExecutor.shutdownNow();

        super.onDestroy();
    }
}
