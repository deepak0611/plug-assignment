package com.example.plug_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.plug_assignment.RandomUser.RandomUser;
import com.example.plug_assignment.RetroFit.ApiInterface;
import com.example.plug_assignment.RetroFit.Retrofit;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";

    private int RC_SIGN_IN = 0611 ;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    ApiInterface apiInterface;
    RandomUser anonymousUser;
    User newUser;
    Dialog loadingDialog;


    private boolean isAllPermissionAllowed = false;
    private static final int PERMISSION_CODE = 101;
    private String[] mPermissionArray = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_login);

        loadingDialog = new Dialog(LoginActivity.this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if user is already logged in then move to home screen
        if(currentUser!=null){
            moveToHomePage();
        }

        if(!isAllPermissionAllowed){
            ActivityCompat.requestPermissions(this,mPermissionArray,PERMISSION_CODE);
        }


        //else signup here
        googleAuthInitialize();
        AnonymousAuthInitialize();

        findViewById(R.id.btn_google_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
        findViewById(R.id.btn_anonymous_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInAnonymously();
            }
        });

    }

    public void AnonymousAuthInitialize(){
        apiInterface = Retrofit.getClient().create(ApiInterface.class);
        Call<RandomUser> call = apiInterface.getTodos();
        call.enqueue(new Callback<RandomUser>() {
            @Override
            public void onResponse(Call<RandomUser> call, Response<RandomUser> response) {
                Log.e(TAG, "onResponse: "+ response.body() );
                anonymousUser = response.body();
            }

            @Override
            public void onFailure(Call<RandomUser> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });

    }
    public void signInAnonymously(){
        showLoadingDiaolog();
        String mEmail = anonymousUser.getResults().get(0).getEmail() ;
        String mFullName = anonymousUser.getResults().get(0).getName().getFullName();
        String mPhotoUrl = anonymousUser.getResults().get(0).getPicture().getMedium();
        mAuth.createUserWithEmailAndPassword(mEmail, "meAnonymousUser")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            newUser = new User(mFullName,mEmail,mPhotoUrl);
                            Log.e(TAG, "onComplete: " + newUser );

//                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users") ;
//                            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newUser);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
//                                        Toast.makeText(LoginActivity.this, "data inserted succesfully", Toast.LENGTH_SHORT).show();
                                        saveStringInSharedPreference("signin","anonymous");
                                        Toast.makeText(LoginActivity.this, "Anonymous signup successful", Toast.LENGTH_SHORT).show();
                                        moveToHomePage();
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "Failed to signup", Toast.LENGTH_SHORT).show();
                                        hideLoadingDialog();
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Failed to signup", Toast.LENGTH_SHORT).show();
                            hideLoadingDialog();
                        }
                    }
                });
    }


    public void googleAuthInitialize(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Exception exception = task.getException();
            if(task.isSuccessful()){
                try {
                    showLoadingDiaolog();
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                }
            }
            else{
                Log.w(TAG, exception.toString());
            }

        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            newUser = new User(currentUser.getDisplayName(),currentUser.getEmail(),currentUser.getPhotoUrl().toString());

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        saveStringInSharedPreference("signin","google");
                                        moveToHomePage();
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "Failed to signup", Toast.LENGTH_SHORT).show();
                                        hideLoadingDialog();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to signup", Toast.LENGTH_SHORT).show();
                            hideLoadingDialog();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            for(int grantResult : grantResults){
                isAllPermissionAllowed = (grantResult == PackageManager.PERMISSION_GRANTED) ;
            }
            if(!isAllPermissionAllowed){
                Toast.makeText(this, "App will not work without permissions.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void showLoadingDiaolog(){
        loadingDialog.show();
    }
    public void hideLoadingDialog() {loadingDialog.dismiss();}
    public void moveToHomePage(){
        Intent i = new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(i);
        finish();
    }
    public void saveStringInSharedPreference(String key, String value){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPrefs",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(key,value);
        myEdit.commit();
    }
    public String getStringFromSharedPreference(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPrefs",MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
}

