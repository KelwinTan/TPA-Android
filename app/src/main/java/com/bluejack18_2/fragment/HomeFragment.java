package com.bluejack18_2.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.HomePageAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private HomePageAdapter homePageAdapter;
    private ViewPager viewPager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homePageAdapter = new HomePageAdapter(getChildFragmentManager());
        homePageAdapter.addFragment(new AllFragment(), "ALL");
        homePageAdapter.addFragment(new AddedFragment(), "ADDED");

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(homePageAdapter);

        TabLayout tabLayout = view.findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

}
