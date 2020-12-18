package com.example.myapplication


import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.annotation.TargetApi
import androidx.annotation.RequiresApi
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {

    var uploadMessage: ValueCallback<Array<Uri>>? = null
    var mUploadMessage: ValueCallback<Uri>? = null

    companion object {
        @JvmField val REQUEST_SELECT_FILE: Int = 100
        @JvmField val FILECHOOSER_RESULT_CODE: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val myWebView: WebView = findViewById<View>(R.id.main_webview) as WebView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        myWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                result.cancel()
                return true
            }
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                }

                uploadMessage = filePathCallback

                val contentSelection = Intent(Intent.ACTION_GET_CONTENT)
                contentSelection.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelection.type = "*/*"

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelection)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose your file")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(contentSelection))

                try {
                    startActivityForResult(chooserIntent, REQUEST_SELECT_FILE)
                } catch (e: Exception) {
                    uploadMessage = null
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG)
                        .show()
                    return false

                }

                return true
            }
        })

        var settings = myWebView.settings
        settings.javaScriptEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort=true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        //settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.domStorageEnabled = true
        myWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })

        myWebView.loadUrl("http://10.0.2.2:8000/")


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return
                }
                uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULT_CODE) {
            if (mUploadMessage == null) {
                return
            }

            var result: Uri? = null
            if (intent != null || resultCode == RESULT_OK) {
                result = intent?.data
            }

            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else {
            Toast.makeText(this, "Error occurred while uploading", Toast.LENGTH_LONG)
                .show()
        }
    }



}