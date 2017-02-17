package com.little.visit.listener;

public interface IOnProgressListener {
    void onStart();
    void onTransferred(String transferedBytes, long totalBytes);
    void onSuccess(String fileStorePath);
    void onError(String tip);
    void onCancel();
    void onDone();
}