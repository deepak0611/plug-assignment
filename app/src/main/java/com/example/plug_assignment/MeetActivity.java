package com.example.plug_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.Socket;
import java.util.ArrayList;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class MeetActivity extends AppCompatActivity {

    private static final String TAG = "MeetActivity";
    private static final int RC_CALL = 111;
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;

    private Socket socket;
    private boolean isInitiator=false;
    private boolean isChannelReady = true;
    private boolean isStarted = false;


    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    MediaConstraints sdpConstraints;
    VideoSource videoSource;
    VideoTrack localVideoTrack;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    SurfaceTextureHelper surfaceTextureHelper;

//    private ActivitySamplePeerConnectionBinding binding;
    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;

    org.webrtc.SurfaceViewRenderer surfaceView, surfaceView2;

    String user_id = "";
    String incoming_user_id = "";
    String createdBy = "";
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
        setContentView(R.layout.activity_meet);

        user_id = getIntent().getStringExtra("user_id");
        incoming_user_id = getIntent().getStringExtra("friend_user_id");
        createdBy = getIntent().getStringExtra("createdBy");

        if(user_id.equals(createdBy)){
            isInitiator = true;
        }


        surfaceView2 = findViewById(R.id.surface_view2);
        surfaceView = findViewById(R.id.surface_view);

        if(!isAllPermissionAllowed){
            ActivityCompat.requestPermissions(this,mPermissionArray,PERMISSION_CODE);
        }
        else{
            start();
        }

    }

    private void start() {


            connectToSignallingServer();

//
            initializeSurfaceViews();

//
            initializePeerConnectionFactory();

//
            createVideoTrackFromCameraAndShowIt();

//
            initializePeerConnections();

//
            startStreamingVideo();
//        Toast.makeText(this, "Recahed", Toast.LENGTH_SHORT).show();

    }

    private void connectToSignallingServer() {
        FirebaseDatabase.getInstance().getReference("CallRoom")
                .child(createdBy)
                .child(createdBy+createdBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Message message = snapshot.getValue(Message.class);
                            if(message.getType().equals("got user media")){
                                maybeStart();
                            }
                            else{
                                if(message.getType().equals("offer")){
                                    Toast.makeText(MeetActivity.this, "initator: "+isInitiator + "isStarted"+ isStarted+" ..", Toast.LENGTH_SHORT).show();
                                    if (!isInitiator && !isStarted) {
                                        maybeStart();
                                    }
                                    if(!isInitiator){
                                        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getSdp()));
                                        doAnswer();
                                    }
                                }
                                else if(message.getType().equals("answer") && isStarted){
                                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getSdp()));
                                }
//                                else if(message.getType().equals("candidate") && isStarted){
//                                    IceCandidate candidate = new IceCandidate(message.getId(), message.getLabel(), message.getCandidate());
//                                    peerConnection.addIceCandidate(candidate);
//                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference("CallRoom")
                .child(createdBy)
                .child(incoming_user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Message message = snapshot.getValue(Message.class);
                            IceCandidate candidate = new IceCandidate(message.getId(), message.getLabel(), message.getCandidate());
                            peerConnection.addIceCandidate(candidate);
                            Toast.makeText(MeetActivity.this, "iceCandiadte set done", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void doAnswer() {
        Toast.makeText(this, "Receiver: answering", Toast.LENGTH_SHORT).show();
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                Message message = new Message();
                message.setType("answer");
                message.setSdp(sessionDescription.description);
                sendMessage(message);

            }
        }, new MediaConstraints());
    }

    private void maybeStart() {
        Log.d(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;
            if (isInitiator) {
                Toast.makeText(this, "Initiator: doing call" , Toast.LENGTH_SHORT).show();
                doCall();
            }
        }
    }

    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                Message message = new Message();
                message.setType("offer");
                message.setSdp(sessionDescription.description);
                sendMessage(message);

            }
        }, sdpMediaConstraints);
    }

    private void sendMessage(Message message) {
//        socket.emit("message", message);
        FirebaseDatabase.getInstance().getReference("CallRoom")
                .child(createdBy)
                .child(createdBy+createdBy)
                .setValue(message);
    }

    private void sendMessage2(Message message, String userId) {
//        socket.emit("message", message);
        FirebaseDatabase.getInstance().getReference("CallRoom")
                .child(createdBy)
                .child(userId)
                .setValue(message);
        Toast.makeText(this, "sent iceCandidate", Toast.LENGTH_SHORT).show();
    }


    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        surfaceView.init(rootEglBase.getEglBaseContext(), null);
        surfaceView.setEnableHardwareScaler(true);
        surfaceView.setMirror(true);

        surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        surfaceView2.setEnableHardwareScaler(true);
        surfaceView2.setMirror(true);

        //add one more
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();
        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(new VideoRenderer(surfaceView));

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);

    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
    }

    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

        Message message = new Message();
        message.setType("got user media");
        sendMessage(message);
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        String URL = "stun:stun.l.google.com:19302";
        iceServers.add(new PeerConnection.IceServer(URL));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");

                Message message = new Message();
                message.setType("candidate");
                message.setLabel(iceCandidate.sdpMLineIndex);
                message.setId(iceCandidate.sdpMid);
                message.setCandidate(iceCandidate.sdp);

                Log.d(TAG, "onIceCandidate: sending candidate " + message);

                sendMessage2(message,user_id);

            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Toast.makeText(MeetActivity.this, "getting remote stream", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addRenderer(new VideoRenderer(surfaceView2));

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
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
            else{
                start();
            }
        }
    }

}