package com.little.visit.util;

import android.content.Context;

import com.little.visit.model.ResultEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.okhttpfinal.BaseHttpRequestCallback;
import cn.finalteam.okhttpfinal.FileDownloadCallback;
import cn.finalteam.okhttpfinal.HttpRequest;
import cn.finalteam.okhttpfinal.RequestParams;
import okhttp3.MediaType;

public class OkHttpUtil {
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static OkHttpUtil okHttpUtil;
    private Context context;//最好使用Application的上下文
    String param = "{\"limit\":{\"auth\":\"239BA6B4594179AAB3B7827E5A8A0D0BFC5139C7BBC78AB2540618361BC318A7D66552E1\",\"uid\":\"64766\",\"userType\":\"40\"},\"param\":{\"versionCode\":\"54\",\"deviceType\":\"1\",\"version\":\"3.0.1\"}}";


    public OkHttpUtil(Context context) {
        this.context = context;
    }

    public static synchronized OkHttpUtil getInstance(Context context){
        if (okHttpUtil ==null){
            okHttpUtil = new OkHttpUtil(context);
        }
        return okHttpUtil;
    }

    public void visit(String url) {
        List<File> files = new ArrayList<>();
        File file = new File("...");
        RequestParams params = new RequestParams();//请求参数
        params.addFormDataPart("jsonParame",param);
//        params.addFormDataPart("username", mUserName);//表单数据
//        params.addFormDataPart("password", mPassword);//表单数据
//        params.addFormDataPart("file", file);//上传单个文件
//        params.addFormDataPart("files", files,MEDIA_TYPE_JPEG);//上传多个文件
//        params.addHeader("token", token);//添加header信息
        HttpRequest.post(url, params, new BaseHttpRequestCallback<ResultEntity>() {

            //请求网络前
            @Override
            public void onStart() {
                LogUtil.e("OkHttpUtil onStart: " + System.currentTimeMillis());
//                buildProgressDialog().show();
            }

            //这里只是网络请求成功了（也就是服务器返回JSON合法）没有没有分装具体的业务成功与失败，大家可以参考demo去分装自己公司业务请求成功与失败
            @Override
            protected void onSuccess(ResultEntity loginResponse) {
                LogUtil.e("OkHttpUtil onTaskSuccess: " + System.currentTimeMillis());
//                toast(loginResponse.getMsg());
            }

            //请求失败（服务返回非法JSON、服务器异常、网络异常）
            @Override
            public void onFailure(int errorCode, String msg) {
//                toast("网络异常~，请检查你的网络是否连接后再试");
                LogUtil.e("OkHttpUtil onFailure: " + System.currentTimeMillis());
            }

            //请求网络结束
            @Override
            public void onFinish() {
//                dismissProgressDialog();
                LogUtil.e("OkHttpUtil onTaskFinish: " + System.currentTimeMillis());
            }
        });
    }

    private void fileDownload(){
        String url = "http://219.128.78.33/apk.r1.market.hiapk.com/data/upload/2015/05_20/14/com.speedsoftware.rootexplorer_140220.apk";
        File saveFile = new File("/sdcard/rootexplorer_140220.apk");
        HttpRequest.download(url, saveFile, new FileDownloadCallback() {
            //开始下载
            @Override
            public void onStart() {
                super.onStart();
            }

            //下载进度
            @Override
            public void onProgress(int progress, long networkSpeed) {
                super.onProgress(progress, networkSpeed);
//                mPbDownload.setProgress(progress);
                //String speed = FileUtils.generateFileSize(networkSpeed);
            }

            //下载失败
            @Override
            public void onFailure() {
                super.onFailure();
//                Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_SHORT).show();
            }

            //下载完成（下载成功）
            @Override
            public void onDone() {
                super.onDone();
//                Toast.makeText(getBaseContext(), "下载成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
