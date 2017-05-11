package com.little.visit.listener;


public interface IOnTaskListener<T> {
    void onPreExecute();
    void onSuccess(T response);
    void onError();
    void onFinish();
    void onVisit();
    void onCancel();
}