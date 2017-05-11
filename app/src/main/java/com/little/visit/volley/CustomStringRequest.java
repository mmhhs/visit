package com.little.visit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.little.visit.listener.IOnVisitListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomStringRequest extends StringRequest {
    private IOnVisitListener onVisitListener;

    public CustomStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    private Map<String, String> headers = new HashMap<>();

    public void setCookies(List<String> cookies) {
        StringBuilder sb = new StringBuilder();
        for (String cookie : cookies) {
            sb.append(cookie).append("; ");
        }
        headers.put("Cookie", sb.toString());
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    private Priority mPriority;

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    @Override
    public Priority getPriority() {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }

    public void setOnVisitListener(IOnVisitListener onVisitListener) {
        this.onVisitListener = onVisitListener;
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        if (onVisitListener!=null){
            onVisitListener.onFinish();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (onVisitListener!=null){
            onVisitListener.onCancel();
        }
    }
}
