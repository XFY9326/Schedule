# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Global
-repackageclasses cls
-keepattributes SourceFile,LineNumberTable

# App
-keep class * extends tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment
-keepclassmembers @tool.xfy9326.schedule.annotation.CourseImportConfig class * {
     <init>(...);
}
-keepclassmembers class * extends tool.xfy9326.schedule.content.base.AbstractCourseParser {
     <init>(...);
}
-keepclassmembers class * extends tool.xfy9326.schedule.content.base.AbstractCourseProvider {
     <init>(...);
}
-keepclassmembers class * extends tool.xfy9326.schedule.content.base.AbstractExternalCourseProcessor {
     <init>(...);
}

# Kotlin Serialization
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

#noinspection ShrinkerUnresolvedReference
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Kotlinx Serialization Custum
-keep, includedescriptorclasses class tool.xfy9326.schedule.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class tool.xfy9326.schedule.** {
    *** Companion;
}
-keepclasseswithmembers @kotlinx.serialization.Serializable class tool.xfy9326.schedule.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#Jsoup
# Ignore intended-to-be-optional re2j classes - only needed if using re2j for jsoup regex
# jsoup safely falls back to JDK regex if re2j not on classpath, but has concrete re2j refs
-dontwarn com.google.re2j.**

# Okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
# May be used with robolectric or deliberate use of Bouncy Castle on Android
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**