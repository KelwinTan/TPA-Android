package com.bluejack18_2.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.HomeActivity;
import com.bluejack18_2.model.Place;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment implements View.OnClickListener{

    private static final int GALLERY_REQUEST_CODE = 100;
    private ImageView imageView;
    private EditText editPlaceName, editPlaceDescription, editGuestCode, editAdminCode;
    private Button btnAddPlace;
    private Uri selectedImage;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;

    public CreateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        editPlaceName = view.findViewById(R.id.edit_place_name);
        editPlaceDescription = view.findViewById(R.id.edit_place_description);
        editGuestCode = view.findViewById(R.id.edit_guest_code);
        editAdminCode = view.findViewById(R.id.edit_admin_code);
        btnAddPlace = view.findViewById(R.id.btn_add_place);

        rootLayout = view.findViewById(R.id.root_layout);
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootLayout.addView(progressBar, params);

        imageView = view.findViewById(R.id.image_view);
        imageView.setOnClickListener(this);
        btnAddPlace.setOnClickListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        return view;
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg","image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "SELECT PICTURE"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    selectedImage = data.getData();
                    Picasso.get().load(selectedImage).resize(350,200).rotate(getOrientation(getContext(), selectedImage)).centerCrop().into(imageView);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view:
                pickFromGallery();
                break;
            case R.id.btn_add_place:
                final String placeName = editPlaceName.getText().toString();
                final String placeDescription = editPlaceDescription.getText().toString();
                final String guestCode = editGuestCode.getText().toString();
                final String adminCode = editAdminCode.getText().toString();

                if(selectedImage == null) {
                    Toast.makeText(getActivity(), "Image must be filled", Toast.LENGTH_SHORT).show();
                } else if(placeDescription.isEmpty()){
                    Toast.makeText(getActivity(), "Place Description must be filled", Toast.LENGTH_SHORT).show();
                } else if(placeName.isEmpty()){
                    Toast.makeText(getActivity(), "Place Name must be filled", Toast.LENGTH_SHORT).show();
                } else if(guestCode.isEmpty()) {
                    Toast.makeText(getActivity(), "Guest Code must be filled", Toast.LENGTH_SHORT).show();
                } else if(adminCode.isEmpty()) {
                    Toast.makeText(getActivity(), "Admin Code must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    final StorageReference ref = storageReference.child("Images").child(UUID.randomUUID().toString());
                    ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Place place = new Place(uri.toString(), placeName, placeDescription, guestCode, adminCode);
                                    databaseReference.child("Places").push().setValue(place).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "SUCCESS UPLOAD", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
