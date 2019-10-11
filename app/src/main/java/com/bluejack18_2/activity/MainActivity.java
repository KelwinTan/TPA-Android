package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Debug;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editUsername, editEmail, editPassword, editConfirmationPassword;
    private DatabaseReference databaseReference;
    private Button btnRegister, btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editConfirmationPassword = findViewById(R.id.edit_confirmation_password);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        btnRegister = findViewById(R.id.btn_register);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnRegister.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
    }

    public void addUser(final User user){
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isValid = true;
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    if(dataSnapshot1.getValue(User.class).getEmail().equals(user.getEmail())) {
                        isValid = false;
                        Toast.makeText(MainActivity.this, "Email has been used", Toast.LENGTH_SHORT).show();
                    }
                }
                if(isValid) {
                    databaseReference.child("Users").push().setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "User created successfully!!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                btnRegister.setEnabled(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                final String username = editUsername.getText().toString();
                final String email = editEmail.getText().toString();
                final String password = editPassword.getText().toString();
                final String confirmationPassword = editConfirmationPassword.getText().toString();

                if(username.isEmpty()) {
                    Toast.makeText(this, "Username must be filled", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()) {
                    Toast.makeText(this, "Email must be filled", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()){
                    Toast.makeText(this, "Password must be filled", Toast.LENGTH_SHORT).show();
                } else if(confirmationPassword.isEmpty()) {
                    Toast.makeText(this, "Confirmation Password must be filled", Toast.LENGTH_SHORT).show();
                } else if(!password.equals(confirmationPassword)){
                    Toast.makeText(this, "Confirmation Password do not match", Toast.LENGTH_SHORT).show();
                } else {
                    btnRegister.setEnabled(false);
                    User user = new User(username, email, password);
                    user.setImageUrl("");
                    addUser(user);
                }
                break;
            case R.id.btn_sign_in:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }


}
