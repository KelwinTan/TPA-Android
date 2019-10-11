package com.bluejack18_2.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.AddReportActivity;
import com.bluejack18_2.activity.HomeActivity;
import com.bluejack18_2.activity.LoginActivity;
import com.bluejack18_2.model.Report;
import com.bluejack18_2.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener {

    private static final int GALLERY_REQUEST_CODE = 100;
    private CircleImageView defaultImageView, circleImageView;
    private EditText editUsername, editOldPassword, editNewPassword, editConfirmNewPassword;
    private Button btnSaveChanges, btnConfirmNewPassword, btnSelectPicture;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String userLoggedInId;
    private TextView textUsername;
    private Uri selectedImage;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;

    public EditProfileFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        rootLayout = view.findViewById(R.id.root_layout);
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootLayout.addView(progressBar, params);

        defaultImageView = view.findViewById(R.id.default_image_view);
        circleImageView = view.findViewById(R.id.circle_image_view);
        editUsername = view.findViewById(R.id.edit_username);
        editOldPassword = view.findViewById(R.id.edit_old_password);
        editNewPassword = view.findViewById(R.id.edit_new_password);
        editConfirmNewPassword = view.findViewById(R.id.edit_confirm_new_password);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnConfirmNewPassword = view.findViewById(R.id.btn_confirm_new_password);
        btnSelectPicture = view.findViewById(R.id.btn_select_picture);

        sharedPreferences = getContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        userLoggedInId = sharedPreferences.getString("Id", null);
        editor = sharedPreferences.edit();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference.child("Users").orderByKey().equalTo(userLoggedInId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    User user = dataSnapshot1.getValue(User.class);
                    if(!user.getImageUrl().equals("")) {
                        defaultImageView.setVisibility(View.GONE);
                        circleImageView.setVisibility(View.VISIBLE);
                        Picasso.get().load(dataSnapshot1.getValue(User.class).getImageUrl()).resize(150,150)
                                .centerCrop().into(circleImageView);
                    } else {
                        defaultImageView.setVisibility(View.VISIBLE);
                        circleImageView.setVisibility(View.GONE);
                    }
                    editUsername.setText(dataSnapshot1.getValue(User.class).getUsername());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        btnSaveChanges.setOnClickListener(this);
        btnConfirmNewPassword.setOnClickListener(this);
        btnSelectPicture.setOnClickListener(this);
        return view;
    }

    public void chooseFromGallery(){
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
            if(requestCode == GALLERY_REQUEST_CODE) {
                selectedImage = data.getData();
            }
            circleImageView.setVisibility(View.VISIBLE);
            defaultImageView.setVisibility(View.GONE);

            Picasso.get().load(selectedImage).resize(150,150)
                    .rotate(getOrientation(getContext(), selectedImage)).centerCrop().into(circleImageView);
        }
    }

    public void updateProfile(final String username, final Uri selectedImage, final String oldPassword) {
        databaseReference.child("Users").orderByKey().equalTo(userLoggedInId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if(oldPassword.equals(dataSnapshot1.getValue(User.class).getPassword())) {
                        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                progressBar.setVisibility(View.VISIBLE);
                                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                editOldPassword.setText("");
                                final Map<String, Object> updates = new HashMap<String, Object>();

                                if(selectedImage != null) {
                                    final StorageReference ref = storageReference.child("Images").child(UUID.randomUUID().toString());
                                    ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    updates.put("username", username);
                                                    updates.put("imageUrl", uri.toString());
                                                    databaseReference.child("Users").child(userLoggedInId).updateChildren(updates);
                                                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                                    editor.putString("Username", username);
                                                    editor.putString("ImageUrl", uri.toString());
                                                    editor.commit();
                                                    progressBar.setVisibility(View.GONE);
                                                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    updates.put("username", username);
                                    databaseReference.child("Users").child(userLoggedInId).updateChildren(updates);
                                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                    editor.putString("Username", username);
                                    editor.commit();
                                    progressBar.setVisibility(View.GONE);
                                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    } else {
                        Toast.makeText(getContext(), "Old Password do not match!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void changePassword(final String oldPassword, final String newPassword, final String confirmNewPassword){
        databaseReference.child("Users").orderByKey().equalTo(userLoggedInId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if (oldPassword.equals(dataSnapshot1.getValue(User.class).getPassword())) {
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("password", newPassword);
                        databaseReference.child("Users").child(userLoggedInId).updateChildren(updates);
                        Toast.makeText(getContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        editOldPassword.setText(""); editNewPassword.setText(""); editConfirmNewPassword.setText("");
                    } else {
                        Toast.makeText(getContext(), "Old Password do not match!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onClick(View view) {
        final String username = editUsername.getText().toString();
        final String oldPassword = editOldPassword.getText().toString();
        final String newPassword = editNewPassword.getText().toString();
        final String confirmNewPassword = editConfirmNewPassword.getText().toString();

        switch (view.getId()) {
            case R.id.btn_save_changes:
                if(username.isEmpty()) {
                    Toast.makeText(getContext(), "Username cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if(oldPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill your Old Password!", Toast.LENGTH_SHORT).show();
                } else {
                    updateProfile(username, selectedImage, oldPassword);
                }
                break;
            case R.id.btn_confirm_new_password:
                if(oldPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Old Password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if(newPassword.isEmpty()) {
                    Toast.makeText(getContext(), "New Password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if(confirmNewPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Confirmation Password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if(!confirmNewPassword.equals(newPassword)) {
                    Toast.makeText(getContext(), "Confirmation Password do not match new password!", Toast.LENGTH_SHORT).show();
                } else {
                    changePassword(oldPassword, newPassword, confirmNewPassword);
                }
                break;
            case R.id.btn_select_picture:
                chooseFromGallery();
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