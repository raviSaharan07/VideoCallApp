package com.ravisaharan.videocall.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.ravisaharan.videocall.R;
import com.ravisaharan.videocall.databinding.ActivityLoginBinding;
import com.ravisaharan.videocall.repository.MainRepository;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding loginBinding;
    MainRepository mainRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loginBinding= DataBindingUtil.setContentView(this,R.layout.activity_login);
        init();
    }

    public void init(){
        mainRepository=MainRepository.getInstance();
        loginBinding.enterBtn.setOnClickListener(v->{
            PermissionX.init(this).
                    permissions(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO).
                    request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                            if(allGranted){
                                //login in here
                                mainRepository.login(loginBinding.username.getText().toString(),getApplicationContext(),()->{
                                    //if successful then move to call activity
                                    startActivity(new Intent(LoginActivity.this,CallActivity.class)  );
                                });
                            }
                        }
                    });



        });
    }

}