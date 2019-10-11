package com.bluejack18_2.fragment;


import android.content.Context;
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
import com.bluejack18_2.adapter.HomeAddedPlaceAdapter;
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
public class AddedFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAddedPlaceAdapter homeAddedPlaceAdapter;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    public AddedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_added, container, false);

        final ArrayList<Place> places = new ArrayList<>();

        recyclerView = view.findViewById(R.id.rv_home_added);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String userLoggedInId = sharedPreferences.getString("Id", null);

        databaseReference.child("Added_Places").orderByChild("userId").equalTo(userLoggedInId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                homeAddedPlaceAdapter = new HomeAddedPlaceAdapter(getContext());
                places.clear();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    databaseReference.child("Places").orderByKey().equalTo(dataSnapshot1.getValue(AddedPlace.class).getPlaceId()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for (DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                Place place = dataSnapshot3.getValue(Place.class);
                                place.setId(dataSnapshot3.getKey());
                                places.add(place);
                            }
                            homeAddedPlaceAdapter.setPlaces(places);
                            recyclerView.setAdapter(homeAddedPlaceAdapter);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                if(dataSnapshot.getChildrenCount() == 0) {
                    homeAddedPlaceAdapter.setPlaces(places);
                    recyclerView.setAdapter(homeAddedPlaceAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;
    }

}