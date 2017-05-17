package com.little.visit.task;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.little.visit.okhttp.OkHttpUtil;
import com.little.visit.util.HttpUtil;
import com.little.visit.util.JacksonUtil;
import com.little.visit.util.PopupUtil;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ToastUtil;
import com.little.visit.util.ViewUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class OKHttpTask implements IOnTaskListener{
    public final static int NET_ERROR = 6;//没有网络
    public final static int POPUPSTYLE = 1;//弹窗
    public final static int VIEWSTYLE = 2;//界面
    public final static int PROGRESSSTYLE = 3;//进度弹窗
    public final static int INTERFACE_VISIT = 1;//接口
    public final static int UPLOAD_FILE_VISIT = 2;//文件上传
    public final static int DOWNLOAD_FILE_VISIT = 3;//文件下载
    private int showStyle = POPUPSTYLE;//遮挡方式：1是弹窗，2是界面
    private Context context;
    private View contentView;//内容界面
    private LinearLayout loadingLayout;//加载界面
    private IOnRetryListener onRetryListener;//重试操作监听
    private ViewUtil viewUtil;//视图管理

    private PopupUtil popupUtil;//弹窗管理
    private PopupWindow loadPopupWindow;//加载框
    private String popupTitle = "";//标题

    private IOnVisitResultListener onVisitResultListener;//返回值判断结果监听
    //显示
    private String loadingString = "";//加载中文字
    private boolean showLoading = false;//显示加载框
    private boolean showTipSuccess = false;//成功时显示提示信息
    private boolean showTipError = true;//错误时显示提示信息
    private boolean showNetToast = true;//显示网络问题toast
    private long totalSize=0;//下载文件大小
    private boolean silenceDownload = false;//静默下载
    private boolean canCancelDownload = false;//能否终止下载

    //访问
    private int visitType = INTERFACE_VISIT;//访问类型 接口，文件上传，文件下载，图片下载
    private int accessType;//访问方式
    private String httpUrl = "";//访问路径
    private Map<String, String> argMap;//访问参数
    private List<File> fileList;//上传文件列表
    private String filePath = "";//下载文件存储路径
    private String keyString = "FileData";//上传文件键值
    private String tagString = "OKHttpTask";//线程标记
    private OkHttpUtil okHttpUtil;
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
    private Handler mHandler;
    private IOnVisitListener<String> onVisitListener;


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
    public OKHttpTask(Context context, String tagString, View contentView, boolean showLoading, String httpUrl, Map<String, String> argMap, int accessType, Class parseClass) {
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
    public OKHttpTask(Context context, String tagString, View contentView, boolean showLoading, String httpUrl, Map<String, String> argMap, List<File> fileList, String keyString, Class parseClass) {
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
    public OKHttpTask(Context context, String tagString, int showStyle, View contentView, boolean showLoading, int visitType, String httpUrl, String filePath, Class parseClass) {
        this.context = context;
        this.showStyle = showStyle;
        this.contentView = contentView;
        this.showLoading = showLoading;
        this.silenceDownload = !showLoading;
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
    public OKHttpTask(Context context, String tagString, View contentView, LinearLayout loadingLayout, boolean showLoading, String httpUrl, Map<String, String> argMap, int accessType, Class parseClass, IOnRetryListener onRetryListener) {
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
        okHttpUtil = OkHttpUtil.getInstance(context);
        if (showStyle==POPUPSTYLE){
            popupUtil = new PopupUtil(context);
            if (!StringUtil.isEmpty(popupTitle)){
                popupUtil.setPopupTitle(popupTitle);
            }
        }else if (showStyle==VIEWSTYLE){
            viewUtil = new ViewUtil();
        }else if (showStyle==PROGRESSSTYLE){
            popupUtil = new PopupUtil(context);
            if (!StringUtil.isEmpty(popupTitle)){
                popupUtil.setPopupTitle(popupTitle);
            }
        }
        if(StringUtil.isEmpty(loadingString)){
            loadingString = context.getResources().getString(R.string.visit0);
        }
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int type = msg.getData().getInt("type");
                if (type==1){
                    String response = msg.getData().getString("response");
                    onTaskSuccess(response);
                }else if (type==2){
                    long bytes = msg.getData().getLong("bytes");
                    long contentLength = msg.getData().getLong("contentLength");
                    boolean done = msg.getData().getBoolean("done");
                    onProgress(bytes,contentLength,done);
                }else if (type==3){
                    onTaskError();
                }else if (type==4){
                    onTaskFinish();
                }else if (type==5){
                    onTaskCancel();
                }
            }
        };
        onVisitListener = new IOnVisitListener<String>() {
            @Override
            public void onSuccess(String response) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("type",1);
                bundle.putString("response",response);
                message.setData(bundle);
                mHandler.sendMessage(message);

            }

            @Override
            public void onError() {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("type",3);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onFinish() {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("type",4);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onCancel() {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("type",5);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onProgress(long bytes, long contentLength, boolean done) {

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("type",2);
                bundle.putLong("bytes",bytes);
                bundle.putLong("contentLength", contentLength);
                bundle.putBoolean("done", done);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        };
    }

    public void execute(){
        onTaskPreExecute();
        if(netFlag!=NET_ERROR){
            onTaskVisit();
        }
    }

    @Override
    public void onTaskPreExecute() {
        try {
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
                }else if (showStyle==PROGRESSSTYLE){
                    if (showNetToast) {
                        ToastUtil.addToast(context, context.getResources().getString(R.string.visit3));
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
                    }else if (showStyle==PROGRESSSTYLE){
                        if (!silenceDownload&&contentView!=null){
                            loadPopupWindow = popupUtil.showDownloadPopup(contentView, canCancelDownload);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onTaskVisit() {
        switch (visitType){
            case INTERFACE_VISIT:
                okHttpUtil.visit(accessType, tagString, httpUrl, argMap, onVisitListener);
                break;
            case UPLOAD_FILE_VISIT:
                okHttpUtil.uploadFile(tagString, httpUrl, keyString, fileList, argMap, onVisitListener);
                break;
            case DOWNLOAD_FILE_VISIT:
                okHttpUtil.downloadFile(tagString, httpUrl, filePath, onVisitListener);
                break;
        }
    }

    @Override
    public void onTaskSuccess(Object response) {
        try {
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskError() {
        try {
            if (!isCanceled) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskFinish() {
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
                }else if (showStyle==PROGRESSSTYLE){
                    if (loadPopupWindow!=null) {
                        loadPopupWindow.dismiss();
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
    public void onTaskCancel() {
        isCanceled = true;
    }

    @Override
    public void onProgress(long bytes, long contentLength, boolean done) {
        if (!silenceDownload){
            popupUtil.updateProgressInfo(""+bytes, contentLength);
        }
        if (onVisitResultListener!=null){
            onVisitResultListener.onProgress(bytes,contentLength);
        }
    }

    /**
     * 添加空视图
     * @param str 描述文字
     * @param imageResourceId 图片资源
     */
    public void addEmptyView(String title, String str, int imageResourceId){
        viewUtil.addEmptyView(context, title, str, imageResourceId, contentView, loadingLayout, onRetryListener);
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

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public String getPopupTitle() {
        return popupTitle;
    }

    public void setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
    }

    public boolean isSilenceDownload() {
        return silenceDownload;
    }

    public void setSilenceDownload(boolean silenceDownload) {
        this.silenceDownload = silenceDownload;
    }

    public boolean isCanCancelDownload() {
        return canCancelDownload;
    }

    public void setCanCancelDownload(boolean canCancelDownload) {
        this.canCancelDownload = canCancelDownload;
    }

    public PopupUtil getPopupUtil() {
        return popupUtil;
    }
}
