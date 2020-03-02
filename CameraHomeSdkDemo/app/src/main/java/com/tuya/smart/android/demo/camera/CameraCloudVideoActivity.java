package com.tuya.smart.android.demo.camera;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.camera.camerasdk.typlayer.callback.IRegistorIOTCListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationCallBack;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.cloud.ITYCloudCamera;
import com.tuya.smart.camera.ipccamerasdk.cloud.TYCloudCamera;
import com.tuya.smart.camera.ipccamerasdk.msgvideo.ITYCloudVideo;
import com.tuya.smart.camera.ipccamerasdk.msgvideo.TYCloudVideoPlayer;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;

import java.nio.ByteBuffer;

public class CameraCloudVideoActivity extends AppCompatActivity implements OnP2PCameraListener {

    private final int OPERATE_SUCCESS = 1;
    private final int OPERATE_FAIL = 0;
    private final int MSG_CLOUD_VIDEO_DEVICE = 1000;

    private ProgressBar mProgressBar;
    private TuyaCameraView mCameraView;

    private ITYCloudVideo mcloudCamera;
    private String playUrl;
    private String encryptKey;
    private int playDuration;
    private String cachePath;
    private String mDevId;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLOUD_VIDEO_DEVICE:
                    startplay();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void startplay() {
        mcloudCamera.playVideo(playUrl, 0, encryptKey, new OperationCallBack() {
            @Override
            public void onSuccess(int i, int i1, String s, Object o) {
                Log.d("mcloudCamera", "onsuccess");
            }

            @Override
            public void onFailure(int i, int i1, int i2, Object o) {

            }
        }, new OperationCallBack() {
            @Override
            public void onSuccess(int i, int i1, String s, Object o) {
                Log.d("mcloudCamera", "finish onsuccess");
            }

            @Override
            public void onFailure(int i, int i1, int i2, Object o) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_cloud_video);
        initview();
        initData();
        initCloudCamera();
    }

    private void initData() {
        playUrl = getIntent().getStringExtra("playUrl");
        encryptKey = getIntent().getStringExtra("encryptKey");
        playDuration = getIntent().getIntExtra("playDuration", 0);
        cachePath = MicroContext.getApplication().getCacheDir().getPath();

    }

    private void initCloudCamera() {
        mcloudCamera = new TYCloudVideoPlayer();
        mcloudCamera.registorOnP2PCameraListener(this);
        mcloudCamera.generateCloudCameraView((IRegistorIOTCListener) mCameraView.createdView());
        mcloudCamera.createCloudDevice(cachePath, mDevId, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_CLOUD_VIDEO_DEVICE, OPERATE_SUCCESS, data));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });

    }

    private void initview() {
        mProgressBar = findViewById(R.id.camera_cloud_video_progressbar);
        mCameraView = findViewById(R.id.camera_cloud_video_view);
        mCameraView.createVideoView(2);
    }


    @Override
    public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

    }

    @Override
    public void onReceiveFrameYUVData(int i, ByteBuffer byteBuffer, ByteBuffer byteBuffer1, ByteBuffer byteBuffer2, int i1, int i2, int i3, int i4, long l, long l1, long l2, Object o) {

    }

    @Override
    public void onSessionStatusChanged(Object o, int i, int i1) {

    }

    @Override
    public void onReceiveSpeakerEchoData(ByteBuffer byteBuffer, int i) {

    }
}
