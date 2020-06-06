# StudyDownload

此项目展示了断点续传功能的简单实现，主要实现了以下功能：
1. 支持下载中途暂定下载
2. 将下载进度存储到本地，重启app后会从上次下载的位置开始下载
3. 支持清除原有的数据

<img src="https://img-blog.csdnimg.cn/20200606110125412.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0RvdWJsZTJoYW8=,size_16,color_FFFFFF,t_70" width = 30% height = 30% /> <img src="https://img-blog.csdnimg.cn/20200606112630318.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0RvdWJsZTJoYW8=,size_16,color_FFFFFF,t_70" width = 30% height = 30% />

# 理论概述

1. 本项目为了让项目更加简单化，尽量避免使用其他非基础知识。下载与文件的异步操作使用AsyncTask来实现，本地存储使用SharedPreferences来实现。
2. 断点续传的相关HTTP的理论已经有博主整理的较好了，对此方面没有接触过的读者推荐看看：[Android Okhttp 断点续传面试解析](https://juejin.im/post/5ceab960e51d4510926a7acf)
# 项目拓展
此项目只是一个让新手更好理解断点续传的Demo，要投入实际使用还有很多需要补充和实现的地方，具体的也是要看实际的使用场景，笔者此处仅例举几项可以拓展的方向，有兴趣的读者可以自己去了解：
1. 文件的校验机制。可以通过存储路径，文件大小等方式。
2. 如果断点续传到一半，再次开始的时候发现服务端的文件变了，如果判断与处理？
3. 文件过期，或者说文件的更新机制。
4. 线程池。如果有同时多个文件在断点续传，如何在线程上支持。