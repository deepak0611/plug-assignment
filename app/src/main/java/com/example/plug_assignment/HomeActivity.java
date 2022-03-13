package com.example.plug_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<UserRoom> userRoomList;
    User userLoggedIn;
    String mUserId;
    ImageView IV_user_photo ;

    TextView TV_name ;
    TextView TV_email;
    Dialog pleaseWaitDialog,loadingDialog;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            moveToLoginPage();
        }
        mUserId = currentUser.getUid();
        loadingDialog = new Dialog(HomeActivity.this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        pleaseWaitDialog = new Dialog(HomeActivity.this);
        pleaseWaitDialog.setContentView(R.layout.dialog_please_wait);
        pleaseWaitDialog.setCancelable(false);
        pleaseWaitDialog.findViewById(R.id.TVcancelSDP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSDP();
                pleaseWaitDialog.dismiss();
            }
        });

        IV_user_photo = findViewById(R.id.IV_user_photo);

        TV_name = findViewById(R.id.TV_name);
        TV_email = findViewById(R.id.TV_email);
        TextView sign_out = findViewById(R.id.sign_out);
        CardView create_sdp = findViewById(R.id.create_sdp);



        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getStringFromSharedPreference("signin").equalsIgnoreCase("anonymous")){
                    loadingDialog.show();
                    if(currentUser!=null) {
                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(HomeActivity.this, "Anonymous User deleted", Toast.LENGTH_SHORT).show();
                                    moveToLoginPage();
                                }
                            }
                        });
                    }
                    else{
                        moveToLoginPage();
                    }
                }
                else{
                    if(currentUser!=null){
                        mAuth.signOut();
                    }
                    moveToLoginPage();
                }

//                mAuth.signOut();
//                moveToLoginPage();

            }
        });
        create_sdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pleaseWaitDialog.show();
                UserRoom newUserRoom = new UserRoom(userLoggedIn.getFullName(),
                        userLoggedIn.getEmail(),userLoggedIn.getPhotoUrl(),currentUser.getUid(),
                        "","true","true");
                FirebaseDatabase.getInstance().getReference("UserRoom")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newUserRoom);

                FirebaseDatabase.getInstance().getReference("UserRoom")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("incoming_user")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue().equals("")){

                        }
                        else{
                            String friendUserId = snapshot.getValue(String.class);
                            moveToCallPage(friendUserId,mUserId);
//                            startActivity(new Intent(HomeActivity.this,CallActivity.class));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });

        setupRecyclerView();
        getLoggedInUserData();

    }


    public void setupRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        userRoomList = new ArrayList<>();
        myAdapter = new MyAdapter(getApplicationContext(),HomeActivity.this,userRoomList);
        recyclerView.setAdapter(myAdapter);
        getUserRooms();
    }
    public void getUserRooms(){

        FirebaseDatabase.getInstance().getReference()
                .child("UserRoom")
                .orderByChild("isAvailable")
                .equalTo("true")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userRoomList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        UserRoom newUserRoom = dataSnapshot.getValue(UserRoom.class);
                        if(!newUserRoom.getUser_id().equals(currentUser.getUid())){
                            userRoomList.add(newUserRoom);
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void getLoggedInUserData(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            userLoggedIn = snapshot.getValue(User.class);
                            TV_name.setText(userLoggedIn.getFullName());
                            TV_email.setText(userLoggedIn.getEmail());
                            Glide.with(getApplicationContext()).load(userLoggedIn.getPhotoUrl()).into(IV_user_photo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void connectToFriend(String friendUserId){
        FirebaseDatabase.getInstance().getReference("UserRoom")
                .child(friendUserId)
                .child("incoming_user").setValue(currentUser.getUid())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        moveToCallPage(friendUserId, friendUserId);
                    }
                });
    }
    public void moveToCallPage(String friendUserId, String createdBy){
        Intent i = new Intent(HomeActivity.this,CallActivity.class);
        i.putExtra("user_id",mUserId);
        i.putExtra("friend_user_id",friendUserId);
        i.putExtra("createdBy",createdBy);
        startActivity(i);
        finish();
    }
    public void moveToLoginPage(){
        startActivity(new Intent(HomeActivity.this,LoginActivity.class));
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

    public void cancelSDP(){
        pleaseWaitDialog.dismiss();
        FirebaseDatabase.getInstance().getReference("UserRoom")
                .child(mUserId)
                .child("isAvailable").setValue("false");
    }

    @Override
    protected void onPause() {
        cancelSDP();
        super.onPause();
    }
}