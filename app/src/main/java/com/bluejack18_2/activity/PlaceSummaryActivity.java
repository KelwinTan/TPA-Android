package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bluejack18_2.R;
import com.bluejack18_2.model.Place;
import com.bluejack18_2.model.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class PlaceSummaryActivity extends AppCompatActivity {

    private String placeId;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userLoggedInId;
    private TextView textNoDataAvailable, textPlaceSummary;
    private PieChartView pieChartView;
    private PieChartData pieChartData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_summary);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        placeId = ((Place)getIntent().getParcelableExtra("Place_Summary")).getId();

        sharedPreferences = getBaseContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        textNoDataAvailable = findViewById(R.id.text_no_data_available);
        textPlaceSummary = findViewById(R.id.text_place_summary);
        pieChartView = findViewById(R.id.pie_chart);
        final List<SliceValue> sliceValues = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(final DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    databaseReference.child("Report").orderByChild("placeId").equalTo(dataSnapshot1.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            float waiting = 0, onProgress = 0, completed = 0, locked = 0, total = 0;
                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                switch (dataSnapshot3.getValue(Report.class).getReportStatus()) {
                                    case "Waiting":
                                        waiting++;
                                        break;
                                    case "On Progress":
                                        onProgress++;
                                        break;
                                    case "Completed":
                                        completed++;
                                        break;
                                    case "Locked":
                                        locked++;
                                        break;
                                }
                            }
                            total = waiting + onProgress + completed + locked;
                            if(total != 0) {
                                sliceValues.clear();
                                if(waiting!=0) { sliceValues.add(new SliceValue((waiting/total)*100, Color.rgb(172, 238, 180)).setLabel("Waiting: " + String.format("%.2f",(waiting/total)*100) + "%")); }
                                if(onProgress!=0) { sliceValues.add(new SliceValue((onProgress/total)*100, Color.rgb(106,205,117)).setLabel("On Progress: " + String.format("%.2f",(onProgress/total)*100) + "%")); }
                                if(completed!=0) { sliceValues.add(new SliceValue((completed/total)*100, Color.rgb(30,141,105)).setLabel("Completed: " + String.format("%.2f",(completed/total)*100) + "%")); }
                                if(locked!=0) { sliceValues.add(new SliceValue((locked/total)*100, Color.rgb(12,88, 40)).setLabel("Locked: " +  String.format("%.2f", (locked/total)*100)  + "%")); }
                                pieChartData = new PieChartData(sliceValues);
                                pieChartData.setHasCenterCircle(true).setCenterText1(dataSnapshot1.getValue(Place.class).getPlaceName()).setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#008577"));
                                pieChartData.setHasLabels(true).setValueLabelTextSize(14);
                                pieChartView.setPieChartData(pieChartData);
                                pieChartView.setVisibility(View.VISIBLE);
                                textPlaceSummary.setVisibility(View.VISIBLE);
                                textNoDataAvailable.setVisibility(View.GONE);
                            } else {
                                pieChartView.setVisibility(View.GONE);
                                textPlaceSummary.setVisibility(View.GONE);
                                textNoDataAvailable.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
