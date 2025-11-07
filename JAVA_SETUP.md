# Java 17 Setup for VeilGuard Android

## Why Java 17?

Android Gradle Plugin 8.2.0 requires **Java 17** to run. If you encounter an error like:

```
Android Gradle plugin requires Java 17 to run. You are currently using Java 11.
```

or

```
Value '/usr/lib/jvm/java-17-openjdk-amd64' given for org.gradle.java.home Gradle property is invalid
```

Follow the instructions below to configure Java 17 properly.

## Option 1: Configure in Android Studio (Recommended)

1. Open Android Studio
2. Go to **File → Settings** (or **Android Studio → Preferences** on macOS)
3. Navigate to **Build, Execution, Deployment → Build Tools → Gradle**
4. Under **Gradle JDK**, select **Java 17** from the dropdown
5. Click **Apply** and **OK**
6. Sync the project

## Option 2: Set JAVA_HOME Environment Variable

### On Windows:

1. Download and install Java 17 from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/#java17)
2. Open **System Properties → Environment Variables**
3. Add or update `JAVA_HOME` to point to your Java 17 installation:
   ```
   C:\Program Files\Java\jdk-17
   ```
4. Restart Android Studio

### On macOS:

1. Install Java 17 using Homebrew:
   ```bash
   brew install openjdk@17
   ```

2. Set JAVA_HOME in your shell profile (`~/.zshrc` or `~/.bash_profile`):
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

3. Reload your shell:
   ```bash
   source ~/.zshrc
   ```

### On Linux (Ubuntu/Debian):

1. Install Java 17:
   ```bash
   sudo apt-get update
   sudo apt-get install openjdk-17-jdk
   ```

2. Set Java 17 as default:
   ```bash
   sudo update-alternatives --config java
   # Select Java 17 from the list
   ```

3. Verify:
   ```bash
   java -version
   # Should show: openjdk version "17.x.x"
   ```

## Option 3: Configure in gradle.properties (Advanced)

If you want to specify a custom Java path for this project only, you can add this line to `gradle.properties`:

```properties
org.gradle.java.home=/path/to/your/java-17
```

**Important**: Replace `/path/to/your/java-17` with the actual path to your Java 17 installation.

### Finding Your Java 17 Path:

**Windows:**
```cmd
where java
```

**macOS/Linux:**
```bash
which java
# Then use: dirname $(dirname $(readlink -f $(which java)))
```

Or on macOS:
```bash
/usr/libexec/java_home -v 17
```

## Verify Java Version

After configuration, verify that Java 17 is being used:

```bash
java -version
```

Expected output:
```
openjdk version "17.x.x" ...
```

## Troubleshooting

### "SDK location not found"

This error means Android SDK is not configured. In Android Studio:

1. Go to **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Note the **Android SDK Location** path
3. The SDK will be automatically detected when you open the project in Android Studio

### "Gradle sync failed"

1. **File → Invalidate Caches → Invalidate and Restart**
2. Delete `.gradle` folder in project root
3. **File → Sync Project with Gradle Files**

### Still having issues?

Make sure:
- Java 17 is properly installed
- Android Studio is using Java 17 (check in Settings → Gradle → Gradle JDK)
- The project is opened in Android Studio (not just a text editor)
- You have a stable internet connection for dependency downloads

## Build the Project

Once Java 17 is configured:

```bash
./gradlew clean
./gradlew assembleDebug
```

Or simply use Android Studio's **Build → Make Project** (Ctrl+F9).

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
