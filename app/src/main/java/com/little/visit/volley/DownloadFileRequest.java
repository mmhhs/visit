package com.little.visit.volley;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.little.visit.listener.IOnVisitListener;

import java.io.File;
import java.io.FileOutputStream;


/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */
public class DownloadFileRequest extends Request<String> {
    /** Socket timeout in milliseconds for image requests */
    private static final int IMAGE_TIMEOUT_MS = 1000;

    /** Default number of retries for image requests */
    private static final int IMAGE_MAX_RETRIES = 2;

    /** Default backoff multiplier for image requests */
    private static final float IMAGE_BACKOFF_MULT = 2f;

    private final Response.Listener<String> mListener;
    private String mFileStorePath;
    private FileOutputStream fileOutputStream;

    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    private IOnVisitListener onVisitListener;

    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     *
     * @param url URL of the image
     * @param listener Listener to receive the decoded bitmap
     * @param errorListener Error listener, or null to ignore errors
     */
    public DownloadFileRequest(String url, String fileStorePath, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        setRetryPolicy(
                new DefaultRetryPolicy(IMAGE_TIMEOUT_MS, IMAGE_MAX_RETRIES, IMAGE_BACKOFF_MULT));
        mListener = listener;
        mFileStorePath = fileStorePath;
    }

    /**
     * For API compatibility with the pre-ScaleType variant of the constructor. Equivalent to
     * the normal constructor with {@code ScaleType.CENTER_INSIDE}.
     */


    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }


    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        // Serialize all decode on a global lock to reduce concurrent heap usage.
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                VolleyLog.e("Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    private Response<String> doParse(NetworkResponse response) {
        byte[] data = response.data;
        File loadFile = new File(mFileStorePath);
        boolean loadSuccess = true;
        try {
            if (!loadFile.exists()){
                loadFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(loadFile);
            fileOutputStream.write(data); //记得关闭输入流
        } catch (Exception e) {
            e.printStackTrace();
            loadSuccess = false;
        } finally {
            if (fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if (!loadSuccess) {
            return Response.error(new ParseError(response));
        } else {
            return Response.success(mFileStorePath, HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
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
