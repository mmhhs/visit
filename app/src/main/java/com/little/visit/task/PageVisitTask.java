package com.little.visit.task;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.little.visit.R;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.listener.IOnResultListener;
import com.little.visit.listener.IOnRetryListener;
import com.little.visit.util.HttpUtil;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ToastUtil;
import com.little.visit.util.ViewUtil;

import java.util.Map;

/**
 * 页面内加载线程
 */
public class PageVisitTask extends VisitTask {
	public View contentView;//内容界面
	public LinearLayout loadingLayout;//加载界面
	public IOnRetryListener iOnRetryListener;//重试操作监听
	public ViewUtil viewUtil;//视图管理

	private IOnResultListener iOnResultListener;//返回值判断结果监听
	//显示
	private String loadingString = "";//加载中文字
	private boolean showLoading = false;//显示加载框
	private boolean showTipSuccess = false;//成功时显示提示信息
	private boolean showTipError = true;//错误时显示提示信息

	/**
	 * 本地处理耗时线程
	 * @param context           上下文
	 * @param tagString         线程唯一标识
	 * @param contentView       内容视图
	 * @param loadingLayout     加载视图
	 * @param loadingString     加载中文字
	 * @param showLoading       是否显示loadview 若显示，则contentView、loadView不能为空
	 * @param iOnRetryListener  重试操作监听
	 */
	public PageVisitTask(Context context, String tagString, View contentView, LinearLayout loadingLayout, String loadingString, boolean showLoading, IOnRetryListener iOnRetryListener){
		this.context = context;
		this.tagString = tagString;
		this.contentView = contentView;
		this.loadingLayout = loadingLayout;
		this.loadingString = loadingString;
		this.showLoading = showLoading;
		this.iOnRetryListener = iOnRetryListener;
		init();
	}

	/**
	 * 网络加载线程
	 * @param context           上下文
	 * @param tagString         线程唯一标识
	 * @param contentView       内容视图
	 * @param loadingLayout     加载视图
	 * @param loadingString     加载中文字
	 * @param showLoading       是否显示loadview 若显示，则contentView、loadView不能为空
	 * @param iOnRetryListener  重试操作监听
	 * @param httpUrl           访问路径
	 * @param argMap            参数集合
	 * @param accessType        访问方式
	 */
	public PageVisitTask(Context context, String tagString, View contentView, LinearLayout loadingLayout, String loadingString, boolean showLoading, IOnRetryListener iOnRetryListener, String httpUrl, Map<String, Object> argMap, int accessType){
		this.context = context;
		this.tagString = tagString;
		this.contentView = contentView;
		this.loadingLayout = loadingLayout;
		this.loadingString = loadingString;
		this.showLoading = showLoading;
		this.iOnRetryListener = iOnRetryListener;
		this.httpUrl = httpUrl;
		this.argMap = argMap;
		this.accessType = accessType;
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
			if(!HttpUtil.isNet(context)){
				netFlag = NET_ERROR;
				if(StringUtil.isEmpty(httpUrl)){
					//不访问网络的情况
					if (showLoading) {
						if(contentView == null || loadingLayout == null){
							return;
						}
						viewUtil.addLoadView(context, loadingString, contentView, loadingLayout);
					}
				}else {
					//网络加载线程 没有网络的情况
					if (showLoading) {
						if(contentView == null || loadingLayout == null){
							return;
						}
						viewUtil.addErrorView(context, context.getString(R.string.visit3),
								contentView, loadingLayout, iOnRetryListener);
					}else {
						ToastUtil.addToast(context, context.getString(R.string.visit3));
					}
				}
			}else {
				if (showLoading) {
					if(contentView == null || loadingLayout == null){
						return;
					}
					viewUtil.addLoadView(context, loadingString, contentView, loadingLayout);
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
			if(showLoading){
				viewUtil.removeLoadView(contentView, loadingLayout);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (iOnResultListener!=null){
			iOnResultListener.onDone(this);
		}
		switch(result){
			case OK:
				if (iOnResultListener!=null){
					iOnResultListener.onSuccess(this);
				}
				if (!StringUtil.isEmpty(resultMsg)&&showTipSuccess){
					ToastUtil.addToast(context, resultMsg +"");
				}
				break;
			case ERROR:
				if (showLoading) {
					viewUtil.addErrorView(context, context.getString(R.string.visit1),
							contentView, loadingLayout, iOnRetryListener);
				}
				if (iOnResultListener!=null){
					iOnResultListener.onError(this);
				}
				if (!StringUtil.isEmpty(resultMsg)&&showTipError){
					ToastUtil.addToast(context, resultMsg +"");
				}
				dealLoginInvalid();
				break;
			case CANCELLED:
				if (showLoading) {
					viewUtil.addErrorView(context, context.getString(R.string.visit4),
							contentView, loadingLayout, iOnRetryListener);
				}
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

	/**
	 * 添加空视图
	 * @param str 描述文字
	 * @param imageResourceId 图片资源
     */
	public void addEmptyView(String title, String str, int imageResourceId){
		viewUtil.addEmptyView(context, title, str, imageResourceId, contentView, loadingLayout, iOnRetryListener);
	}

	public View getContentView() {
		return contentView;
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
	}

	public LinearLayout getLoadingLayout() {
		return loadingLayout;
	}

	public void setLoadingLayout(LinearLayout loadingLayout) {
		this.loadingLayout = loadingLayout;
	}

	public IOnRetryListener getiOnRetryListener() {
		return iOnRetryListener;
	}

	public void setiOnRetryListener(IOnRetryListener iOnRetryListener) {
		this.iOnRetryListener = iOnRetryListener;
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




}