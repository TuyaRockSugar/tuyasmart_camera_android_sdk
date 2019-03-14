package com.tuya.smart.android.demo.camera;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.bean.CameraInfoBean;
import com.tuya.smart.android.demo.utils.Constants;
import com.tuya.smart.camera.middleware.ITuyaSmartCamera;
import com.tuya.smart.camera.middleware.TuyaSmartCameraFactory;
import com.tuya.smart.camera.middleware.utils.CRC32;
import com.tuya.smart.camera.middleware.utils.IntToButeArray;
import com.tuya.smart.camera.middleware.view.TuyaMonitorView;
import com.tuya.smart.camera.tuyadeleagte.ICameraP2P;
import com.tuya.smart.camera.tuyadeleagte.OnDelegateCameraListener;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IRequestCallback;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_LOCALKEY;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_P2P_TYPE;

/**
 * @author chenbj
 */
public class CameraPanelActivity extends AppCompatActivity implements OnDelegateCameraListener, View.OnClickListener {

    private static final String TAG = "CameraPanelActivity";
    private Toolbar toolbar;
    private TuyaMonitorView mVideoView;
    private ImageView muteImg;
    private TextView qualityTv;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt;

    private ITuyaSmartCamera camera;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", p2pWd = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int isPreviewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;

    private String picPath, videoPath;

    private boolean mIsRunSoft;
    private int p2pType;

    private String devId;
    private CameraInfoBean infoBean;

    /**
     * the lower power Doorbell device change to true
     */
    private boolean isDoorbell = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_panel);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mVideoView = findViewById(R.id.camera_video_view);
        muteImg = findViewById(R.id.camera_mute);
        qualityTv = findViewById(R.id.camera_quality);
        speakTxt = findViewById(R.id.speak_Txt);
        recordTxt = findViewById(R.id.record_Txt);
        photoTxt = findViewById(R.id.photo_Txt);
        replayTxt = findViewById(R.id.replay_Txt);

        //播放器view最好宽高比设置16:9
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);

        muteImg.setSelected(true);
    }

    private void initData() {

        localKey = getIntent().getStringExtra(INTENT_LOCALKEY);
        devId = getIntent().getStringExtra(INTENT_DEVID);
        p2pType = getIntent().getIntExtra(INTENT_P2P_TYPE, -1);
        mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", true);
        getApi();
    }

    private void initCameraView() {
        mVideoView.createVideoView(p2pType, mIsRunSoft);
        camera = TuyaSmartCameraFactory.generateTuyaSmartCamera(p2pType);
        camera.registorOnDelegateCameraListener(this);
        camera.generateCameraView(mVideoView.createdView());
        camera.createDevice(p2pId, mInitStr, mP2pKey, mIsRunSoft);
    }

    private void getApi() {
        Map postData = new HashMap();
        postData.put("devId", devId);
        TuyaHomeSdk.getRequestInstance().requestWithApiName("tuya.m.ipc.config.get", "1.0",
                postData, new IRequestCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        infoBean = JSONObject.parseObject(o.toString(), CameraInfoBean.class);
                        Log.d("onSuccess", o.toString());
                        p2pId = infoBean.getP2pId().split(",")[0];
                        p2pWd = infoBean.getPassword();
                        mInitStr = infoBean.getP2pConfig().getInitStr();
                        initCameraView();
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        ToastUtil.shortToast(CameraPanelActivity.this, "get cameraInfo failed");
                    }
                });
    }

    private void initListener() {
        muteImg.setOnClickListener(this);
        qualityTv.setOnClickListener(this);
        speakTxt.setOnClickListener(this);
        recordTxt.setOnClickListener(this);
        photoTxt.setOnClickListener(this);
        replayTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_mute:
                int mute;
                mute = isPreviewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
                camera.setMute(ICameraP2P.PLAYMODE.LIVE, mute, CameraPanelActivity.this);
                break;
            case R.id.camera_quality:
                camera.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD);
                break;
            case R.id.speak_Txt:
                if (isSpeaking) {
                    camera.stopAudioTalk();
                } else {
                    if (Constants.hasRecordPermission()) {
                        camera.startAudioTalk();
                    } else {
                        Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.RECORD_AUDIO, Constants.EXTERNAL_AUDIO_REQ_CODE, "open_recording");
                    }
                }
                break;
            case R.id.record_Txt:
                if (!isRecording) {
                    if (Constants.hasStoragePermission()) {
                        String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                        File file = new File(picPath);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + ".mp4";
                        videoPath = picPath + fileName;
                        int result = camera.startRecordLocalMp4WithoutAudio(picPath, fileName, CameraPanelActivity.this);
                        isRecording = result == 0;
                        recordStatue(true);
                    } else {
                        Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "open_storage");
                    }
                } else {
                    camera.stopRecordLocalMp4();
                    isRecording = false;
                    recordStatue(false);
                }
                break;
            case R.id.photo_Txt:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    picPath = path + System.currentTimeMillis() + ".png";
                }
                camera.snapshot(picPath, CameraPanelActivity.this, ICameraP2P.PLAYMODE.LIVE);
                break;
            case R.id.replay_Txt:
                Intent intent = new Intent(CameraPanelActivity.this, CameraPlaybackActivity.class);
                intent.putExtra("isRunsoft", mIsRunSoft);
                intent.putExtra("p2pId", p2pId);
                intent.putExtra("p2pWd", p2pWd);
                intent.putExtra("localKey", localKey);
                intent.putExtra("p2pType", p2pType);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void recordStatue(boolean isRecording) {
        speakTxt.setEnabled(!isRecording);
        photoTxt.setEnabled(!isRecording);
        replayTxt.setEnabled(!isRecording);
        recordTxt.setEnabled(true);
        recordTxt.setSelected(isRecording);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        //must register again,or can't callback
        camera = TuyaSmartCameraFactory.generateTuyaSmartCamera(p2pType);
        camera.registorOnDelegateCameraListener(this);
        camera.generateCameraView(mVideoView.createdView());
        if (!isPlay) {
            camera.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isSpeaking) {
            camera.stopAudioTalk();
        }
        if (isPlay) {
            camera.stopPreview();
            isPlay = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.disconnect();
        TuyaSmartCameraFactory.onDestroyTuyaSmartCamera();
    }

    @Override
    public void onCreateDeviceSuccess() {
        if (isDoorbell) {
            int crcsum = CRC32.getChecksum(localKey.getBytes());
            String topicId = "m/w/" + devId;
            byte[] bytes = IntToButeArray.intToByteArray(crcsum);
            ITuyaHomeCamera homeCamera = TuyaHomeSdk.getCameraInstance();
            homeCamera.publishWirelessWake(topicId, bytes);
        }
        camera.connect(p2pId, p2pWd, localKey);
    }

    @Override
    public void onCreateDeviceFail(int i) {
        Log.d(TAG, "onCreateDeviceFail ret" + i);
    }

    @Override
    public void connectFail(final String errorCode, String s1) {
        Log.d(TAG, "connectFail errorCode" + errorCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraPanelActivity.this, "connectFail " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChannel0StartSuccess() {
        camera.startPreview();
        camera.getVideoClarity();
    }

    @Deprecated
    @Override
    public void onChannel1StartSuccess() {
    }

    @Deprecated
    @Override
    public void onChannelOtherStatus(int errorCode) {
        Log.d(TAG, "onChannelOtherStatus errorCode " + errorCode);
    }

    @Override
    public void onPreviewSuccess() {
        isPlay = true;
    }

    @Override
    public void onPreviewFail(int i) {
        isPlay = false;
    }

    @Override
    public void onStopPreviewSuccess() {

    }

    @Override
    public void onStopPreviewFail() {

    }

    @Override
    public void onMuteOperateSuccess(ICameraP2P.PLAYMODE playmode, int isMute) {
        isPreviewMute = isMute;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                muteImg.setSelected(isPreviewMute == ICameraP2P.MUTE);
            }
        });
    }

    @Override
    public void onMuteOperateFail(ICameraP2P.PLAYMODE playmode) {

    }

    @Override
    public void onDefinitionStatusCallback(boolean b, int i) {
        videoClarity = i;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                qualityTv.setText(videoClarity == ICameraP2P.HD ? "HD" : "SD");
            }
        });
    }

    @Override
    public void onSnapshotSuccessCallback() {
        Log.d(TAG, "onSnapshotSuccessCallback");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraPanelActivity.this, "snapShot success:" + picPath, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSnapshotFailCallback() {
        Log.d(TAG, "onSnapshotFailCallback");
    }

    @Override
    public void onPlaybackEnterFail(String s, String s1) {

    }

    @Override
    public void onPlaybackStartSuccess() {
    }

    @Override
    public void onPlaybackStartFail(String s, String s1) {
    }

    @Override
    public void onPlaybackPauseSuccess() {

    }

    @Override
    public void onPlaybackPauseFail(String s, String s1) {

    }

    @Override
    public void onPlaybackResumeSuccess() {

    }

    @Override
    public void onPlaybackResumeFail(String s, String s1) {

    }

    @Override
    public void onPlaybackEnd() {

    }

    @Override
    public void onPlaybackEndFail() {

    }

    @Override
    public void onQueryPlaybackDataSuccessByMonth(int i, int i1, Object o) {
    }

    @Override
    public void onQueryPlaybackDataFailureByMonth(int i, String s) {
        Log.d(TAG, "onQueryPlaybackDataFailureByMonth errorCode " + i);
    }

    @Override
    public void onQueryPlaybackDataSuccessByDay(String yearmonthday, Object timePieceBeanList) {
    }

    @Override
    public void onQueryPlaybackDataFailureByDay(int i, String s) {

    }

    @Override
    public void onreceiveFrameDataCallback() {

    }

    @Override
    public void onSpeakSuccessCallback() {
        isSpeaking = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speakTxt.setSelected(true);
            }
        });
    }

    @Override
    public void onSpeakFailueCallback(int i) {

    }

    @Override
    public void onStopSpeakSuccessCallback() {
        isSpeaking = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speakTxt.setSelected(false);
            }
        });
    }

    @Override
    public void onStopSpeakFailueCallback(int i) {

    }

    @Override
    public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

    }

    @Override
    public void onReceiveFrameYUVData(int sessionId, ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height, long timestamp, Object camera) {
        mVideoView.receiveFrameYUVData(sessionId, y, u, v, width, height, camera);
    }

    @Override
    public void onSessionStatusChanged(Object o, int i, int i1) {

    }
}
