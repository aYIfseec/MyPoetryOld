# MyPoetry
初学Android一星期写的期末课程设计Android项目，是关于古诗朗读的（机器朗读与用户上传的朗读音频）。
<br/>
## 一、Android端
 * 1.采用Android Studio编写<br/>
 * 2.使用到的技术（严谨来说本人才学一周，其实没有什么技术含量可言）：<br/>
    + 机器朗读 使用了__百度语音开放的api__、<br/>
    + 获取古诗 使用了`阿凡达数据`的古诗api、<br/>
    + 手机短信验证码 用的是`Mob平台`的、<br/>
    + 网络访问 用的是`okhttp3`.<br/>
 * 3.用户数据则是由自己写的java后台提供支持.<br/>
## 二、服务器端
---------
 * 1.使用java编写运行于tomcat服务器，mysql数据库<br/>
 * 2.主要功能：配合Android端完成用户注册、登录、用户收藏喜欢的古诗、上传用户朗读语音等.<br/>
## 三、项目效果图
<br/>
<div align=center>
侧边栏 界面<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(4).jpeg"/>
</div>
<br/>

<br/>
<div align=center>
我的收藏 界面<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(3).jpeg"/>
</div>
<br/>

<br/>
<div align=center>
一首 界面<br/>
右上角是搜索，下边分别是录音、机器朗读、收藏古诗
<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(5).jpeg"/>
</div>
<br/>

<br/>
<div align=center>
搜索结果<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default.jpeg"/>
</div>
<br/>

<br/>
<div align=center>
其他用户对该古诗词上传的朗读音频<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(1).jpeg"/>
</div>
<br/>

<br/>
<div align=center>
注册<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(7).jpeg"/>
</div>
<br/>

<br/>
<div align=center>
手机短信验证码<br/><br/>
<img width="320" height="520"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/S80228-180356.jpg"/>
</div>
<br/>

<br/>
<div align=center>
登录<br/>
<img width="320" height="540"
src="https://github.com/aYIfseec/MyPoetry/blob/master/MyPoetryAndroid/picture/default%20(6).jpeg"/>
</div>
<br/>

## 四、TODO
 * 1.用户上传古诗词的注解<br/>
 * 2.后台管理系统<br/>
    + 用户管理、<br/>
    + 音频管理、<br/>
    + 注解管理.
