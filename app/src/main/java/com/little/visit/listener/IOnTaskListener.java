package com.little.visit.listener;


public interface IOnTaskListener<T> {
    void onTaskPreExecute();
    void onTaskSuccess(T response);
    void onTaskError();
    void onTaskFinish();
    void onTaskVisit();
    void onTaskCancel();
}