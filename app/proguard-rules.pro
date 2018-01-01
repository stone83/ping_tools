# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Users\zhangbing\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5          # 指定代码的压缩级别
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法
-verbose                # 混淆时是否记录日志

-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验

# 指定不去忽略非公共库的类和成员
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# 输出类名->混淆后类名的映射关系
-printmapping map.txt

# 可以指定移除哪些方法没有副作用,如在android开发中,如想在release版本可以把所有log输出都移除,可以配置.
#-assumenosideeffects public class com.ccmt.appmaster.util.LogUtil {
#    public static void i(java.lang.String);
#    public static void d(java.lang.String,java.lang.String,java.lang.String);
#    public static void e(java.lang.String,java.lang.String,java.lang.Exception);
#}

-dontwarn java.lang.invoke.**
-dontwarn com.jj.game.boost.view.CustomAlertDialog
-dontwarn com.jj.game.boost.view.CustomAlertDialog$*
-dontwarn com.jj.game.boost.activity.ProgressbarActivity

#保持Annotation不混淆
-keepattributes *Annotation*,InnerClasses

#避免混淆泛型
-keepattributes Signature

#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

#-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
#-keep public class * extends com.ccmt.appmaster.activity.** {*;}
#-keep public class * extends com.ccmt.appmaster.service.** {*;}
#-keep public class * extends android.app.Application {*;}   # 保持哪些类不被混淆
#-keep public class * extends android.app.Service {*;}      # 保持哪些类不被混淆
#-keep public class * extends com.ccmt.library.service.AbstractService {*;}     # 保持哪些类不被混淆
#-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
#-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
#-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
#-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.view.View
#-keep public class * extends com.ccmt.library.service.AbstractService
#-keep public class * extends com.ccmt.appmaster.activity.**
#-keep public class * extends com.ccmt.appmaster.service.**

-keep public class * extends android.app.Application {*;}
-keep public class * extends android.app.Activity {*;}
-keep public class * extends android.app.Service {*;}
-keep public class * extends android.content.BroadcastReceiver {*;}
-keep public class * extends android.content.ContentProvider {*;}
-keep public class * extends android.app.backup.BackupAgentHelper {*;}
-keep public class * extends android.preference.Preference {*;}
#-keep public class * extends android.view.View {*;}
#-keep public class * extends com.ccmt.library.service.AbstractService {*;}
#-keep public class * extends com.jj.game.boost.activity.** {*;}
#-keep public class * extends com.wifi.boost.clean.accelerate.service.** {*;}

# 保持自定义控件类不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep public class com.android.vending.licensing.ILicensingService {*;}

# 保持support下的所有类及其内部类
-keep class android.support.** {*;}

-keep public class com.android.** {*;}   # 保持哪些类不被混淆
-keep public class com.google.** {*;}
-keep public class android.** {*;}
#-keep public class com.ccmt.library.lru.** {*;}

-keep public class android.app.AppOpsManager {*;}

# 保持内部类不被混淆
-keep public class com.ccmt.appmaster.R$*{
public static final int *;
}
-keep public class android.app.AppOpsManager$* {*;}

-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}

# 保持反射类不被混淆
-keep public class com.jj.game.boost.utils.ReflectUtils {*;}

# 保持我们自定义控件(继承自View)不被混淆
-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}

# 保持Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保持自定义WebViewClient类的方法不被混淆
#-keepclassmembers class * extends android.webkit.WebViewClient {
#    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#    public void *(android.webkit.WebView, jav.lang.String);
#    public boolean *(android.webkit.WebView, java.lang.String);
#}

# 保持app与HTML5的JavaScript的交互类的方法不被混淆
# package com.ljd.example;
#
# public class JSInterface {
#     @JavascriptInterface
#     public void callAndroidMethod(){
#         // do something
#     }
# }
# 我们需要确保这些js要调用的原生方法不能够被混淆,于是我们需要做如下处理.
#-keepclassmembers class com.ljd.example.JSInterface {
#    <methods>;
#}

# 保持带有回调函数的onXXEvent不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
}

-keepclasseswithmembernames class * {  # 保持native方法不被混淆
    native <methods>;
}

#友盟
-keep public class com.umeng.analytics.** {*;}

#Youmeng Analysis
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#Tencent Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#针对HttpConnection报错的解决
-dontwarn org.apache.http.**
-keep class org.apache.http.** { *;}

#glide
-keep class com.bumptech.glide.** {*;}

#EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

#greendao3.2.0,此是针对3.2.0,如果是之前的,可能需要更换下包名.
#-dontwarn org.greenrobot.greendao.**
-dontwarn org.greenrobot.greendao.database.DatabaseOpenHelper$EncryptedHelper
-dontwarn org.greenrobot.greendao.rx.RxQuery$*
-dontwarn org.greenrobot.greendao.rx.RxUtils$*
-dontwarn org.greenrobot.greendao.AbstractDao
-dontwarn org.greenrobot.greendao.AbstractDaoSession
-dontwarn org.greenrobot.greendao.database.DatabaseOpenHelper
-dontwarn org.greenrobot.greendao.database.DatabaseOpenHelper$EncryptedHelper
-dontwarn org.greenrobot.greendao.database.EncryptedDatabase
-dontwarn org.greenrobot.greendao.database.EncryptedDatabaseStatement
-dontwarn org.greenrobot.greendao.query.Query
-dontwarn org.greenrobot.greendao.rx.RxBase
-dontwarn org.greenrobot.greendao.rx.RxDao
-dontwarn org.greenrobot.greendao.rx.RxQuery
-dontwarn org.greenrobot.greendao.rx.RxQuery$*
-dontwarn org.greenrobot.greendao.rx.RxTransaction
-dontwarn org.greenrobot.greendao.rx.RxUtils
-keep public class * extends org.greenrobot.greendao.** {*;}
-keep class **$Properties
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
