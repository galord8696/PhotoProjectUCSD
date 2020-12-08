package com.example.photoproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_directions.*

class Directions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directions)
        dirText.text = "1. Wait for confirmation of headset connecrion \n 2. Train algorithm \n 3. Select picture \n 4. Experiment"
        backB.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}