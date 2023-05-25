package com.example.videocallapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.example.videocallapp.CallActivity



//val myClass: Class<CallActivity> = CallActivity::class.java

class MainActivity : AppCompatActivity() {

    private lateinit var loginBtn: Button
    private lateinit var usernameEdit: EditText

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val requestcode = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginBtn=findViewById(R.id.loginBtn)
        usernameEdit=findViewById(R.id.usernameEdit)


        if (!isPermissionGranted()) {
            askPermission()
        }

        Firebase.initialize(this)

        loginBtn.setOnClickListener{
            val username = usernameEdit.text.toString()

            Log.d("LoginActivity", "Username: $username")
            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestcode)
    }

    private fun isPermissionGranted(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}
