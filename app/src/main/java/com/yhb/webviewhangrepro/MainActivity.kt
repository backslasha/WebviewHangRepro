package com.yhb.webviewhangrepro

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yhb.webviewhangrepro.webview.LocalWebViewActivity

/**
 * WebView1: main page with an embedded WebView for touch interaction.
 *
 * Tap the button to open LocalWebViewActivity (WebView2, high-freq prompt page).
 * While WebView2 is running, press back to destroy it — WebView1 touch freezes.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WVHangRepro"
    }

    private lateinit var webView: WebView
    private lateinit var statusText: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)

        val container = findViewById<FrameLayout>(R.id.webview_container)
        webView = WebView(this)
        container.addView(webView)
        webView.settings.javaScriptEnabled = true

        webView.loadDataWithBaseURL(
            null,
            """
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <style>
                body { font-family: sans-serif; padding: 20px; background: #f5f5f5; margin: 0; }
                h2 { color: #333; }
                .touch-area {
                    background: #4CAF50; color: white; padding: 40px; text-align: center;
                    border-radius: 12px; margin: 20px 0; font-size: 18px;
                    user-select: none; -webkit-user-select: none;
                    transition: transform 0.1s ease, background 0.15s ease;
                }
                .touch-area:active {
                    background: #388E3C;
                    transform: scale(0.95);
                }
                #counter { font-size: 48px; font-weight: bold; color: #1976D2; text-align: center; }
            </style>
            </head>
            <body>
                <h2>WebView1 — Touch Test Area</h2>
                <p>This is the main WebView. Tap the area below to test touch response.</p>
                <div id="counter">0</div>
                <div class="touch-area" onclick="count++; document.getElementById('counter').textContent=count;">
                    Tap here to count (verify touch is working)
                </div>
                <p style="color:#888; font-size:13px;">
                    If touch stops responding after returning from WebView2, the freeze bug is reproduced.
                </p>
                <script>var count = 0;</script>
            </body>
            </html>
            """.trimIndent(),
            "text/html",
            "UTF-8",
            null
        )

        val btnOpenWebView2: Button = findViewById(R.id.btn_open_webview2)
        btnOpenWebView2.setOnClickListener {
            Log.d(TAG, "Opening WebView2 (LocalWebViewActivity)")
            statusText.text = "WebView2 opened. Wait for PING to start, then press Back."
            startActivity(LocalWebViewActivity.newIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume")
        statusText.text = "Tap the area above to verify touch, then open WebView2."
    }
}
