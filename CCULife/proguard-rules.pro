# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/zankio/Android/Sdk/tools/proguard/proguard-android.txt
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
-keepattributes SourceFile,LineNumberTable

# retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# jsoup
-keeppackagenames org.jsoup.nodes

#-dontwarn javax.jcr.**
-dontwarn org.slf4j.**
-dontwarn net.fortuna.ical4j.model.**
-dontwarn edu.emory.mathcs.backport.java.util.concurrent.helpers.**
-dontwarn org.antlr.runtime.tree.**
-dontnote **

# OK HTTP
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature

-keep class org.zankio.ccudata.base.source.annotation.** { *; }
-keep class org.zankio.ccudata.base.source.http.annotation.** { *; }
-keep class org.zankio.ccudata.ecourse.annotation.** { *; }

#  rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}