package com.little.visit.listener;


import com.little.visit.task.VisitTask;

public interface IOnResultListener {
    void onSuccess(VisitTask task);
    void onError(VisitTask task);
    void onDone(VisitTask task);
}