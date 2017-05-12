package com.little.visit.task;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.little.visit.R;
import com.little.visit.impl.ResultJudge;
import com.little.visit.listener.IOnRetryListener;
import com.little.visit.listener.IOnTaskListener;
import com.little.visit.listener.IOnVisitListener;
import com.little.visit.listener.IOnVisitResultListener;
import com.little.visit.model.ResultEntity;
import com.little.visit.util.HttpUtil;
import com.little.visit.util.JacksonUtil;
import com.little.visit.util.PopupUtil;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ToastUtil;
import com.little.visit.util.ViewUtil;
import com.little.visit.volley.VolleyUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VolleyTask implements IOnTaskListener{
    public final static int NET_ERROR = 6;//没有网络
    public final static int POPUPSTYLE = 1;//弹窗
    public final static int VIEWSTYLE = 2;//界面
    public final static int INTERFACE_VISIT = 1;//接口
    public final static int UPLOAD_FILE_VISIT = 2;//文件上传
    public final static int DOWNLOAD_FILE_VISIT = 3;//文件下载
    public final static int DOWNLOAD_IMAGE_VISIT = 4;//图片下载
    private int showStyle = POPUPSTYLE;//遮挡方式：1是弹窗，2是界面
    private Context context;
    private View contentView;//内容界面
    private LinearLayout loadingLayout;//加载界面
    private IOnRetryListener onRetryListener;//重试操作监听
    private ViewUtil viewUtil;//视图管理

    private PopupUtil popupUtil;//弹窗管理
    private PopupWindow loadPopupWindow;//加载框

    private IOnVisitResultListener onVisitResultListener;//返回值判断结果监听
    //显示
    private String loadingString = "";//加载中文字
    private boolean showLoading = false;//显示加载框
    private boolean showTipSuccess = false;//成功时显示提示信息
    private boolean showTipError = true;//错误时显示提示信息
    private boolean showNetToast = true;//显示网络问题toast

    //访问
    private int visitType = INTERFACE_VISIT;//访问类型 接口，文件上传，文件下载，图片下载
    private int accessType;//访问方式
    private String httpUrl = "";//访问路径
    private Map<String, String> argMap;//访问参数
    private List<File> fileList;//上传文件列表
    private String filePath = "";//下载文件存储路径
    private String keyString = "FileData";//上传文件键值
    private String tagString = "VolleyTask";//线程标记
    private VolleyUtil volleyUtil;
    private int netFlag = 0;//网络标识
    //解析
    private ResultEntity resultEntity;//返回值解析结果父类 向上转型
    public Class parseClass;//用于解析的实体类
    public ResultJudge resultJudge;//返回值判断实现类，子类继承需设置*
    //返回值
    private String resultMsg;//返回提示信息
    private String resultsString = null;//返回值
    private boolean loginInvalid = false;//登录失效
    private boolean isCanceled = false;//任务终止
    private boolean showErrorView = false;//显示错误页面


    /**
     * 接口访问 弹窗遮挡
     * @param context
     * @param tagString
     * @param contentView
     * @param showLoading
     * @param httpUrl
     * @param argMap
     * @param accessType
     * @param parseClass
     */
    public VolleyTask(Context context, String tagString, View contentView, boolean showLoading, String httpUrl, Map<String, String> argMap, int accessType, Class parseClass) {
        this.context = context;
        this.showStyle = POPUPSTYLE;
        this.contentView = contentView;
        this.showLoading = showLoading;
        this.visitType = INTERFACE_VISIT;
        this.httpUrl = httpUrl;
        this.argMap = argMap;
        this.tagString = tagString;
        this.parseClass = parseClass;
        this.accessType = accessType;
        init();
    }

    /**
     * 上传文件
     * @param context
     * @param tagString
     * @param contentView
     * @param showLoading
     * @param httpUrl
     * @param argMap
     * @param fileList
     * @param keyString
     * @param parseClass
     */
    public VolleyTask(Context context, String tagString, View contentView, boolean showLoading, String httpUrl, Map<String, String> argMap, List<File> fileList, String keyString, Class parseClass) {
        this.context = context;
        this.showStyle = POPUPSTYLE;
        this.contentView = contentView;
        this.showLoading = showLoading;
        this.visitType = UPLOAD_FILE_VISIT;
        this.httpUrl = httpUrl;
        this.argMap = argMap;
        this.tagString = tagString;
        this.parseClass = parseClass;
        this.fileList = fileList;
        this.keyString = keyString;
        init();
    }

    /**
     * 文件或图片下载
     * @param context
     * @param tagString
     * @param contentView
     * @param showLoading
     * @param visitType
     * @param httpUrl
     * @param filePath
     * @param parseClass
     */
    public VolleyTask(Context context, String tagString, View contentView, boolean showLoading, int visitType, String httpUrl, String filePath, Class parseClass) {
        this.context = context;
        this.showStyle = POPUPSTYLE;
        this.contentView = contentView;
        this.showLoading = showLoading;
        this.visitType = visitType;
        this.httpUrl = httpUrl;
        this.tagString = tagString;
        this.parseClass = parseClass;
        this.filePath = filePath;
        init();
    }


    /**
     * 接口访问 界面遮挡
     * @param context
     * @param tagString
     * @param contentView
     * @param loadingLayout
     * @param showLoading
     * @param httpUrl
     * @param argMap
     * @param accessType
     * @param parseClass
     * @param onRetryListener
     */
    public VolleyTask(Context context, String tagString, View contentView, LinearLayout loadingLayout, boolean showLoading, String httpUrl, Map<String, String> argMap, int accessType, Class parseClass, IOnRetryListener onRetryListener) {
        this.context = context;
        this.showStyle = VIEWSTYLE;
        this.contentView = contentView;
        this.loadingLayout = loadingLayout;
        this.onRetryListener = onRetryListener;
        this.visitType = INTERFACE_VISIT;
        this.accessType = accessType;
        this.httpUrl = httpUrl;
        this.argMap = argMap;
        this.parseClass = parseClass;
        this.tagString = tagString;
        this.showLoading = showLoading;
        init();
    }

    private void init(){
        if (parseClass==null){
            parseClass = ResultEntity.class;
        }
        resultJudge = ResultJudge.getInstance();
        volleyUtil = VolleyUtil.getInstance(context);
        if (showStyle==POPUPSTYLE){
            popupUtil = new PopupUtil(context);
        }else if (showStyle==VIEWSTYLE){
            viewUtil = new ViewUtil();
        }
        if(StringUtil.isEmpty(loadingString)){
            loadingString = context.getResources().getString(R.string.visit0);
        }
    }

    public void execute(){
        onPreExecute();
        if(netFlag!=NET_ERROR){
           onVisit();
        }
    }

    @Override
    public void onPreExecute() {
        if(!HttpUtil.isNet(context)){
            netFlag = NET_ERROR;
            if (showStyle==POPUPSTYLE){
                if (showNetToast) {
                    ToastUtil.addToast(context, context.getResources().getString(R.string.visit3));
                }
            }else if (showStyle==VIEWSTYLE){
                if (showLoading) {
                    if(contentView != null && loadingLayout != null){
                        viewUtil.addErrorView(context, context.getString(R.string.visit3),
                                contentView, loadingLayout, onRetryListener);
                    }
                }else {
                    ToastUtil.addToast(context, context.getString(R.string.visit3));
                }
            }
        }else {
            if (showLoading) {
                if (showStyle==POPUPSTYLE){
                    if(contentView !=null){
                        loadPopupWindow = popupUtil.showLoadingPopup(contentView, loadingString);
                    }
                }else if (showStyle==VIEWSTYLE){
                    if(contentView != null && loadingLayout != null){
                        viewUtil.addLoadView(context, loadingString, contentView, loadingLayout);
                    }
                }
            }
        }
    }



    @Override
    public void onVisit() {
        switch (visitType){
            case INTERFACE_VISIT:
                volleyUtil.visit(accessType, httpUrl, tagString, argMap, new IOnVisitListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onError() {
                        onError();
                    }

                    @Override
                    public void onFinish() {
                        onFinish();
                    }

                    @Override
                    public void onCancel() {
                        onCancel();
                    }
                });
                break;
            case UPLOAD_FILE_VISIT:
                volleyUtil.uploadFile(httpUrl, tagString, keyString, fileList, argMap, new IOnVisitListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onError() {
                        onError();
                    }

                    @Override
                    public void onFinish() {
                        onFinish();
                    }

                    @Override
                    public void onCancel() {
                        onCancel();
                    }
                });
                break;
            case DOWNLOAD_FILE_VISIT:
                volleyUtil.downloadFile(httpUrl, tagString, filePath, new IOnVisitListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onError() {
                        onError();
                    }

                    @Override
                    public void onFinish() {
                        onFinish();
                    }

                    @Override
                    public void onCancel() {
                        onCancel();
                    }
                });
                break;
            case DOWNLOAD_IMAGE_VISIT:
                volleyUtil.downloadImage(httpUrl, tagString, new IOnVisitListener<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onError() {
                        onError();
                    }

                    @Override
                    public void onFinish() {
                        onFinish();
                    }

                    @Override
                    public void onCancel() {
                        onCancel();
                    }
                });
                break;
        }
    }

    @Override
    public void onSuccess(Object response) {
        if (!isCanceled){
            switch (visitType) {
                case INTERFACE_VISIT:
                case UPLOAD_FILE_VISIT:
                    resultsString = (String)response;
                    JacksonUtil json = JacksonUtil.getInstance();
                    resultEntity = (ResultEntity)json.readValue(resultsString, parseClass);
                    if(resultEntity !=null){
                        resultMsg = resultEntity.getMsg();
                        if(resultJudge.judgeSuccess("" + resultEntity.getCode())){
                            if(onVisitResultListener !=null){
                                this.onVisitResultListener.onSuccess(resultEntity);
                            }
                            if (!StringUtil.isEmpty(resultMsg)&&showTipSuccess){
                                ToastUtil.addToast(context, resultMsg + "");
                            }
                        }else{
                            judgeLoginInvalid(""+ resultEntity.getCode());
                            if(onVisitResultListener !=null){
                                this.onVisitResultListener.onError(resultMsg);
                            }
                            if (!StringUtil.isEmpty(resultMsg)&&showTipError){
                                ToastUtil.addToast(context, resultMsg +"");
                            }
                            dealLoginInvalid();
                            if (showStyle==VIEWSTYLE){
                                if (showLoading) {
                                    showErrorView = true;
                                    viewUtil.addErrorView(context, context.getString(R.string.visit1),
                                            contentView, loadingLayout, onRetryListener);
                                }
                            }
                        }
                    }else{
                        ToastUtil.addToast(context, context.getString(R.string.visit4));
                    }
                    break;
                case DOWNLOAD_FILE_VISIT:
                    String filePath = (String)response;
                    if(onVisitResultListener !=null){
                        this.onVisitResultListener.onSuccess(filePath);
                    }
                    break;
                case DOWNLOAD_IMAGE_VISIT:
                    Bitmap bitmap = (Bitmap)response;
                    if(onVisitResultListener !=null){
                        this.onVisitResultListener.onSuccess(bitmap);
                    }
                    break;
            }
        }
    }

    @Override
    public void onError() {
        resultMsg = context.getString(R.string.visit4);
        if(onVisitResultListener !=null){
            this.onVisitResultListener.onError(resultMsg);
        }
        if (!StringUtil.isEmpty(resultMsg)&&showTipError){
            ToastUtil.addToast(context, resultMsg);
        }
        if (showStyle==VIEWSTYLE){
            if (showLoading) {
                showErrorView = true;
                viewUtil.addErrorView(context, resultMsg,
                        contentView, loadingLayout, onRetryListener);
            }
        }
    }

    @Override
    public void onFinish() {
        try {
            if (showLoading){
                if (showStyle==POPUPSTYLE){
                    if (loadPopupWindow!=null) {
                        loadPopupWindow.dismiss();
                    }
                }else if (showStyle==VIEWSTYLE){
                    if (!showErrorView){
                        viewUtil.removeLoadView(contentView, loadingLayout);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(onVisitResultListener !=null){
            this.onVisitResultListener.onFinish();
        }
    }

    @Override
    public void onCancel() {
        isCanceled = true;
    }

    /**
     * 判断登录失效
     */
    private void judgeLoginInvalid(String code){
        if (resultJudge.judgeLoginInvalid("" + code)){
            loginInvalid = true;
        }else {
            loginInvalid = false;
        }
    }

    /**
     * 处理登录失效
     */
    public void dealLoginInvalid(){
        if (isLoginInvalid()){
            resultJudge.dealLoginInvalid(context,contentView);
        }
    }

    public boolean isLoginInvalid() {
        return loginInvalid;
    }

    public IOnVisitResultListener getOnVisitResultListener() {
        return onVisitResultListener;
    }

    public void setOnVisitResultListener(IOnVisitResultListener onVisitResultListener) {
        this.onVisitResultListener = onVisitResultListener;
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

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getResultsString() {
        return resultsString;
    }

    public void setResultsString(String resultsString) {
        this.resultsString = resultsString;
    }

    public ResultJudge getResultJudge() {
        return resultJudge;
    }

    public void setResultJudge(ResultJudge resultJudge) {
        this.resultJudge = resultJudge;
    }
}
