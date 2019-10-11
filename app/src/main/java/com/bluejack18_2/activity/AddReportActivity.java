package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.model.AddedPlace;
import com.bluejack18_2.model.Place;
import com.bluejack18_2.model.Report;
import com.bluejack18_2.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

public class AddReportActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private EditText editReportTitle, editReportDescription;
    private Spinner reportLocationSpinner;
    private Button btnInsertReport;
    private DatabaseReference databaseReference;
    private Uri selectedImage;
    private SharedPreferences sharedPreferences;
    private final ArrayList<AddedPlace> addedPlaces = new ArrayList<>();
    private String userLoggedInId;
    private StorageReference storageReference;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rootLayout = findViewById(R.id.root_layout);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootLayout.addView(progressBar, params);

        imageView = findViewById(R.id.image_view);
        editReportTitle = findViewById(R.id.edit_report_title);
        editReportDescription = findViewById(R.id.edit_report_description);
        reportLocationSpinner = findViewById(R.id.spinner_report_location);
        btnInsertReport = findViewById(R.id.btn_insert_report);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        selectedImage = getIntent().getParcelableExtra("Report");
        if(selectedImage.toString().startsWith("content://")) {
            Picasso.get().load(selectedImage).resize(350, 250).rotate(getOrientation(this, selectedImage)).centerCrop().into(imageView);
        } else {
            selectedImage = Uri.parse("file://" + selectedImage);
            Picasso.get().load(selectedImage).resize(350, 250).rotate(getOrientation(this, selectedImage)).centerCrop().into(imageView);
        }



        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);

        final ArrayList<String> items = new ArrayList<>();
        databaseReference.child("Added_Places").orderByChild("userId").equalTo(userLoggedInId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addedPlaces.clear();
                for(final DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    databaseReference.child("Places").orderByKey().equalTo(dataSnapshot1.getValue(AddedPlace.class).getPlaceId()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for(DataSnapshot dataSnapshot3: dataSnapshot2.getChildren()) {
                                String placeName = dataSnapshot3.getValue(Place.class).getPlaceName();
                                AddedPlace addedPlace = dataSnapshot1.getValue(AddedPlace.class);
                                addedPlace.setId(dataSnapshot1.getKey());
                                addedPlace.setPlaceName(placeName);
                                addedPlaces.add(addedPlace);
                            }
                            items.clear();
                            items.add("Choose Location to Insert:");
                            for(int i=0; i<addedPlaces.size(); i++) {
                                items.add(addedPlaces.get(i).getPlaceName());
                            }
                            ArrayAdapter<String> reportLocationAdapter = new ArrayAdapter<String>(AddReportActivity.this, R.layout.support_simple_spinner_dropdown_item, items);
                            reportLocationSpinner.setAdapter(reportLocationAdapter);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                if(dataSnapshot.getChildrenCount() == 0) {
                    items.add("Choose Location to Insert:");
                    ArrayAdapter<String> reportLocationAdapter = new ArrayAdapter<String>(AddReportActivity.this, R.layout.support_simple_spinner_dropdown_item, items);
                    reportLocationSpinner.setAdapter(reportLocationAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        btnInsertReport.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_insert_report:
                final String reportTitle = editReportTitle.getText().toString();
                final String reportDescription = editReportDescription.getText().toString();
                final String reportLocation = reportLocationSpinner.getSelectedItem().toString();

                if(reportTitle.isEmpty()) {
                    Toast.makeText(this, "Report Title must be filled", Toast.LENGTH_SHORT).show();
                } else if(reportDescription.isEmpty()) {
                    Toast.makeText(this, "Report Description must be filled", Toast.LENGTH_SHORT).show();
                } else if(reportLocation.equals("Choose Location to Insert:")) {
                    Toast.makeText(this, "Report Location must be chosen", Toast.LENGTH_SHORT).show();
                } else {
                    btnInsertReport.setEnabled(false);

                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    final StorageReference ref = storageReference.child("Images").child(UUID.randomUUID().toString());
                    ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    int spinnerIndex = reportLocationSpinner.getSelectedItemPosition() - 1;
                                    Report report = new Report(addedPlaces.get(spinnerIndex).getPlaceId(), userLoggedInId, uri.toString(), reportTitle, reportDescription, "Waiting");
                                    databaseReference.child("Report").push().setValue(report).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(AddReportActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            finish();
                                        }
                                    });
                                }
                            });
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

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
        int result = -1;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }
        return result;
    }
}
