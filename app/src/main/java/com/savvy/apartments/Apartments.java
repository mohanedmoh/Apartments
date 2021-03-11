package com.savvy.apartments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.savvy.apartments.Model.Apartment;
import com.savvy.apartments.Model.Building;
import com.savvy.apartments.Network.Iokihttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

public class Apartments extends AppCompatActivity {
    Iokihttp iokihttp;
    SharedPreferences shared;
    Building building;
    private ArrayList<Apartment> apartments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartments);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initGrid(String ap) throws JSONException {
        JSONArray sections = new JSONArray(ap);
        int length = sections.length();
        final int columnNo = 2;
        int rowNo = 0;
        if (length % 2 != 0) {
            length = length + 1;
        }
        rowNo = length / 2;
        final GridLayout section_grid = findViewById(R.id.section_grid);
        final int finalRowNo = rowNo;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.setColumnCount(columnNo);
                section_grid.setRowCount(finalRowNo);
            }
        });


        for (int x = 0, c = 0, r = 0; x < length; x++, c++) {
            if (c == columnNo) {
                c = 0;
                r++;
            }
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View section_card = inflater.inflate(R.layout.section_card, null);

            final CardView section_cardView = section_card.findViewById(R.id.section_cardView);
            section_cardView.setId(getID("1", x));
            ImageView icon = section_cardView.findViewById(R.id.section_icon);
            icon.setVisibility(View.GONE);
            icon.setId(getID("2", x));
            TextView lable = section_cardView.findViewById(R.id.section_label);
            lable.setId(getID("3", x));
            JSONObject section = null;
            try {
                if (x < sections.length()) {
                    section = sections.getJSONObject(x);
                    Apartment apartment = new Apartment();
                    apartment.setId(section.getString("id"));
                    apartment.setName(section.getString("title"));
                    apartment.setReservation(section.getString("last_reservation"));
                    apartment.setStatus_id(section.getString("status_id"));
                    lable.setText(apartment.getName());
                    section_cardView.setTag(x);
                    apartments.add(apartment);
                } else section_cardView.setVisibility(View.INVISIBLE);
                // URL url = new URL(getResources().getString(R.string.imgUrl)+section.getString("image"));
                // Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                //icon.setImageBitmap(bmp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //icon.setImageURI(Uri.parse(section.getString("img")));

            section_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("FDFFFFFFFFFFFFF" + view.getTag());
                    if (view.getTag().equals("hide")) {
                    } else
                        openApartmentProfile(Integer.parseInt(view.getTag().toString()));
                }
            });
            GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1);
            GridLayout.Spec colspan = GridLayout.spec(GridLayout.UNDEFINED, 1);
            if (r == 0 && c == 0) {
                Log.e("", "spec");
                colspan = GridLayout.spec(GridLayout.UNDEFINED, 2);
                rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 2);
            }
            final GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);

            if (section_cardView.getParent() != null) {
                ((ViewGroup) section_cardView.getParent()).removeView(section_cardView); // <- fix
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    section_grid.addView(section_cardView);
                }
            });
        }
    }

    private void openApartmentProfile(int i) {
        Intent intent = new Intent(getApplicationContext(), ApartmentProfile.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("apartment", apartments.get(i));
        bundle.putSerializable("building", building);
        intent.putExtra("apartment", bundle);
        System.out.println("SSSSSSSSSSSSSSSS");
        startActivityForResult(intent, 112);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 112 && resultCode == 1) {
            setResult(1);
            finish();
        }
    }

    private void fillBuildingModel() throws JSONException {
        building = (Building) (getIntent().getExtras().getBundle("building")).getSerializable("building");
    }

    private void init() throws JSONException {
        fillBuildingModel();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ((TextView) findViewById(R.id.title)).setText(getResources().getString(R.string.apartments));
        ((TextView) findViewById(R.id.building_name)).setText(building.getName());
        shared = this.getSharedPreferences("com.savvy.apartments", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        apartments = new ArrayList<>();
        initGrid(getIntent().getExtras().getString("apartments"));
    }

    private int getID(String id, int number) {
        String s = id + number;
        return Integer.parseInt(s);
    }

    public void showLoading() {
        final View main = findViewById(R.id.layout);
        final ProgressBar loading = findViewById(R.id.main_loading);
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
        final ProgressBar loading = findViewById(R.id.main_loading);
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