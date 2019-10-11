package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.fragment.MyPlaceFragment;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class PlaceSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private String placeId;
    private ImageView imageView;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userLoggedInId;
    private LinearLayout layoutCurrentPlaceInformation;
    private TextView textActiveRole;
    private EditText editGuestCode, editAdminCode, editCurrentPlaceName, editCurrentPlaceDesc, editCurrentGuestCode, editCurrentAdminCode;
    private Button btnConvertToGuest, btnConvertToAdmin, btnSaveChanges, btnLeaveThisPlace, btnDeleteThisPlace;
    private AddedPlace addedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.image_view);
        editGuestCode = findViewById(R.id.edit_guest_code);
        editAdminCode = findViewById(R.id.edit_admin_code);
        textActiveRole = findViewById(R.id.text_active_role);
        layoutCurrentPlaceInformation = findViewById(R.id.layout_current_place_information);
        editCurrentPlaceName = findViewById(R.id.edit_current_place_name);
        editCurrentPlaceDesc = findViewById(R.id.edit_current_place_desc);
        editCurrentGuestCode = findViewById(R.id.edit_current_guest_code);
        editCurrentAdminCode = findViewById(R.id.edit_current_admin_code);
        btnConvertToGuest = findViewById(R.id.btn_convert_to_guest);
        btnConvertToAdmin = findViewById(R.id.btn_convert_to_admin);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnLeaveThisPlace = findViewById(R.id.btn_leave_this_place);
        btnDeleteThisPlace = findViewById(R.id.btn_delete_this_place);

        placeId = ((Place)getIntent().getParcelableExtra("Place_Settings")).getId();

        sharedPreferences = getBaseContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    Place place = dataSnapshot1.getValue(Place.class);
                    Picasso.get().load(place.getImageUrl()).resize(350,250).centerCrop().into(imageView);
                    editCurrentPlaceName.setText(place.getPlaceName());
                    editCurrentPlaceDesc.setText(place.getPlaceDescription());
                    editCurrentGuestCode.setText(place.getGuestCode());
                    editCurrentAdminCode.setText(place.getAdminCode());
                    databaseReference.child("Added_Places").orderByChild("placeId").equalTo(placeId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                if(dataSnapshot3.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)) {
                                    addedPlace = dataSnapshot3.getValue(AddedPlace.class);
                                    addedPlace.setId(dataSnapshot3.getKey());
                                    if(addedPlace.getRoleName().equals("Admin")) {
                                        layoutCurrentPlaceInformation.setVisibility(View.VISIBLE);
                                        btnSaveChanges.setVisibility(View.VISIBLE);
                                        btnDeleteThisPlace.setVisibility(View.VISIBLE);
                                    } else {
                                        layoutCurrentPlaceInformation.setVisibility(View.GONE);
                                        btnSaveChanges.setVisibility(View.GONE);
                                        btnDeleteThisPlace.setVisibility(View.GONE);
                                    }
                                    String temp = "Active Role : " + addedPlace.getRoleName();
                                    textActiveRole.setText(temp);
                                }
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

        btnConvertToGuest.setOnClickListener(this);
        btnConvertToAdmin.setOnClickListener(this);
        btnSaveChanges.setOnClickListener(this);
        btnLeaveThisPlace.setOnClickListener(this);
        btnDeleteThisPlace.setOnClickListener(this);
    }

    public void switchToGuest(final String guestCode){
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(Place.class).getGuestCode().equals(guestCode)) {
                        databaseReference.child("Added_Places").orderByChild("placeId").equalTo(dataSnapshot1.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                    if(dataSnapshot3.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)) {
                                        Map<String, Object> updates = new HashMap<String, Object>();
                                        updates.put("roleName", "Guest");
                                        databaseReference.child("Added_Places").child(dataSnapshot3.getKey()).updateChildren(updates);
                                        Toast.makeText(PlaceSettingsActivity.this, "You Are Now A Guest", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    } else {
                        Toast.makeText(PlaceSettingsActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void switchToAdmin(final String adminCode){
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(Place.class).getAdminCode().equals(adminCode)) {
                        databaseReference.child("Added_Places").orderByChild("placeId").equalTo(dataSnapshot1.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                    if(dataSnapshot3.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)) {
                                        Map<String, Object> updates = new HashMap<String, Object>();
                                        updates.put("roleName", "Admin");
                                        databaseReference.child("Added_Places").child(dataSnapshot3.getKey()).updateChildren(updates);
                                        Toast.makeText(PlaceSettingsActivity.this, "You Are Now An Admin", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    } else {
                        Toast.makeText(PlaceSettingsActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void updatePlaceInformation(final String placeName, final String placeDesc, final String guestCode, final String adminCode) {
        databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("placeName", placeName);
                    updates.put("placeDesc", placeDesc);
                    updates.put("guestCode", guestCode);
                    updates.put("adminCode", adminCode);
                    databaseReference.child("Places").child(dataSnapshot1.getKey()).updateChildren(updates);
                    Toast.makeText(PlaceSettingsActivity.this, "Information Updated Successfully!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void leavePlace() {
        databaseReference.child("Added_Places").orderByChild("placeId").equalTo(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)) {
                        dataSnapshot1.getRef().removeValue();
                        Toast.makeText(getBaseContext(), "Leave Place Successfull", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("LOAD_FRAGMENT", "My_Place_Fragment");
                        startActivity(intent);
                        finish();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void removePlace(){

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are You Sure?");
        alertDialog.setMessage("This Will Remove Place Permanently!");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseReference.child("Places").orderByKey().equalTo(placeId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            dataSnapshot1.getRef().removeValue();
                            Toast.makeText(getBaseContext(), "Place has been deleted!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("LOAD_FRAGMENT", "My_Place_Fragment");
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_convert_to_guest:
                final String guestCode = editGuestCode.getText().toString();
                editGuestCode.setText("");
                if(guestCode.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Guest Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    switchToGuest(guestCode);
                }
                break;
            case R.id.btn_convert_to_admin:
                final String adminCode = editAdminCode.getText().toString();
                editAdminCode.setText("");
                if(adminCode.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Admin Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    switchToAdmin(adminCode);
                }
                break;
            case R.id.btn_save_changes:
                final String newPlaceName = editCurrentPlaceName.getText().toString();
                final String newPlaceDesc = editCurrentPlaceDesc.getText().toString();
                final String newGuestCode = editCurrentGuestCode.getText().toString();
                final String newAdminCode = editCurrentGuestCode.getText().toString();

                if(newPlaceName.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Place Name must be filled", Toast.LENGTH_SHORT).show();
                } else if(newPlaceDesc.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Place Desc must be filled", Toast.LENGTH_SHORT).show();
                } else if(newGuestCode.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Guest Code must be filled", Toast.LENGTH_SHORT).show();
                } else if(newAdminCode.isEmpty()) {
                    Toast.makeText(PlaceSettingsActivity.this, "Admin Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    updatePlaceInformation(newPlaceName, newPlaceDesc, newGuestCode, newAdminCode);
                }
                break;
            case R.id.btn_leave_this_place:
                leavePlace();
                break;
            case R.id.btn_delete_this_place:
                removePlace();
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
