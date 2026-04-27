package com.unibo.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unibo.android.ui.databinding.ActivityMainBinding
import com.unibo.android.ui.libretto.LibrettoFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, LibrettoFragment())
                .commit()
        }
    }
}
