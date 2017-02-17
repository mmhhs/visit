package com.little.visit.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.little.visit.R;
import com.little.visit.listener.IOnRetryListener;


public class ViewUtil {
	private IOnRetryListener iOnRetryListener;
    private View loadingView;
    private View errorView;
	private View emptyView;

    public ViewUtil(){

    }

	/**
	 * 添加加载视图
	 * @param context
	 * @param loadingString
	 * @param contentView
	 * @param loadLayout
     */
	public void addLoadView(Context context,String loadingString,View contentView,LinearLayout loadLayout){
		try {
			contentView.setVisibility(View.GONE);
			loadLayout.setVisibility(View.VISIBLE);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			loadingView = getLoadView(context, loadingString);
			
			loadLayout.removeAllViews();
			loadLayout.addView(loadingView, layoutParams);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * 移除加载视图
	 * @param contentView
	 * @param loadLayout
	 */
	public void removeLoadView(View contentView,LinearLayout loadLayout){
		try {
			contentView.setVisibility(View.VISIBLE);
			loadLayout.setVisibility(View.GONE);
			loadLayout.removeAllViews();
			loadingView = null;
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * 添加错误视图
	 * @param context
	 * @param errorString
	 * @param contentView
	 * @param loadLayout
	 * @param onRetryListener
     */
	public void addErrorView(Context context,String errorString,View contentView,LinearLayout loadLayout,IOnRetryListener onRetryListener){
		try {
			contentView.setVisibility(View.GONE);
			loadLayout.setVisibility(View.VISIBLE);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			errorView = getErrorView(context, errorString);
			loadLayout.removeAllViews();
			loadLayout.addView(errorView, layoutParams);
			this.iOnRetryListener = onRetryListener;
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * 移除错误视图
	 * @param contentView
	 * @param loadLayout
	 */
	public void removeErrorView(View contentView,LinearLayout loadLayout){
		try {
			contentView.setVisibility(View.VISIBLE);
			loadLayout.setVisibility(View.GONE);
			loadLayout.removeAllViews();
			this.iOnRetryListener = null;
			errorView = null;
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * 添加空视图
	 * @param context
	 * @param str
	 * @param imageResourceId
	 * @param contentView
	 * @param loadLayout
	 * @param onRetryListener
     */
	public void addEmptyView(Context context, String title, String str, int imageResourceId, View contentView, LinearLayout loadLayout, IOnRetryListener onRetryListener){
		try {
			contentView.setVisibility(View.GONE);
			loadLayout.setVisibility(View.VISIBLE);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			emptyView = getEmptyView(context, title,str,imageResourceId);
			loadLayout.removeAllViews();
			loadLayout.addView(emptyView, layoutParams);
			this.iOnRetryListener = onRetryListener;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移除空视图
	 * @param contentView
	 * @param loadLayout
	 */
	public void removeEmptyView(View contentView,LinearLayout loadLayout){
		try {
			contentView.setVisibility(View.VISIBLE);
			loadLayout.setVisibility(View.GONE);
			loadLayout.removeAllViews();
			this.iOnRetryListener = null;
			emptyView = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public View getLoadView(Context context,String loadingString) {
		View view = LayoutInflater.from(context).inflate(R.layout.visit_link_loading,null, false);
	    TextView loadText = (TextView) view.findViewById(R.id.visit_link_loading_item_text);
        if(!loadingString.isEmpty()){
        	loadText.setText(loadingString);
        }
		return view;
	}
	
	public View getErrorView(Context context,String errorString) {
		View view = LayoutInflater.from(context).inflate(R.layout.visit_link_error,null, false);
	    TextView titleText = (TextView) view.findViewById(R.id.visit_link_error_title);
        LinearLayout containLayout = (LinearLayout) view.findViewById(R.id.visit_link_error_layout);
        if(!StringUtil.isEmpty(errorString)){
        	titleText.setText(errorString);
        }
		containLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onRetry();
            }

        });
		return view;
	}

	public View getEmptyView(Context context,String title,String str,int imageResourceId) {
		View view = LayoutInflater.from(context).inflate(R.layout.visit_link_empty,null, false);
		ImageView contentImage = (ImageView) view.findViewById(R.id.visit_link_empty_image);
		TextView titleText = (TextView) view.findViewById(R.id.visit_link_empty_title);
		TextView contentText = (TextView) view.findViewById(R.id.visit_link_empty_content);
		LinearLayout containLayout = (LinearLayout) view.findViewById(R.id.visit_link_empty_layout);
		if(!StringUtil.isEmpty(title)){
			titleText.setText(title);
		}
		if(!StringUtil.isEmpty(str)){
			contentText.setText(str);
		}
		if (imageResourceId!=0){
			contentImage.setImageResource(imageResourceId);
		}
		containLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRetry();
			}

		});
		return view;
	}


    private void onRetry(){
        if(iOnRetryListener !=null){
            this.iOnRetryListener.onRetry();
        }
    }

	public IOnRetryListener getiOnRetryListener() {
		return iOnRetryListener;
	}

	public void setiOnRetryListener(IOnRetryListener iOnRetryListener) {
		this.iOnRetryListener = iOnRetryListener;
	}
}