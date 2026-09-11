# Preserve readable stack traces in minified release builds.
-keepattributes SourceFile,LineNumberTable

# EmoticonResolver discovers bundled emoticons by reflecting on these field names.
-keepclassmembers class app.tiebalite.core.ui.R$drawable {
    public static int image_emoticon*;
}

# Protobuf lite stores generated field names in message metadata and resolves them via
# reflection at runtime. Keep message fields unrenamed so release builds can still read
# proto-backed DataStore and network messages.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite$ExtendableMessage {
    <fields>;
}
