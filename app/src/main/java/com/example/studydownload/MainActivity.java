package com.example.studydownload;

import java.io.File;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  //constants
  private static final int PROGRESS_MAX = 100;
  //ui
  private TextView tvProgress;
  private ProgressBar pbDownload;
  private Button btnStart;
  private Button btnCancel;
  private Button btnClear;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initViews();
  }

  private void initViews() {
    tvProgress = findViewById(R.id.tv_progress_download);
    pbDownload = findViewById(R.id.pb_download);
    btnStart = findViewById(R.id.btn_start_download);
    btnCancel = findViewById(R.id.btn_cancel_download);
    btnClear = findViewById(R.id.btn_clear_download);

    pbDownload.setMax(PROGRESS_MAX);
    btnStart.setOnClickListener(this);
    btnCancel.setOnClickListener(this);
    btnClear.setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.btn_start_download) {
      startDownload();
    } else if (id == R.id.btn_cancel_download) {
      DownloadManager.getInstance().cancelDownload(DownloadConstants.DOWNLOAD_URL);
      showToast(getString(R.string.cancel_download));
    } else if (id == R.id.btn_clear_download) {
      DownloadManager.getInstance().clearDownloadData(DownloadConstants.DOWNLOAD_URL);
      showToast(getString(R.string.clear));
    }
  }

  private void startDownload() {
    if (DownloadManager.getInstance().isDownloading(DownloadConstants.DOWNLOAD_URL)) {
      showToast(getString(R.string.downloading));
      return;
    }
    DownloadManager.getInstance()
        .download(DownloadConstants.DOWNLOAD_URL, new DownloadManager.DownloadCallback() {
          @Override public void onProcess(long current, long total) {
            if (tvProgress == null || pbDownload == null) {
              return;
            }
            if (current == total) {
              File downloadFile = DownloadManager.getInstance().findDownloadFile();
              if (downloadFile != null) {
                tvProgress.setText(
                    getString(R.string.download_path, downloadFile.getAbsolutePath()));
              }
            } else {
              tvProgress.setText(getString(R.string.download_progress, current, total));
              int progress = (int) (((float) current) / total * PROGRESS_MAX);
              pbDownload.setProgress(progress);
            }
          }
        });
    showToast(getString(R.string.start_download));
  }

  private void showToast(String toastText) {
    Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
  }
}
