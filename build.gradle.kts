// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // ✅ FIXED: Changed version from 4.4.4 to 4.4.0 (stable version)
    id("com.google.gms.google-services") version "4.4.0" apply false
}