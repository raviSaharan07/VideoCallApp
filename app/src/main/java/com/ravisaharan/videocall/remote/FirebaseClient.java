package com.ravisaharan.videocall.remote;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.ravisaharan.videocall.utils.DataModel;
import com.ravisaharan.videocall.utils.ErrorCallBack;
import com.ravisaharan.videocall.utils.NewEventCallBack;
import com.ravisaharan.videocall.utils.SuccessCallBack;

import java.util.Objects;

public class FirebaseClient {
    private final Gson gson=new Gson();
    private final DatabaseReference db=FirebaseDatabase.getInstance().getReference();
    private String currentUsername;
    private final String LATEST_EVENT_FIELD_NAME="latestEvent";

    public void login(String userName, SuccessCallBack callBack){
        db.child(userName).setValue(" ").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentUsername=userName;
                callBack.onSuccess();
            }
        });
    }

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(dataModel.getTarget()).exists()){
                    //send the message to the target user
                    db.child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(gson.toJson(dataModel));
                }
                else{
                    errorCallBack.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    errorCallBack.onError();
            }
        });
    }

    public void observeIncomingLatestEvent(NewEventCallBack callBack){
        db.child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    String data= Objects.requireNonNull(snapshot.getValue()).toString();
                    DataModel dataModel=gson.fromJson(data,DataModel.class);
                    callBack.onNewEventReceived(dataModel);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
