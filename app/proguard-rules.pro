# TwinSpace ProGuard / R8 Rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepclassmembers class com.twinspace.app.model.** { *; }
-keep class com.twinspace.app.ui.TwinSpaceViewModel { <init>(android.app.Application); }
-keep enum com.twinspace.app.ui.SelectionSlot { *; }
