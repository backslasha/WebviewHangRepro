package com.yhb.webviewhangrepro.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.yhb.webviewhangrepro.R;

/**
 * WebView2: loads a page that fires window.prompt() every 50ms.
 *
 * Press back while PING is running — the destroy/PING IPC race causes
 * SetBlocked(true) without a matching SetBlocked(false), freezing all
 * in-process WebView touch input.
 */
public class LocalWebViewActivity extends AppCompatActivity {

    private static final String TAG = "WVHangRepro";

    private WebView mWebView;
    private FrameLayout mFrameLayout;
    private int promptCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mFrameLayout = findViewById(R.id.frame_layout_web_view);

        mWebView = new WebView(this);
        mFrameLayout.addView(mWebView, 0);

        setupWebView(mWebView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy — promptCount=" + promptCount + ", about to destroy WebView");
        destroyWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                      String defaultValue, JsPromptResult result) {
                promptCount++;
                result.confirm("pong_" + promptCount);
                Log.d(TAG, "[WV2] onJsPrompt #" + promptCount + " confirm, msg=" + message);
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/dialogLeakDemo.html");
    }

    private void destroyWebView() {
        if (mWebView == null) return;
        Log.w(TAG, "=== destroyWebView START, promptCount=" + promptCount + " ===");
        if (mFrameLayout != null) mFrameLayout.removeAllViews();
        mWebView.destroy();
        mWebView = null;
        Log.w(TAG, "=== destroyWebView END ===");
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, LocalWebViewActivity.class);
    }
}
