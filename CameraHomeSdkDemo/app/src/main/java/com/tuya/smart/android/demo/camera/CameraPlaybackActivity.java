package com.tuya.smart.android.demo.camera;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.bean.RecordInfoBean;
import com.tuya.smart.android.demo.camera.bean.TimePieceBean;
import com.tuya.smart.camera.camerasdk.TuyaCameraSDK;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.camerasdk.typlayer.callback.ProgressCallBack;
import com.tuya.smart.camera.ipccamerasdk.bean.MonthDays;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuyasmart.camera.devicecontrol.model.PTZDirection;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MSG_DATA_DATE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.MSG_DATA_DATE_BY_DAY_SUCC;
import static com.tuya.smart.android.demo.utils.Constants.MSG_MUTE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_PLAYBACK_TOAST;


/**
 * @author chenbj
 */
public class CameraPlaybackActivity extends AppCompatActivity implements OnP2PCameraListener, View.OnClickListener, TuyaCameraView.CreateVideoViewCallback {

    private static final String TAG = "CameraPlaybackActivity";
    private Toolbar toolbar;
    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    private Button queryBtn, startBtn, pauseBtn, resumeBtn, stopBtn;
    private Button operaBtn;
    private LinearLayout operaLl;
    private Button multiPlay1Btn, multiPlay2Btn, startDownloadBtn, stopDownloadBtn, pauseDownloadBtn, resumeDownloadBtn, eventDownloadBtn, deletePlaybackBtn;

    private ICameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String p2pId = "", p2pWd = "", localKey = "", mInitStr = "EEGDFHBAKJINGGJKFAHAFKFIGINJGFMEHIEOAACPBFIDKMLKCMBPCLONHCKGJGKHBEMOLNCGPAMC", mP2pKey = "nVpkO1Xqbojgr4Ks";
    private int queryDay;
    private CameraPlaybackTimeAdapter adapter;
    private List<TimePieceBean> queryDateList;
    private TimePieceBean currentTimePieceBean;

    private boolean isPlayback = false;

    protected Map<String, List<String>> mBackDataMonthCache;
    protected Map<String, List<TimePieceBean>> mBackDataDayCache;
    private int mPlaybackMute = ICameraP2P.MUTE;
    private boolean mIsRunSoft;
    private int p2pType;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_DATA_DATE:
                    handleDataDate(msg);
                    break;
                case MSG_DATA_DATE_BY_DAY_SUCC:
                case MSG_DATA_DATE_BY_DAY_FAIL:
                    handleDataDay(msg);
                    break;
                case MSG_PLAYBACK_TOAST:
                    ToastUtil.shortToast(CameraPlaybackActivity.this, "" + msg.obj.toString());
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleDataDay(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            queryDateList.clear();
            //Timepieces with data for the query day
            List<TimePieceBean> timePieceBeans = mBackDataDayCache.get(mCameraP2P.getDayKey());
            if (timePieceBeans != null) {
                queryDateList.addAll(timePieceBeans);
            } else {
                showErrorToast();
            }
            adapter.notifyDataSetChanged();
        } else {

        }
    }

    private void handleDataDate(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            List<String> days = mBackDataMonthCache.get(mCameraP2P.getMonthKey());

            try {
                if (null == days || days.size() == 0) {
                    showErrorToast();
                    return;
                }
                String inputStr = dateInputEdt.getText().toString();
                String[] substring = inputStr.split("/");
                int year = Integer.parseInt(substring[0]);
                int mouth = Integer.parseInt(substring[1]);
                int day = Integer.parseInt(substring[2]);
                mCameraP2P.queryRecordTimeSliceByDay(year, mouth, day, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        parsePlaybackData(data);
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendEmptyMessage(MSG_DATA_DATE_BY_DAY_FAIL);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private void parsePlaybackData(Object obj) {
        try {
            RecordInfoBean recordInfoBean = JSONObject.parseObject(obj.toString(), RecordInfoBean.class);
            if (recordInfoBean.getCount() != 0) {
                List<TimePieceBean> timePieceBeanList = recordInfoBean.getItems();
                if (timePieceBeanList != null && timePieceBeanList.size() != 0) {
                    mBackDataDayCache.put(mCameraP2P.getDayKey(), timePieceBeanList);
                }
                mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_SUCC, ARG1_OPERATE_SUCCESS));
            } else {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_FAIL, ARG1_OPERATE_FAIL));
            }
        } catch (Exception e) {
            L.d(TAG, obj.toString());
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(mPlaybackMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, "operation fail");
        }
    }

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

        multiPlay1Btn = findViewById(R.id.multi_playback_1);
        multiPlay2Btn = findViewById(R.id.multi_playback_2);
        startDownloadBtn = findViewById(R.id.start_download);
        stopDownloadBtn = findViewById(R.id.stop_download);
        pauseDownloadBtn = findViewById(R.id.pause_download);
        resumeDownloadBtn = findViewById(R.id.resume_download);
        eventDownloadBtn = findViewById(R.id.event_download);
        deletePlaybackBtn = findViewById(R.id.delete_playback);

        operaBtn = findViewById(R.id.opera_btn);
        operaLl = findViewById(R.id.operaLl);
    }

    private void initData() {
        mBackDataMonthCache = new HashMap<>();
        mBackDataDayCache = new HashMap<>();
        mIsRunSoft = getIntent().getBooleanExtra("isRunsoft", false);
        p2pId = getIntent().getStringExtra("p2pId");
        p2pWd = getIntent().getStringExtra("p2pWd");
        localKey = getIntent().getStringExtra("localKey");
        p2pType = getIntent().getIntExtra("p2pType", 1);

        mVideoView.createVideoView(p2pType);
        mVideoView.setCameraViewCallback(this);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        queryDateList = new ArrayList<>();
        adapter = new CameraPlaybackTimeAdapter(this, queryDateList);
        queryRv.setAdapter(adapter);

        //there is no need to reconnect（createDevice） with a single column object（Of course，you can create it again）
        mCameraP2P = TuyaSmartCameraP2PFactory.generateTuyaSmartCamera(p2pType);
        mCameraP2P.connectPlayback();

        muteImg.setSelected(true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateInputEdt.setText(simpleDateFormat.format(date));
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
                currentTimePieceBean = timePieceBean;
                mCameraP2P.startPlayBack(timePieceBean.getStartTime(),
                        timePieceBean.getEndTime(),
                        timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                isPlayback = true;
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                isPlayback = false;
                            }
                        }, new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                isPlayback = false;
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                isPlayback = false;
                            }
                        });
            }
        });

        multiPlay1Btn.setOnClickListener(this);
        multiPlay2Btn.setOnClickListener(this);
        startDownloadBtn.setOnClickListener(this);
        stopDownloadBtn.setOnClickListener(this);
        pauseDownloadBtn.setOnClickListener(this);
        resumeDownloadBtn.setOnClickListener(this);
        eventDownloadBtn.setOnClickListener(this);
        deletePlaybackBtn.setOnClickListener(this);

        operaBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_mute:
                muteClick();
                break;
            case R.id.query_btn:
                queryDayByMonthClick();
                break;
            case R.id.start_btn:
                startPlayback();
                break;
            case R.id.pause_btn:
                pauseClick();
                break;
            case R.id.resume_btn:
                resumeClick();
                break;
            case R.id.stop_btn:
                stopClick();
                break;

            case R.id.opera_btn:
                operaLl.setVisibility(operaLl.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;

            case R.id.multi_playback_1:
                if (isPlayback) {
                    mCameraP2P.setPlayBackSpeed(TuyaCameraSDK.TY_SPEED_10TIMES, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {

                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {

                        }
                    });
                }
                break;
            case R.id.multi_playback_2:
                if (isPlayback) {
                    //回调可以不实现，下同
                    mCameraP2P.setPlayBackSpeed(TuyaCameraSDK.TY_SPEED_20TIMES, null);
                }
                break;
            case R.id.start_download:
                if (null != queryDateList && queryDateList.size() > 0) {
                    // 确认所选的时间片 在查询到的列表范围内，下载文件格式为mp4
                    TimePieceBean timePieceBean = queryDateList.get(0);
                    if (null != currentTimePieceBean) {
                        timePieceBean = currentTimePieceBean;
                    } else {
                        ToastUtil.shortToast(this, "未选择片段，默认下载第一段");
                    }
                    final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/playback/";
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    final String fileName = System.currentTimeMillis() + ".mp4";
                    mCameraP2P.startPlayBackDownload(timePieceBean.getStartTime(), timePieceBean.getEndTime(), path, fileName,
                            new OperationDelegateCallBack() {
                                @Override
                                public void onSuccess(int sessionId, int requestId, String data) {
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "start download success"));
                                }

                                @Override
                                public void onFailure(int sessionId, int requestId, int errCode) {
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "start download failed"));
                                }
                            }, new ProgressCallBack() {
                                @Override
                                public void onProgress(int sessionId, int requestId, int pos, Object camera) {
                                    L.e(TAG, "download progress -" + pos);
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, " download progress：" + pos + " %"));
                                }
                            }, new OperationDelegateCallBack() {

                                @Override
                                public void onSuccess(int sessionId, int requestId, String data) {
                                    L.e(TAG, "download success ");
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download finished success：" + path + fileName));
                                }

                                @Override
                                public void onFailure(int sessionId, int requestId, int errCode) {
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download finished failed"));
                                }
                            });
                }
                break;
            case R.id.stop_download:
                mCameraP2P.stopPlayBackDownload(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download stop success"));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download stop faileds"));
                    }
                });
                break;
            case R.id.pause_download:
                mCameraP2P.pausePlayBackDownload(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download pause success"));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download pause failed"));
                    }
                });
                break;
            case R.id.resume_download:
                mCameraP2P.resumePlayBackDownload(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download resume success"));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download resume failed"));
                    }
                });
                break;
            case R.id.event_download:
                // 取当天事件中支持图片
                // 这里只取第一张 下载文件格式为jpg
                if (null != currentTimePieceBean) {
                    if (currentTimePieceBean.getType() > 0) {

                        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/playback/";
                        File file = new File(path);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        final String fileName = System.currentTimeMillis() + ".jpg";
                        mCameraP2P.downloadPlaybackEventImage(currentTimePieceBean.getStartTime(), currentTimePieceBean.getEndTime(), path, fileName, new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                L.d(TAG, "download success");
                                mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download 第一个事件图片 success：" + path + fileName));
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, "download 第一个事件图片 failed"));
                            }
                        });

                    } else {
                        ToastUtil.shortToast(this, "该片段没有图片");
                    }
                }
                break;
            case R.id.delete_playback:
                // 注意 传日期格式 "20200608"
                String inputStr = dateInputEdt.getText().toString().trim();
                if (TextUtils.isEmpty(inputStr)) {
                    return;
                }
                String date = inputStr;
                if (inputStr.contains("/")) {
                    date = inputStr.replaceAll("/", "");
                }
                mCameraP2P.deletePlaybackDataByDay(date, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, " delete success"));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, " delete failed"));
                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        L.d(TAG, "delete success");
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, " delete  finished "));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_PLAYBACK_TOAST, " delete finished failed "));
                    }
                });

                break;
            default:
                break;
        }
    }

    private void startPlayback() {
        if (null != queryDateList && queryDateList.size() > 0) {
            TimePieceBean timePieceBean = queryDateList.get(0);
            if (null != timePieceBean) {
                mCameraP2P.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
            }
        } else {
            ToastUtil.shortToast(this, "No data for query date");
        }
    }

    private void stopClick() {
        mCameraP2P.stopPlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
        isPlayback = false;
    }

    private void resumeClick() {
        mCameraP2P.resumePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void pauseClick() {
        mCameraP2P.pausePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = false;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void queryDayByMonthClick() {
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
                    mCameraP2P.queryRecordDaysByMonth(year, mouth, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            MonthDays monthDays = JSONObject.parseObject(data, MonthDays.class);
                            mBackDataMonthCache.put(mCameraP2P.getMonthKey(), monthDays.getDataDays());
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_SUCCESS, data));
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_FAIL));
                        }
                    });
                } catch (Exception e) {
                    ToastUtil.shortToast(CameraPlaybackActivity.this, "Input Error");
                }
            }
        }
    }

    private void muteClick() {
        int mute;
        mute = mPlaybackMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(ICameraP2P.PLAYMODE.PLAYBACK, mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mPlaybackMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registorOnP2PCameraListener(this);
            mCameraP2P.generateCameraView(mVideoView.createdView());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isPlayback) {
            mCameraP2P.stopPlayBack(null);
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
        }
        AudioUtils.changeToNomal(this);
    }


    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.shortToast(CameraPlaybackActivity.this, "No data for query date");
            }
        });
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

    @Override
    public void onCreated(Object o) {
        mCameraP2P.generateCameraView(mVideoView.createdView());
    }

    @Override
    public void videoViewClick() {

    }

    @Override
    public void startCameraMove(PTZDirection ptzDirection) {

    }

    @Override
    public void onActionUP() {

    }


}