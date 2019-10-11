package com.bluejack18_2.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.MyPlacePageAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaceFragment extends Fragment {

    private MyPlacePageAdapter myPlacePageAdapter;
    private ViewPager viewPager;

    public MyPlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_place, container, false);

        myPlacePageAdapter = new MyPlacePageAdapter(getChildFragmentManager());
        myPlacePageAdapter.addFragment(new MyPlaceAsGuestFragment(), "AS GUEST");
        myPlacePageAdapter.addFragment(new MyPlaceAsAdminFragment(), "AS ADMIN");

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(myPlacePageAdapter);

        TabLayout tabLayout = view.findViewById(R.id.my_place_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

}