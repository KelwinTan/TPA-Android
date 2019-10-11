package com.bluejack18_2.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.MyReportPageAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyReportFragment extends Fragment {

    private MyReportPageAdapter myReportPageAdapter;
    private ViewPager viewPager;

    public MyReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report, container, false);

        myReportPageAdapter = new MyReportPageAdapter(getChildFragmentManager());
        myReportPageAdapter.addFragment(new MyReportWaitingFragment(), "WAITING");
        myReportPageAdapter.addFragment(new MyReportOnProgressFragment(), "ON PROGRESS");
        myReportPageAdapter.addFragment(new MyReportCompletedFragment(), "COMPLETED");
        myReportPageAdapter.addFragment(new MyReportLockedFragment(), "LOCKED");

        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(myReportPageAdapter);

        TabLayout tabLayout = view.findViewById(R.id.my_report_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

}
