package com.savvy.apartments;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.savvy.apartments.Model.Building;
import com.savvy.apartments.Network.Iokihttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    Iokihttp iokihttp;
    SharedPreferences shared;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Building> buildings;

    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        view = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private void init() throws JSONException {
        shared = getActivity().getSharedPreferences("com.savvy.apartments", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();

        setUserInfo();
        getBuildings();

    }

    private void getBuildings() throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        subJSON.put("user_id", shared.getString("user_id", "0"));
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "getBuildings", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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
                                    final JSONArray subresJSON = resJSON.getJSONArray("data");
                                    initGrid(subresJSON);
                                }
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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
    private void setUserInfo() {
        TextView welcome = view.findViewById(R.id.welcome);
        welcome.setText(getResources().getString(R.string.welcome) + " " + shared.getString("name", ""));
    }

    private void initGrid(JSONArray sections) throws IOException {

        int length = sections.length();
        final int columnNo = 2;
        int rowNo = 0;
        if (length % 2 != 0) {
            length = length + 1;
        }
        rowNo = length / 2;
        final GridLayout section_grid = view.findViewById(R.id.section_grid);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.removeAllViewsInLayout();
            }
        });
        buildings = new ArrayList<>();
        final int finalRowNo = rowNo;
        getActivity().runOnUiThread(new Runnable() {
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
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View section_card = inflater.inflate(R.layout.section_card, null);

            final CardView section_cardView = section_card.findViewById(R.id.section_cardView);
            section_cardView.setId(getID("1", x));
            ImageView icon = section_cardView.findViewById(R.id.section_icon);
            icon.setId(getID("2", x));
            TextView lable = section_cardView.findViewById(R.id.section_label);
            lable.setId(getID("3", x));
            JSONObject section = null;
            try {
                if (x < sections.length()) {
                    section = sections.getJSONObject(x);
                    Building building = new Building();
                    building.setId(section.getString("id"));
                    building.setName(section.getString("title"));
                    building.setAddress(section.getString("address"));
                    building.setApartments_no(section.getString("flats_no"));
                    building.setApartments(section.getJSONArray("flats").toString());
                    lable.setText(building.getName());
                    section_cardView.setTag(x);
                    buildings.add(building);
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
                    openApartments(Integer.parseInt(view.getTag().toString()));
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    section_grid.addView(section_cardView);
                }
            });
        }
    }

    private void openApartments(int tag) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("building", buildings.get(tag));
        Intent intent = new Intent(getContext(), Apartments.class);
        intent.putExtra("apartments", buildings.get(tag).getApartments());
        intent.putExtra("building", bundle);
        startActivityForResult(intent, 111);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == 1) {
            try {
                getBuildings();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int getID(String id, int number) {
        String s = id + number;
        return Integer.parseInt(s);
    }

    public void showLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.main_loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.main_loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);

                main.setVisibility(View.VISIBLE);

                loading.setVisibility(View.GONE);
            }
        });
    }

    public interface OnFragmentInteractionListener {
    }
}