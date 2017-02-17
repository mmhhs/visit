package com.little.visit.listener;


import com.little.visit.TaskConstant;
import com.little.visit.task.VisitTask;

public interface IOnBackgroundListener {
    TaskConstant.TaskResult onBackground(VisitTask task);
}