# Auto-Launch WebView Android App

A robust Android application that automatically launches and maintains a WebView-based kiosk experience with intelligent auto-recovery mechanisms. Perfect for digital signage, information displays, or any application that needs persistent visibility.

## 🚀 Features

### Core Functionality
- **WebView Integration**: Loads and displays web content (default: techstern.com)
- **Auto-Launch on Boot**: Automatically starts when device powers on
- **Inactivity Recovery**: Relaunches after 30 seconds of inactivity
- **Call Integration**: Launches during incoming phone calls
- **Secret Code Access**: Manual launch via `*#*#1234#*#*`

### Smart Persistence
- **Background Monitoring**: Foreground service monitors app state
- **Multiple Launch Strategies**: Direct launch, overlay launch, task management
- **Crash Recovery**: Automatically recovers from app crashes
- **Battery Optimization Bypass**: Prevents system from killing the app

### MIUI Optimization
- **Xiaomi/MIUI Support**: Specialized handling for MIUI devices
- **Automatic Permissions**: Programmatically enables required MIUI permissions
- **Reflection-based Configuration**: Uses internal APIs to bypass restrictions
- **Manufacturer-specific Delays**: Optimized boot delays for different OEMs

## 📱 Supported Devices

- **Primary**: Xiaomi/MIUI devices (MIUI 8+)
- **Secondary**: All Android devices (API 21+)
- **Optimized for**: Tablets and dedicated display devices

## 🔧 Installation

1. Clone the repository:
```bash
git clone https:https://github.com/No-codeZone/Persistent-Auto-Launch.git
cd autolaunch-webview
```

2. Open in Android Studio

3. Configure your target URL in `MainActivity.java`:
```java
webView.loadUrl("https://your-website.com");
```

4. Build and install:
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ Configuration

### Required Permissions
The app automatically requests these permissions:
- `CALL_PHONE` - For InCall service integration
- `SYSTEM_ALERT_WINDOW` - For overlay launching
- `RECEIVE_BOOT_COMPLETED` - For boot auto-launch
- `FOREGROUND_SERVICE` - For background monitoring
- Battery optimization exemption

### MIUI-Specific Setup
For Xiaomi devices, the app will automatically:
- Enable autostart permission
- Disable battery optimization
- Enable popup permissions
- Configure background app refresh
- Set display over other apps

### Custom URL Configuration
Edit `MainActivity.java` line 67:
```java
webView.loadUrl("https://your-custom-url.com");
```

## 🏗️ Architecture

### Core Components

#### Services
- **`InactivityMonitorService`**: Background monitoring and auto-relaunch
- **`MyInCallService`**: Phone call integration

#### Activities
- **`MainActivity`**: Primary WebView container
- **`LaunchActivity`**: Lightweight launcher for quick startup

#### Receivers
- **`BootReceiver`**: Handles device boot events
- **`SecretCodeReceiver`**: Processes secret dial codes

#### Utilities
- **`MiuiAutoPermissionManager`**: MIUI-specific permission handling
- **`OverlayLauncher`**: Multiple launch strategy implementation

### Launch Flow
```
Device Boot → BootReceiver → LaunchActivity → MainActivity
      ↓
Inactivity Timer (30s) → InactivityMonitorService → Auto-relaunch
      ↓
Call Received → MyInCallService → MainActivity
      ↓
Secret Code (*#*#1234#*#*) → SecretCodeReceiver → LaunchActivity
```

## 🔐 Security Considerations

### Permissions Required
- **High**: System Alert Window (overlay launching)
- **Medium**: Phone access (call integration)
- **Low**: Boot completion, foreground service

### Privacy Notes
- No data collection or transmission
- WebView follows standard browser security
- All permissions used for legitimate auto-launch functionality

## 🛠️ Customization

### Inactivity Timeout
Change timeout in `MainActivity.java`:
```java
handler.postDelayed(inactivityRunnable, 30_000); // 30 seconds
```

### Boot Delay
Modify manufacturer-specific delays in `BootReceiver.java`:
```java
private int getBootDelayForManufacturer() {
    // Customize delays per manufacturer
}
```

### Secret Code
Update dial code in `AndroidManifest.xml`:
```xml
<data android:host="1234" android:scheme="android_secret_code" />
```

## 📊 Performance

### Resource Usage
- **Memory**: ~50MB (WebView + services)
- **CPU**: Minimal background usage
- **Battery**: Optimized for minimal drain
- **Network**: Depends on loaded web content

### Compatibility
- **Android Versions**: 5.0+ (API 21+)
- **Screen Sizes**: Optimized for tablets
- **Orientations**: Supports portrait/landscape

## 🐛 Troubleshooting

### Common Issues

**App doesn't auto-launch on boot:**
- Check autostart permissions in device settings
- Verify battery optimization is disabled
- Ensure app isn't in app hibernation/sleep mode

**Inactivity timer not working:**
- Confirm foreground service is running
- Check notification permissions
- Verify overlay permission is granted

**MIUI-specific problems:**
- Run the automatic MIUI permission configuration
- Manually check Security app permissions
- Disable MIUI optimization in Developer Options

### Debug Mode
Enable debug logging by modifying log levels in each class:
```java
private static final String TAG = "YourClassName";
Log.d(TAG, "Debug message");
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Android development best practices
- Test on multiple device manufacturers
- Ensure MIUI compatibility
- Add appropriate logging for debugging

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Android Open Source Project
- MIUI development community
- WebView security best practices
- Kiosk mode implementation patterns

---

**Note**: This app is designed for legitimate use cases like digital signage and information displays. Ensure compliance with your organization's security policies and local regulations when deploying.
