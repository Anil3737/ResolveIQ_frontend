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

# Retrofit 2 rules
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp 3 rules
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepclassmembers class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Gson rules
-keep class com.google.gson.** { *; }
-keep class com.simats.resolveiq_frontend.data.model.** { *; }
-keepattributes Signature, *Annotation*
-keepclassmembers class com.simats.resolveiq_frontend.data.model.** {
    <fields>;
}

# Preserve line numbers for debugging release crashes
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile