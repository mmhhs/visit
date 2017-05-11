package com.little.visit.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.little.visit.R;
import com.little.visit.listener.IOnVisitListener;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VolleyUtil {
    private static VolleyUtil volleyUtil;
    private RequestQueue mRequestQueue;
    private Context context;//最好使用Application的上下文

    public VolleyUtil(Context context) {
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized VolleyUtil getInstance(Context context){
        if (volleyUtil==null){
            volleyUtil = new VolleyUtil(context);
        }
        return volleyUtil;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void add(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelByTag(String tag) {
        mRequestQueue.cancelAll(tag);
    }

    /**
     * 网络访问
     * @param method
     * @param url
     * @param tag
     * @param params
     * @return
     */
    public CustomStringRequest visit(int method, String url,String tag,final Map<String, String> params, final IOnVisitListener onVisitListener){
        CustomStringRequest customStringRequest = new CustomStringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onVisitListener!=null){
                            onVisitListener.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onVisitListener!=null){
                            onVisitListener.onError();
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        return params;
                    }
                };
        customStringRequest.setOnVisitListener(onVisitListener);
        add(customStringRequest, tag);
        return customStringRequest;
    }

    /**
     * 下载图片 ImageRequest
     * @param imageUrl
     * @param tag
     */
    public void downloadImage(String imageUrl,String tag, final IOnVisitListener onVisitListener) {
        CustomImageRequest request = new CustomImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if (onVisitListener!=null){
                            onVisitListener.onSuccess(bitmap);
                        }
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (onVisitListener!=null){
                            onVisitListener.onError();
                        }
                    }
                });
        request.setPriority(Request.Priority.LOW);
        request.setOnVisitListener(onVisitListener);
        add(request, tag);
    }

    public void downloadImage(String imageUrl,ImageView imageView) {
        ImageLoader imageLoader = new ImageLoader(getRequestQueue(), new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {
            }

            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }
        });
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView,
                R.color.visit_light_grey, R.color.visit_light_grey);
        imageLoader.get(imageUrl,listener,360,640);
    }

    /**
     * 上传文件
     * @param url
     * @param tag
     * @param filePartName
     * @param files
     * @param params
     * @return
     */
    public UploadFileRequest uploadFile(String url,String tag,String filePartName, List<File> files,final Map<String, String> params, final IOnVisitListener onVisitListener){
        UploadFileRequest uploadFileRequest = new UploadFileRequest(url,filePartName,files,params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onVisitListener!=null){
                            onVisitListener.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onVisitListener!=null){
                            onVisitListener.onError();
                        }
                    }
                });
        uploadFileRequest.setShouldCache(false);
        uploadFileRequest.setOnVisitListener(onVisitListener);
        add(uploadFileRequest,tag);
        return uploadFileRequest;
    }

    /**
     * 下载文件
     * @param url
     * @param tag
     * @param filePath
     * @return
     */
    public DownloadFileRequest downloadFile(String url,String tag,String filePath, final IOnVisitListener onVisitListener){
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(url,filePath,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onVisitListener!=null){
                            onVisitListener.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onVisitListener!=null){
                            onVisitListener.onError();
                        }
                    }
                });
        downloadFileRequest.setShouldCache(false);
        downloadFileRequest.setOnVisitListener(onVisitListener);
        add(downloadFileRequest,tag);
        return downloadFileRequest;
    }

}
