package com.example.blinkit.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.blinkit.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    val binding: ActivityAuthBinding by lazy {
        ActivityAuthBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)




    }
}