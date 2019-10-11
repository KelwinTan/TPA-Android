package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.PlaceInformationAdapter;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.bluejack18_2.model.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaceInformationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private TextView textPlaceName, textPlaceDescription, textReportList;
    private String addedId, addedPlaceId;
    private Button btnEditSettings, btnPlaceSummary;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private PlaceInformationAdapter placeInformationAdapter;
    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addedId = ((AddedPlace)getIntent().getParcelableExtra("My_Place")).getId();
        addedPlaceId = ((AddedPlace)getIntent().getParcelableExtra("My_Place")).getPlaceId();
        textPlaceName = findViewById(R.id.text_place_name);
        textPlaceDescription = findViewById(R.id.text_place_description);
        textReportList = findViewById(R.id.text_report_list);
        imageView = findViewById(R.id.image_view);
        btnEditSettings = findViewById(R.id.btn_edit_settings);
        btnPlaceSummary = findViewById(R.id.btn_place_summary);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Added_Places").orderByKey().equalTo(addedId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    databaseReference.child("Places").orderByKey().equalTo(dataSnapshot1.getValue(AddedPlace.class).getPlaceId()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                place = dataSnapshot3.getValue(Place.class);
                                place.setId(dataSnapshot3.getKey());
                                textPlaceName.setText(place.getPlaceName());
                                textPlaceDescription.setText(place.getPlaceDescription());
                                Picasso.get().load(place.getImageUrl()).resize(350,250).centerCrop().into(imageView);
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

        recyclerView = findViewById(R.id.rv_place_information_report);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        final ArrayList<Report> reports = new ArrayList<>();
        databaseReference.child("Report").orderByChild("placeId").equalTo(addedPlaceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reports.clear();
                placeInformationAdapter = new PlaceInformationAdapter(getBaseContext());
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    Report report = dataSnapshot1.getValue(Report.class);
                    report.setId(dataSnapshot1.getKey());
                    reports.add(report);
                }
                if(dataSnapshot.getChildrenCount() == 0) {
                    textReportList.setVisibility(View.GONE);
                } else {
                    textReportList.setVisibility(View.VISIBLE);
                }
                placeInformationAdapter.setReports(reports);
                recyclerView.setAdapter(placeInformationAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        btnEditSettings.setOnClickListener(this);
        btnPlaceSummary.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_edit_settings:
                intent = new Intent(this, PlaceSettingsActivity.class);
                intent.putExtra("Place_Settings", place);
                startActivity(intent);
                break;
            case R.id.btn_place_summary:
                intent = new Intent(this, PlaceSummaryActivity.class);
                intent.putExtra("Place_Summary", place);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
