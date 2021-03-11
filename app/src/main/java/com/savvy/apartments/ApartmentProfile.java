package com.savvy.apartments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import com.savvy.apartments.Model.Apartment;
import com.savvy.apartments.Model.Building;
import com.savvy.apartments.Model.Reservation;
import com.savvy.apartments.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ApartmentProfile extends AppCompatActivity {
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;

    SharedPreferences shared;
    Iokihttp iokihttp;
    Reservation reservation;
    View reservation_include;
    Spinner period_spinner;
    AlertDialog.Builder builder;
    Apartment apartment;
    boolean nameEdit,noteEdit = false;
    boolean main = true;
    SmoothDateRangePickerFragment smoothDateRangePickerFragment;
    Building building;
    double Tdays, Tprice,dayPrice,monthPrice = 0.0;
    int y1, y2, m1, m2, d1, d2 = 0;
    Date date1, date2 = new Date();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_profile);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() throws JSONException {
        apartment = new Apartment();
        iokihttp = new Iokihttp();
        shared = this.getSharedPreferences("com.savvy.apartments", Context.MODE_PRIVATE);
        reservation = new Reservation();
        builder = new AlertDialog.Builder(ApartmentProfile.this);
        initDatePicker();
        reservation_include = findViewById(R.id.reserve_include);
        reservation_include.findViewById(R.id.reserve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    try {
                        reserve();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        initSpinner();
        ((EditText) reservation_include.findViewById(R.id.price)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Double price=0.0;
                if(isNumerice(editable.toString())){
                  price=Double.parseDouble(editable.toString());
                }
                dayPrice=price;
                monthPrice=price/30;
                Tprice = period_spinner.getSelectedItemPosition() == 0 ? price : price / 30;
                calculateTotal();

            }
        });
        period_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Tprice = period_spinner.getSelectedItemPosition() == 0 ? dayPrice : monthPrice;
                calculateTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        findViewById(R.id.cancel_reservation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sureCancelReservation();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ((TextView) findViewById(R.id.title)).setText(getResources().getString(R.string.apartment_profile));

        fillApartmentModel();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    showOrHide();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.nameB).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (nameEdit) {
                    try {
                        changeForSave(R.id.nameE, R.id.name, R.id.nameB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    nameEdit = false;
                } else {
                    changeForEdit(R.id.nameE, R.id.name, R.id.nameB);
                    nameEdit = true;
                }
            }
        });
        findViewById(R.id.noteB).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (noteEdit) {
                    try {
                        changeForSave(R.id.noteE, R.id.note, R.id.noteB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    noteEdit = false;
                } else {
                    changeForEdit(R.id.noteE, R.id.note, R.id.noteB);
                    noteEdit = true;
                }
            }
        });
        findViewById(R.id.reserve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReservation();
            }
        });
        reservation_include.findViewById(R.id.start_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
            }
        });
        reservation_include.findViewById(R.id.end_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
            }
        });
        fillProfile();
    }
    private void initSpinner(){
        period_spinner = reservation_include.findViewById(R.id.period_type);

        String[] types = getResources().getStringArray(R.array.period_types);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item_custom, R.id.text, types);

        period_spinner.setAdapter(adapter);
        period_spinner.setSelection(0);
    }
private boolean isNumerice(String value){
        if(value==null || value.isEmpty()) return false;
        try {
            double d=Double.parseDouble(value);
        }catch (NumberFormatException e){
            return false;
        }
        return true;
}
    private void fillApartmentModel() throws JSONException {
        apartment = ((Apartment) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBundle("apartment")).getSerializable("apartment"));
    }

    private void initDatePicker() {
        smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                    @Override
                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                               int yearStart, int monthStart,
                                               int dayStart, int yearEnd,
                                               int monthEnd, int dayEnd) {
                        try {
                            y1 = yearStart;
                            y2 = yearEnd;
                            m1 = monthStart;
                            m2 = monthEnd;
                            d1 = dayStart;
                            d2 = dayEnd;
                            reservation.setStart_date(yearStart + "-" + (monthStart + 1) + "-" + dayStart);
                            reservation.setEnd_date(yearEnd + "-" + (monthEnd + 1) + "-" + dayEnd);
                            reservation.setDays(String.valueOf(calculateDays(yearStart, monthStart + 1, dayStart, yearEnd, monthEnd + 1, dayEnd)));
                            Tdays = Double.parseDouble(reservation.getDays());
                            ((TextView) reservation_include.findViewById(R.id.start_date)).setText(reservation.getStart_date());
                            ((TextView) reservation_include.findViewById(R.id.end_date)).setText(reservation.getEnd_date());
                            ((TextView) reservation_include.findViewById(R.id.days)).setText(reservation.getDays());
                            calculateTotal();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
        // smoothDateRangePickerFragment.
        // smoothDateRangePickerFragment.du

    }

    private long calculateDays(int yearStart, int monthStart,
                               int dayStart, int yearEnd,
                               int monthEnd, int dayEnd) throws ParseException {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();
        SimpleDateFormat stf = new SimpleDateFormat("dd/MM/yyyy");
        String inputString1 = dayStart + "/" + monthStart + "/" + yearStart;
        String inputString2 = dayEnd + "/" + monthEnd + "/" + yearEnd;
        Date date1 = stf.parse(inputString1);
        cal1.setTime(date1);
        Date date2 = stf.parse(inputString2);
        cal2.setTime(date2);
        long monthsBetween = monthsBetweenT(cal1.getTime(), cal2.getTime());
        long daysBetween = daysBetween(cal1.getTime(), cal2.getTime());
        System.out.println("days=" + daysBetween);
            return daysBetween;

    }
    private long calculateMonths(int yearStart, int monthStart,
                               int dayStart, int yearEnd,
                               int monthEnd, int dayEnd) throws ParseException {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();
        SimpleDateFormat stf = new SimpleDateFormat("dd/MM/yyyy");
        String inputString1 = dayStart + "/" + monthStart + "/" + yearStart;
        String inputString2 = dayEnd + "/" + monthEnd + "/" + yearEnd;
        Date date1 = stf.parse(inputString1);
        cal1.setTime(date1);
        Date date2 = stf.parse(inputString2);
        cal2.setTime(date2);
        long monthsBetween = monthsBetweenT(cal1.getTime(), cal2.getTime());
        long daysBetween = daysBetween(cal1.getTime(), cal2.getTime());
        System.out.println("days=" + daysBetween);

            return ((int) daysBetween)/30 ;

    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    public int monthsBetweenT(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 ));
    }

    private boolean validateForm() {

        reservation.setReserver_name(((EditText) reservation_include.findViewById(R.id.reserver_name)).getText().toString());
        reservation.setNote(((EditText) reservation_include.findViewById(R.id.reserver_note)).getText().toString());
        reservation.setReserver_phone(((EditText) reservation_include.findViewById(R.id.phone)).getText().toString());
        reservation.setDay_price(((EditText) reservation_include.findViewById(R.id.price)).getText().toString());
        if (validateDate()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(findViewById(R.id.layout), getResources().getString(R.string.error_date), Snackbar.LENGTH_SHORT).show();
                }
            });
            return false;
        } else if (reservation.getReserver_phone().isEmpty() || reservation.getReserver_name().isEmpty() || reservation.getDay_price().isEmpty()) {
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

    private void reserve() throws JSONException {
        reservation.setDay_price(period_spinner.getSelectedItemPosition() == 0 ? reservation.getDay_price() : String.valueOf((Long.parseLong(reservation.getDay_price()) / 30)));
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("flat_id", apartment.getId());
        subJSON.put("start_date", reservation.getStart_date());
        subJSON.put("end_date", reservation.getEnd_date());
        subJSON.put("price", reservation.getDay_price());
        subJSON.put("name", reservation.getReserver_name());
        subJSON.put("phone", reservation.getReserver_phone());
        subJSON.put("user_id", shared.getString("user_id", "0"));
        subJSON.put("note",reservation.getNote());
        System.out.println("json=" + subJSON);
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "reserve", shared.getString("token", ""), json.toString(), new Callback() {
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
                                    //final JSONObject subresJSON = resJSON.getJSONObject("data");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.success_reserved), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    setResult(1);
                                    finish();

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
                        System.out.println("Response=" + response + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }

            });
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

    private boolean validateDate() {
        return y1 <= 0 || y2 <= 0 || m1 <= 0 || m2 <= 0 || d1 <= 0 || d2 <= 0 || Tdays <= 0 || reservation.getStart_date().isEmpty() || reservation.getEnd_date().isEmpty();
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

    private void sureCancelReservation() {
        builder.setMessage(getString(R.string.insure_dialog))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            cancelReservation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void cancelReservation() throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("id", reservation.getId());
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "cancel", shared.getString("token", ""), json.toString(), new Callback() {
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.success_cancel), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    setResult(1);
                                    finish();

                                    //   final JSONObject subresJSON = resJSON.getJSONObject("data");
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

    private void calculateTotal() {
        TextView total = reservation_include.findViewById(R.id.total_price);
        total.setText(String.valueOf(Tdays * Tprice));
    }

    private void editApartmentName(String newName) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("id", apartment.getId());
        subJSON.put("title", newName);
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "setFlat", shared.getString("token", ""), json.toString(), new Callback() {
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.done_editting), Toast.LENGTH_SHORT).show();
                                        }
                                    });                                }
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

    private void editNote(String note) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("id", reservation.getId());
        subJSON.put("note", note);
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "setNote", shared.getString("token", ""), json.toString(), new Callback() {
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.done_editting), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //final JSONObject subresJSON = resJSON.getJSONObject("data");
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

    @SuppressLint("SetTextI18n")
    private void fillLastReservation() throws JSONException {
        TextView last_reservation_date = findViewById(R.id.last_reservation_date);
        TextView price_day = findViewById(R.id.price_day);
        TextView price_month = findViewById(R.id.price_month);

        JSONObject jsonObject = new JSONObject(apartment.getReservation());
        reservation.setId(jsonObject.getString("id"));
        reservation.setReserver_name(jsonObject.getString("name"));
        reservation.setReserver_phone(jsonObject.getString("phone"));
        reservation.setDay_price(jsonObject.getString("price"));
        reservation.setStart_date(jsonObject.getString("start_date"));
        reservation.setEnd_date(jsonObject.getString("end_date"));

        last_reservation_date.setText(reservation.getEnd_date());
        price_day.setText(reservation.getDay_price() + "/" + getResources().getString(R.string.day));
        price_month.setText((Double.parseDouble(reservation.getDay_price()) * 30) + "/" + getResources().getString(R.string.month));

        findViewById(R.id.last_reservation_info).setVisibility(View.VISIBLE);
        findViewById(R.id.last_reservation_divider).setVisibility(View.VISIBLE);

    }

    @SuppressLint("SetTextI18n")
    private void fillReserver() throws JSONException {
        TextView reserver_name = findViewById(R.id.reserver_name);
        TextView reserver_phone = findViewById(R.id.reserver_phone);
        TextView start_date = findViewById(R.id.reservation_start_date);
        TextView end_date = findViewById(R.id.reservation_end_date);
        TextView note = findViewById(R.id.note);

        TextView reservation_price_day = findViewById(R.id.reservation_price_day);
        JSONObject jsonObject = new JSONObject(apartment.getReservation());
        reservation.setId(jsonObject.getString("id"));
        reservation.setReserver_name(jsonObject.getString("name"));
        reservation.setReserver_phone(jsonObject.getString("phone"));
        reservation.setDay_price(jsonObject.getString("price"));
        reservation.setStart_date(jsonObject.getString("start_date"));
        reservation.setEnd_date(jsonObject.getString("end_date"));
        reservation.setNote(jsonObject.getString("note").isEmpty()||jsonObject.getString("note")==null?"":jsonObject.getString("note"));

        reserver_name.setText(reservation.getReserver_name());
        reserver_phone.setText(reservation.getReserver_phone());
        reserver_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call(reservation.getReserver_phone());
            }
        });
        note.setText(reservation.getNote());
        start_date.setText(reservation.getStart_date());
        end_date.setText(reservation.getEnd_date());
        reservation_price_day.setText(reservation.getDay_price() + "/" + getResources().getString(R.string.day));

        findViewById(R.id.reserver1).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver_divider1).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver2).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver_divider2).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver3).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver_divider3).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver4).setVisibility(View.VISIBLE);
        findViewById(R.id.reserver_divider4).setVisibility(View.VISIBLE);
    }

    private void showOrHide() throws JSONException {
        Button reserve = findViewById(R.id.reserve);
        Button cancel = findViewById(R.id.cancel_reservation);
        switch (Integer.parseInt(apartment.getStatus_id())) {
            case 0: {
                reserve.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.VISIBLE);
                fillReserver();
            }
            break;
            case 1: {
                reserve.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.INVISIBLE);
                System.out.println("fjkdsfkjjksdfjkdsk" + apartment.getReservation());
                if (!apartment.getReservation().equals("null")) fillLastReservation();
            }
            break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void changeForEdit(int editID, int textID, int buttonID) {
        EditText editText = findViewById(editID);
        TextView textView = findViewById(textID);
        ImageView imageView = findViewById(buttonID);
        editText.setText(textView.getText().toString());
        textView.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.save));
        editText.requestFocus();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void changeForSave(int editID, int textID, int buttonID) throws JSONException {
        EditText editText = findViewById(editID);
        TextView textView = findViewById(textID);
        ImageView imageView = findViewById(buttonID);
        textView.setText(editText.getText().toString());
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.GONE);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.edit_icon));
        if(buttonID==R.id.nameB) {
            editApartmentName(editText.getText().toString());
        }
        else{
            editNote(editText.getText().toString());
        }
    }

    private void openReservation() {
        main = false;
        reservation_include.setVisibility(View.VISIBLE);
        findViewById(R.id.profile).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (main) {
            finish();
        } else {
            reservation_include.setVisibility(View.GONE);
            findViewById(R.id.profile).setVisibility(View.VISIBLE);
            main = true;
        }
    }

    private void fillProfile() {
        building = ((Building) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBundle("apartment")).getSerializable("building"));
        TextView name = findViewById(R.id.name);
        TextView building_name = findViewById(R.id.building_name);
        TextView address = findViewById(R.id.address);

        name.setText(apartment.getName());
        building_name.setText(building.getName());
        address.setText(building.getAddress().equals("null") ? "" : building.getAddress());


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void call(String phone) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
        System.out.println("call");
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            String dial = "tel:" + phone;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            Toast.makeText(ApartmentProfile.this, "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

}