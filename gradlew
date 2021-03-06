package com.tam.workoutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_exercise.*
import java.util.*
import kotlin.collections.ArrayList


class ExerciseActivity : AppCompatActivity(),TextToSpeech.OnInitListener {

   private  var restTimer : CountDownTimer? = null
   private var resetProgress = 0

   private  var exerciseTimer : CountDownTimer? = null
   private  var exerciseProgress = 0
   private var exerciseDuration : Long = 30
    private var tts : TextToSpeech? = null

   private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        setSupportActionBar(tool_bar_exercise_id)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tool_bar_exercise_id.setNavigationOnClickListener{
                 onBackPressed()
        }
        setUpRestView()
        exerciseList = Constants.defaultExerciseList()
        tts = TextToSpeech(this,this)

    }

    override fun onDestroy() {
        if(restTimer != null){
            restTimer!!.cancel()
            resetProgress = 0
        }
        super.onDestroy()
    }

    private fun setRestProgress(){
        time_pro.progress = resetProgress
        restTimer = object : CountDownTimer(10000,1000){
            override fun onTick(p0: Long) {
                resetProgress++
                time_pro.progress = 10-resetProgress
                tvTimer.text = (10 - resetProgress).toString()

                upComeId.text = exerciseList!![currentExercisePosition].getName()

            }

            override fun onFinish() {


                setUpExerciseView()


            }
        }.start()

    }
    private fun setExerciseProgress(){
        time_pro_excersis.progress = exerciseProgress
        exerciseTimer = object : CountDownTimer(exerciseDuration * 1000,1000){
            override fun onTick(p0: Long) {
                exerciseProgress++
                time_pro_excersis.progress = exerciseDuration.toInt()-exerciseProgress
                tvTimer_exersise.text = (exerciseDuration.toInt() - exerciseProgress).toString()

            }

            override fun onFinish() {
               if(currentExercisePosition < exerciseList!!.size - 1){
                   setUpRestView()
               }else{
                   Toast.makeText(this@ExerciseActivity,"hi congrates",Toast.LENGTH_SHORT).show()
               }

            }
        }.start()

    }

    private fun setUpExerciseView(){
        llRestView.visibility = View.GONE
        llExerciseView.visibility = View.VISIBLE
        if(exerciseTimer != null){
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }
        setExerciseProgress()
        IvImage.setImageResource(exerciseList!![currentExercisePosition].getImage())
        tvExercise.text = exerciseList!![currentExercisePosition].getName()

    }

    private fun setUpRestView(){
        currentExercisePosition++
        llRestView.visibility = View.VISIBLE
        llExerciseView.visibility = View.GONE
        if(restTimer != null){
            restTimer!!.cancel()
            resetProgress = 0
        }
        setRestProgress()
    }

    override fun onInit(status: Int) {

        if(status == TextToSpeech.SUCCESS){
           var result = tts!!.setLanguage(Locale.ENGLISH)
        if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            Log.e("tts","this language not supported")

        }else{
            Log.e("tts","initiaization is failed")
        }

    }

    private fun speakOut(text : String){
        tts!!.speak()

    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       