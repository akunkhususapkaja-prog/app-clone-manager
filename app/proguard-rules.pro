# Virtual Engine - BlackBox
-keep class com.blackbox.** { *; }
-keep class com.lody.** { *; }
-keep class com.appclone.manager.engine.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom Application class
-keep class com.appclone.manager.AppCloneApplication { *; }
