package com.example.quizotime_admin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizotime_admin.R
import com.example.quizotime_admin.databinding.ActivityMainBinding
import com.example.quizotime_admin.models.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private var questionNumber: Int = 1
    lateinit var db: DocumentReference
    var question = HashMap<String, Any>()
    var quiz = HashMap<String, Any>()
    val gson = Gson()
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var binding: ActivityMainBinding


    private lateinit var description: String
    private lateinit var option1: String
    private lateinit var option2: String
    private lateinit var option3: String
    private lateinit var option4: String
    private lateinit var answer: String


    private var conditionOneFlag: Boolean = false
    private var conditionTwoFlag: Boolean = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance().document("/")

        setTitle()

        binding.btnNext.setOnClickListener {
            val flag = addData()
            if (flag) nextQuestion()
        }
        binding.btnSubmit.setOnClickListener {
            val flag = addData()
            if (flag) submitQuiz()
        }
    }

    private fun addData(): Boolean {

        conditionsCheck()

        if (conditionOneFlag && conditionTwoFlag) {
            val quizQuestion = Question(description, option1, option2, option3, option4, answer)
            val questionMap = quizQuestion.serializeToMap()
            question["question$questionNumber"] = questionMap
            return true
        }

        if (!conditionOneFlag) {
            Toast.makeText(this, "Any field cannot be empty", Toast.LENGTH_SHORT).show()
        }

        if (!conditionTwoFlag) {
            binding.answer.text.clear()
            Toast.makeText(this, "Answer must be same as one of the options", Toast.LENGTH_SHORT)
                .show()
        }
        return false
    }


    private fun nextQuestion() {
        binding.description.text.clear()
        binding.option1.text.clear()
        binding.option2.text.clear()
        binding.option3.text.clear()
        binding.option4.text.clear()
        binding.answer.text.clear()
        questionNumber++
        setTitle()
    }


    private fun getInfo() {
        description = binding.description.text.toString().trim()
        option1 = binding.option1.text.toString().trim()
        option2 = binding.option2.text.toString().trim()
        option3 = binding.option3.text.toString().trim()
        option4 = binding.option4.text.toString().trim()
        answer = binding.answer.text.toString().trim()
    }

    private fun conditionsCheck() {
        conditionTwoFlag = false
        conditionOneFlag = false
        getInfo()

        if (description.isNotEmpty() && option1.isNotEmpty() && option2.isNotEmpty() && option3.isNotEmpty() && option4.isNotEmpty() && description.isNotEmpty() && answer.isNotEmpty()) {
            conditionOneFlag = true
        }
        if (answer == option1 || answer == option2 || answer == option3 || answer == option4) {
            conditionTwoFlag = true
        }
    }

    private fun setTitle() {
        binding.questionTitle.text = "Enter Question $questionNumber"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun submitQuiz() {

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirm Submit")
        alertDialogBuilder.setMessage("Do you want to Submit?")

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            val currentDate = setQuizDate()
            quiz["title"] = currentDate
            quiz["questions"] = question
            val documentString = "quiz${quiz.getValue("title")}"
            db.collection("quizzes").document(documentString).set(quiz).addOnSuccessListener {
                Toast.makeText(this, "Successfully uploaded to the database :)", Toast.LENGTH_LONG)
                    .show()
            }.addOnFailureListener { exception: java.lang.Exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
            }

            val intent = Intent(this, SubmitActivity::class.java)
            intent.putExtra("QuesNum", questionNumber.toString())
            intent.putExtra("Date", quiz.getValue("title").toString())
            startActivity(intent)
            finish()
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            Toast.makeText(applicationContext, "Clicked No", Toast.LENGTH_LONG).show()
        }
        alertDialogBuilder.setNeutralButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Clicked cancel", Toast.LENGTH_LONG).show()
        }

        alertDialogBuilder.create().setCancelable(false)
        alertDialogBuilder.create().show()


    }


    private fun userLogoutDialogAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirm Logout")
        alertDialogBuilder.setMessage("Do you want to logout?")

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            userLogout()
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
            Toast.makeText(applicationContext, "Clicked No", Toast.LENGTH_LONG).show()
        }
        alertDialogBuilder.setNeutralButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Clicked cancel", Toast.LENGTH_LONG).show()
        }

        alertDialogBuilder.create().setCancelable(false)
        alertDialogBuilder.create().show()

    }


    private fun userLogout() {
        firebaseAuth.signOut()
        Toast.makeText(this, "User Logged Out :)", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                userLogoutDialogAlert()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setQuizDate(): String {
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
        return dateFormatter.format(Date())
    }

    private fun <T> T.serializeToMap(): Map<String, Any> {
        return convert()
    }

    //convert an object of type I to type O

    private inline fun <I, reified O> I.convert(): O {
        val json = gson.toJson(this)
        return gson.fromJson(json, object : TypeToken<O>() {}.type)
    }

}