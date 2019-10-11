package com.bluejack18_2.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.MyReportOnProgressAdapter;
import com.bluejack18_2.adapter.MyReportWaitingAdapter;
import com.bluejack18_2.model.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyReportOnProgressFragment extends Fragment {

    private MyReportOnProgressAdapter myReportOnProgressAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    public MyReportOnProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_report_on_progress, container, false);

        final ArrayList<Report> reports = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_my_report_on_progress);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String userLoggedInId = sharedPreferences.getString("Id", null);

        databaseReference.child("Report").orderByChild("userId").equalTo(userLoggedInId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reports.clear();
                myReportOnProgressAdapter = new MyReportOnProgressAdapter(getContext());
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    final Report report = dataSnapshot1.getValue(Report.class);
                    report.setId(dataSnapshot1.getKey());
                    if(report.getReportStatus().equals("On Progress")) {
                        reports.add(report);
                    }
                }
                myReportOnProgressAdapter.setReports(reports);
                recyclerView.setAdapter(myReportOnProgressAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }

}
