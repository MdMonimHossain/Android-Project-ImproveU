package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private String quote, author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        OkHttpClient client = new OkHttpClient();
        String url = "https://zenquotes.io/api/random";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String myResponse = null;
                    if (response.body() != null) {
                        myResponse = response.body().string();
                    }
                    try {
                        JSONArray data = new JSONArray(myResponse);
                        JSONObject data2 = data.getJSONObject(0);
                        quote = data2.getString("q");
                        author = "- " + data2.getString("a");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(!quote.isEmpty() && !author.isEmpty()){
                        saveQuote();
                    }

                }
            }
        });

        new Handler().postDelayed(() -> {
            final Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }

    private void saveQuote() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("appPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("quote", quote);
        editor.putString("author", author);
        editor.apply();
    }

}