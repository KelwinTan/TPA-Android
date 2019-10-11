package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bluejack18_2.R;
import com.bluejack18_2.adapter.CommentAdapter;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Comment;
import com.bluejack18_2.model.MySingleton;
import com.bluejack18_2.model.Place;
import com.bluejack18_2.model.Report;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ReportDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private String reportId;
    private String reportUserId;

    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userLoggedInId;
    private StorageReference storageReference;

    private ImageView imageView;
    private TextView textReportTitle, textReportDescription, textReportStatus;
    private Spinner spinnerReportStatus;
    private boolean initialDisplay;
    private Button btnRemoveReport;
    private Button btnAddComment;
    private EditText commentText;
    private ArrayList<Comment> comments;
    private RecyclerView recyclerView;

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notification, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) { }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=AAAAwmvIl1c:APA91bEic61KX5Ax-WtM5mnZJ0K34Y9N_u2TY9j11mIq8cemd2Iohx6M685RekC4F_S_rMi93OuDM8iiG51V3iM-tsfoOP70EGuJJclXfkwC7Mv1IpCGsJHbCEMUMK_WjRM-EZgoWUrD");
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        comments = new ArrayList<>();
        reportId = ((Report)getIntent().getParcelableExtra("My_Report")).getId();
        reportUserId = ((Report)getIntent().getParcelableExtra("My_Report")).getUserId();

        sharedPreferences = getBaseContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        imageView = findViewById(R.id.image_view);
        textReportTitle = findViewById(R.id.text_report_title);
        textReportDescription = findViewById(R.id.text_report_description);
        textReportStatus = findViewById(R.id.text_report_status);
        spinnerReportStatus = findViewById(R.id.spinner_report_status);
        btnRemoveReport = findViewById(R.id.btn_remove_report);
        btnAddComment = findViewById(R.id.btn_add_comment);
        commentText = findViewById(R.id.comment);

        recyclerView = findViewById(R.id.rv_comments);

        final ArrayAdapter<String> reportStatusAdapter = new ArrayAdapter<String>(ReportDetailActivity.this, R.layout.support_simple_spinner_dropdown_item, new ArrayList<String>(Arrays.asList("Waiting", "On Progress", "Completed", "Locked")));
        spinnerReportStatus.setAdapter(reportStatusAdapter);

        if(reportUserId.equals(userLoggedInId)) {
            btnRemoveReport.setVisibility(View.VISIBLE);
            btnAddComment.setVisibility(View.VISIBLE);
        }
        btnRemoveReport.setOnClickListener(this);
        btnAddComment.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Comment").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                        final String id = Objects.requireNonNull(dataSnapshot1.child("id").getValue()).toString();
                        final String reportId = Objects.requireNonNull(dataSnapshot1.child("reportId").getValue()).toString();
                        final String userId = Objects.requireNonNull(dataSnapshot1.child("userId").getValue()).toString();
                        final String content = Objects.requireNonNull(dataSnapshot1.child("content").getValue()).toString();

                        databaseReference.child("Users").child(userId).child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String username = String.valueOf(dataSnapshot.getValue());

                                Comment com = new Comment(id,reportId,userId,content,username);
                                if(com.getReportId().equals(reportId)){
                                    comments.add(com);
                                }

                                CommentAdapter commentAdapter = new CommentAdapter(getApplicationContext(), comments);
                                recyclerView.setAdapter(commentAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        databaseReference.child("Report").orderByKey().equalTo(reportId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    Report report = dataSnapshot1.getValue(Report.class);
                    Picasso.get().load(report.getImageUrl()).resize(350,250).centerCrop().into(imageView);
                    textReportTitle.setText(report.getReportTitle());
                    String temp = "Description: " + report.getReportDescription(); textReportDescription.setText(temp);
                    temp = "Status: " + report.getReportStatus(); textReportStatus.setText(temp);
                    if(report.getReportStatus().equals("Waiting")) {
                        spinnerReportStatus.setSelection(0);
                    } else if(report.getReportStatus().equals("On Progress")) {
                        spinnerReportStatus.setSelection(1);
                    } else if(report.getReportStatus().equals("Completed")) {
                        spinnerReportStatus.setSelection(2);
                    } else {
                        spinnerReportStatus.setSelection(3);
                    }

                    databaseReference.child("Comment").orderByKey().equalTo(report.getPlaceId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.wtf("Test", "Out");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    databaseReference.child("Places").orderByKey().equalTo(report.getPlaceId()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                Place place = dataSnapshot3.getValue(Place.class);
                                databaseReference.child("Added_Places").orderByChild("placeId").equalTo(dataSnapshot3.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot4) {
                                        textReportStatus.setVisibility(View.GONE);
                                        spinnerReportStatus.setVisibility(View.GONE);
                                        for(DataSnapshot dataSnapshot5: dataSnapshot4.getChildren()) {
                                            AddedPlace addedPlace = dataSnapshot5.getValue(AddedPlace.class);
                                            if(addedPlace.getUserId().equals(userLoggedInId)) {
                                                if(addedPlace.getRoleName().equals("Guest")) {
                                                    textReportStatus.setVisibility(View.VISIBLE);
                                                } else {
                                                    spinnerReportStatus.setVisibility(View.VISIBLE);
                                                }
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

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        initialDisplay = true;
        spinnerReportStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(initialDisplay) {
                    initialDisplay = false;
                } else {
                    Map<String, Object> updates = new HashMap<String, Object>();
                    updates.put("reportStatus", spinnerReportStatus.getSelectedItem().toString());

                    databaseReference.child("Report").child(reportId).updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            databaseReference.child("Report").orderByKey().equalTo(reportId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                        JSONObject notification = new JSONObject();
                                        JSONObject notificationBody = new JSONObject();
                                        try {
                                            notificationBody.put("title", "Report Status Changed");
                                            notificationBody.put("message", "Your Report: " + textReportTitle.getText().toString() + " status has been changed to " + spinnerReportStatus.getSelectedItem().toString());
                                            notificationBody.put("reportId", dataSnapshot1.getKey());
                                            notificationBody.put("imageUrl", dataSnapshot1.getValue(Report.class).getImageUrl());
                                            notificationBody.put("placeId", dataSnapshot1.getValue(Report.class).getPlaceId());
                                            notificationBody.put("reportDescription", dataSnapshot1.getValue(Report.class).getReportDescription());
                                            notificationBody.put("reportStatus", dataSnapshot1.getValue(Report.class).getReportStatus());
                                            notificationBody.put("reportTitle", dataSnapshot1.getValue(Report.class).getReportTitle());
                                            notificationBody.put("userId", dataSnapshot1.getValue(Report.class).getUserId());
                                            notification.put("to", "/topics/" + reportUserId);
                                            notification.put("data", notificationBody);
                                        } catch (JSONException e) { }
                                        sendNotification(notification);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                            Toast.makeText(ReportDetailActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_remove_report:
                btnRemoveReport.setEnabled(false);
                databaseReference.child("Report").orderByKey().equalTo(reportId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            dataSnapshot1.getRef().removeValue();
                            Toast.makeText(getBaseContext(), "Remove Success", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                break;
            case R.id.btn_add_comment:
                final String context = commentText.getText().toString();
                final String userId = userLoggedInId;
                final String insertReportId = reportId;
                if(context.isEmpty()){
                    Toast.makeText(this, "Comment must be filled", Toast.LENGTH_SHORT).show();
                }else{
                    btnAddComment.setEnabled(false);
                    Comment comment = new Comment(UUID.randomUUID().toString(), insertReportId, userId, context,"");
                    databaseReference.child("Comment").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ReportDetailActivity.this, "Comment Inserted", Toast.LENGTH_SHORT).show();
                            btnAddComment.setEnabled(true);
                        }
                    });

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
