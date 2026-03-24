# Keep annotations/signatures used by Kotlin and Firebase reflection paths.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Keep Kotlin metadata annotation (used by some tooling/reflection).
-keep class kotlin.Metadata { *; }

# Keep enum helper methods (defensive for model enums).
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Firestore annotations must remain available for field mapping.
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.DocumentId <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}

# Silence known optional annotation package warnings.
-dontwarn org.jetbrains.annotations.**
