package com.example.releaft.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.releaft.R;
import com.example.releaft.data.ActionApi;
import com.example.releaft.data.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitActionActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 201;
    private static final int REQUEST_LOCATION_PERMISSION = 202;
    private static final int REQUEST_CAMERA_PERMISSION = 203;
    private static final int REQUEST_PICK_IMAGE = 204;

    private RadioGroup rgActionType;
    private RadioButton rbPlant, rbWater;
    private ImageView ivProofPhoto;
    private TextView tvPhotoPlaceholder, tvActionLocation, tvSubmissionStatus;
    private TextInputEditText etActionDescription;
    private MaterialButton btnSubmitAction, btnTakePhoto, btnChoosePhoto, btnGetActionLocation;

    private Uri selectedPhotoUri;
    private File cameraPhotoFile;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Call<ResponseBody> submitCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_action);

        bindViews();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setupListeners();

        String actionType = getIntent().getStringExtra("action_type");

        if ("water".equals(actionType)) {
            rbWater.setChecked(true);
        } else {
            rbPlant.setChecked(true);
        }
    }

    private void bindViews() {
        rgActionType = findViewById(R.id.rgActionType);
        rbPlant = findViewById(R.id.rbPlant);
        rbWater = findViewById(R.id.rbWater);
        ivProofPhoto = findViewById(R.id.ivProofPhoto);
        tvPhotoPlaceholder = findViewById(R.id.tvPhotoPlaceholder);
        tvActionLocation = findViewById(R.id.tvActionLocation);
        tvSubmissionStatus = findViewById(R.id.tvSubmissionStatus);
        etActionDescription = findViewById(R.id.etActionDescription);
        btnSubmitAction = findViewById(R.id.btnSubmitAction);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnGetActionLocation = findViewById(R.id.btnGetActionLocation);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnChoosePhoto.setOnClickListener(v -> choosePhoto());
        btnGetActionLocation.setOnClickListener(v -> getCurrentLocation());
        btnSubmitAction.setOnClickListener(v -> submitAction());
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
            return;
        }

        launchCamera();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            showStatus("No camera app is available on this device.", true);
            return;
        }
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        if (requestCode == REQUEST_PICK_IMAGE && data.getData() != null) {
            selectedPhotoUri = data.getData();
            cameraPhotoFile = null;
            try {
                getContentResolver().takePersistableUriPermission(
                        selectedPhotoUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {
                // Some document providers do not support persistable grants.
            }
            ivProofPhoto.setImageURI(selectedPhotoUri);
            tvPhotoPlaceholder.setVisibility(View.GONE);
            showStatus("Photo selected. Remember to submit your activity.", false);
        } else if (requestCode == REQUEST_CAMERA) {
            Object extra = data.getExtras() == null ? null : data.getExtras().get("data");
            if (extra instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) extra;
                try {
                    cameraPhotoFile = new File(getCacheDir(), "releaf_proof_" +
                            System.currentTimeMillis() + ".jpg");
                    try (FileOutputStream output = new FileOutputStream(cameraPhotoFile)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
                    }
                    selectedPhotoUri = null;
                    ivProofPhoto.setImageBitmap(bitmap);
                    tvPhotoPlaceholder.setVisibility(View.GONE);
                    showStatus("Photo captured. Remember to submit your activity.", false);
                } catch (IOException e) {
                    showStatus("Could not save the captured photo. Please try again.", true);
                }
            } else {
                showStatus("The camera did not return a photo. Please try again.", true);
            }
        }
    }

    private void getCurrentLocation() {
        boolean fineGranted = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        }

        try {
            Location bestLocation = null;
            String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};

            for (String provider : providers) {
                if (!locationManager.isProviderEnabled(provider)) {
                    continue;
                }
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate != null &&
                        (bestLocation == null ||
                                candidate.getTime() > bestLocation.getTime())) {
                    bestLocation = candidate;
                }
            }

            if (bestLocation != null) {
                setLocation(bestLocation);
                return;
            }

            tvActionLocation.setText("Getting GPS location...");
            requestOneLocationUpdate(fineGranted ? LocationManager.GPS_PROVIDER
                    : LocationManager.NETWORK_PROVIDER);

        } catch (SecurityException e) {
            showStatus("Location permission is required to continue.", true);
        } catch (Exception e) {
            showStatus("Could not get location. Enable GPS and try again.", true);
        }
    }

    private void requestOneLocationUpdate(String provider) {
        try {
            if (!locationManager.isProviderEnabled(provider)) {
                String alternate = provider.equals(LocationManager.GPS_PROVIDER)
                        ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER;
                if (!locationManager.isProviderEnabled(alternate)) {
                    tvActionLocation.setText("Enable GPS or network location and retry.");
                    return;
                }
                provider = alternate;
            }

            final String requestedProvider = provider;
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    setLocation(location);
                    try {
                        locationManager.removeUpdates(this);
                    } catch (SecurityException ignored) {
                    }
                }

                @Override
                public void onProviderDisabled(@NonNull String providerName) {
                    tvActionLocation.setText("Location provider disabled.");
                }

                @Override
                public void onProviderEnabled(@NonNull String providerName) {
                }

                @Override
                public void onStatusChanged(String providerName, int status, Bundle extras) {
                }
            };

            locationManager.requestSingleUpdate(
                    requestedProvider, locationListener, getMainLooper()
            );
        } catch (SecurityException e) {
            showStatus("Location permission is required.", true);
        } catch (Exception e) {
            tvActionLocation.setText("Could not get location. Try again.");
        }
    }

    private void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        tvActionLocation.setText(String.format(
                java.util.Locale.US, "%.6f, %.6f", latitude, longitude
        ));
        showStatus("Location captured.", false);
    }

    private void submitAction() {
        if (selectedPhotoUri == null && cameraPhotoFile == null) {
            showStatus("Please take a photo or choose one from your gallery.", true);
            return;
        }

        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            showStatus("Please capture your current location first.", true);
            return;
        }

        String token = getSharedPreferences("ReLeafAuth", MODE_PRIVATE)
                .getString("access_token", null);
        if (token == null || token.trim().isEmpty()) {
            showStatus("Your session has expired. Please log in again.", true);
            return;
        }

        String actionType = rbWater.isChecked() ? "water" : "plant";
        String description = etActionDescription.getText() == null
                ? "" : etActionDescription.getText().toString().trim();

        try {
            byte[] imageBytes = getSelectedImageBytes();
            if (imageBytes == null || imageBytes.length == 0) {
                showStatus("Could not read the selected photo. Please choose it again.", true);
                return;
            }

            RequestBody actionBody = textBody(actionType);
            RequestBody latitudeBody = textBody(String.valueOf(latitude));
            RequestBody longitudeBody = textBody(String.valueOf(longitude));
            RequestBody descriptionBody = textBody(description);
            RequestBody imageBody = RequestBody.create(
                    MediaType.parse("image/jpeg"), imageBytes
            );
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", "releaf_proof.jpg", imageBody
            );

            btnSubmitAction.setEnabled(false);
            btnSubmitAction.setText("Submitting...");
            showStatus("Uploading your activity proof...", false);

            ActionApi api = ApiClient.getActionApi();
            submitCall = api.submitAction(
                    "Bearer " + token,
                    actionBody,
                    latitudeBody,
                    longitudeBody,
                    descriptionBody,
                    imagePart
            );

            submitCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(
                        @NonNull Call<ResponseBody> call,
                        @NonNull Response<ResponseBody> response
                ) {
                    btnSubmitAction.setEnabled(true);
                    btnSubmitAction.setText("Submit Activity");

                    String responseText = "";
                    try {
                        if (response.body() != null) {
                            responseText = response.body().string();
                        } else if (response.errorBody() != null) {
                            responseText = response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }

                    if (!response.isSuccessful()) {
                        String message = "Submission failed (" + response.code() + ").";
                        try {
                            JSONObject error = new JSONObject(responseText);
                            if (error.has("detail")) {
                                message = error.optString("detail", message);
                            } else if (error.has("image")) {
                                message = "Photo error: " + error.optString("image");
                            } else if (error.has("non_field_errors")) {
                                message = error.optJSONArray("non_field_errors").optString(0);
                            }
                        } catch (Exception ignored) {
                        }
                        showStatus(message, true);
                        return;
                    }

                    try {
                        JSONObject result = new JSONObject(responseText);
                        String message = result.optString(
                                "message", "Your activity was submitted."
                        );
                        int points = result.optInt("points_awarded", 0);
                        showStatus(message, false);
                        Toast.makeText(
                                SubmitActionActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                        // Keep the form visible so the user can read the result.
                        if (points > 0) {
                            tvSubmissionStatus.append("\nPoints awarded: " + points);
                        }
                        btnSubmitAction.setText("Submitted");
                    } catch (Exception e) {
                        showStatus("Activity submitted successfully.", false);
                    }
                }

                @Override
                public void onFailure(
                        @NonNull Call<ResponseBody> call,
                        @NonNull Throwable t
                ) {
                    btnSubmitAction.setEnabled(true);
                    btnSubmitAction.setText("Submit Activity");
                    if (!call.isCanceled()) {
                        showStatus("Could not connect to the server. Check your connection and try again.", true);
                    }
                }
            });

        } catch (Exception e) {
            btnSubmitAction.setEnabled(true);
            btnSubmitAction.setText("Submit Activity");
            showStatus("Could not prepare the photo: " + e.getMessage(), true);
        }
    }

    private RequestBody textBody(String value) {
        return RequestBody.create(
                MediaType.parse("text/plain; charset=utf-8"), value
        );
    }

    private byte[] getSelectedImageBytes() throws IOException {
        if (cameraPhotoFile != null && cameraPhotoFile.exists()) {
            try (InputStream input = new java.io.FileInputStream(cameraPhotoFile);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                return output.toByteArray();
            }
        }

        if (selectedPhotoUri != null) {
            try (InputStream input = getContentResolver().openInputStream(selectedPhotoUri);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                if (input == null) {
                    return null;
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                return output.toByteArray();
            }
        }
        return null;
    }

    private void showStatus(String message, boolean isError) {
        tvSubmissionStatus.setVisibility(View.VISIBLE);
        tvSubmissionStatus.setText(message);
        tvSubmissionStatus.setTextColor(
                getColor(isError ? android.R.color.holo_red_dark : android.R.color.holo_green_dark)
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                            (grantResults.length > 1 &&
                                    grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                getCurrentLocation();
            } else {
                showStatus("Location permission is needed to submit an activity.", true);
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                showStatus("Camera permission was denied. You can still choose a photo.", true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (submitCall != null) {
            submitCall.cancel();
        }
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {
            }
        }
        super.onDestroy();
    }
}
