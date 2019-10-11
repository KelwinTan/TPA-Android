package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PlaceAddedDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private TextView textPlaceName, textDescription;
    private Button btnRemoveThisPlace;
    private DatabaseReference databaseReference;
    private String placeId;
    private SharedPreferences sharedPreferences;
    private String userLoggedInId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_added_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textPlaceName = findViewById(R.id.text_place_name);
        imageView = findViewById(R.id.image_view_place);
        textDescription = findViewById(R.id.text_description);
        btnRemoveThisPlace = findViewById(R.id.btn_remove_this_place);

        placeId = ((Place)getIntent().getParcelableExtra("Place")).getId();
        sharedPreferences = getBaseContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    textPlaceName.setText(dataSnapshot1.getValue(Place.class).getPlaceName());
                    Picasso.get().load(dataSnapshot1.getValue(Place.class).getImageUrl()).resize(350, 250).centerCrop().into(imageView);
                    textDescription.setText(dataSnapshot1.getValue(Place.class).getPlaceDescription());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        btnRemoveThisPlace.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_remove_this_place:
                btnRemoveThisPlace.setEnabled(false);
                databaseReference.child("Added_Places").orderByChild("placeId").equalTo(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            if(dataSnapshot1.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)){
                                dataSnapshot1.getRef().removeValue();
                                Toast.makeText(PlaceAddedDetailActivity.this, "Remove Successful", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
