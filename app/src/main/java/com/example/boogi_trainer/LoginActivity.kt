package com.example.boogi_trainer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val button=findViewById<Button>(R.id.btn_login)
        button.setOnClickListener {
            val intent = Intent(this@LoginActivity, MainActivity::class.java);
            startActivity(intent)
        }
    }
}
