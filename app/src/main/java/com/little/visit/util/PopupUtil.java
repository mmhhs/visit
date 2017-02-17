package com.little.visit.util;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.little.visit.R;
import com.little.visit.listener.IOnDismissListener;
import com.little.visit.listener.IOnProgressListener;

public class PopupUtil {
    private Context context;

    private boolean dismissOutside = false;//点击界面关闭
    private boolean dismissKeyback = false;//点击返回键关闭
    private int animStyle = 0;//动画资源ID
    private String popupTitle = "";//对话框标题

    //下载框
    private ProgressBar progressBar;//进度条
    private TextView progressText;//进度文字

    private IOnDismissListener onDismissListener;
    private IOnProgressListener onProgressListener;

    public PopupUtil(Context context) {
        this.context = context;
    }


    /**
     * 显示遮挡界面
     * @param view 父视图
     * @param loadStr 加载文字
     * @return
     */
    public PopupWindow showLoadingPopup(View view, String loadStr){
        PopupWindow popupWindow = getLoadingPopup(loadStr);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        return popupWindow;
    }

    public PopupWindow getLoadingPopup(String loadStr) {
        View view = LayoutInflater.from(context).inflate(R.layout.visit_popup_loading,null, false);
        TextView loadText = (TextView) view.findViewById(R.id.visit_link_loading_item_text);
        LinearLayout containerLayout = (LinearLayout) view.findViewById(R.id.visit_popup_loading_layout);
        if(!StringUtil.isEmpty(loadStr)){
            loadText.setText(loadStr);
        }
        final PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        if(animStyle>0){
            popupWindow.setAnimationStyle(animStyle);
        }
        if(dismissKeyback){
            popupWindow.setBackgroundDrawable(new BitmapDrawable()); //使按返回键能够消失
        }
        containerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (dismissOutside) {
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onDismissListener !=null){
                    onDismissListener.onDismiss();
                }
            }
        });
        return popupWindow;
    }


    /**
     * 下载框
     * @param view 父视图
     * @param canCancel 是否可以取消
     * @return
     */
    public PopupWindow showDownloadPopup(View view, boolean canCancel){
        PopupWindow popupWindow = getDownloadPopup(canCancel);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        return popupWindow;
    }

    public PopupWindow getDownloadPopup(boolean canCancel) {
        View view = LayoutInflater.from(context).inflate(R.layout.visit_popup_download,null, false);
        TextView titleText = (TextView) view.findViewById(R.id.visit_popup_download_title);
        progressBar = (ProgressBar) view.findViewById(R.id.visit_popup_download_progressBar);
        progressText = (TextView) view.findViewById(R.id.visit_popup_download_content);
        TextView cancelText = (TextView) view.findViewById(R.id.visit_popup_download_cancel);
        LinearLayout containerLayout = (LinearLayout) view.findViewById(R.id.visit_popup_download_layout);
        LinearLayout cancelLayout = (LinearLayout) view.findViewById(R.id.visit_popup_download_cancel_layout);
        if (!StringUtil.isEmpty(popupTitle)){
            titleText.setText(popupTitle);
        }
        progressBar.setProgress(0);
        progressText.setText("0 %");
        if (canCancel){
            cancelLayout.setVisibility(View.VISIBLE);
        }else {
            cancelLayout.setVisibility(View.GONE);
        }
        final PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        if(animStyle>0){
            popupWindow.setAnimationStyle(animStyle);
        }
        if(dismissKeyback){
            popupWindow.setBackgroundDrawable(new BitmapDrawable()); //使按返回键能够消失
        }
        cancelText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onProgressListener != null) {
                    onProgressListener.onCancel();
                }
                popupWindow.dismiss();
            }

        });
        containerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (dismissOutside) {
                    popupWindow.dismiss();
                }
            }

        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onDismissListener !=null){
                    onDismissListener.onDismiss();
                }
            }
        });

        return popupWindow;
    }

    public void updateProgressInfo(String transferedBytes,long totalBytes){
        try {
            if (progressBar!=null&& progressText !=null){
                long tran = Long.parseLong(transferedBytes);
                int progress = (int)(100*tran/totalBytes);
                progressBar.setProgress(progress);
                progressText.setText(progress + " %");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isDismissOutside() {
        return dismissOutside;
    }

    public void setDismissOutside(boolean dismissOutside) {
        this.dismissOutside = dismissOutside;
    }

    public boolean isDismissKeyback() {
        return dismissKeyback;
    }

    public void setDismissKeyback(boolean dismissKeyback) {
        this.dismissKeyback = dismissKeyback;
    }

    public int getAnimStyle() {
        return animStyle;
    }

    public void setAnimStyle(int animStyle) {
        this.animStyle = animStyle;
    }

    public String getPopupTitle() {
        return popupTitle;
    }

    public void setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
    }

    public IOnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public void setOnDismissListener(IOnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public IOnProgressListener getOnProgressListener() {
        return onProgressListener;
    }

    public void setOnProgressListener(IOnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }
}