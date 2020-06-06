package com.example.studydownload;

import android.app.Application;
import android.content.Context;

/**
 * author: xujiajia
 * created on: 2020/5/25 5:40 PM
 * description:
 */
public class MyApplication extends Application {
  public static Context globalContext = null;//保留一个全局的context

  @Override public void onCreate() {
    globalContext = this;
    super.onCreate();
  }
}
