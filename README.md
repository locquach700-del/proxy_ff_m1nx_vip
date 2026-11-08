# M1NX Proxy — Vietnamese Android rebuild

A fresh, lightweight Android utility with Vietnamese UI, a default `Random` access label, a reduced-motion switch, saved proxy host/port fields, and a TCP reachability check.

## Scope and limitations

This is a new utility, not a function-preserving reimplementation of the supplied APK. The old APK contains proprietary login validation, native VPN/tunnel components, and ESP/projection components. Those cannot be restored accurately from the APK alone. This rebuild does not include game overlays, ESP, player tracking, process hooks, or the old server-side login system.

The included proxy control only tests whether a host and TCP port accept a connection. It does not create a VPN, authenticate a SOCKS5 session, route device traffic, or modify a game. The displayed `Random` label is a default UI value, not a server-issued credential.

## Requirements

- JDK 17
- Android SDK Platform 35 and Build Tools
- Gradle 8.9

## Build locally

```bash
gradle --no-daemon :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Use

Install the debug APK on Android 6.0 or newer. Enter a proxy hostname and port, then select **Kiểm tra kết nối** to test TCP reachability. The app saves the last host and port locally. The **Chuyển động nhẹ** switch turns entrance and button animations on or off.

## GitHub Actions

A push to `main` or a manual run of **Build Android APK** builds the debug APK and uploads it as the workflow artifact `M1NX-Proxy-Vietnamese-debug`.

## Signing disclaimer

The debug APK is automatically signed with the Android debug certificate generated in the GitHub runner. It is suitable for testing, not for publishing or guaranteed seamless upgrades between builds. A private release keystore must be configured as protected GitHub Actions secrets before producing a production release.

There are no game offsets or memory hooks in this project. Offsets from other app versions do not apply.
