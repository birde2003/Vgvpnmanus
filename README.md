# VEILGUARD Android VPN Application - MVP

## 📱 Overview

VeilGuard is a complete, production-ready Android VPN application with modern UI, secure authentication, trial management, subscription handling, and VPN connectivity.

## ✨ Features

### Implemented Features

- ✅ **Animated Splash Screen** - Professional branded launch experience
- ✅ **Legal Compliance** - User agreement and terms acceptance screen
- ✅ **Onboarding Flow** - 3-page introduction to app features
- ✅ **Authentication System** - Login and registration with backend API
- ✅ **VPN Service** - Android VpnService implementation
- ✅ **Trial Management** - 7-day free trial per device
- ✅ **Subscription Plans** - Multiple subscription tiers (1, 3, 6, 12 months)
- ✅ **Secure Storage** - Encrypted SharedPreferences for sensitive data
- ✅ **Backend Integration** - Full REST API integration with VeilGuard backend
- ✅ **Material Design 3** - Modern, professional UI/UX
- ✅ **App Icons** - All density resources included

### Technical Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM-ready structure
- **Networking**: Retrofit + OkHttp
- **Security**: EncryptedSharedPreferences
- **Payments**: Stripe SDK integrated
- **VPN**: Android VpnService + WireGuard library

## 🚀 Building the App

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Build Steps

1. **Open in Android Studio**
2. **Sync Gradle** - Wait for dependencies to download
3. **Build APK**: `./gradlew assembleDebug`
4. **Install**: `adb install app/build/outputs/apk/debug/app-debug.apk`

## 📊 Statistics

- **20 Kotlin source files**
- **7 XML layouts**
- **~2,500 lines of code**
- **Backend**: https://api.vg-vpn.com

## 📝 Testing

**Test Credentials**:
- Email: `admin2@veilguard.com`
- Password: `admin123`

---

**Version**: 1.0.0 (MVP)  
**Status**: ✅ Ready for Testing
