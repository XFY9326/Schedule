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


-optimizationpasses 5
-allowaccessmodification

-repackageclasses pkg
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature

# App
-keep public class * extends tool.xfy9326.schedule.ui.fragment.base.AbstractSettingsFragment

-keepclassmembers class * extends androidx.viewbinding.ViewBinding{
    public inflate(...);
}

# Kotlin Serialization
-dontnote kotlinx.serialization.AnnotationsKt
# noinspection ShrinkerUnresolvedReference
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class tool.xfy9326.schedule.json.**$$serializer { *; }
-keepclassmembers class tool.xfy9326.schedule.json.** {
    *** Companion;
}
-keepclasseswithmembers class tool.xfy9326.schedule.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}