package com.example.studydownload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.MODE_PRIVATE;

/**
 * author: xujiajia
 * created on: 2020/5/25 2:33 PM
 * description:
 */
public class DownloadManager {
  //constants
  private static final String TAG = "DownloadManager";
  //data
  private static OkHttpClient mClient = new OkHttpClient();
  private HashMap<String, Call> mapCall = new HashMap<>();

  private static class Host {
    private static DownloadManager instance = new DownloadManager();
  }

  private DownloadManager() {
  }

  public static DownloadManager getInstance() {
    return Host.instance;
  }

  public void download(String url, DownloadCallback downloadCallback) {
    if (isDownloading(url)) {
      return;
    }
    DownloadTask downloadTask = new DownloadTask(url, downloadCallback);
    downloadTask.execute(null, null);
  }

  //判断是否正在下载
  public boolean isDownloading(String url) {
    return mapCall.containsKey(url);
  }

  //停止下载
  public void cancelDownload(String url) {
    Call call = mapCall.get(url);
    if (call != null) {
      call.cancel();
      mapCall.remove(url);
    }
  }

  //清理下载的文件
  public void clearDownloadData(String url) {
    cancelDownload(url);
    new ClearTask().execute(url);
  }

  interface DownloadCallback {

    void onProcess(long current, long total);
  }

  private class DownloadTask extends AsyncTask<Void, Long, Void> {

    private String mUrl;
    private DownloadCallback mCallback;

    DownloadTask(String url, DownloadCallback callback) {
      this.mUrl = url;
      this.mCallback = callback;
    }

    @Override protected Void doInBackground(Void... voids) {
      if (TextUtils.isEmpty(this.mUrl)) {
        return null;
      }

      try {
        startDownload(mUrl, mCallback, getDownloadProgressFromSp(mUrl));
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override protected void onProgressUpdate(Long... values) {
      if (this.mCallback != null && values.length >= 2) {
        this.mCallback.onProcess(values[0], values[1]);
      }
    }

    @WorkerThread
    private void startDownload(String url, final DownloadCallback downloadCallback, long startPoint)
        throws Exception {
      if (TextUtils.isEmpty(url)) {
        return;
      }
      Request request = new Request.Builder()
          .url(url)
          .header("Range", "bytes=" + startPoint + "-")
          .build();
      Call call = mClient.newCall(request);
      final Response response = call.execute();

      //206表示支持断点续传,如果不是表示不支持
      if (response.code() != HttpURLConnection.HTTP_PARTIAL) {
        Log.d(TAG, "download fail code:" + response.code() + " message:" + response.message());
        return;
      }
      mapCall.put(url, call);

      FileChannel fileChannel = null;
      ResponseBody body = response.body();
      if (body == null) {
        return;
      }
      long currentLength = startPoint;
      final long total = body.contentLength() + startPoint;
      if (currentLength == total) {
        Log.d(TAG, "文件已下载完 url：" + url);
        return;//已经下载完了
      }

      InputStream inputStream = body.byteStream();
      try {
        RandomAccessFile randomAccessFile = new RandomAccessFile(findDownloadFile(), "rws");
        fileChannel = randomAccessFile.getChannel();

        MappedByteBuffer
            mappedByteBuffer =
            fileChannel.map(FileChannel.MapMode.READ_WRITE, startPoint, total - startPoint);
        int len;
        byte[] buffer = new byte[10240];
        while ((len = inputStream.read(buffer)) != -1) {
          if (call.isCanceled()) {
            return;
          }
          currentLength = currentLength + len;
          mappedByteBuffer.put(buffer, 0, len);//读取buffer
          putDownloadProgressToSp(url, currentLength);//存储进度到sp
          if (downloadCallback != null) {
            publishProgress(currentLength, total);//回调onProgress
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          mapCall.remove(url);
          inputStream.close();
          if (fileChannel != null) {
            fileChannel.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class ClearTask extends AsyncTask<String, Long, Void> {
    @Override protected Void doInBackground(String... strings) {
      File file = findDownloadFile();
      if (file != null && file.exists()) {
        file.delete();
      }
      putDownloadProgressToSp(strings[0], 0);
      return null;
    }
  }

  //找到下载的文件，如果没有就创建一个
  //（由于没有在sd卡读写，因此不需要申请权限）
  public File findDownloadFile() {
    try {
      File dir = new File(MyApplication.globalContext.getExternalFilesDir(null),
          DownloadConstants.DIR_NAME);
      if (!dir.exists() || !dir.isDirectory()) {
        dir.mkdir();
      }
      File file = new File(dir, DownloadConstants.FILE_NAME);
      if (!file.exists()) {
        file.createNewFile();
      }
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private long getDownloadProgressFromSp(String url) {
    SharedPreferences sp = MyApplication.globalContext.getSharedPreferences("data", MODE_PRIVATE);
    return sp.getLong(url, 0);
  }

  private void putDownloadProgressToSp(String url, long currentLength) {
    if (TextUtils.isEmpty(url)) {
      return;
    }
    SharedPreferences sp =
        MyApplication.globalContext.getSharedPreferences("data", MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit();
    editor.putLong(url, currentLength);
    editor.commit();
  }
}
