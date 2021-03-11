package com.savvy.apartments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.savvy.apartments.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Splash extends AppCompatActivity {

    private static int splash_time_out = 1000;
    SharedPreferences shared;
    Iokihttp iokihttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        shared = this.getSharedPreferences("com.savvy.apartments", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();

        if (getIntent().hasExtra("defaultLang")) {
            changeLang(getIntent().getExtras().getString("defaultLang"));
        } else {
            changeLang(shared.getString("defaultLang", Locale.getDefault().getLanguage()));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                check();
            }
        }, splash_time_out);
    }

    private void changeLang(String langCode) {
        System.out.println(langCode + "ddddd" + Locale.getDefault().getLanguage() + "ddddd" + shared.getString("defaultLang", "none"));
        Resources res = getResources();
// Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            System.out.println("inside fun");
            conf.setLocale(new Locale(langCode)); // API 17+ only.
            conf.setLayoutDirection(new Locale(langCode));

        } else {
            conf.locale = new Locale(langCode);
        }


// Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
        shared.edit().putString("defaultLang", langCode).apply();

    }

    public void getVersion(final Intent intent) {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {

            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "version", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL" + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONObject subresJSON = resJSON.getJSONObject("data");
                                checkVersion(Double.valueOf(shared.getString("version", "0")), subresJSON.getDouble("version"), intent);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }

            });
        }
    }

    public void checkVersion(double localVersion, double serverVersion, Intent intent) {
        if (serverVersion > localVersion) {
            getSystemData(serverVersion);
        } else {
            check();
        }
    }

    private void getSystemData(final double serverVersion) {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {

            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "startup", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                final JSONObject subresJSON = resJSON.getJSONObject("data");
                                shared.edit().putString("version", String.valueOf(serverVersion)).apply();
                                shared.edit().putInt("counter", 1).apply();
                                check();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }

            });
        }
    }

    private void check() {
        boolean login = shared.getBoolean("login", false);
        Intent i;
        if (login) {
            i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        } else {
            i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }
    }

    private boolean checkCounter() {
        int counter = shared.getInt("counter", 0);
        Double version = Double.parseDouble(shared.getString("version", "0"));
        if (version == 0) return true;
        if (counter == 0) {
            return true;
        } else if (counter == 5) {
            shared.edit().putInt("counter", 0).apply();
            return false;
        } else {
            shared.edit().putInt("counter", ++counter).apply();
            return false;
        }
    }
}