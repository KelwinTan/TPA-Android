package com.bluejack18_2.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.adapter.HomeAllPlaceAdapter;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllFragment extends Fragment {

    private DatabaseReference databaseReference;
    private HomeAllPlaceAdapter homeAllPlaceAdapter;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;

    public AllFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        final ArrayList<Place> places = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_home_all);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final String userLoggedInId = sharedPreferences.getString("Id", null);

        final ArrayList<String> bannedId = new ArrayList<>();
        databaseReference.child("Added_Places").orderByChild("userId").equalTo(userLoggedInId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bannedId.clear();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    bannedId.add(dataSnapshot1.getValue(AddedPlace.class).getPlaceId());
                }
                databaseReference.child("Places").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                        places.clear();
                        for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                            boolean isBanned = false;
                            Place place = dataSnapshot3.getValue(Place.class);
                            place.setId(dataSnapshot3.getKey());
                            for(int i=0; i<bannedId.size(); i++) {
                                if(place.getId().equals(bannedId.get(i))) { isBanned = true; }
                            }
                            if(!isBanned) { places.add(place); }
                        }
                        homeAllPlaceAdapter = new HomeAllPlaceAdapter(getContext());
                        homeAllPlaceAdapter.setPlaces(places);
                        recyclerView.setAdapter(homeAllPlaceAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }
}





//        databaseReference.child("Places").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                final ArrayList<Place> places1 = new ArrayList<>();
//                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
//                    Place place = dataSnapshot1.getValue(Place.class);
//                    place.setId(dataSnapshot1.getKey());
//                    places1.add(place);
//                }
//                final Iterator iterator = places1.iterator();
//                while (iterator.hasNext()) {
//                    final Place placeIterator = (Place)iterator.next();
//                    databaseReference.child("Added_Places").orderByChild("placeId").equalTo(placeIterator.getId()).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
//                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
//                                if(dataSnapshot3.getValue(AddedPlace.class).getUserId().equals(userLoggedInId)) {
//                                    places1.remove(places1.indexOf(placeIterator));
//                                }
//                            }
//                            if(!iterator.hasNext()) {
//                                homeAllPlaceAdapter = new HomeAllPlaceAdapter(getContext());
//                                homeAllPlaceAdapter.setPlaces(places1);
//                                recyclerView.setAdapter(homeAllPlaceAdapter);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) { }
//                    });
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) { }
//        });
