package com.little.visit.task;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.little.visit.R;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.listener.IOnResultListener;
import com.little.visit.listener.IOnRetryListener;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ViewUtil;

/**
 * 页面内加载线程
 */
public class PageVisitTask extends VisitTask {
	public View contentView;//内容界面
	public LinearLayout loadingLayout;//加载界面
	public IOnRetryListener onRetryListener;//重试操作监听
	public ViewUtil viewUtil;//视图管理

	private IOnResultListener onResultListener;//返回值判断结果监听
	//显示
	private String loadingString = "";//加载中文字
	private boolean showLoading = false;//显示加载

	/**
	 * 本地处理耗时线程
	 * @param context           上下文
	 * @param tagString         线程唯一标识
	 * @param contentView       内容视图
	 * @param loadingLayout     加载视图
	 * @param loadingString     加载中文字
	 * @param showLoading       是否显示loadview 若显示，则contentView、loadView不能为空
	 * @param onRetryListener  重试操作监听
	 */
	public PageVisitTask(Context context, String tagString, View contentView, LinearLayout loadingLayout, String loadingString, boolean showLoading, IOnRetryListener onRetryListener,IOnResultListener onResultListener){
		this.context = context;
		this.tagString = tagString;
		this.contentView = contentView;
		this.loadingLayout = loadingLayout;
		this.loadingString = loadingString;
		this.showLoading = showLoading;
		this.onRetryListener = onRetryListener;
		this.onResultListener = onResultListener;
		init();
	}

	public void init(){
		super.init();
		viewUtil = new ViewUtil();

	}

	@Override
	public void onPreExecute()
	{
		try {
			super.onPreExecute();
			if(StringUtil.isEmpty(loadingString)){
				loadingString = context.getResources().getString(R.string.visit0);
			}
			if (showLoading && contentView != null && loadingLayout != null) {
				viewUtil.addLoadView(context, loadingString, contentView, loadingLayout);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPostExecute(TaskResult result)
	{
		try {
			if(showLoading){
				viewUtil.removeLoadView(contentView, loadingLayout);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!isCancelled()){
			if (onResultListener !=null){
				onResultListener.onDone();
			}
			switch(result){
				case OK:
					if (onResultListener !=null){
						onResultListener.onSuccess();
					}
					break;
				case ERROR:
					if (showLoading) {
						viewUtil.addErrorView(context, context.getString(R.string.visit1),
								contentView, loadingLayout, onRetryListener);
					}
					if (onResultListener !=null){
						onResultListener.onError();
					}
					break;
			}
		}else {
			if (showLoading) {
				viewUtil.addErrorView(context, context.getString(R.string.visit8),
						contentView, loadingLayout, onRetryListener);
				if (onResultListener !=null){
					onResultListener.onCancel();
				}
			}
		}

		super.onPostExecute(result);
	}


	/**
	 * 添加空视图
	 * @param str 描述文字
	 * @param imageResourceId 图片资源
     */
	public void addEmptyView(String title, String str, int imageResourceId){
		viewUtil.addEmptyView(context, title, str, imageResourceId, contentView, loadingLayout, onRetryListener);
	}



}