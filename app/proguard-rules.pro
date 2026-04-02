# Preserve readable stack traces in minified release builds.
-keepattributes SourceFile,LineNumberTable

# Protobuf lite stores generated field names in message metadata and resolves them via
# reflection at runtime. Keep message fields unrenamed so release builds can still read
# proto-backed DataStore and network messages.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite$ExtendableMessage {
    <fields>;
}
