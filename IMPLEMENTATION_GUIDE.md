# VeilGuard Android VPN - Implementation Guide

## Table of Contents

1. [Getting Started](#getting-started)
2. [Project Setup](#project-setup)
3. [Building the App](#building-the-app)
4. [Testing](#testing)
5. [Deployment](#deployment)
6. [API Integration](#api-integration)
7. [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17 or later
- **Android SDK**: API Level 34
- **Git**: For version control
- **Device/Emulator**: Android 7.0 (API 24) or higher

### System Requirements

- **OS**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: 8 GB minimum (16 GB recommended)
- **Disk Space**: 10 GB free space
- **Internet**: Required for Gradle dependencies

## Project Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/birde2003/Vgvpnmanus.git
cd Vgvpnmanus
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Click **File** → **Open**
3. Navigate to the cloned project directory
4. Click **OK**
5. Wait for Gradle sync to complete (this may take 5-10 minutes on first run)

### Step 3: Configure SDK

If prompted:
1. Install missing SDK platforms (API 34)
2. Accept Android SDK licenses
3. Install required build tools

### Step 4: Verify Configuration

Check that these files exist:
- `build.gradle` (root)
- `app/build.gradle`
- `settings.gradle`
- `gradle.properties`

## Building the App

### Debug Build

For development and testing:

```bash
./gradlew assembleDebug
```

Output location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

For production deployment:

```bash
./gradlew assembleRelease
```

Output location: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Build from Android Studio

1. Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Wait for build to complete
3. Click **locate** in the notification to find the APK

### Signing Release Build

For Play Store deployment, you need a signed APK:

1. **Generate Keystore** (first time only):
```bash
keytool -genkey -v -keystore veilguard-release.keystore -alias veilguard -keyalg RSA -keysize 2048 -validity 10000
```

2. **Configure signing** in `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("../veilguard-release.keystore")
            storePassword "your_password"
            keyAlias "veilguard"
            keyPassword "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

3. **Build signed APK**:
```bash
./gradlew assembleRelease
```

## Testing

### Install on Device

#### Via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Via Android Studio

1. Connect device or start emulator
2. Click **Run** → **Run 'app'**
3. Select target device
4. App will install and launch automatically

### Test Credentials

Use these credentials to test the app:

- **Email**: `admin2@veilguard.com`
- **Password**: `admin123`

### Test Flow

1. **Launch App** → Splash screen displays (2 seconds)
2. **Accept Terms** → Check the box and click Continue
3. **Onboarding** → Swipe through 3 pages or skip
4. **Login** → Enter test credentials
5. **Dashboard** → Main VPN interface appears
6. **Connect VPN** → Grant VPN permission when prompted
7. **Test Trial** → Click "Start 7-Day Free Trial"
8. **View Plans** → Click "Subscribe Now"

### Verify API Connection

Check logcat for API calls:

```bash
adb logcat | grep "OkHttp"
```

You should see requests to `https://api.vg-vpn.com`

## Deployment

### Google Play Store

#### 1. Prepare Release

- Update version in `app/build.gradle`:
```gradle
versionCode 2
versionName "1.0.1"
```

- Build signed APK or AAB:
```bash
./gradlew bundleRelease
```

#### 2. Create Play Console Listing

1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app
3. Fill in app details:
   - **App name**: VEILGUARD
   - **Category**: Tools
   - **Content rating**: Everyone
   - **Privacy policy**: Required

#### 3. Upload Build

1. Navigate to **Production** → **Create new release**
2. Upload AAB file
3. Add release notes
4. Review and rollout

#### 4. Store Listing

Required assets:
- **Icon**: 512x512 PNG (already included in project)
- **Feature graphic**: 1024x500 PNG
- **Screenshots**: At least 2 (phone), 1024x500 each
- **Short description**: 80 characters max
- **Full description**: 4000 characters max

### Direct Distribution

For beta testing or enterprise distribution:

1. Build signed APK
2. Host on your server or use Firebase App Distribution
3. Share download link
4. Users must enable "Install from Unknown Sources"

### Firebase App Distribution

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Deploy
firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
  --app YOUR_FIREBASE_APP_ID \
  --groups testers
```

## API Integration

### Backend Endpoints

The app connects to: `https://api.vg-vpn.com`

#### Authentication

**Login**:
```http
POST /token
Content-Type: application/x-www-form-urlencoded

username=user@example.com&password=password123
```

Response:
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "token_type": "bearer"
}
```

**Register**:
```http
POST /register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "device_id": "unique-device-id"
}
```

#### Trial Management

**Check Eligibility**:
```http
GET /api/v1/trials/check/{device_id}
Authorization: Bearer {token}
```

**Start Trial**:
```http
POST /api/v1/trials/start
Authorization: Bearer {token}
Content-Type: application/json

{
  "device_id": "unique-device-id",
  "email": "user@example.com"
}
```

#### VPN Servers

**Get Servers**:
```http
GET /api/v1/vpn/servers
Authorization: Bearer {token}
```

Response:
```json
[
  {
    "id": "server-1",
    "name": "New York",
    "ip_address": "192.168.1.1",
    "location": "nyc1",
    "status": "active"
  }
]
```

#### Subscriptions

**Get Plans**:
```http
GET /api/v1/subscriptions/plans
Authorization: Bearer {token}
```

### Modifying API Configuration

To change the API base URL, edit `app/build.gradle`:

```gradle
buildConfigField "String", "API_BASE_URL", "\"https://your-api.com/\""
```

Then rebuild the app.

## Troubleshooting

### Common Issues

#### Gradle Sync Failed

**Problem**: Gradle sync fails with dependency errors

**Solution**:
1. Check internet connection
2. File → Invalidate Caches → Invalidate and Restart
3. Delete `.gradle` folder and sync again
4. Update Gradle version in `gradle/wrapper/gradle-wrapper.properties`

#### Build Failed

**Problem**: Build fails with compilation errors

**Solution**:
1. Clean project: Build → Clean Project
2. Rebuild: Build → Rebuild Project
3. Check JDK version (must be 17)
4. Update Android Studio to latest version

#### App Crashes on Launch

**Problem**: App crashes immediately after launch

**Solution**:
1. Check logcat for error messages:
   ```bash
   adb logcat | grep "AndroidRuntime"
   ```
2. Verify all dependencies are installed
3. Check if device meets minimum SDK requirement (API 24)
4. Clear app data and reinstall

#### VPN Connection Fails

**Problem**: VPN connection button doesn't work

**Solution**:
1. Grant VPN permission when prompted
2. Check if another VPN is active
3. Verify backend API is accessible
4. Check logcat for VPN service errors

#### API Calls Fail

**Problem**: Network requests return errors

**Solution**:
1. Verify backend is running: `curl https://api.vg-vpn.com/health`
2. Check internet permission in AndroidManifest.xml
3. Verify SSL certificate is valid
4. Check authentication token is being sent
5. Review OkHttp logs in logcat

#### Stripe Integration Issues

**Problem**: Payment flow doesn't work

**Solution**:
1. Verify Stripe publishable key in build.gradle
2. Check Stripe SDK version compatibility
3. Test with Stripe test cards
4. Review Stripe logs in logcat

### Debug Mode

Enable verbose logging by adding to `Application` class:

```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### Performance Profiling

Use Android Studio Profiler:
1. Run → Profile 'app'
2. Monitor CPU, Memory, Network usage
3. Identify bottlenecks

### Network Debugging

Use Charles Proxy or similar:
1. Configure proxy on device
2. Install SSL certificate
3. Monitor all API calls

## Additional Resources

- **Android Documentation**: https://developer.android.com
- **Kotlin Documentation**: https://kotlinlang.org/docs
- **Material Design**: https://m3.material.io
- **Retrofit**: https://square.github.io/retrofit
- **Stripe Android**: https://stripe.com/docs/mobile/android
- **WireGuard**: https://www.wireguard.com/

## Support

For issues or questions:
- **Backend API**: https://api.vg-vpn.com
- **Admin Panel**: https://admin.vg-vpn.com
- **GitHub**: https://github.com/birde2003/Vgvpnmanus

---

**Last Updated**: November 2025  
**Version**: 1.0.0
