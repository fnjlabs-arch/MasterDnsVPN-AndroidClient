# ProGuard rules for SadSha DNS VPN Android

# Keep Go mobile bindings
-keep class mobile.** { *; }
-keep class go.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep Room entities
-keep class com.sadsha.dnsvpn.data.local.** { *; }
