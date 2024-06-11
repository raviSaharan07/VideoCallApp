package com.ravisaharan.videocall.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.ravisaharan.videocall.remote.FirebaseClient;
import com.ravisaharan.videocall.utils.DataModel;
import com.ravisaharan.videocall.utils.DataModelType;
import com.ravisaharan.videocall.utils.ErrorCallBack;
import com.ravisaharan.videocall.utils.NewEventCallBack;
import com.ravisaharan.videocall.utils.SuccessCallBack;
import com.ravisaharan.videocall.webRtc.MyPeerConnectionObserver;
import com.ravisaharan.videocall.webRtc.WebRTCClient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class MainRepository implements WebRTCClient.Listener {

    public Listener listener;
    private final Gson gson=new Gson();
    private final FirebaseClient firebaseClient;
    private WebRTCClient webRTCClient;
    private static MainRepository instance;
    private SurfaceViewRenderer remoteView;
    private String currentUserName;
    private String target;
    private void updateCurrentUserName(String userName){
        this.currentUserName=userName;
    }


    private MainRepository() {
        this.firebaseClient=new FirebaseClient();
    }

    public static MainRepository getInstance(){
        if(instance==null){
            instance=new MainRepository();
        }
        return instance;
    }

    public void login(String userName, Context context, SuccessCallBack callBack){
        firebaseClient.login(userName, ()->{
            updateCurrentUserName(userName);
            this.webRTCClient=new WebRTCClient(context,new MyPeerConnectionObserver(){
                @Override
                public void onAddStream(MediaStream mediaStream) {
                    super.onAddStream(mediaStream);
                    try{
                        mediaStream.videoTracks.get(0).addSink(remoteView);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    super.onConnectionChange(newState);

                    if(newState==PeerConnection.PeerConnectionState.CONNECTED&&listener!=null){
                        listener.webrtcConnected();
                    }

                    if(newState==PeerConnection.PeerConnectionState.CLOSED||
                            newState==PeerConnection.PeerConnectionState.DISCONNECTED){
                        if(listener!=null){
                            listener.webrtcClose();
                        }
                    }
                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    super.onIceCandidate(iceCandidate);
                    webRTCClient.sendIceCandidate(iceCandidate,target);
                }
            },userName);
            webRTCClient.listener=this;
            callBack.onSuccess();
        });

    }

    public void initLocalView(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView=view;
    }

    public void startCall(String target){
        webRTCClient.call(target);
    }

    public void switchCamera(){
        webRTCClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }

    public void endCall(){
        webRTCClient.closeConnection();
    }

    public void sendCallRequest(String target, ErrorCallBack errorCallBack){
        firebaseClient.sendMessageToOtherUser(
                new DataModel(target,currentUserName,null, DataModelType.StartCall),errorCallBack);
    }
    public void subscribeForLatestEvent(NewEventCallBack callBack){
        firebaseClient.observeIncomingLatestEvent(model->{
            switch (model.getType()){

                case Offer:
                    this.target=model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER, model.getData()
                    ));
                    webRTCClient.answer(model.getSender());
                    break;

                case Answer:
                    this.target=model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER, model.getData()
                    ));
                    break;

                case IceCandidate:
                    try{
                        IceCandidate candidate=gson.fromJson(model.getData(),IceCandidate.class);
                        webRTCClient.addIceCandidate(candidate);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;

                case StartCall:
                    this.target=model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
            }
        });
    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        firebaseClient.sendMessageToOtherUser(model,()->{});
    }

    public interface Listener{
        void webrtcConnected();
        void webrtcClose();
    }
}
