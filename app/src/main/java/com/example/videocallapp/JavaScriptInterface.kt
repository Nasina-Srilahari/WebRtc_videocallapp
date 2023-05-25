package com.example.videocallapp



import android.webkit.JavascriptInterface


class JavaScriptInterface(val callActivity: CallActivity) {
    @JavascriptInterface
    public fun onPeerConnected(){
        callActivity.onPeerConnected()
    }
}