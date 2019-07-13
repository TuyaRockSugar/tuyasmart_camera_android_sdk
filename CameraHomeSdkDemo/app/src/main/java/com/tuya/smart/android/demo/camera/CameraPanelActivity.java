package com.tuya.smart.android.demo.camera;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.bean.CameraInfoBean;
import com.tuya.smart.android.demo.utils.Constants;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.monitor.Monitor;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_LOCALKEY;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_SDK_POROVIDER;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MSG_CONNECT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_CREATE_DEVICE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_GET_CLARITY;
import static com.tuya.smart.android.demo.utils.Constants.MSG_MUTE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_SCREENSHOT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_TALK_BACK_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_TALK_BACK_OVER;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_OVER;
import static com.tuya.smart.camera.ipccamerasdk.utils.CameraConstant.P2P_2;
import static com.tuya.smart.camera.ipccamerasdk.utils.CameraConstant.P2P_4;

/**
 * @author chenbj
 */
public class CameraPanelActivity extends AppCompatActivity implements OnP2PCameraListener, View.OnClickListener {

    private static final String TAG = "CameraPanelActivity";
    private Toolbar toolbar;
    private Monitor mVideoView;
    private ImageView muteImg;
    private TextView qualityTv;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt, settingTxt;

    private ICameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", mP2p3Id = null, token = null, p2pWd = "", mlocalId = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;

    private String picPath, videoPath;

    private boolean mIsRunSoft;
    private int sdkProvider;
    private int p2pType = -1;

    private String devId;
    private CameraInfoBean infoBean;
    private ITuyaCameraDevice mDeviceControl;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CREATE_DEVICE:
                    handleCreateDevice(msg);
                    break;
                case MSG_CONNECT:
                    handleConnect(msg);
                    break;
                case MSG_GET_CLARITY:
                    handleClarity(msg);
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(CameraPanelActivity.this, "record start success");
                    break;
                case MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(CameraPanelActivity.this, "record start fail");
                    break;
                case MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
                case MSG_TALK_BACK_BEGIN:
                    handleStartTalk(msg);
                    break;
                case MSG_TALK_BACK_OVER:
                    handleStopTalk(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleStopTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "stop talk success" + msg.obj);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handleStartTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "start talk success" + msg.obj);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "record success " + msg.obj);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "snapshot success " + msg.obj);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }


    private void handleClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            qualityTv.setText(videoClarity == ICameraP2P.HD ? "HD" : "SD");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "connect fail");
        }
    }


    private void handleCreateDevice(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            connect();
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "create device fail");
        }
    }


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
        settingTxt = findViewById(R.id.setting_Txt);
        settingTxt.setOnClickListener(this);

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
        sdkProvider = getIntent().getIntExtra(INTENT_SDK_POROVIDER, -1);
        mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", true);
        if (null != TuyaHomeSdk.getUserInstance().getUser()) {
            mlocalId = TuyaHomeSdk.getUserInstance().getUser().getUid();
        }
        mCameraP2P = TuyaSmartCameraP2PFactory.generateTuyaSmartCamera(sdkProvider);
        mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);
        getApi();
    }

    private void initCameraView() {
        if (P2P_2 == p2pType) {
            mCameraP2P.createDevice(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CREATE_DEVICE, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CREATE_DEVICE, ARG1_OPERATE_FAIL));
                }
            }, p2pType, devId, p2pId, mInitStr, "");
        } else if (P2P_4 == p2pType) {
            mCameraP2P.createDevice(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CREATE_DEVICE, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CREATE_DEVICE, ARG1_OPERATE_FAIL));
                }
            }, p2pType, devId, mP2p3Id, mInitStr, mlocalId);
        }

    }


    private void connect() {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);

        if (null != mDeviceControl && null != deviceBean
                && null != deviceBean.getUiName()
                && deviceBean.getUiName().equals("CameraPbList")) {
            String localkey = deviceBean.getLocalKey();
            mDeviceControl.wirelessWake(localkey, devId);
        }
        mCameraP2P.connect(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL, errCode));
            }
        }, p2pId, p2pWd, localKey, token);
    }


    private void preview() {
        mCameraP2P.startPreview(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.d(TAG, "start preview onSuccess");
                isPlay = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.d(TAG, "start preview onFailure");
                isPlay = false;
            }
        });
    }

    private void getApi() {
        Map postData = new HashMap();
        postData.put("devId", devId);
        TuyaHomeSdk.getRequestInstance().requestWithApiName("tuya.m.ipc.config.get", "2.0",
                postData, new IRequestCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        infoBean = JSONObject.parseObject(o.toString(), CameraInfoBean.class);
                        Log.d("onSuccess", o.toString());
                        mP2p3Id = infoBean.getId();
                        p2pType = infoBean.getP2pSpecifiedType();
                        p2pId = infoBean.getP2pId().split(",")[0];
                        p2pWd = infoBean.getPassword();
                        mInitStr = infoBean.getP2pConfig().getInitStr();
                        mP2pKey = infoBean.getP2pConfig().getP2pKey();
                        mInitStr += ":" + mP2pKey;
                        if (null != infoBean.getP2pConfig().getIces()) {
                            token = infoBean.getP2pConfig().getIces().toString();
                        }
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
                muteClick();
                break;
            case R.id.camera_quality:
                setVideoClarity();
                break;
            case R.id.speak_Txt:
                speakClick();
                break;
            case R.id.record_Txt:
                recordClick();
                break;
            case R.id.photo_Txt:
                snapShotClick();
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
            case R.id.setting_Txt:
                Intent intent1 = new Intent(CameraPanelActivity.this, SettingActivity.class);
                intent1.putExtra("devId", devId);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    private void recordClick() {
        if (!isRecording) {
            if (Constants.hasStoragePermission()) {
                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                File file = new File(picPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String fileName = System.currentTimeMillis() + ".mp4";
                videoPath = picPath + fileName;
                mCameraP2P.startRecordLocalMp4(picPath, fileName, CameraPanelActivity.this, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isRecording = true;
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_BEGIN);

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_FAIL);
                    }
                });
                recordStatue(true);
            } else {
                Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "open_storage");
            }
        } else {
            mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_SUCCESS, data));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_FAIL));
                }
            });
            recordStatue(false);
        }
    }

    private void snapShotClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            picPath = path;
        }
        mCameraP2P.snapshot(picPath, CameraPanelActivity.this, ICameraP2P.PLAYMODE.LIVE, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_SUCCESS, data));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void muteClick() {
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(ICameraP2P.PLAYMODE.LIVE, mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                previewMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void speakClick() {
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_FAIL));

                }
            });
        } else {
            if (Constants.hasRecordPermission()) {
                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isSpeaking = true;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                        ToastUtil.shortToast(CameraPanelActivity.this, "start talk success");
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isSpeaking = false;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                        ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");

                    }
                });
            } else {
                Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.RECORD_AUDIO, Constants.EXTERNAL_AUDIO_REQ_CODE, "open_recording");
            }
        }
    }

    private void setVideoClarity() {
        mCameraP2P.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                videoClarity = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_CLARITY, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_CLARITY, ARG1_OPERATE_FAIL));
            }
        });

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
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registorOnP2PCameraListener(this);
            mCameraP2P.generateCameraView(mVideoView);
            if (mCameraP2P.isConnecting()) {
                mCameraP2P.startPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlay = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(null);
        }
        if (isPlay) {
            mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
            isPlay = false;
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
        }
        AudioUtils.changeToNomal(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCameraP2P) {
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
        }
        TuyaSmartCameraP2PFactory.onDestroyTuyaSmartCamera();
    }


    @Override
    public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

    }

    @Override
    public void onReceiveFrameYUVData(int sessionId, ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height, int nFrameRate, int nIsKeyFrame, long timestamp, long nProgress, long nDuration, Object camera) {
    }


    @Override
    public void onSessionStatusChanged(Object o, int i, int i1) {

    }
}
