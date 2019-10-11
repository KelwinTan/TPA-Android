package com.bluejack18_2.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.print.PrinterId;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.MyPlaceAsGuestAdapter;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaceAsGuestFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyPlaceAsGuestAdapter myPlaceAsGuestAdapter;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    public MyPlaceAsGuestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_place_as_guest, container, false);

        final ArrayList<AddedPlace> addedPlaces = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_my_place_guest);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String userLoggedInId = sharedPreferences.getString("Id", null);

        databaseReference.child("Added_Places").orderByChild("userId").equalTo(userLoggedInId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPlaceAsGuestAdapter = new MyPlaceAsGuestAdapter(getContext());
                addedPlaces.clear();
                boolean noGuestPlaces = true;
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    final AddedPlace addedPlace = dataSnapshot1.getValue(AddedPlace.class);
                    addedPlace.setId(dataSnapshot1.getKey());
                    if(addedPlace.getRoleName().equals("Guest")) {
                        noGuestPlaces = false;
                        databaseReference.child("Places").orderByKey().equalTo(addedPlace.getPlaceId()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                    addedPlace.setPlaceName(dataSnapshot3.getValue(Place.class).getPlaceName());
                                    addedPlace.setImageUrl(dataSnapshot3.getValue(Place.class).getImageUrl());
                                    boolean isInserted = false;
                                    for(int i=0; i<addedPlaces.size(); i++) {
                                        if(addedPlaces.get(i).getPlaceId().equals(dataSnapshot3.getKey())) {
                                            addedPlaces.get(i).setPlaceName(dataSnapshot3.getValue(Place.class).getPlaceName());
                                            addedPlaces.get(i).setImageUrl(dataSnapshot3.getValue(Place.class).getImageUrl());
                                            isInserted = true;
                                        }
                                    }
                                    if(!isInserted) { addedPlaces.add(addedPlace); }
                                }
                                myPlaceAsGuestAdapter.setAddedPlaces(addedPlaces);
                                recyclerView.setAdapter(myPlaceAsGuestAdapter);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                    if(noGuestPlaces) {
                        myPlaceAsGuestAdapter.setAddedPlaces(addedPlaces);
                        recyclerView.setAdapter(myPlaceAsGuestAdapter);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }



}
