package com.little.visit.util;

import android.content.Context;

import com.little.visit.OKHttpManager;
import com.little.visit.listener.IOnVisitListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static OkHttpUtil okHttpUtil;
    private Context context;//最好使用Application的上下文
    private OkHttpClient mOkHttpClient;
    private OKHttpManager okHttpManager;

    public OkHttpUtil(Context context) {
        this.context = context;
        init();
    }

    public static synchronized OkHttpUtil getInstance(Context context){
        if (okHttpUtil ==null){
            okHttpUtil = new OkHttpUtil(context);
        }
        return okHttpUtil;
    }

    public void init(){
        File cacheDir = new File(context.getExternalCacheDir() + "/cache/net/");
        if (!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(5 * 60, TimeUnit.SECONDS)
                .readTimeout(5 * 60, TimeUnit.SECONDS)
                .cache(new Cache(cacheDir.getAbsoluteFile(), cacheSize));
        builder.sslSocketFactory(createSSLSocketFactory());
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mOkHttpClient = builder.build();
        okHttpManager = OKHttpManager.getOkHttpManager();
    }


    public void visit(int methodType, String tag, String url, Map<String,String> params, final IOnVisitListener onVisitListener){
        Request.Builder requestBuilder = new Request.Builder();
        RequestBody formBody = null;
        Request request = null;
        if (methodType==GET){
            requestBuilder.url(formatGetParameter(url,params));
            //可以省略，默认是GET请求
            requestBuilder.method("GET",null);
            request = requestBuilder.build();
        }else if (methodType == POST){
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBodyBuilder.add(entry.getKey(),entry.getValue());
            }
            formBody = formBodyBuilder.build();
            requestBuilder.url(url).post(formBody);
            request = requestBuilder.build();
        }else if (methodType == PUT){
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBodyBuilder.add(entry.getKey(),entry.getValue());
            }
            formBody = formBodyBuilder.build();
            requestBuilder.url(url).put(formBody);
            request = requestBuilder.build();
        }

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()){
                    if (onVisitListener!=null){
                        onVisitListener.onError();
                    }
                }else {
                    if (onVisitListener!=null){
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener!=null){
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                if (null != response.cacheResponse()) {
//                    String str = response.cacheResponse().toString();
//                } else {
//                    String str = response.networkResponse().toString();
//                }
                if (!call.isCanceled()){
                    String str = response.body().string();
                    if (onVisitListener!=null){
                        onVisitListener.onSuccess(str);
                    }
                }else {
                    if (onVisitListener!=null){
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener!=null){
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }
        });
        okHttpManager.addTask(tag,call);
    }

    public void uploadFile(String tag, String url, String folderName, List<File> fileList, Map<String,String> params,  final IOnVisitListener onVisitListener){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        for (int i=0;i<fileList.size();i++){
            builder.addFormDataPart(folderName, fileList.get(i).getName(),
                    RequestBody.create(MEDIA_TYPE_JPEG, fileList.get(i)));
        }
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    if (onVisitListener != null) {
                        onVisitListener.onError();
                    }
                } else {
                    if (onVisitListener != null) {
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener != null) {
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!call.isCanceled()) {
                    String str = response.body().string();
                    if (onVisitListener != null) {
                        onVisitListener.onSuccess(str);
                    }
                } else {
                    if (onVisitListener != null) {
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener != null) {
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }
        });
        okHttpManager.addTask(tag,call);
    }

    public void downloadFile(String tag, String url, final String filePath, final IOnVisitListener onVisitListener) {
        Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    if (onVisitListener != null) {
                        onVisitListener.onError();
                    }
                } else {
                    if (onVisitListener != null) {
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener != null) {
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }

            @Override
            public void onResponse(Call call, Response response) {
                boolean hasException = false;
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File(filePath));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    hasException = true;
                }

                if (!call.isCanceled()) {
                    if (onVisitListener != null && !hasException) {
                        onVisitListener.onSuccess(filePath);
                    } else {
                        onVisitListener.onError();
                    }
                } else {
                    if (onVisitListener != null) {
                        onVisitListener.onCancel();
                    }
                }
                if (onVisitListener != null) {
                    onVisitListener.onFinish();
                }
                okHttpManager.removeTask(call);
            }
        });
        okHttpManager.addTask(tag,call);
    }

    /**
     * 构造GET请求地址的参数拼接
     * @param url
     * @param argsMap
     * @return String
     */
    private static String formatGetParameter(String url,Map<String, String> argsMap){
        if (url!=null && url.length()>0) {
            if (!url.endsWith("?")) {
                url = url +"?";
            }

            if (argsMap!=null && !argsMap.isEmpty()) {
                Set<Map.Entry<String, String>> entrySet = argsMap.entrySet();
                Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
                while(iterator.hasNext()){
                    Map.Entry<String, String> entry = iterator.next();
                    if (entry!=null) {
                        String key = entry.getKey();
                        String value = (String) entry.getValue();
                        url = url + key + "=" + value;
                        if (iterator.hasNext()) {
                            url = url +"&";
                        }
                    }
                }
            }
        }
        return url;
    }

    public static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }
}
