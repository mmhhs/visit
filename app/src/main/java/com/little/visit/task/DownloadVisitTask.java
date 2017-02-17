package com.little.visit.task;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import com.little.visit.R;
import com.little.visit.TaskConstant.TaskResult;
import com.little.visit.listener.IOnProgressListener;
import com.little.visit.listener.IProgressListener;
import com.little.visit.util.PopupUtil;
import com.little.visit.util.StringUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DownloadVisitTask extends VisitTask {
	private String downloadUrl = "";// 文件下载地址
    private String fileStorePath;//文件存储路径
    private long totalSize=0;//下载文件大小
    private boolean silenceDownload = false;//静默下载
    private boolean canCancelDownload = false;//能否终止下载
    private String popupTitle = "";//标题
    private View contentView;//视图 承载弹窗
    private PopupUtil popupUtil;
    private PopupWindow downloadPopupWindow;
    //下载相关
    private HttpClient httpClient = new DefaultHttpClient();
    private FileOutputStream fileOutputStream=null;
    private CountingInputStream cis = null;
    private IOnProgressListener onProgressListener;//进度监听

    public DownloadVisitTask(Context context, View contentView, String popupTitle, boolean silenceDownload, boolean canCancelDownload, String downloadUrl, String fileStorePath) {
        this.context = context;
        this.contentView = contentView;
        this.popupTitle = popupTitle;
        this.silenceDownload = silenceDownload;
        this.canCancelDownload = canCancelDownload;
        this.downloadUrl = downloadUrl;
        this.fileStorePath = fileStorePath;
        init();
    }

    public void init(){
        setTagString(downloadUrl);
        super.init();
        popupUtil = new PopupUtil(context);
        if (!StringUtil.isEmpty(popupTitle)){
            popupUtil.setPopupTitle(popupTitle);
        }
    }

	@Override
    public void onPreExecute() {
        try {
            if (onProgressListener !=null){
                onProgressListener.onStart();
            }
            if (!silenceDownload){
                downloadPopupWindow = popupUtil.showDownloadPopup(contentView, canCancelDownload);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
	}

	@Override
    public TaskResult doInBackground(Void... params) {
		TaskResult taskResult = TaskResult.NOTHING;
		try {
			httpClient.getParams().setParameter(
					CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			HttpGet httpGet = new HttpGet(downloadUrl);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();
                totalSize = entity.getContentLength();
				cis = new CountingInputStream(
						entity.getContent(), new IProgressListener() {
							@Override
							public void transferred(long transferedBytes) {
								publishProgress(""+(transferedBytes));
							}
						});
                File loadFile = new File(fileStorePath);
                if (!loadFile.exists()){
                    loadFile.createNewFile();
                }
				byte[] byteIn = toByteArray(cis, (int) totalSize);
                fileOutputStream = new FileOutputStream(loadFile);
                fileOutputStream.write(byteIn); //记得关闭输入流

                taskResult = TaskResult.OK;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
            taskResult = TaskResult.ERROR;
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
            taskResult = TaskResult.ERROR;
		} catch (Exception e) {
			e.printStackTrace();
            taskResult = TaskResult.ERROR;
		} finally {
            stopDownload();
		}
		return taskResult;
	}

	@Override
    public void onProgressUpdate(String... progress) {
        try {
            if (onProgressListener !=null){
                onProgressListener.onTransferred(progress[0], totalSize);
            }
            if (!silenceDownload){
                popupUtil.updateProgressInfo(progress[0], totalSize);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
	}

	@Override
    public void onPostExecute(TaskResult result) {
        if (onProgressListener !=null){
            onProgressListener.onDone();
        }
        try {
            if (downloadPopupWindow !=null){
                downloadPopupWindow.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        switch(result){
            case OK:
                if (onProgressListener !=null){
                    onProgressListener.onSuccess(fileStorePath);
                }
                break;
            case ERROR:
                if (onProgressListener !=null){
                    onProgressListener.onError(context.getString(R.string.visit6));
                }
                break;
            case CANCELLED:
                if (onProgressListener !=null){
                    onProgressListener.onError(context.getString(R.string.visit6));
                }
                break;
            default:
                break;
        }
	}

	/**
	 * InputStream转化为Byte数组
	 * 
	 * @param instream
	 * @param contentLength
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray(InputStream instream, int contentLength)
			throws IOException {
		if (instream == null) {
			return null;
		}
		try {
			if (contentLength < 0) {
				contentLength = 1024*4;
			}
			final ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
			final byte[] tmp = new byte[1024*4];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			return buffer.toByteArray();
		} finally {
			instream.close();
		}
	}



    public void stopDownload(){
        try {
            if (cis!=null){
                try {
                    cis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (httpClient != null && httpClient.getConnectionManager() != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileStorePath() {
        return fileStorePath;
    }

    public void setFileStorePath(String fileStorePath) {
        this.fileStorePath = fileStorePath;
    }

    public boolean isSilenceDownload() {
        return silenceDownload;
    }

    public void setSilenceDownload(boolean silenceDownload) {
        this.silenceDownload = silenceDownload;
    }

    public boolean isCanCancelDownload() {
        return canCancelDownload;
    }

    public void setCanCancelDownload(boolean canCancelDownload) {
        this.canCancelDownload = canCancelDownload;
    }

    public String getPopupTitle() {
        return popupTitle;
    }

    public void setPopupTitle(String popupTitle) {
        this.popupTitle = popupTitle;
    }

    public IOnProgressListener getOnProgressListener() {
        return onProgressListener;
    }

    public void setOnProgressListener(IOnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        popupUtil.setOnProgressListener(onProgressListener);
    }
}
