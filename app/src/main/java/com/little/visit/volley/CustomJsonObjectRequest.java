package com.little.visit.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.little.visit.listener.IOnVisitListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomJsonObjectRequest extends JsonObjectRequest {
    private IOnVisitListener onVisitListener;

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                   Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
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

    private Request.Priority mPriority;

    public void setPriority(Request.Priority priority) {
        mPriority = priority;
    }

    @Override
    public Request.Priority getPriority() {
        return mPriority == null ? Request.Priority.NORMAL : mPriority;
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
