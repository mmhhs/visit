package com.little.visit.task;

import android.content.Context;

import com.little.visit.TaskConstant;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.TaskManager;
import com.little.visit.impl.LittleHttpClient;
import com.little.visit.impl.ResultJudge;
import com.little.visit.listener.IOnBackgroundListener;
import com.little.visit.listener.IOnEncryptListener;
import com.little.visit.model.ResultEntity;
import com.little.visit.network.VisitHttpClient;
import com.little.visit.util.JacksonUtil;
import com.little.visit.util.StringUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 实现线程管理
 * 网络访问
 * 返回值解析成实体
 */
public abstract class VisitTask extends AsycnTask<Void, String, TaskResult> {
    public Context context;//上下文
    //访问
    public LittleHttpClient littleHttpClient;//访问实现类，子类继承需设置*
    public int accessType;//访问方式
    public String httpUrl = "";//访问路径
    public Map<String, Object> argMap;//访问参数
    public List<File> fileList;//上传文件列表
    public String keyString = "FileData";//上传文件键值
    public final static int NET_ERROR = 6;//没有网络
    public int netFlag = 0;//网络标识
    //线程管理
    public TaskManager taskManager = TaskManager.getTaskManagerInstance();//AsycnTask线程管理类
    public String tagString = "VisitTask";//线程标记
    //解析
    private ResultEntity resultEntity;//返回值解析结果父类 向上转型
    public Class parseClass;//用于解析的实体类
    public ResultJudge resultJudge;//返回值判断实现类，子类继承需设置*
    //返回值
    public String resultMsg;//返回提示信息
    private String resultsString = null;//返回值
    public boolean loginInvalid = false;//登录失效
    //监听
    public IOnBackgroundListener onBackgroundListener;//后台运行代码监听
    public IOnEncryptListener onEncryptListener;//对参数增加加密操作时使用


    public VisitTask() {
        init();
    }

    public void init(){
        addTask();
        parseClass = ResultEntity.class;
        littleHttpClient = VisitHttpClient.getInstance();
        resultJudge = ResultJudge.getInstance();
    }


    @Override
    public void onPreExecute()
    {

    }

    @Override
    public TaskResult doInBackground(Void... params) {
        TaskResult taskResult = TaskResult.NOTHING;
        //不访问网络的情况
        if(StringUtil.isEmpty(httpUrl)){
            taskResult = doOnBackgroundListener(this);
            return taskResult;
        }
        //无网络情况
        if (netFlag == NET_ERROR) {
            return taskResult;
        }
        if (onEncryptListener !=null){
            onEncryptListener.onEncrypt(argMap);
        }
        switch (accessType) {
            case TaskConstant.POST:
                try {
                    resultsString = littleHttpClient.post(httpUrl, argMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TaskConstant.PUT:
                try {
                    resultsString = littleHttpClient.put(httpUrl, argMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TaskConstant.GET:
                try {
                    resultsString = littleHttpClient.get(httpUrl, argMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TaskConstant.UPLOAD:
                try {
                    resultsString = littleHttpClient.uploadFiles(httpUrl, argMap, fileList, keyString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        if (!isCancelled()){
            if(StringUtil.isEmpty(resultsString)){
                taskResult = TaskConstant.TaskResult.CANCELLED;
            }else{
                if (onBackgroundListener ==null) {
                    onBackgroundListener = defaultBackgroundListener;
                }
                taskResult = doOnBackgroundListener(this);
            }
        }
        return taskResult;
    }

    @Override
    public void onPostExecute(TaskConstant.TaskResult result)
    {
        removeTask();
    }

    @Override
    public void onCancelled() {
        removeTask();
        super.onCancelled();
    }

    private void addTask(){
        taskManager.addTask(tagString, this);
    }

    private void removeTask(){
        taskManager.removeTask(this);
    }

    /**
     * 默认后台解析返回结果
     */
    private IOnBackgroundListener defaultBackgroundListener = new IOnBackgroundListener(){

        @Override
        public TaskResult onBackground(VisitTask task) {
            TaskResult taskResult = TaskResult.NOTHING;
            JacksonUtil json = JacksonUtil.getInstance();
            resultEntity = (ResultEntity)json.readValue(resultsString, parseClass);
            if(resultEntity !=null){
                resultMsg = resultEntity.getMsg();
                if(resultJudge.judgeSuccess("" + resultEntity.getCode())){
                    taskResult = TaskResult.OK;
                }else{
                    taskResult = TaskResult.ERROR;
                    judgeLoginInvalid(""+ resultEntity.getCode());
                }
            }else{
                taskResult = TaskResult.CANCELLED;
            }
            return taskResult;
        }
    };

    private TaskResult doOnBackgroundListener(VisitTask visitTask){
        TaskResult taskResult = TaskResult.NOTHING;
        if(onBackgroundListener !=null){
            taskResult = onBackgroundListener.onBackground(visitTask);
        }
        return taskResult;
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

    public boolean isLoginInvalid() {
        return loginInvalid;
    }

    public void setLoginInvalid(boolean loginInvalid) {
        this.loginInvalid = loginInvalid;
    }

    public LittleHttpClient getLittleHttpClient() {
        return littleHttpClient;
    }

    public void setLittleHttpClient(LittleHttpClient littleHttpClient) {
        this.littleHttpClient = littleHttpClient;
    }

    public ResultJudge getResultJudge() {
        return resultJudge;
    }

    public void setResultJudge(ResultJudge resultJudge) {
        this.resultJudge = resultJudge;
    }

    public int getAccessType() {
        return accessType;
    }

    public void setAccessType(int accessType) {
        this.accessType = accessType;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public Map<String, Object> getArgMap() {
        return argMap;
    }

    public void setArgMap(Map<String, Object> argMap) {
        this.argMap = argMap;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    public String getKeyString() {
        return keyString;
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }

    public int getNetFlag() {
        return netFlag;
    }

    public void setNetFlag(int netFlag) {
        this.netFlag = netFlag;
    }

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public Class getParseClass() {
        return parseClass;
    }

    public void setParseClass(Class parseClass) {
        this.parseClass = parseClass;
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

    public ResultEntity getResultEntity() {
        return resultEntity;
    }

    public void setResultEntity(ResultEntity resultEntity) {
        this.resultEntity = resultEntity;
    }

    public IOnBackgroundListener getOnBackgroundListener() {
        return onBackgroundListener;
    }

    public void setOnBackgroundListener(IOnBackgroundListener onBackgroundListener) {
        this.onBackgroundListener = onBackgroundListener;
    }

    public IOnEncryptListener getOnEncryptListener() {
        return onEncryptListener;
    }

    public void setOnEncryptListener(IOnEncryptListener onEncryptListener) {
        this.onEncryptListener = onEncryptListener;
    }
}