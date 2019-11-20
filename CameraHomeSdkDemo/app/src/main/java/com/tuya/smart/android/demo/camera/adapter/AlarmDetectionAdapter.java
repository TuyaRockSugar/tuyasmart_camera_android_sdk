package com.tuya.smart.android.demo.camera.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.bean.TimePieceBean;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class AlarmDetectionAdapter extends RecyclerView.Adapter<AlarmDetectionAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private List<CameraMessageBean> cameraMessageBeans;
    private OnItemListener listener;

    public AlarmDetectionAdapter(Context context, List<CameraMessageBean> cameraMessageBeans) {
        mInflater = LayoutInflater.from(context);
        this.cameraMessageBeans = cameraMessageBeans;
    }

    public void updateAlarmDetectionMessage(List<CameraMessageBean> messageBeans){
        if (null != cameraMessageBeans){
            cameraMessageBeans.clear();
            cameraMessageBeans.addAll(messageBeans);
        }
    }

    public void setListener(OnItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.camera_newui_more_motion_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CameraMessageBean ipcVideoBean = cameraMessageBeans.get(position);
        holder.mTvStartTime.setText(ipcVideoBean.getDateTime());
        holder.mTvDescription.setText(ipcVideoBean.getMsgTypeContent());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != listener){
                    listener.onLongClick(ipcVideoBean);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return cameraMessageBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvStartTime;
        TextView mTvDescription;

        public MyViewHolder(final View view) {
            super(view);
            mTvStartTime = view.findViewById(R.id.tv_time_range_start_time);
            mTvDescription = view.findViewById(R.id.tv_alarm_detection_description);
        }
    }


    public interface OnItemListener {
        void onLongClick(CameraMessageBean o);
    }

}
