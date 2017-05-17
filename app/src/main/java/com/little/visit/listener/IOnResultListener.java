package com.little.visit.listener;


public interface IOnResultListener {
    void onSuccess();
    void onError();
    void onDone();
    void onCancel();
}