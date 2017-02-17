package com.little.visit.listener;

import android.content.Context;
import android.view.View;

public interface ResultJudgeInterface {

    boolean judgeSuccess(String code);

    boolean judgeLoginInvalid(String code);

    void dealLoginInvalid(Context context,View view);

}