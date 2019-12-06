# VirtualAPK的特性
VirtualAPK是滴滴出行自研的一款优秀的插件化框架，主要有如下几个特性。
# 功能完备
**·** 支持几乎所有的Android特性；
**·** 四大组件方面
# 四大组件均不需要在宿主manifest中预注册，每个组件都有完整的生命周期。
1.Activity：支持显示和隐式调用，支持Activity的theme和LaunchMode，支持透明主题；
2.Service：支持显示和隐式调用，支持Service的start、stop、bind和unbind，并支持跨进程bind插件中的Service；
3.Receiver：支持静态注册和动态注册的Receiver；
4.ContentProvider：支持provider的所有操作，包括CRUD和call方法等，支持跨进程访问插件中的Provider。
5.自定义View：支持自定义View，支持自定义属性和style，支持动画；
6.PendingIntent：支持PendingIntent以及和其相关的Alarm、Notification和AppWidget；
7.支持插件Application以及插件manifest中的meta-data；
8.支持插件中的so。
# 优秀的兼容性
 1.兼容市面上几乎所有的Android手机，这一点已经在滴滴出行客户端中得到验证；
 2.资源方面适配小米、Vivo、Nubia等，对未知机型采用自适应适配方案；
 3.极少的Binder Hook，目前仅仅hook了两个Binder：AMS和IContentProvider，hook过程做了充分的兼容性适配；
 4.插件运行逻辑和宿主隔离，确保框架的任何问题都不会影响宿主的正常运行。
# 入侵性极低
1.插件开发等同于原生开发，四大组件无需继承特定的基类；
2.精简的插件包，插件可以依赖宿主中的代码和资源，也可以不依赖；
3.插件的构建过程简单，通过Gradle插件来完成插件的构建，整个过程对开发者透明。
```
以上内容均引用(https://github.com/didi/VirtualAPK/wiki)
```
 #  集成
#### 先列一下项目的结构。![项目结构.png](https://upload-images.jianshu.io/upload_images/10881059-79344da540e0be92.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 开始集成
1.项目级的build.gradle文件中
```
 classpath 'com.android.tools.build:gradle:3.0.0'
 classpath 'com.didi.virtualapk:gradle:0.9.8.6'
 这是第一个坑，VirtualAPK目前只能在gradle3.0一下的版本使用
```
2.宿主app的build.gradle
```
apply plugin: 'com.didi.virtualapk.host'
implementation 'com.didi.virtualapk:core:0.9.8'
```
3.插件plugin_one和plugin_two的build.gradle中
```
apply plugin: 'com.didi.virtualapk.plugin'
virtualApk {
         // 插件资源表中的packageId，需要确保不同插件有不同的packageId这个值的范围在系统和宿主的之间即大于0x02，小于0x7f
        packageId = 0x5f
        // 宿主App模块的路径，可以填写绝对路径
        targetHost = '../PluginDemo/app' 
        //默认为true，如果插件有引用宿主的类，那么这个选项可以使得插件和宿主保持混淆一致
        applyHostMapping = true
```
4.在自定义的Application中
```
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.getInstance(base).init();
    }
```
5.在需要加载插件的Activity中
```
   //加载插件需要文件读写权限，这里我图方便没有写，但是是必须要的
   //lugin_one.apk 和 plugin_two.apk 是打好插件后手动push到sd卡根目录的，实际开发中，应该根据实际情况作处理
   private fun loadPlugin(){
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            Toast.makeText(this, "sdcard was NOT MOUNTED!", Toast.LENGTH_SHORT).show()
        }
        val pluginManager = PluginManager.getInstance(this)
        val pluginOne = File(Environment.getExternalStorageDirectory(), "plugin_one.apk")
        val pluginTwo = File(Environment.getExternalStorageDirectory(), "plugin_two.apk")
        try {
            if (pluginOne.exists()){
                pluginManager.loadPlugin(pluginOne)
                Log.e("MainActivity--->","load succss $pluginOne")
            }

            if (pluginTwo.exists()){
                pluginManager.loadPlugin(pluginTwo)
                Log.e("MainActivity--->","load succss $pluginTwo")
            }
        }catch (e:Exception){
            Log.e("MainActivity--->",e.toString())
        }
    }

    //跳转插件Activity(BundleUrl位于common模块，用于管理所有插件Activity的地址,
    //插件和宿主都依赖该模块，我的理解是用该模块作一下常规通信)
    findViewById<Button>(R.id.go_plugin_one)?.setOnClickListener {
            val pkg="com.jason.plugin.one"
            if (PluginManager.getInstance(this).getLoadedPlugin(pkg) == null) {
                Toast.makeText(this, "plugin $pkg not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent()
            intent.setClassName(this, BundleUrl.PLUGIN_ONE_MAIN_URL)
            startActivity(intent)
    }
```
6.插件生成
```
在项目根目录下执行
./gradlew assemblePlugin
如果顺利的话，插件apk会在插件项目的 build/outputs/plugin/release 文件夹下
~~~当然大部分情况下基本都是失败，所以接下来列一下所有踩过的坑
```
# 踩坑记录
1.在执行./gradlew assemblePlugin前，先执行一下Make Project 确保宿主app/build/VAHost文件夹下有versions.txt文件，否则：
```
* What went wrong:
A problem occurred configuring project ':plugin_one'.
> Failed to notify project evaluation listener.
   > Can't find /Users/asure/AndroidStudioProjects/PluginDemo/app/build/VAHost/versions.txt, please check up your host application
       need apply com.didi.virtualapk.host in build.gradle of host application 

   > Cannot invoke method onProjectAfterEvaluate() on null object

```
2.确保在gradle.properties文件中配置android.useDexArchive=false，否则：
```
* What went wrong:
A problem occurred configuring project ':plugin_one'.
> Failed to notify project evaluation listener.
   > Can't using incremental dexing mode, please add 'android.useDexArchive=false' in gradle.properties of :plugin_one.
   > Cannot invoke method onProjectAfterEvaluate() on null object

```
3.确保插件的manifest文件下的包名和applicationId保持一致，否则在生成插件时会找不到R文件
```
/Users/asure/AndroidStudioProjects/PluginDemo/plugin_one/src/main/java/com/jason/plugin/one/activitis/PluginOneActivity.java:6: 错误: 找不到符号
import com.jason.plugin.plugin_one.R;
                                  ^
  符号:   类 R
  位置: 程序包 com.jason.plugin.plugin_one
/Users/asure/AndroidStudioProjects/PluginDemo/plugin_one/src/main/java/com/jason/plugin/one/activitis/PluginOneActivity.java:18: 错误: 程序包R不存在
        setContentView(R.layout.activity_plugin_one);
                        ^
2 个错误

这个坑卡了我好久~~ 就是因为手贱改了一下applicationId
```
4.插件包需要签名才能被宿主加载
```
这个也挺坑的~~加载不出来还没有提示~只能跟着源码去看错误，想吐
```
5.构建插件包最好使用命令./gradlew assemblePlugin ，反正我点击侧边栏的assemblePlugin就没有成功过

6.buildToolsVersion 不支持28.0.0往上，我项目里用的是26.0.2

# 以上是集成VirtualAPK的整个流程和踩的坑

github地址:(https://github.com/liujun123456/VirtualAPKDemo)
对插件化感兴趣的朋友可以把代码拉下来玩一下，基本能省略掉你很多踩坑的地方


  