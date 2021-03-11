package com.savvy.apartments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AboutUs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutUs extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String FACEBOOK_URL = "https://www.facebook.com/savvy.sd";
    public static String FACEBOOK_PAGE_ID = "332657020659894";
    View view;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AboutUs() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AboutUs.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutUs newInstance(String param1, String param2) {
        AboutUs fragment = new AboutUs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void openInsta() {
        try {
            // mediaLink is something like "https://instagram.com/p/6GgFE9JKzm/" or
            // "https://instagram.com/_u/sembozdemir"
            Uri uri = Uri.parse("https://www.instagram.com/_u/savvytechnology/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            intent.setPackage("com.instagram.android");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    private void openFB() {
        try {
            getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + FACEBOOK_PAGE_ID));
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
            startActivity(intent);
        }
    }

    private void openWeb() {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.savvy-technology.com"));
        startActivity(intent);
    }

    private void init() {
        view.findViewById(R.id.insta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInsta();
            }
        });
        view.findViewById(R.id.fb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFB();
            }
        });
        view.findViewById(R.id.web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb();
            }
        });
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
        view = inflater.inflate(R.layout.fragment_about_us, container, false);
        init();
        return view;
    }

    public interface OnFragmentInteractionListener {
    }
}