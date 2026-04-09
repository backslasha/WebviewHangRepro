# WebView Touch Freeze Repro

Minimal Android project to reproduce the WebView touch freeze bug caused by a race condition between `webView.destroy()` and high-frequency `window.prompt()` IPC.

## Bug Summary

When a WebView (WebView2) fires `window.prompt()` at high frequency (default every 10ms) and is destroyed while prompts are still in-flight, the last prompt IPC gets dispatched during the `~AwContents` destructor chain. At that point `AwContentsClientBridge` is already null, so `SetBlocked(true)` executes but the corresponding `SetBlocked(false)` never fires — freezing touch input for **all** WebViews in the same process.

## Repro Steps

1. Launch the app — WebView1 shows a tap counter
2. Tap the green area a few times to confirm touch works
3. Tap **"Open WebView2"** — the PING counter starts automatically (10ms interval)
4. While the counter is running, **press Back** to destroy WebView2
5. Back on WebView1, tap the green area — **touch is frozen**

> **Tip:** Shorter interval = higher repro rate. Use the **Faster** button in WebView2 to go as low as 1ms if it doesn't trigger on the first try.

## Project Structure

```
app/src/main/
├── java/com/yhb/webviewhangrepro/
│   ├── MainActivity.kt              # WebView1 — touch test page
│   └── webview/
│       └── LocalWebViewActivity.java # WebView2 — high-freq prompt page
├── assets/
│   └── dialogLeakDemo.html           # JS that fires prompt() every 10ms
└── res/layout/
    ├── activity_main.xml             # WebView1 layout
    └── activity_web_view.xml         # WebView2 layout
```

## Logcat Filter

```
adb logcat -s WVHangRepro
```
