package com.example.quizotime_admin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.quizotime_admin.databinding.ActivitySubmitBinding

class SubmitActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubmitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val date = intent.getStringExtra("Date")
        val quesNum = intent.getStringExtra("QuesNum")

        Log.e("Submit Activity","$quesNum")
        binding.numQuesText.text = "Questions Submitted:- $quesNum"
        binding.quizDateText.text = "Date of Quiz:- $date"

        binding.button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}