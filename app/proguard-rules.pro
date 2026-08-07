# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools proguard-defaults.txt.

# Keep Google API client classes (Sheets + Drive)
-keep class com.google.api.services.sheets.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.sheets.**
-dontwarn com.google.api.services.drive.**

# Keep Gson serialization (used by Google HTTP client)
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends com.google.api.client.json.GenericJson { *; }

# Keep Room entities and DAOs
-keep class com.expensetracker.data.local.entity.** { *; }
-keep class com.expensetracker.data.local.dao.** { *; }

# Keep domain models (used in reflection by mappers)
-keep class com.expensetracker.domain.model.** { *; }
