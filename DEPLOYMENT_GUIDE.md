# VeilGuard Android VPN - Deployment Guide

## Overview

This guide covers the complete deployment process for the VeilGuard Android VPN application, from building the APK to publishing on Google Play Store and managing updates.

## Pre-Deployment Checklist

Before deploying, ensure:

- [ ] All features tested and working
- [ ] Backend API is stable and accessible
- [ ] Stripe integration configured with live keys
- [ ] App icons and branding finalized
- [ ] Privacy policy and terms of service published
- [ ] Version number updated in build.gradle
- [ ] ProGuard rules configured for release
- [ ] Signing keystore generated and secured
- [ ] Google Play Console account created
- [ ] All required Play Store assets prepared

## Building for Production

### 1. Update Version

Edit `app/build.gradle`:

```gradle
android {
    defaultConfig {
        versionCode 1      // Increment for each release
        versionName "1.0.0" // Semantic versioning
    }
}
```

### 2. Configure ProGuard

Ensure ProGuard is enabled in `app/build.gradle`:

```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### 3. Generate Signing Key

**First time only**:

```bash
keytool -genkey -v -keystore veilguard-release.keystore \
  -alias veilguard \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Important**: Store keystore file and passwords securely. Loss means you cannot update the app.

### 4. Configure Signing

Create `keystore.properties` in project root:

```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=veilguard
storeFile=../veilguard-release.keystore
```

Add to `.gitignore`:
```
keystore.properties
*.keystore
```

Update `app/build.gradle`:

```gradle
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

### 5. Build Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### 6. Build App Bundle (Recommended for Play Store)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 7. Verify Build

```bash
# Check APK signature
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Analyze APK size
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app.apks \
  --mode=universal

# Extract and check
unzip app.apks -d apks
ls -lh apks/
```

## Google Play Store Deployment

### 1. Create Play Console Account

1. Go to [Google Play Console](https://play.google.com/console)
2. Pay one-time $25 registration fee
3. Complete account verification

### 2. Create App

1. Click **Create app**
2. Fill in details:
   - **App name**: VEILGUARD
   - **Default language**: English (US)
   - **App or game**: App
   - **Free or paid**: Free (with in-app purchases)

3. Complete declarations:
   - Privacy policy URL (required)
   - App access (full access)
   - Ads (no ads)
   - Content rating questionnaire
   - Target audience
   - Data safety

### 3. Prepare Store Listing

#### Required Assets

**App icon**:
- Size: 512 x 512 pixels
- Format: PNG (32-bit)
- Already included in project at `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

**Feature graphic**:
- Size: 1024 x 500 pixels
- Format: PNG or JPEG
- No transparency

**Screenshots** (minimum 2):
- Phone: 320-3840 pixels on shortest side
- Recommended: 1080 x 1920 pixels
- Format: PNG or JPEG

**Optional but recommended**:
- Promo video (YouTube URL)
- TV banner: 1280 x 720 pixels
- Wear OS screenshots

#### Store Listing Text

**Short description** (80 characters):
```
Secure VPN with military-grade encryption. 7-day free trial.
```

**Full description** (4000 characters):
```
VEILGUARD - Your Ultimate VPN Protection

Protect your online privacy with VEILGUARD, a premium VPN service featuring military-grade encryption, global server network, and lightning-fast speeds.

🔒 MILITARY-GRADE SECURITY
• WireGuard protocol for maximum security
• AES-256 encryption
• No-logs policy
• Kill switch protection
• DNS leak protection

🌍 GLOBAL SERVER NETWORK
• Servers in multiple locations worldwide
• High-speed connections
• Unlimited bandwidth
• Low latency

⚡ FAST & RELIABLE
• One-tap connect
• Auto-reconnect
• Optimized for streaming
• Works with all apps

🎁 7-DAY FREE TRIAL
• No credit card required
• Full feature access
• One trial per device
• Cancel anytime

💎 FLEXIBLE PLANS
• 1 Month: $9.99
• 3 Months: $24.99 (Save 17%)
• 6 Months: $44.99 (Save 25%)
• 12 Months: $79.99 (Save 33%)

✨ PREMIUM FEATURES
• Unlimited VPN access
• All server locations
• Priority support
• Regular updates
• No ads

📱 EASY TO USE
• Simple, intuitive interface
• One-tap connection
• Automatic server selection
• Connection statistics

🔐 PRIVACY FIRST
• No activity logs
• No connection logs
• Secure payment via Stripe
• GDPR compliant

Download VEILGUARD now and experience true online freedom with our 7-day free trial!

Support: support@vg-vpn.com
Privacy Policy: https://vg-vpn.com/privacy
Terms of Service: https://vg-vpn.com/terms
```

### 4. Content Rating

Complete the questionnaire:
- Category: Tools/Utilities
- Violence: None
- Sexual content: None
- Profanity: None
- Controlled substances: None
- Result: PEGI 3, ESRB Everyone

### 5. Pricing & Distribution

- **Pricing**: Free (with in-app purchases)
- **Countries**: Select all or specific countries
- **In-app products**: Configure subscription plans
  - 1 Month: $9.99
  - 3 Months: $24.99
  - 6 Months: $44.99
  - 12 Months: $79.99

### 6. App Content

Complete all sections:
- **Privacy policy**: URL to your policy
- **App access**: All functionality available
- **Ads**: No ads
- **Content rating**: As determined by questionnaire
- **Target audience**: 18+
- **News app**: No
- **COVID-19 contact tracing**: No
- **Data safety**: Complete form about data collection

### 7. Upload Build

1. Go to **Production** → **Create new release**
2. Upload AAB file
3. Add release notes:
```
Version 1.0.0 - Initial Release

• Secure VPN with WireGuard protocol
• 7-day free trial
• Multiple subscription plans
• Global server network
• One-tap connect
• Beautiful Material Design UI
• Trial management
• Secure authentication
```

4. Review release
5. Click **Start rollout to Production**

### 8. Review Process

- Google review typically takes 1-3 days
- You'll receive email notification
- Address any issues if rejected
- Once approved, app goes live

## Alternative Distribution Methods

### 1. Direct APK Distribution

For beta testing or enterprise:

```bash
# Build signed APK
./gradlew assembleRelease

# Host on your server
scp app/build/outputs/apk/release/app-release.apk user@server:/var/www/downloads/

# Share link
https://your-server.com/downloads/app-release.apk
```

Users must:
1. Enable "Install from Unknown Sources"
2. Download APK
3. Install manually

### 2. Firebase App Distribution

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Initialize
firebase init appdistribution

# Distribute
firebase appdistribution:distribute \
  app/build/outputs/apk/release/app-release.apk \
  --app YOUR_FIREBASE_APP_ID \
  --groups "testers" \
  --release-notes "Beta version 1.0.0"
```

### 3. Amazon Appstore

1. Create Amazon Developer account
2. Submit APK
3. Similar process to Google Play

### 4. Samsung Galaxy Store

1. Register at Samsung Seller Portal
2. Upload APK
3. Complete store listing

## Post-Deployment

### 1. Monitor Crashes

Use Firebase Crashlytics:

```gradle
// Add to app/build.gradle
dependencies {
    implementation 'com.google.firebase:firebase-crashlytics:18.6.0'
}
```

### 2. Track Analytics

Implement Firebase Analytics:

```kotlin
val analytics = FirebaseAnalytics.getInstance(this)
analytics.logEvent("vpn_connected", Bundle())
```

### 3. Collect Feedback

- Monitor Play Store reviews
- Respond to user feedback
- Track ratings

### 4. Plan Updates

Regular update schedule:
- Bug fixes: As needed
- Minor updates: Monthly
- Major updates: Quarterly

## Updating the App

### Version Numbering

Use semantic versioning:
- **Major** (1.0.0): Breaking changes
- **Minor** (1.1.0): New features
- **Patch** (1.0.1): Bug fixes

### Update Process

1. **Update version** in `app/build.gradle`:
```gradle
versionCode 2  // Increment
versionName "1.0.1"
```

2. **Build new release**:
```bash
./gradlew bundleRelease
```

3. **Upload to Play Console**:
   - Production → Create new release
   - Upload AAB
   - Add release notes
   - Rollout

4. **Staged rollout** (recommended):
   - Start with 10% of users
   - Monitor for issues
   - Increase to 50%, then 100%

## Rollback Plan

If critical issues found:

1. **Halt rollout** in Play Console
2. **Fix issue** in code
3. **Build hotfix** with incremented version
4. **Upload and rollout** immediately
5. **Communicate** with affected users

## Continuous Deployment

### GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Play Store

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Build Release AAB
        run: ./gradlew bundleRelease
        
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.SERVICE_ACCOUNT_JSON }}
          packageName: com.veilguard.vpn
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: production
```

## Security Best Practices

1. **Never commit**:
   - Keystore files
   - Passwords
   - API keys
   - Service account JSON

2. **Use environment variables**:
```gradle
buildConfigField "String", "API_KEY", "\"${System.getenv('API_KEY')}\""
```

3. **Enable Play App Signing**:
   - Let Google manage signing key
   - Protects against key loss

4. **Regular security audits**:
   - Review dependencies
   - Update libraries
   - Scan for vulnerabilities

## Compliance

### GDPR

- Provide privacy policy
- Allow data deletion
- Obtain consent for data collection
- Implement data export

### COPPA

- If targeting children under 13
- Additional restrictions apply
- Consult legal counsel

### VPN Regulations

- Comply with local laws
- Some countries restrict VPN use
- Implement geo-blocking if needed

## Support & Maintenance

### User Support

- Email: support@vg-vpn.com
- Response time: 24-48 hours
- FAQ section in app
- Help center website

### Maintenance Schedule

- **Daily**: Monitor crashes and errors
- **Weekly**: Review user feedback
- **Monthly**: Update dependencies
- **Quarterly**: Major feature updates

## Checklist

Before going live:

- [ ] All features working
- [ ] Backend stable
- [ ] Signed APK/AAB built
- [ ] Play Store listing complete
- [ ] Screenshots uploaded
- [ ] Privacy policy published
- [ ] Terms of service published
- [ ] Support email configured
- [ ] Analytics implemented
- [ ] Crash reporting enabled
- [ ] Beta testing completed
- [ ] Legal review done
- [ ] Marketing materials ready

---

**Last Updated**: November 2025  
**Version**: 1.0.0
