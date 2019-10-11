package com.bluejack18_2.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.service.media.MediaBrowserService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.activity.AddReportActivity;
import com.bluejack18_2.activity.PlaceAddedDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int REQUEST_CAMERA_CODE = 300;
    private static final int REQUEST_WRITE_ES_CODE = 400;
    private ImageView imageView;
    private Button btnTakePhoto, btnChooseFromGallery, btnMakeReport;
    private Uri selectedImage;
    private String imageFilePath;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        imageView = view.findViewById(R.id.image_view);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        btnChooseFromGallery = view.findViewById(R.id.btn_choose_from_gallery);
        btnMakeReport = view.findViewById(R.id.btn_make_report);

        btnTakePhoto.setOnClickListener(this);
        btnChooseFromGallery.setOnClickListener(this);
        btnMakeReport.setOnClickListener(this);

        return view;
    }

    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
        } else if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_ES_CODE);
        } else {
            cameraIntent();
        }
    }

    public void cameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = null;
        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(imageFile != null) {
            Uri photoUri = FileProvider.getUriForFile(getContext(), "com.bluejack18_2.fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void chooseFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg","image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "SELECT PICTURE"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_ES_CODE);
                    } else {
                         cameraIntent();
                    }
                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if(!shouldShowRequestPermissionRationale(permissions[0])){
                        Toast.makeText(getActivity(), "You have forcefully denied some of the required permissions for this action. Please open settings, go to permissions and allow them.", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_WRITE_ES_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "GRANTED", Toast.LENGTH_LONG).show();
                    cameraIntent();
                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if(!shouldShowRequestPermissionRationale(permissions[0])){
                        Toast.makeText(getActivity(), "You have forcefully denied some of the required permissions for this action. Please open settings, go to permissions and allow them.", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CAMERA_REQUEST_CODE) {
                selectedImage = Uri.parse(imageFilePath);
            } else if(requestCode == GALLERY_REQUEST_CODE) {
                selectedImage = data.getData();
            }

            Uri loadedImage;
            if(selectedImage.toString().startsWith("content://")) {
                loadedImage = selectedImage;
            } else {
                loadedImage = Uri.parse("file://" + selectedImage);
            }
            Picasso.get().load(loadedImage).resize(350,250).rotate(getOrientation(getContext(), loadedImage)).centerCrop().into(imageView);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_photo:
                checkPermission();
                break;
            case R.id.btn_choose_from_gallery:
                chooseFromGallery();
                break;
            case R.id.btn_make_report:
                if(selectedImage == null) {
                    Toast.makeText(getActivity(), "Image must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getContext(), AddReportActivity.class);
                    intent.putExtra("Report", selectedImage);
                    getContext().startActivity(intent);
                }
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

//            imageView.setImageURI(selectedImage);