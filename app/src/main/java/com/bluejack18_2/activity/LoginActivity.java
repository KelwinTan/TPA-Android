package com.bluejack18_2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluejack18_2.R;
import com.bluejack18_2.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editEmail, editPassword;
    private DatabaseReference databaseReference;
    private Button btnLogin, btnRegister;
    private SignInButton btnGoogleSignIn;
    private LoginButton btnFacebookSignIn;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageView imageViewLogo;
    private static final int GOOGLE_SIGN_IN_CODE = 9001;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;
    private String facebookEmail;
    private String googleEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rootLayout = findViewById(R.id.root_layout);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rootLayout.addView(progressBar, params);

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnFacebookSignIn = findViewById(R.id.btn_facebook_sign_in);
        btnFacebookSignIn.setPermissions(Arrays.asList("email","public_profile","user_friends"));
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnGoogleSignIn.setOnClickListener(this);
        btnFacebookSignIn.setOnClickListener(this);
        imageViewLogo = findViewById(R.id.image_view_logo);
        Picasso.get().load(R.drawable.dispatch).into(imageViewLogo);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        callbackManager = CallbackManager.Factory.create();
        btnFacebookSignIn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() { }
            @Override
            public void onError(FacebookException error) {
                Log.d("DEBUG", error.toString());
            }
        });

        boolean isLogged = sharedPreferences.contains("Id");
        if(isLogged) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void insertIntoEditor(String id, String email, String username, String password, String imageUrl) {
        editor.putString("Id", id);
        editor.putString("Email", email);
        editor.putString("Username", username);
        editor.putString("Password", password);
        editor.putString("ImageUrl", imageUrl);
        editor.commit();
        FirebaseMessaging.getInstance().subscribeToTopic(id);
    }

    public void authenticate(String email, final String password){
        databaseReference.child("Users").orderByChild("email").equalTo(email).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Incorrect Username/Password", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    closeProgressBar(); Log.d("DEBUG", "CLOSE1");
                }
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue(User.class).getPassword().equals(password)){
                        User user = snapshot.getValue(User.class);
                        insertIntoEditor(snapshot.getKey(), user.getEmail(), user.getUsername(), user.getPassword(), user.getImageUrl());
                        closeProgressBar(); Log.d("DEBUG", "CLOSE6"); redirectToHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Incorrect Username/Password", Toast.LENGTH_SHORT).show();
                        closeProgressBar(); Log.d("DEBUG", "CLOSE2");
                    }
                    btnLogin.setEnabled(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                final String email = editEmail.getText().toString();
                final String password = editPassword.getText().toString();
                if(email.isEmpty()) {
                    Toast.makeText(this, "Email must be filled", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()){
                    Toast.makeText(this, "Password must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    btnLogin.setEnabled(false);
                    authenticate(email, password);
                }
                break;
            case R.id.btn_register:
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_google_sign_in:
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(googleSignInAccount);
            } catch (ApiException e) { closeProgressBar(); Log.d("DEBUG", "CLOSE3"); }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount){
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    List<? extends UserInfo> providerData = firebaseUser.getProviderData();
                    for(final UserInfo userInfo: providerData) {
                        if(userInfo.getProviderId().equals("google.com")) {
                            googleEmail = userInfo.getEmail();
                            databaseReference.child("Users").orderByChild("email").equalTo(googleEmail).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                            if(firebaseUser.getPhotoUrl() != null) {
                                                insertIntoEditor(dataSnapshot1.getKey(), googleEmail, userInfo.getDisplayName(), "", userInfo.getPhotoUrl().toString());
                                            } else {
                                                insertIntoEditor(dataSnapshot.getKey(), googleEmail, userInfo.getDisplayName(), "", "");
                                            }
                                            closeProgressBar(); Log.d("DEBUG", "CLOSE7"); redirectToHome();
                                        }
                                    } else {
                                        final User user = new User(firebaseUser.getDisplayName(), googleEmail, "");
                                        if(firebaseUser.getPhotoUrl() != null) { user.setImageUrl(firebaseUser.getPhotoUrl().toString());
                                        } else { user.setImageUrl(""); }

                                        databaseReference.child("Users").child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                insertIntoEditor(firebaseUser.getUid(), user.getEmail(), user.getUsername(), user.getPassword(), user.getImageUrl());
                                                closeProgressBar(); Log.d("DEBUG", "CLOSE8"); redirectToHome();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }

                }
            }
        });
    }

    public void firebaseAuthWithFacebook(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    List<? extends UserInfo> providerData = firebaseUser.getProviderData();
                    for(UserInfo userInfo: providerData) {
                        if(userInfo.getProviderId().equals("facebook.com")) {
                            facebookEmail = userInfo.getEmail();
                            databaseReference.child("Users").orderByChild("email").equalTo(facebookEmail).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                            if(firebaseUser.getPhotoUrl() != null) {
                                                insertIntoEditor(dataSnapshot1.getKey(), facebookEmail, firebaseUser.getDisplayName(), "", firebaseUser.getPhotoUrl().toString());
                                            } else {
                                                insertIntoEditor(dataSnapshot1.getKey(), facebookEmail, firebaseUser.getDisplayName(), "", "");
                                            }
                                            closeProgressBar(); Log.d("DEBUG", "CLOSE9"); redirectToHome();
                                        }
                                    } else {
                                        final User user = new User(firebaseUser.getDisplayName(), facebookEmail, "");
                                        if(firebaseUser.getPhotoUrl() != null) { user.setImageUrl(firebaseUser.getPhotoUrl().toString());
                                        } else { user.setImageUrl(""); }

                                        databaseReference.child("Users").child(firebaseUser.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                insertIntoEditor(firebaseUser.getUid(), user.getEmail(), user.getUsername(), user.getPassword(), user.getImageUrl());
                                                closeProgressBar(); Log.d("DEBUG", "CLOSE10"); redirectToHome();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }

                } else { Log.d("DEBUG", "FAILED"); }
            }
        });
    }

    private void closeProgressBar() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void redirectToHome(){
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}
