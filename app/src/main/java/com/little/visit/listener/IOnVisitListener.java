package com.little.visit.listener;


public interface IOnVisitListener<T> {
    void onSuccess(T response);
    void onError();
    void onFinish();
    void onCancel();
}