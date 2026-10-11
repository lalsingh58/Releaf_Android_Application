
package com.example.releaft.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class LeaderboardActivity extends AppCompatActivity {

    private LinearLayout leaderboardContainer;
    private TextView tvLeaderboardMessage;
    private ProgressBar progressLeaderboard;
    private Button btnRefreshLeaderboard;

    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardContainer =
                findViewById(R.id.leaderboardContainer);
        tvLeaderboardMessage =
                findViewById(R.id.tvLeaderboardMessage);
        progressLeaderboard =
                findViewById(R.id.progressLeaderboard);
        btnRefreshLeaderboard =
                findViewById(R.id.btnRefreshLeaderboard);

        accessToken = getSharedPreferences(
                "ReLeafAuth",
                MODE_PRIVATE
        ).getString("access_token", "");

        btnRefreshLeaderboard.setOnClickListener(
                v -> loadLeaderboard()
        );

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            tvLeaderboardMessage.setText(
                    "Your session is missing. Please log in again."
            );
            return;
        }

        progressLeaderboard.setVisibility(View.VISIBLE);
        btnRefreshLeaderboard.setEnabled(false);
        tvLeaderboardMessage.setText("Loading leaderboard...");
        leaderboardContainer.removeAllViews();

        ApiClient.getLeaderboardApi()
                .getLeaderboard("Bearer " + accessToken)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response
                    ) {
                        progressLeaderboard.setVisibility(View.GONE);
                        btnRefreshLeaderboard.setEnabled(true);

                        if (!response.isSuccessful()) {
                            tvLeaderboardMessage.setText(
                                    "Unable to load leaderboard. HTTP "
                                            + response.code()
                            );
                            return;
                        }

                        try {
                            if (response.body() == null) {
                                tvLeaderboardMessage.setText(
                                        "The server returned an empty response."
                                );
                                return;
                            }

                            String json = response.body().string();
                            JSONArray entries = parseEntries(json);

                            if (entries == null) {
                                tvLeaderboardMessage.setText(
                                        "Unexpected leaderboard response format."
                                );
                                return;
                            }

                            if (entries.length() == 0) {
                                tvLeaderboardMessage.setText(
                                        "No leaderboard entries yet. Start "
                                                + "taking environmental actions!"
                                );
                                return;
                            }

                            tvLeaderboardMessage.setText(
                                    entries.length() + " leaderboard entries"
                            );

                            for (int i = 0; i < entries.length(); i++) {
                                JSONObject entry = entries.getJSONObject(i);
                                addLeaderboardEntry(entry);
                            }

                        } catch (Exception e) {
                            tvLeaderboardMessage.setText(
                                    "Could not read leaderboard data."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t
                    ) {
                        progressLeaderboard.setVisibility(View.GONE);
                        btnRefreshLeaderboard.setEnabled(true);

                        tvLeaderboardMessage.setText(
                                "Connection failed. Check your Django server "
                                        + "and internet connection."
                        );
                    }
                });
    }

    private JSONArray parseEntries(String json) throws Exception {
        Object parsed = new org.json.JSONTokener(json).nextValue();

        if (parsed instanceof JSONArray) {
            return (JSONArray) parsed;
        }

        if (parsed instanceof JSONObject) {
            JSONObject object = (JSONObject) parsed;

            if (object.has("results")
                    && object.get("results") instanceof JSONArray) {
                return object.getJSONArray("results");
            }

            if (object.has("leaderboard")
                    && object.get("leaderboard") instanceof JSONArray) {
                return object.getJSONArray("leaderboard");
            }
        }

        return null;
    }

    private void addLeaderboardEntry(JSONObject entry) {
        int rank = entry.optInt("rank", 0);
        String name = entry.optString("full_name", "ReLeaf User");
        String city = entry.optString("city", "");
        int points = entry.optInt("points", 0);
        int level = entry.optInt("level", 1);
        int streak = entry.optInt("current_streak", 0);
        int actions = entry.optInt("total_actions", 0);
        boolean isMe = entry.optBoolean("is_me", false);

        if (name.trim().isEmpty() || name.equals("null")) {
            name = "ReLeaf User";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        if (isMe) {
            card.setBackgroundColor(Color.rgb(220, 241, 219));
        } else {
            card.setBackgroundColor(Color.WHITE);
        }

        TextView title = new TextView(this);
        title.setText("#" + rank + "  " + name
                + (isMe ? "  (You)" : ""));
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(30, 65, 39));

        TextView location = new TextView(this);
        location.setText(
                city == null || city.trim().isEmpty()
                        || city.equals("null")
                        ? "ReLeaf community member"
                        : city
        );
        location.setTextSize(13);
        location.setTextColor(Color.rgb(100, 112, 102));

        TextView score = new TextView(this);
        score.setText(points + " points  •  Level " + level);
        score.setTextSize(16);
        score.setTypeface(null, Typeface.BOLD);
        score.setTextColor(Color.rgb(44, 121, 58));

        TextView details = new TextView(this);
        details.setText(
                "🔥 " + streak + " day streak"
                        + "    •    " + actions + " actions"
        );
        details.setTextSize(13);
        details.setTextColor(Color.rgb(75, 88, 78));

        card.addView(title);
        card.addView(location);
        card.addView(score);
        card.addView(details);

        leaderboardContainer.addView(card);
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }
}
