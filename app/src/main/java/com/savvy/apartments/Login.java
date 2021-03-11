package com.savvy.apartments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.savvy.apartments.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    boolean hide = true;
    Iokihttp iokihttp;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        iokihttp = new Iokihttp();
        shared = this.getSharedPreferences("com.savvy.apartments", Context.MODE_PRIVATE);
        findViewById(R.id.show_pass_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHidePassword();
            }
        });
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    try {
                        login();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private String encrypt(String password) throws NoSuchAlgorithmException {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateForm() {
        String phone, password;
        phone = ((EditText) findViewById(R.id.username)).getText().toString();
        password = ((EditText) findViewById(R.id.password2)).getText().toString();
        if (phone.isEmpty() || password.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(findViewById(R.id.layout), getResources().getString(R.string.fill_error), Snackbar.LENGTH_SHORT).show();
                }
            });
            return false;
        } else {
            return true;
        }
    }


    private void login() throws NoSuchAlgorithmException, JSONException {
        String password1, username;
        String token = FirebaseInstanceId.getInstance().getToken();

        password1 = ((EditText) findViewById(R.id.password2)).getText().toString();
        username = ((EditText) findViewById(R.id.username)).getText().toString();
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("password", encrypt(password1));
        subJSON.put("username", username);
        subJSON.put("token", token);
        System.out.println("11111111111" + subJSON);
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "login", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
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
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {


                            JSONObject resJSON = new JSONObject(responseStr);

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                if (Integer.parseInt(resJSON.get("code").toString()) == 1) {
                                    final JSONObject subresJSON = resJSON.getJSONObject("data");
                                    shared.edit().putString("name", subresJSON.getJSONObject("user").getString("name")).apply();
                                    shared.edit().putString("phone", subresJSON.getJSONObject("user").getString("phone")).apply();
                                    shared.edit().putString("user_id", subresJSON.getJSONObject("user").getString("id")).apply();
                                    shared.edit().putString("token", subresJSON.getString("token")).apply();
                                    openMain();
                                } else if (Integer.parseInt(resJSON.get("code").toString()) == 2) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.fill_error), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
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

    private void openMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        shared.edit().putBoolean("login", true).apply();
        startActivity(intent);
        finish();

    }

    private void showHidePassword() {
        if (hide) {
            ((EditText) findViewById(R.id.password2)).setTransformationMethod(null);
            hide = false;
        } else {
            ((EditText) findViewById(R.id.password2)).setTransformationMethod(new PasswordTransformationMethod());
            hide = true;
        }
    }

    public void showLoading() {
        final View main = findViewById(R.id.layout);
        final ProgressBar loading = findViewById(R.id.loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        final View main = findViewById(R.id.layout);
        final ProgressBar loading = findViewById(R.id.loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);

                main.setVisibility(View.VISIBLE);

                loading.setVisibility(View.GONE);
            }
        });
    }
}
