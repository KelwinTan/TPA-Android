package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PlaceDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private TextView textPlaceName, textDescription;
    private EditText editGuestCode, editAdminCode;
    private Button btnJoinAsGuest, btnJoinAsAdmin;
    private String placeId;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textPlaceName = findViewById(R.id.text_place_name);
        imageView = findViewById(R.id.image_view_place);
        textDescription = findViewById(R.id.text_description);
        editGuestCode = findViewById(R.id.edit_guest_code);
        editAdminCode = findViewById(R.id.edit_admin_code);
        btnJoinAsGuest = findViewById(R.id.btn_join_as_guest);
        btnJoinAsAdmin = findViewById(R.id.btn_join_as_admin);

        placeId = ((Place)getIntent().getParcelableExtra("Place")).getId();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

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

        btnJoinAsGuest.setOnClickListener(this);
        btnJoinAsAdmin.setOnClickListener(this);
    }

    public void addAddedPlaceGuest(final String guestCode, final AddedPlace addedPlace) {
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(Place.class).getGuestCode().equals(guestCode)) {
                        databaseReference.child("Added_Places").push().setValue(addedPlace).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getBaseContext(), "Place added successfully!!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PlaceDetailActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        btnJoinAsGuest.setEnabled(true);
                        Toast.makeText(PlaceDetailActivity.this, "Code do not match", Toast.LENGTH_SHORT).show();
                    }
                }
                if(dataSnapshot.getChildrenCount() == 0) {
                    btnJoinAsGuest.setEnabled(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void addAddedPlaceAdmin(final String adminCode, final AddedPlace addedPlace) {
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(Place.class).getAdminCode().equals(adminCode)) {
                        databaseReference.child("Added_Places").push().setValue(addedPlace).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getBaseContext(), "Place added successfully!!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PlaceDetailActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        btnJoinAsAdmin.setEnabled(true);
                        Toast.makeText(PlaceDetailActivity.this, "Code do not match", Toast.LENGTH_SHORT).show();
                    }
                }
                if(dataSnapshot.getChildrenCount() == 0) {
                    btnJoinAsAdmin.setEnabled(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_join_as_guest:
                final String guestCode = editGuestCode.getText().toString();
                if(guestCode.isEmpty()) {
                    Toast.makeText(this, "Guest Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    btnJoinAsGuest.setEnabled(false);
                    AddedPlace addedPlace = new AddedPlace(placeId, sharedPreferences.getString("Id", null), "Guest");
                    addAddedPlaceGuest(guestCode, addedPlace);
                }
                break;
            case R.id.btn_join_as_admin:
                final String adminCode = editAdminCode.getText().toString();
                if(adminCode.isEmpty()) {
                    Toast.makeText(this, "Admin Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    btnJoinAsAdmin.setEnabled(false);
                    AddedPlace addedPlace = new AddedPlace(placeId, sharedPreferences.getString("Id", null), "Admin");
                    addAddedPlaceAdmin(adminCode, addedPlace);
                }
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

