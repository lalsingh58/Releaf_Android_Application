package com.example.releaft.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.releaft.R;
import com.example.releaft.data.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyActionsActivity extends AppCompatActivity {

    private LinearLayout actionsContainer;
    private TextView tvActionsMessage;
    private ProgressBar progressActions;
    private MaterialButton btnRefreshActions;
    private Call<ResponseBody> actionsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_actions);

        actionsContainer = findViewById(R.id.actionsContainer);
        tvActionsMessage = findViewById(R.id.tvActionsMessage);
        progressActions = findViewById(R.id.progressActions);
        btnRefreshActions = findViewById(R.id.btnRefreshActions);

        btnRefreshActions.setOnClickListener(v -> loadActions());

        loadActions();
    }

    private void loadActions() {
        String token = getSharedPreferences("ReLeafAuth", MODE_PRIVATE)
                .getString("access_token", null);

        if (token == null || token.trim().isEmpty()) {
            tvActionsMessage.setText("Please log in to view your history.");
            return;
        }

        if (actionsCall != null) {
            actionsCall.cancel();
        }

        progressActions.setVisibility(View.VISIBLE);
        btnRefreshActions.setEnabled(false);
        tvActionsMessage.setText("Loading your activities...");
        actionsContainer.removeAllViews();

        actionsCall = ApiClient.getActionApi()
                .getMyActions("Bearer " + token);

        actionsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    @NonNull Call<ResponseBody> call,
                    @NonNull Response<ResponseBody> response) {

                progressActions.setVisibility(View.GONE);
                btnRefreshActions.setEnabled(true);

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    tvActionsMessage.setText(
                            response.code() == 401
                                    ? "Your session expired. Please log in again."
                                    : "Unable to load your activity history."
                    );
                    return;
                }

                try {
                    JSONObject unused = null;
                    String body = response.body().string();

                    JSONArray results;

                    if (body.trim().startsWith("[")) {
                        results = new JSONArray(body);
                    } else {
                        JSONObject root = new JSONObject(body);
                        results = root.optJSONArray("results");

                        if (results == null) {
                            results = new JSONArray();
                        }
                    }

                    if (results.length() == 0) {
                        tvActionsMessage.setText(
                                "No activities yet. Plant a tree or water a plant to get started!"
                        );
                        return;
                    }

                    tvActionsMessage.setText(
                            "Your submitted activities: " + results.length()
                    );

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject action = results.getJSONObject(i);
                        addActionCard(action);
                    }

                } catch (Exception e) {
                    tvActionsMessage.setText(
                            "Could not read your activity history."
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ResponseBody> call,
                    @NonNull Throwable t) {

                progressActions.setVisibility(View.GONE);
                btnRefreshActions.setEnabled(true);

                if (!call.isCanceled() && !isFinishing()) {
                    tvActionsMessage.setText(
                            "Connection failed. Check your server and try again."
                    );
                }
            }
        });
    }

    private void addActionCard(JSONObject action) {
        String type = action.optString("action_type", "activity");
        String title = "plant".equalsIgnoreCase(type)
                ? "🌱 Plant a tree"
                : "💧 Water a plant";

        int points = action.optInt("points", 0);
        boolean pointsAwarded = action.optBoolean("points_awarded", false);
        String date = action.optString("created_at", "");
        String description = action.optString("description", "");

        JSONObject proof = action.optJSONObject("proof");
        String status = proof == null
                ? "unknown"
                : proof.optString("status", "pending");

        String note = proof == null
                ? ""
                : proof.optString("verification_note", "");

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dp(16));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(0xFFDCE8DC);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(17);
        titleView.setTextColor(0xFF174D2A);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(titleView);

        TextView dateView = new TextView(this);
        dateView.setText("Submitted: " + date);
        dateView.setTextSize(12);
        dateView.setTextColor(0xFF68796C);
        dateView.setPadding(0, dp(6), 0, 0);
        content.addView(dateView);

        TextView pointsView = new TextView(this);
        pointsView.setText(
                pointsAwarded
                        ? points + " points awarded"
                        : points + " points (not yet awarded)"
        );
        pointsView.setTextSize(14);
        pointsView.setTextColor(0xFF237A43);
        pointsView.setPadding(0, dp(6), 0, 0);
        content.addView(pointsView);

        TextView statusView = new TextView(this);
        statusView.setText("Proof status: " + status.toUpperCase());
        statusView.setTextSize(13);
        statusView.setTypeface(null, android.graphics.Typeface.BOLD);
        statusView.setPadding(0, dp(8), 0, 0);

        if ("approved".equalsIgnoreCase(status)) {
            statusView.setTextColor(0xFF237A43);
        } else if ("rejected".equalsIgnoreCase(status)) {
            statusView.setTextColor(0xFFD32F2F);
        } else {
            statusView.setTextColor(0xFF996600);
        }

        content.addView(statusView);

        if (!description.trim().isEmpty()) {
            TextView descriptionView = new TextView(this);
            descriptionView.setText(description);
            descriptionView.setTextSize(14);
            descriptionView.setTextColor(0xFF405444);
            descriptionView.setPadding(0, dp(8), 0, 0);
            content.addView(descriptionView);
        }

        if (!note.trim().isEmpty()) {
            TextView noteView = new TextView(this);
            noteView.setText(note);
            noteView.setTextSize(12);
            noteView.setTextColor(0xFF68796C);
            noteView.setPadding(0, dp(8), 0, 0);
            content.addView(noteView);
        }

        card.addView(content);
        actionsContainer.addView(card);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (actionsCall != null) {
            actionsCall.cancel();
        }
        super.onDestroy();
    }
}