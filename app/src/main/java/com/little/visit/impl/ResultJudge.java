package com.little.visit.impl;

import android.content.Context;
import android.view.View;

import com.little.visit.R;
import com.little.visit.listener.ResultJudgeInterface;
import com.little.visit.util.StringUtil;
import com.little.visit.util.ToastUtil;

public class ResultJudge implements ResultJudgeInterface{

    private static ResultJudge resultJudge = null;
    private String successCode = "0";
    private String loginInvalidCode = "1000";

    public ResultJudge() {
    }

    public static synchronized ResultJudge getInstance()
    {
        if (resultJudge == null)
            resultJudge = new ResultJudge();
        return resultJudge;
    }

    /**
     * 判断返回值成功
     * @param code
     * @return
     */
    @Override
    public boolean judgeSuccess(String code){
        if (StringUtil.isEmpty(code)){
            return false;
        }
        if (code.equals(successCode)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断登录失效
     * @param code
     * @return
     */
    @Override
    public boolean judgeLoginInvalid(String code){
        if (StringUtil.isEmpty(code)){
            return false;
        }
        if(code.equals(loginInvalidCode)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 处理登录失效
     */
    @Override
    public void dealLoginInvalid(Context context,View view) {
        ToastUtil.addToast(context,context.getString(R.string.visit7));
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public String getLoginInvalidCode() {
        return loginInvalidCode;
    }

    public void setLoginInvalidCode(String loginInvalidCode) {
        this.loginInvalidCode = loginInvalidCode;
    }
}