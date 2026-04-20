package com.unibo.android.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val logTag = "MainActivityTest"

    private fun myLog(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myLog(tag = logTag, msg = "SplashActivity onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            showHomePage()
        }, 3000)
    }

    override fun onStart() {
        super.onStart()
        myLog(tag = logTag, msg = "SplashActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        myLog(tag = logTag, msg = "SplashActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        myLog(tag = logTag, msg = "SplashActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        myLog(tag = logTag, msg = "SplashActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        myLog(tag = logTag, msg = "SplashActivity onDestroy")
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == homeActivityRequestCode) {
                val resultMessage = data?.getStringExtra(homeActivityResultMessageKey)
                myLog(tag = logTag, msg = "SplashActivity onActivityResult, requestCode=$requestCode, resultCode=$resultCode, resultMessage=$resultMessage")
            }
        }
    }

    private fun showHomePage() {
        myLog(tag = logTag, msg = "showHomePage")
        val intent = Intent(
            this,
            //TestActivity2::class.java --> SE vogliamo vedere la prova di utilizzo permessi utilizzare TestActivity2 al posto di HOME
            HomeActivity::class.java
        )
        intent.putExtra(
            splashActivityFakeDataKey,
            splashActivityFakeDataValue
        )
        startActivity(intent)
        finish()
    }

    private fun showHomePageForResults() {
        myLog(tag = logTag, msg = "showHomePageForResults")
        val intent = Intent(
            this,
            HomeActivity::class.java
        )
        intent.putExtra(
            splashActivityFakeDataKey,
            splashActivityFakeDataValue
        )
        startActivityForResult(intent, homeActivityRequestCode)
    }
}