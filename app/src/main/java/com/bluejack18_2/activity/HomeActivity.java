package com.bluejack18_2.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bluejack18_2.R;
import com.bluejack18_2.fragment.CameraFragment;
import com.bluejack18_2.fragment.CreateFragment;
import com.bluejack18_2.fragment.EditProfileFragment;
import com.bluejack18_2.fragment.HomeFragment;
import com.bluejack18_2.fragment.MyPlaceFragment;
import com.bluejack18_2.fragment.MyReportFragment;
import com.bluejack18_2.model.MySingleton;
import com.bluejack18_2.model.User;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String userLoggedInId;
    private ImageView imageView;
    private TextView textUsername, textEmail;
    private DatabaseReference databaseReference;

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_home, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userLoggedInId = sharedPreferences.getString("Id", null);

        NavigationView navigationView = findViewById(R.id.nav_view);
        imageView = navigationView.getHeaderView(0).findViewById(R.id.image_view);
        textUsername = navigationView.getHeaderView(0).findViewById(R.id.text_username);
        textEmail = navigationView.getHeaderView(0).findViewById(R.id.text_email);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").orderByKey().equalTo(userLoggedInId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    User user = dataSnapshot1.getValue(User.class);
                    if(!user.getImageUrl().equals("")) {
                        Picasso.get().load(user.getImageUrl()).resize(100,100).centerCrop().into(imageView);
                    }
                    textUsername.setText(user.getUsername());
                    textEmail.setText(user.getEmail());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.nav_profile) {
                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    replaceFragment(editProfileFragment);
                } else if (id == R.id.nav_logout) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(userLoggedInId);
                    editor.clear();
                    editor.commit();
                    if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                    }
                    finish();
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.bnav_home) {
                    HomeFragment homeFragment = new HomeFragment();
                    replaceFragment(homeFragment);
                } else if (id == R.id.bnav_create) {
                    CreateFragment createFragment = new CreateFragment();
                    replaceFragment(createFragment);
                } else if (id == R.id.bnav_camera) {
                    CameraFragment cameraFragment = new CameraFragment();
                    replaceFragment(cameraFragment);
                } else if (id == R.id.bnav_my_place) {
                    MyPlaceFragment myPlaceFragment = new MyPlaceFragment();
                    replaceFragment(myPlaceFragment);
                } else if (id == R.id.bnav_my_report) {
                    MyReportFragment myReportFragment = new MyReportFragment();
                    replaceFragment(myReportFragment);
                }
                return true;
            }
        });

        if(getIntent().hasExtra("LOAD_FRAGMENT")) {
            switch (getIntent().getStringExtra("LOAD_FRAGMENT")) {
                case "My_Place_Fragment":
                    MyPlaceFragment myPlaceFragment = new MyPlaceFragment();
                    replaceFragment(myPlaceFragment);
                    bottomNavigationView.setSelectedItemId(R.id.bnav_my_place);
                    break;
            }
        } else {
            HomeFragment homeFragment = new HomeFragment();
            replaceFragment(homeFragment);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
