package com.little.visit.listener;


public interface IOnVisitResultListener<T> {
    void onSuccess(T res);
    void onError(String msg);
    void onFinish();
}