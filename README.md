# AIDLService
### 简介
  该示例代码，用来演示AIDL跨进程传输对象数据。跨进程之间的回调如何实现。

### Service端
1. Service端实现了相机的基本功能。
### Client端
1. 通过bindService，实现了Client端调用Service开启关闭相机。
2. 获取Service端相机的回调数据，进行渲染
