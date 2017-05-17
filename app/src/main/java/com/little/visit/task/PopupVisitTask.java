package com.little.visit.task;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import com.little.visit.R;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.listener.IOnResultListener;
import com.little.visit.util.PopupUtil;
import com.little.visit.util.StringUtil;


/**
 * 弹窗加载线程
 */
public class PopupVisitTask extends VisitTask{
	public PopupUtil popupUtil;
	public View contentView;//视图 承载弹窗
	public PopupWindow loadPopupWindow;//加载框

	private IOnResultListener onResultListener;//返回值判断结果监听
	//显示
	private String loadingString = "";//加载中文字
	private boolean showLoading = false;//显示加载框

	/**
	 * 本地处理耗时线程
	 * @param context      上下文
	 * @param tagString    线程唯一标识
	 * @param contentView  父类视图
	 * @param loadsString  显示文字
	 * @param showLoading  是否显示弹窗 若显示，则contentView不能为空
	 * @param onResultListener  结果监听
	 */
	public PopupVisitTask(Context context, String tagString, View contentView, String loadsString, boolean showLoading,IOnResultListener onResultListener){
		this.context = context;
		this.contentView = contentView;
		this.loadingString = loadsString;
		this.showLoading = showLoading;
		this.tagString = tagString;
		this.onResultListener = onResultListener;
		init();
	}



	public void init(){
		super.init();
		popupUtil = new PopupUtil(context);
	}

	@Override
	public void onPreExecute()
	{
		try {
			super.onPreExecute();
			if(StringUtil.isEmpty(loadingString)){
				loadingString = context.getResources().getString(R.string.visit0);
			}
			if (showLoading&&contentView!=null) {
				loadPopupWindow = popupUtil.showLoadingPopup(contentView, loadingString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPostExecute(TaskResult result)
	{
		try {
			if (loadPopupWindow!=null) {
				loadPopupWindow.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!isCancelled()){
			if(onResultListener !=null){
				this.onResultListener.onDone();
			}
			switch(result){
				case OK:
					if(onResultListener !=null){
						this.onResultListener.onSuccess();
					}
					break;
				case ERROR:
					if(onResultListener !=null){
						this.onResultListener.onError();
					}
					break;
				default:
					break;
			}
		}else {
			if(onResultListener !=null){
				this.onResultListener.onCancel();
			}
		}
		super.onPostExecute(result);
	}


}