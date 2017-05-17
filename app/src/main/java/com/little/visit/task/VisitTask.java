package com.little.visit.task;

import android.content.Context;

import com.little.visit.TaskConstant;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.TaskManager;
import com.little.visit.listener.IOnBackgroundListener;

/**
 * 实现线程管理
 */
public abstract class VisitTask extends AsyncTask<Void, String, TaskResult> {
    public Context context;//上下文
    //线程管理
    public TaskManager taskManager = TaskManager.getTaskManagerInstance();//AsycnTask线程管理类
    public String tagString = "VisitTask";//线程标记
    //监听
    public IOnBackgroundListener onBackgroundListener;//后台运行代码监听


    public VisitTask() {
        init();
    }

    public void init(){
        addTask();
    }


    @Override
    public void onPreExecute()
    {

    }

    @Override
    public TaskResult doInBackground(Void... params) {
        TaskResult taskResult = TaskResult.NOTHING;
        //不访问网络的情况
        taskResult = doOnBackgroundListener(this);
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



    private TaskResult doOnBackgroundListener(VisitTask visitTask){
        TaskResult taskResult = TaskResult.NOTHING;
        if(onBackgroundListener !=null){
            taskResult = onBackgroundListener.onBackground(visitTask);
        }
        return taskResult;
    }

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public IOnBackgroundListener getOnBackgroundListener() {
        return onBackgroundListener;
    }

    public void setOnBackgroundListener(IOnBackgroundListener onBackgroundListener) {
        this.onBackgroundListener = onBackgroundListener;
    }
}