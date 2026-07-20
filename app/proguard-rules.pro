# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools proguard-defaults.txt.

# Keep Google API client classes
-keep class com.google.api.services.sheets.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.sheets.**

# Keep Room entities
-keep class com.expensetracker.data.local.entity.** { *; }
