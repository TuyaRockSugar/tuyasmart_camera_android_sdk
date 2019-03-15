package com.tuya.smart.android.demo.camera;

import android.graphics.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.camera.middleware.ITuyaSmartCamera;
import com.tuya.smart.camera.middleware.TuyaSmartCameraFactory;
import com.tuya.smart.camera.middleware.view.TuyaMonitorView;
import com.tuya.smart.camera.tuyadeleagte.ICameraP2P;
import com.tuya.smart.camera.tuyadeleagte.OnDelegateCameraListener;
import com.tuya.smart.camera.tuyadeleagte.bean.TimePieceBean;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * @author chenbj
 */
public class CameraPlaybackActivity extends AppCompatActivity implements OnDelegateCameraListener, View.OnClickListener {

    private static final String TAG = "CameraPlaybackActivity";
    private Toolbar toolbar;
    private TuyaMonitorView mVideoView;
    private ImageView muteImg;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    private Button queryBtn, startBtn, pauseBtn, resumeBtn, stopBtn;

    private ITuyaSmartCamera camera;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", p2pWd = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private int queryDay;
    private CameraPlaybackTimeAdapter adapter;
    private List<TimePieceBean> queryDateList;

    private boolean isPlayback = false;

    private int isPlaybackMute = ICameraP2P.MUTE;
    private boolean mIsRunSoft;
    private int p2pType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_playback);
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
        dateInputEdt = findViewById(R.id.date_input_edt);
        queryBtn = findViewById(R.id.query_btn);
        startBtn = findViewById(R.id.start_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        resumeBtn = findViewById(R.id.resume_btn);
        stopBtn = findViewById(R.id.stop_btn);
        queryRv = findViewById(R.id.query_list);

        //播放器view最好宽高比设置16:9
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);
    }

    private void initData() {
        mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", false);
        p2pId = getIntent().getStringExtra("p2pId");
        p2pWd = getIntent().getStringExtra("p2pWd");
        localKey = getIntent().getStringExtra("localKey");
        p2pType = getIntent().getIntExtra("p2pType", 1);
        mVideoView.createVideoView(p2pType, mIsRunSoft);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        queryDateList = new ArrayList<>();
        adapter = new CameraPlaybackTimeAdapter(this, queryDateList);
        queryRv.setAdapter(adapter);

        //there is no need to reconnect（createDevice） with a single column object（Of course，you can create it again）
        camera = TuyaSmartCameraFactory.generateTuyaSmartCamera(p2pType);
        camera.registorOnDelegateCameraListener(this);
        camera.generateCameraView(mVideoView.createdView());
//        camera.createDevice(p2pId, mInitStr, mP2pKey, mIsRunSoft);
        camera.connectPlayback();

        muteImg.setSelected(true);
        dateInputEdt.setText("2019/3/4");
    }

    private void initListener() {
        muteImg.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        resumeBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        adapter.setListener(new CameraPlaybackTimeAdapter.OnTimeItemListener() {
            @Override
            public void onClick(TimePieceBean timePieceBean) {
                if (null != timePieceBean) {
                    if (isPlayback) {
                        camera.stopPlayBack();
                        isPlayback = false;
                    }
                    camera.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_mute:
                int mute = isPlaybackMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
                camera.setMute(ICameraP2P.PLAYMODE.PLAYBACK, mute, CameraPlaybackActivity.this);
                break;
            case R.id.query_btn:
                String inputStr = dateInputEdt.getText().toString();
                if (TextUtils.isEmpty(inputStr)) {
                    return;
                }
                if (inputStr.contains("/")) {
                    String[] substring = inputStr.split("/");
                    if (substring.length > 2) {
                        try {
                            int year = Integer.parseInt(substring[0]);
                            int mouth = Integer.parseInt(substring[1]);
                            queryDay = Integer.parseInt(substring[2]);
                            camera.queryRecordDaysByMonth(year, mouth);
                        } catch (Exception e) {
                            ToastUtil.shortToast(CameraPlaybackActivity.this, "Input Error");
                        }
                    }
                }
                break;
            case R.id.start_btn:
                if (isPlayback) {
                    camera.stopPlayBack();
                    isPlayback = false;
                    return;
                }
                if (null != queryDateList && queryDateList.size() > 0) {
                    TimePieceBean timePieceBean = queryDateList.get(0);
                    if (null != timePieceBean) {
                        camera.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime());
                    }
                } else {
                    ToastUtil.shortToast(this, "No data for query date");
                }
                break;
            case R.id.pause_btn:
                camera.pausePlayBack();
                break;
            case R.id.resume_btn:
                camera.resumePlayBack();
                break;
            case R.id.stop_btn:
                camera.stopPlayBack();
                isPlayback = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlayback) {
            camera.stopPlayBack();
        }
    }

    @Override
    public void onCreateDeviceSuccess() {
//        camera.connect(p2pId, p2pWd, localKey);
//        camera.connectPlayback();
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
                Toast.makeText(CameraPlaybackActivity.this, "connectFail " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChannel0StartSuccess() {
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

    }

    @Override
    public void onPreviewFail(int i) {

    }

    @Override
    public void onStopPreviewSuccess() {

    }

    @Override
    public void onStopPreviewFail() {

    }

    @Override
    public void onMuteOperateSuccess(ICameraP2P.PLAYMODE playmode, int isMute) {
        isPlaybackMute = isMute;
        muteImg.setSelected(isPlaybackMute == ICameraP2P.MUTE);
    }

    @Override
    public void onMuteOperateFail(ICameraP2P.PLAYMODE playmode) {

    }

    @Override
    public void onDefinitionStatusCallback(boolean b, int i) {
    }

    @Override
    public void onSnapshotSuccessCallback() {
        Log.d(TAG, "onSnapshotSuccessCallback");
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
        isPlayback = true;
    }

    @Override
    public void onPlaybackStartFail(String s, String s1) {
        isPlayback = false;
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
        isPlayback = false;
    }

    @Override
    public void onPlaybackEndFail() {

    }

    @Override
    public void onQueryPlaybackDataSuccessByMonth(int i, int i1, Object o) {
        if (null == o) {
            ToastUtil.shortToast(this, "No data for query date");
            return;
        }
        if (o instanceof List) {
            String queryDayStr = "";
            if (queryDay < 10) {
                queryDayStr = "0" + queryDay;
            } else {
                queryDayStr = "" + queryDay;
            }
            camera.queryRecordTimeSliceByDay(i, i1, Integer.parseInt(queryDayStr));
        } else {
            //Dates with data for the query month
            try {
                JSONObject jsonObject = JSONObject.parseObject(o.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("DataDays");
                List<String> days = JSONArray.parseArray(jsonArray.toJSONString(), String.class);
                if (days.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.shortToast(CameraPlaybackActivity.this, "No data for query date");
                        }
                    });

                    return;
                }
                String queryDayStr;
                if (queryDay < 10) {
                    queryDayStr = "0" + queryDay;
                } else {
                    queryDayStr = "" + queryDay;
                }
                if (days.contains(queryDayStr)) {
                    camera.queryRecordTimeSliceByDay(i, i1, queryDay);
                } else {
                    ToastUtil.shortToast(this, "No data for query date");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onQueryPlaybackDataFailureByMonth(int i, String s) {
        Log.d(TAG, "onQueryPlaybackDataFailureByMonth errorCode " + i);
    }

    @Override
    public void onQueryPlaybackDataSuccessByDay(String yearmonthday, Object timePieceBeanList) {
        if (null == timePieceBeanList) {
            ToastUtil.shortToast(this, "No data for query date");
            return;
        }
        queryDateList.clear();
        //Timepieces with data for the query day
        queryDateList.addAll((List<TimePieceBean>) timePieceBeanList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onQueryPlaybackDataFailureByDay(int i, String s) {

    }

    @Override
    public void onreceiveFrameDataCallback() {

    }

    @Override
    public void onSpeakSuccessCallback() {
    }

    @Override
    public void onSpeakFailueCallback(int i) {

    }

    @Override
    public void onStopSpeakSuccessCallback() {
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