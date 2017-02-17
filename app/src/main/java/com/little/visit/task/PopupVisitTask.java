package com.little.visit.task;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import com.little.visit.R;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.listener.IOnResultListener;
import com.little.visit.util.HttpUtil;
import com.little.visit.util.PopupUtil;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ToastUtil;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * 弹窗加载线程
 */
public class PopupVisitTask extends VisitTask{
	public PopupUtil popupUtil;
	public View contentView;//视图 承载弹窗
	public PopupWindow loadPopupWindow;//加载框

	private IOnResultListener iOnResultListener;//返回值判断结果监听
	//显示
	private String loadingString = "";//加载中文字
	private boolean showLoading = false;//显示加载框
	private boolean showTipSuccess = false;//成功时显示提示信息
	private boolean showTipError = true;//错误时显示提示信息
	private boolean showNetToast = true;//显示网络问题toast

	/**
	 * 本地处理耗时线程
	 * @param context      上下文
	 * @param tagString    线程唯一标识
	 * @param contentView  父类视图
	 * @param loadsString  显示文字
	 * @param showLoading  是否显示弹窗 若显示，则contentView不能为空
	 */
	public PopupVisitTask(Context context, String tagString, View contentView, String loadsString, boolean showLoading){
		this.context = context;
		this.contentView = contentView;
		this.loadingString = loadsString;
		this.showLoading = showLoading;
		this.tagString = tagString;
		init();
	}

	/**
	 * 网络加载线程
	 * @param context      上下文
	 * @param tagString    线程唯一标识
	 * @param contentView 父类视图
	 * @param loadsString 显示文字
	 * @param showLoading 是否显示dialog 若显示parentView不能为空
	 * @param httpUrl 访问路径
	 * @param argMap 参数集合
	 * @param accessType 访问方式
	 */
	public PopupVisitTask(Context context, String tagString, View contentView, String loadsString, boolean showLoading, String httpUrl, Map<String, Object> argMap, int accessType){
		this.context = context;
		this.contentView = contentView;
		this.loadingString = loadsString;
		this.showLoading = showLoading;
		this.httpUrl = httpUrl;
		this.argMap = argMap;
		this.accessType = accessType;
		this.tagString = tagString;
		init();
	}

	/**
	 * 网络加载线程-上传文件-文件标识
	 * @param context      上下文
	 * @param tagString    线程唯一标识
	 * @param contentView 父类视图
	 * @param loadsString 显示文字
	 * @param showLoading 是否显示dialog 若显示parentView不能为空
	 * @param httpUrl 访问路径
	 * @param argMap 参数集合
	 * @param fileList 文件集合
	 * @param key 服务器判断文件标识
	 * @param accessType 访问方式
	 */
	public PopupVisitTask(Context context, String tagString, View contentView, String loadsString, boolean showLoading, String httpUrl, Map<String, Object> argMap, List<File> fileList, String key, int accessType){
		this.context = context;
		this.contentView = contentView;
		this.loadingString = loadsString;
		this.showLoading = showLoading;
		this.httpUrl = httpUrl;
		this.argMap = argMap;
		this.fileList = fileList;
		this.keyString = key;
		this.accessType = accessType;
		this.tagString = tagString;
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
			if(!HttpUtil.isNet(context)){
				netFlag = NET_ERROR;
				if(StringUtil.isEmpty(httpUrl)){
					//不访问网络的情况
					if (showLoading) {
						if(contentView ==null){
							return;
						}
						loadPopupWindow = popupUtil.showLoadingPopup(contentView, loadingString);
					}
				}else {
					//网络加载线程 没有网络的情况
					if (showNetToast) {
						ToastUtil.addToast(context, context.getResources().getString(R.string.visit3));
					}
				}
			}else {
				if (showLoading) {
					if(contentView ==null){
						return;
					}
					loadPopupWindow = popupUtil.showLoadingPopup(contentView, loadingString);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPostExecute(TaskResult result)
	{
		try {
			if (!StringUtil.isEmpty(httpUrl) && netFlag == NET_ERROR) {
				//网络加载线程 没有网络的情况
				return;
			}
			if (loadPopupWindow!=null) {
				loadPopupWindow.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(iOnResultListener !=null){
			this.iOnResultListener.onDone(this);
		}
		switch(result){
			case OK:
				if(iOnResultListener !=null){
					this.iOnResultListener.onSuccess(this);
				}
				if (!StringUtil.isEmpty(resultMsg)&&showTipSuccess){
					ToastUtil.addToast(context, resultMsg + "");
				}
				break;
			case ERROR:
				if(iOnResultListener !=null){
					this.iOnResultListener.onError(this);
				}
				if (!StringUtil.isEmpty(resultMsg)&&showTipError){
					ToastUtil.addToast(context, resultMsg +"");
				}
				dealLoginInvalid();
				break;
			case CANCELLED:
				ToastUtil.addToast(context, context.getString(R.string.visit4));
				break;
			default:
				break;
		}
		super.onPostExecute(result);
	}

	/**
	 * 处理登录失效
	 */
	public void dealLoginInvalid(){
		if (isLoginInvalid()){
			resultJudge.dealLoginInvalid(context,contentView);
		}
	}

	public View getContentView() {
		return contentView;
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
	}

	public PopupWindow getLoadPopupWindow() {
		return loadPopupWindow;
	}

	public void setLoadPopupWindow(PopupWindow loadPopupWindow) {
		this.loadPopupWindow = loadPopupWindow;
	}

	public IOnResultListener getiOnResultListener() {
		return iOnResultListener;
	}

	public void setiOnResultListener(IOnResultListener iOnResultListener) {
		this.iOnResultListener = iOnResultListener;
	}

	public String getLoadingString() {
		return loadingString;
	}

	public void setLoadingString(String loadingString) {
		this.loadingString = loadingString;
	}

	public boolean isShowLoading() {
		return showLoading;
	}

	public void setShowLoading(boolean showLoading) {
		this.showLoading = showLoading;
	}

	public boolean isShowTipSuccess() {
		return showTipSuccess;
	}

	public void setShowTipSuccess(boolean showTipSuccess) {
		this.showTipSuccess = showTipSuccess;
	}

	public boolean isShowTipError() {
		return showTipError;
	}

	public void setShowTipError(boolean showTipError) {
		this.showTipError = showTipError;
	}

	public boolean isShowNetToast() {
		return showNetToast;
	}

	public void setShowNetToast(boolean showNetToast) {
		this.showNetToast = showNetToast;
	}
}