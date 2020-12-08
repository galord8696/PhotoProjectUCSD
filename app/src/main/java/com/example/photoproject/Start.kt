package com.example.photoproject

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock.sleep
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.photoproject.R.drawable
import com.example.photoproject.R.layout
import com.madapps.prefrences.EasyPrefrences
import com.zomato.photofilters.SampleFilters
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter
import kotlinx.android.synthetic.main.activity_train.*
import kotlin.concurrent.thread


class Start : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_start)

        trainView.visibility = View.INVISIBLE

        startBut.setOnClickListener {
            backBut.visibility = View.INVISIBLE
            startBut.visibility = View.INVISIBLE

            timerFunction(31)
        }

        backBut.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun getName(num: Int): String {
        val mod: String = if (num >= 10) {
            num.toString()
        } else {
            "0$num"
        }
        return "s$mod"
    }

    private fun applyFilters(src: Bitmap): ArrayList<Bitmap> {
        System.loadLibrary("NativeImageProcessor")

        val fil = Filter()
        fil.addSubFilter(ContrastSubfilter(1.9f))
        val fil1 = SampleFilters.getAweStruckVibeFilter() //works
        val fil2 = SampleFilters.getBlueMessFilter()
        val fil3 = SampleFilters.getLimeStutterFilter()
        val fil4 = SampleFilters.getNightWhisperFilter()
        val fil5 = SampleFilters.getStarLitFilter()

        val list = listOf(fil, fil1, fil2, fil3, fil4, fil5)
        var listBit = arrayListOf<Bitmap>()

        var srcBit = src.copy(Bitmap.Config.ARGB_8888, true)

        for (item in list) {
            listBit.add(item.processFilter(src.copy(Bitmap.Config.ARGB_8888, true)))
//            d("item is ", item.toString())
//            d("filter is ", item.processFilter(src.copy(Bitmap.Config.ARGB_8888 ,true)).toString())
        }

        return listBit
    }

    private fun timerFunction(nameCount: Int) {
        val interval: Long = 700 //700 is time between photos
        val totalTime = interval * 5 //4 is the number of filters - 1
        val easyPrefrences = EasyPrefrences(this)

        var count = 1
        var bmpList = ArrayList<Bitmap>()

        var tempBMP = ResourcesCompat.getDrawable(
            resources,
            resources.getIdentifier(getName(nameCount), "drawable", packageName),
            null
        )


        var timer = object : CountDownTimer(totalTime, interval) {
            override fun onFinish() {
                thread{
                    sleep(2000)
                    runOnUiThread {
                        if (nameCount < 90)
                            return@runOnUiThread (timerFunction(nameCount + 1))
                    }
                }
            }

            override fun onTick(p0: Long) {
                //                    val myImage: Drawable? = ResourcesCompat.getDrawable(resources, resources.getIdentifier(getName(count) , "drawable", packageName) , null)
                //                    trainView.setImageDrawable(myImage)
                trainView.setImageBitmap(bmpList[count])
                trainView.visibility=View.VISIBLE
                count++
                //                    trainView.setBackgroundResource(R.drawable.s08)

            }
        }

        thread {
            val temp = applyFilters(tempBMP!!.toBitmap())

            runOnUiThread {
                trainView.setImageResource(drawable.black)
                startBut.visibility = View.INVISIBLE
                trainView.visibility = View.VISIBLE
            }

            sleep(2000)

            runOnUiThread {
                bmpList = temp
                trainView.setImageBitmap(bmpList[0])
            }

            sleep(interval)

            runOnUiThread {
                timer.start()
            }
        }

        
    }
}