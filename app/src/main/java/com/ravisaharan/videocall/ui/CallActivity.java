package com.ravisaharan.videocall.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ravisaharan.videocall.R;
import com.ravisaharan.videocall.databinding.ActivityCallBinding;
import com.ravisaharan.videocall.repository.MainRepository;
import com.ravisaharan.videocall.utils.DataModelType;

public class CallActivity extends AppCompatActivity implements MainRepository.Listener {

    ActivityCallBinding callBinding;
    MainRepository mainRepository;

    public boolean isCameraMuted=false;
    public boolean isMicMuted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        callBinding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(callBinding.getRoot());
        init();
    }

    public void init(){
        mainRepository=MainRepository.getInstance();
        callBinding.callBtn.setOnClickListener(v->{
            //make call request
            mainRepository.sendCallRequest(callBinding.targetUserNameEt.getText().toString(),()->{
                Toast.makeText(this, "Couldn't find the above target", Toast.LENGTH_SHORT).show();
            });
        });

        mainRepository.initLocalView(callBinding.localView);
        mainRepository.initRemoteView(callBinding.remoteView);
        mainRepository.listener=this;

            mainRepository.subscribeForLatestEvent(data->{
                if(data.getType()== DataModelType.StartCall) {
                    runOnUiThread(()->{
                    String text = data.getSender() + " is calling you..";
                    callBinding.incomingNameTV.setText(text);
                    callBinding.incomingCallLayout.setVisibility(View.VISIBLE);
                    callBinding.acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //start call
                            mainRepository.startCall(data.getSender());
                            callBinding.incomingCallLayout.setVisibility(View.GONE);
                        }
                    });
                    callBinding.rejectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            callBinding.incomingCallLayout.setVisibility(View.GONE);
                        }
                    });
                });
            }
            });

            callBinding.switchCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainRepository.switchCamera();
                }
            });

            callBinding.micButton.setOnClickListener(v->{
                if(isMicMuted){
                    callBinding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }else{
                    callBinding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                }
                mainRepository.toggleAudio(isMicMuted);
                isMicMuted=!isMicMuted;
            });

            callBinding.videoButton.setOnClickListener(v->{
                if(isCameraMuted){
                    callBinding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }else{
                    callBinding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
                }
                mainRepository.toggleVideo(isCameraMuted);
                isCameraMuted=!isCameraMuted;
            });

            callBinding.endCallButton.setOnClickListener(v->{
                mainRepository.endCall();
                finish();
            });

    }

    @Override
    public void webrtcConnected() {
        runOnUiThread(()->{
            callBinding.incomingCallLayout.setVisibility(View.GONE);
            callBinding.whoToCallLayout.setVisibility(View.GONE);
            callBinding.callLayout.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void webrtcClose() {
        runOnUiThread(this::finish);
    }
}