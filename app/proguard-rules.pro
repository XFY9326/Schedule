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
-keepattributes LineNumberTable,SourceFile

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

# Okio
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Okhttp (v4.9.0)
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder