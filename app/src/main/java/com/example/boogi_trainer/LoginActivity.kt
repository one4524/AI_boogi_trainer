package com.example.boogi_trainer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.boogi_trainer.repository.APIManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val button=findViewById<Button>(R.id.btn_login)
        button.setOnClickListener {
//            var inputId = edit_id.text.toString()
//            var inputPw = edit_pw.text.toString()
            var inputId = "test"
            var inputPw = "test"

            runBlocking{
                GlobalScope.launch {
                    """
                    var user = APIManager.getUser(inputId)
                    if(inputPw == user?.password){
                        val intent = Intent(this@LoginActivity, MainActivity::class.java);
                        startActivity(intent)
                    }
"""
                    val intent = Intent(this@LoginActivity, MainActivity::class.java);
                    startActivity(intent)
                }
            }

        }
    }
}
