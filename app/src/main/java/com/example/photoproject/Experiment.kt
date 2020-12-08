package com.example.photoproject

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock.sleep
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_experiment.*
import kotlin.concurrent.thread


class Experiment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiment)

        val viewList = listOf(imageViewExperiment, imageViewExperiment2, imageViewExperiment3, imageViewExperiment4, imageViewExperiment5)
        val listName = listOf("cont", "fil2", "fil3", "fil4", "fil5")

        startBut.visibility=View.INVISIBLE
        setImageViews()

//        Sets up the reject filter button and accompanied variable
        rejectBut.visibility= View.INVISIBLE
        var rej = false
        rejectBut.setOnClickListener {
            rej = true
        }

        startBut.visibility=View.VISIBLE

        startBut.setOnClickListener {
            startBut.visibility=View.INVISIBLE
            var count = 0

            var item = viewList[count]
            item.bringToFront()
            item.visibility=View.VISIBLE
            Log.d("VISIBLE", count.toString())

            val interval: Long = 700 //700 is time between photos
            val totalTime = interval * 4 //4 is the number of filters - 1

            var timer = object: CountDownTimer(totalTime,interval){
                override fun onFinish() {
                    imageViewExperiment2.bringToFront()
                    imageViewExperiment2.setImageBitmap(openImageFromDir(listName[2] + ".png"))
                    imageViewExperiment2.visibility=View.VISIBLE


                    thread {
                        runOnUiThread{
                            rejectBut.visibility=View.VISIBLE
                        }
                        sleep(2000)
                    }
                }
                override fun onTick(p0: Long) {
                    item.visibility=View.INVISIBLE
                    count++

                    var item = viewList[count]
                    item.bringToFront()
                    item.visibility=View.VISIBLE

                    Log.d("VISIBLE", count.toString())

                }
            }

            timer.start()
        }
    }

    private fun setImageViews(){
        val viewList = listOf(imageViewExperiment, imageViewExperiment2, imageViewExperiment3, imageViewExperiment4, imageViewExperiment5)
        val listName = listOf("cont", "fil2", "fil3", "fil4", "fil5")

        var count = 0
        for (item in viewList){
            item.setImageBitmap(openImageFromDir(listName[count] + ".png"))
            item.visibility=View.INVISIBLE
            count++
        }
    }

    private fun openImageFromDir(nam: String) : Bitmap{
        val dir = getDir("imagesFold", 0)
        val filepath = dir.path + "/" + nam
        return BitmapFactory.decodeFile(filepath)
    }


}