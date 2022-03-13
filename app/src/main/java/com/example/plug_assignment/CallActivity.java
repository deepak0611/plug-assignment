package com.example.plug_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallActivity extends AppCompatActivity {

    String user_id = "";
    String incoming_user_id = "";
    String createdBy = "";
    Boolean isAudio = true;
    Boolean isVideo = true;

    WebView webView;

    private boolean isAllPermissionAllowed = false;
    private static final int PERMISSION_CODE = 101;
    private String[] mPermissionArray = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        user_id = getIntent().getStringExtra("user_id");
        incoming_user_id = getIntent().getStringExtra("friend_user_id");
        createdBy = getIntent().getStringExtra("createdBy");

        if(!isAllPermissionAllowed){
            ActivityCompat.requestPermissions(this,mPermissionArray,PERMISSION_CODE);
        }


        ImageView IV_mic = findViewById(R.id.IV_mic);
        ImageView IV_video = findViewById(R.id.IV_video);
        ImageView IV_call_end = findViewById(R.id.IV_call_end);
        IV_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\""+ isAudio + "\")");
                if(isAudio){
                    IV_mic.setImageResource(R.drawable.ic_baseline_mic_24);
                }
                else{
                    IV_mic.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });
        IV_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\""+ isVideo + "\")");
                if(isVideo){
                    IV_video.setImageResource(R.drawable.ic_baseline_videocam_24);
                }
                else{
                    IV_video.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }
            }
        });
        IV_call_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
            }
        });


        FirebaseDatabase.getInstance().getReference("UserRoom")
                .child(createdBy)
                .child("status")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String status = snapshot.getValue(String.class);
                            if(status.equalsIgnoreCase("false")){
                                Toast.makeText(CallActivity.this, "Call ended", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        setupWebView();
    }

    public void setupWebView(){
        webView = findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
//                super.onPermissionRequest(request);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        loadVideoCall();

    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Toast.makeText(CallActivity.this, "Page loaded", Toast.LENGTH_SHORT).show();
                initializePeer();
            }
        });
    }

    private void initializePeer() {

        callJavascriptFunction("javascript:init(\""+ user_id + "\")");
        Toast.makeText(this, "Please wait for some time", Toast.LENGTH_SHORT).show();

        if(!user_id.equals(createdBy)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendCallRequest();
                }
            },2000);

        }

    }

    private void sendCallRequest(){

        callJavascriptFunction("javascript:startCall(\""+ incoming_user_id + "\")");
        Toast.makeText(this, "starting call", Toast.LENGTH_SHORT).show();
    }

    private void callJavascriptFunction(String function){
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(function,null);
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

    public void endCall(){
        FirebaseDatabase.getInstance().getReference("UserRoom")
                .child(createdBy)
                .child("status").setValue("false");
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        CallActivity.this.finish();
        System.exit(0);
//        super.onBackPressed();
    }
}