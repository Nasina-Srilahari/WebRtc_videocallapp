package com.example.videocallapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallActivity : AppCompatActivity() {

    private lateinit var callBtn: Button
    private lateinit var toggleVideoBtn: ImageView
    private lateinit var toggleAudioBtn: ImageView
    private lateinit var  webview: WebView
    private lateinit var acceptBtn: ImageView
    private lateinit var rejectBtn: ImageView
    private lateinit var incomingCallTxt: TextView
    private lateinit var callLayout: RelativeLayout
    private lateinit var callControlLayout:LinearLayout
    private lateinit var inputLayout: RelativeLayout
    private lateinit var friendNameEdit : EditText

    var username=""
    var friendsUsername=""

    var isPeerConnected = false

    var firebaseRef = Firebase.database.getReference("users")

    var isAudio = true
    var isVideo = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        callBtn = findViewById(R.id.callBtn)
        toggleAudioBtn=findViewById(R.id.toggleAudioBtn)
        toggleVideoBtn=findViewById(R.id.toggleVideoBtn)
        webview=findViewById(R.id.webview)
        acceptBtn=findViewById(R.id.acceptBtn)
        rejectBtn=findViewById(R.id.rejectBtn)
        incomingCallTxt=findViewById(R.id.incomingCallTxt)
        callLayout = findViewById(R.id.callLayout)
        callControlLayout = findViewById(R.id.callControlLayout)
        inputLayout = findViewById(R.id.inputLayout)
        friendNameEdit = findViewById(R.id.friendNameEdit)


        username = intent.getStringExtra("username").toString()
        Log.d("username in main", "Username1: $username")

        callBtn.setOnClickListener{
            friendsUsername = friendNameEdit.text.toString()
            Log.d("CallActivity", "Username2: $friendNameEdit")
            sendCallRequest()
        }
        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavaScriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24 )
        }
        toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavaScriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24 )
        }

        setupWebView()
    }


    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You're not connected. Check your internet", Toast.LENGTH_LONG).show()
            return
        }

        firebaseRef.child(friendsUsername).child("incoming").setValue(username)
        firebaseRef.child(friendsUsername).child("isAvailable").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }

            }

        })

    }

    private fun listenForConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                switchToControls()
                callJavaScriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

        })
    }


    private fun setupWebView(){
        webview.webChromeClient = object:WebChromeClient(){
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webview.settings.javaScriptEnabled=true
        webview.settings.mediaPlaybackRequiresUserGesture=false
        webview.addJavascriptInterface(JavaScriptInterface(this), "Android")
        loadVideoCall()
    }

    private fun loadVideoCall(){
        val filePath = "file:///android_asset/call.html"
        webview.loadUrl(filePath)

        webview.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()


            }
        }
    }
    var uniqueID = ""

    private fun initializePeer(){

        uniqueID= getUniqueID1()

        callJavaScriptFunction("javascript:init(\"${uniqueID}\")")
        firebaseRef.child(username).child("incoming").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }


        })


    }
    private fun onCallRequest(caller: String?) {
        if(caller == null) return

        callLayout.visibility = View.VISIBLE
        incomingCallTxt.text = "$caller is calling..."

        acceptBtn.setOnClickListener{
            firebaseRef.child(username).child("connId").setValue(uniqueID)
            firebaseRef.child(username).child("isAvailable").setValue(true)

            callLayout.visibility=View.GONE
            switchToControls()


        }
        rejectBtn.setOnClickListener{
            firebaseRef.child(username).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
        }
    }

    private fun switchToControls() {
        inputLayout.visibility=View.GONE
        callControlLayout.visibility=View.VISIBLE
    }


    private fun getUniqueID1(): String{
        return UUID.randomUUID().toString()
    }

    private  fun callJavaScriptFunction(functionString: String){
        webview.post{webview.evaluateJavascript(functionString,null)}
    }


    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        firebaseRef.child(username).setValue(null)
        webview.loadUrl("about:blank")
        super.onDestroy()
    }



}