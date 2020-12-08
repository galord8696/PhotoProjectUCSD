package com.example.photoproject
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zomato.photofilters.SampleFilters
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter
import kotlinx.android.synthetic.main.activity_train.*
import kotlin.concurrent.thread
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log.d
import kotlinx.android.synthetic.main.activity_select.*
import android.os.SystemClock.sleep
import android.util.Log
import androidx.core.os.HandlerCompat.postDelayed
import kotlin.concurrent.thread
import android.app.Activity
import android.app.usage.UsageEvents
import android.content.Context
import android.device.eeg.CognionicsQ30
import android.net.Uri
import android.numbersuggestionbci_opengl30.MyCognionicsUSBDongleThread
import android.os.Looper
import kotlinx.android.synthetic.main.activity_select.*
import android.util.EventLog
import android.util.Log.d
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.photoproject.R.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import com.madapps.prefrences.EasyPrefrences
//import android.numbersuggestionbci_opengl30.TrainingDataUploadManager
import android.os.AsyncTask
import android.provider.ContactsContract
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
import com.example.photoproject.FFT.*
import com.myAlgorithm.FastFourierTransform
import smile.classification.LDA


//2 second black screen between cycles (bout)
//wait for reject/confirm for experiment

//, RatingBar.OnRatingBarChangeListener

//Directions on 9/15
//  Power spectrum amplitude Fast Fourier Transform convert to amplitude. during black screen measure baseline spectrum. LDA algorithm classifier

class Train : AppCompatActivity(), RatingBar.OnRatingBarChangeListener{

    private var cogThread: MyCognionicsUSBDongleThread? = null
    private var mActivity: Activity? = null
    private var rateChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_train)

        trainView.visibility = View.INVISIBLE
        rateBut.visibility = View.INVISIBLE

        rateBut.onRatingBarChangeListener

//        mActivity = this

        var ratings = arrayListOf<Int>()

        startBut.setOnClickListener {
            backBut.visibility = View.INVISIBLE
            startBut.visibility = View.INVISIBLE

            timerFunction(1, ratings)

            d("stars ratings", ratings.toString())
        }

        rateBut.onRatingBarChangeListener = this

//        rateBut.setOnClickListener {
//            rateChange = true
//            d("TRACKING1", "change")
//        }



        backBut.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        mActivity = this

//        checkCogThread()

//        if (! Python.isStarted()) {
//            Python.start(AndroidPlatform(applicationContext))
//        }
    }

    override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean): Unit {
        if (p2 && p1>0.0f){
            rateChange = true
            d("TRACKING1", "changeOccured")
        }
    }


    private fun afterRateChange(){
        d("TRACKING1", rateChange.toString())
        while (!rateChange){
            sleep(20)
        }
        d("TRACKING1", "changeContinued")
        rateChange = false
    }

    companion object {
        private var playQueue: Queue<DoubleArray> = LinkedList()
        var data = arrayListOf<Double>()
//        var storage = arrayListOf<ArrayList<Double>>()
        var storageMap = mutableMapOf<Int, ArrayList<Double>>()
        var needToAnalyze = arrayListOf<Int>()

        fun addToQueue(fa: DoubleArray) {
//        Toast.
            d("DataReceived", fa[0].toString())
            with(playQueue) {
                add(fa)
            }
            d("CurrentQueue", playQueue.toString())
        }

        fun beginQueue(){
            //make sure queue is empty
            emptyQueue()
            //start recording and add to queue
            CognionicsQ30.isStartSampling = true
        }

        fun endQueue(){
            //stop recording
            CognionicsQ30.isStartSampling = false
            //empty queue to storage
            emptyQueue()

        }

        fun saveAndEndQueue(value: Int){
            var temp = arrayListOf<Double>()
            for (array in playQueue)
                for (double in array)
                    temp.add(double)
            storageMap[value] = temp
            temp.clear()
            playQueue.clear()
            needToAnalyze.add(value)

            thread {
                val temp = needToAnalyze.toMutableList()
                needToAnalyze.clear()

                if (storageMap.size - 1 % 10 == 0) {
                    for (x in temp)
                        analyzeData(x)
                }
            }
        }

        fun emptyQueue(){
//            for (array in playQueue)
//                for (double in array)
//                    storage.add(double)
            playQueue.clear()
        }

        fun analyzeData(value: Int): DoubleArray{
            var tempList = storageMap[value]!!.toMutableList()
            var tempX = tempList.toDoubleArray()
            try {
                d("LIST SIZE", tempList.size.toString())
                var tempClass = FFT(tempList.size)
                var tempY = doubleArrayOf()
                Arrays.fill(tempY, 0.0)

                tempClass.fft(tempX, tempY) // tempY is output

                return (tempY)

            } catch(e : java.lang.RuntimeException) {
                d("LIST SIZE ERROR - Size ", tempList.size.toString())
                return(tempX)
            }

        }

    }

    private fun checkCogThread() {
        thread {
            if (cogThread == null) {
                cogThread = MyCognionicsUSBDongleThread(null, mActivity)
            }
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
            d("COGTHREAD", cogThread!!.toString())

            cogThread!!.start()
//            beginQueue()
        }
    }

    override fun onDestroy() {
        endQueue()
        super.onDestroy()
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
        System.loadLibrary("NativeImageProcessor");

        val fil = Filter()
        fil.addSubFilter(ContrastSubfilter(1.9f))
        val fil1 = SampleFilters.getAweStruckVibeFilter() //works
        val fil2 = SampleFilters.getBlueMessFilter()
        val fil3 = SampleFilters.getLimeStutterFilter()
        val fil4 = SampleFilters.getNightWhisperFilter()
        val fil5 = SampleFilters.getStarLitFilter()

        val list = listOf(fil, fil1, fil2, fil3, fil4, fil5)
        var listBit = arrayListOf<Bitmap>()

//        var srcBit = src.copy(Bitmap.Config.ARGB_8888, true)

        for (item in list) {
            listBit.add(item.processFilter(src.copy(Bitmap.Config.ARGB_8888, true)))
//            d("item is ", item.toString())
//            d("filter is ", item.processFilter(src.copy(Bitmap.Config.ARGB_8888 ,true)).toString())
        }

        return listBit
    }



//    private fun timerFunction(nameCount: Int, ratings: ArrayList<Int>) {
//        val firstStage: Long = 1300
//        val secondStage: Long = 1000
//        val interval: Long = firstStage + secondStage //700 is time between photos
//        val totalTime = interval * 5 //4 is the number of filters - 1
//        val easyPrefrences = EasyPrefrences(this)
//
//        var count = 1
//        var bmpList = ArrayList<Bitmap>()
//
//        rateBut.visibility = View.INVISIBLE
//        rateBut.stepSize = 1.0f
//        rateBut.rating = 0.0f
//
//        var tempBMP = ResourcesCompat.getDrawable(
//            resources,
//            resources.getIdentifier(getName(nameCount), "drawable", packageName),
//            null
//        )
//
//        var timer = object : CountDownTimer(totalTime, interval) {
//            override fun onFinish() {
//                val tem = rateBut.numStars.toInt()
//                if (tem > 0)
//                    ratings.add(tem)
//
//                trainView.visibility = View.INVISIBLE
//                rateBut.visibility = View.INVISIBLE
//
//                thread {
//                    easyPrefrences.putListInt("trainRating", ratings)
//                }
//
//
//
//                if (nameCount < 30)
//                    return (timerFunction(nameCount + 1, ratings))
//            }
//
//            override fun onTick(p0: Long) {
//                //                    val myImage: Drawable? = ResourcesCompat.getDrawable(resources, resources.getIdentifier(getName(count) , "drawable", packageName) , null)
//                //                    trainView.setImageDrawable(myImage)
//                rateBut.visibility = View.INVISIBLE
//                rateBut.rating = 0.0f
//
//                thread {
//                    val tem = rateBut.numStars.toInt()
//                    if (tem > 0)
//                        ratings.add(tem)
//                    runOnUiThread {
//                        trainView.setImageBitmap(bmpList[count])
//
//                        beginQueue()
//
//                        trainView.visibility = View.VISIBLE
//                    }
//                    sleep(firstStage)
//                    runOnUiThread {
//                        //                        trainView.visibility=View.INVISIBLE
//
//                        endQueue()
//
//                        rateBut.visibility = View.VISIBLE
//                        count++
//                    }
////                    afterRateChange()
//                    sleep(200)
//                }
//                //                    trainView.setBackgroundResource(R.drawable.s08)
//
//            }
//        }
//
//        thread {
//            val temp = applyFilters(tempBMP!!.toBitmap())
//
//            runOnUiThread {
//
//                trainView.setImageResource(drawable.black)
//                startBut.visibility = View.INVISIBLE
//                trainView.visibility = View.VISIBLE
//            }
//
//            sleep(2000)
//
//            runOnUiThread {
//                bmpList = temp
//
//                beginQueue()
//
//                trainView.setImageBitmap(bmpList[0])
//            }
//
//            sleep(firstStage)
//
//            runOnUiThread {
//
//                endQueue()
//
//                rateBut.visibility = View.VISIBLE
//
//                d("TRACKING1", "1")
//            }
//
////            sleep(secondStage)
//            afterRateChange()
//
//            runOnUiThread {
//                d("TRACKING1", "2")
//                timer.start()
//            }
//        }
//    }

    private fun blackScreen(){
        runOnUiThread {
            beginQueue()
            trainView.setImageResource(drawable.black)
            startBut.visibility = View.INVISIBLE
            trainView.visibility = View.VISIBLE
        }

        sleep(2000)
        saveAndEndQueue(0)
    }

    private fun timerFunction(nameCount: Int, ratings: ArrayList<Int>) {
        val firstStage: Long = 1300
        val easyPrefrences = EasyPrefrences(this)

        rateBut.visibility = View.INVISIBLE
        rateBut.stepSize = 1.0f
        rateBut.rating = 0.0f


        var tempBMP = ResourcesCompat.getDrawable(
            resources,
            resources.getIdentifier(getName(nameCount), "drawable", packageName),
            null
        )

        thread {
            val temp = applyFilters(tempBMP!!.toBitmap())

            blackScreen()
            var currentInt = 0
            for (x in temp) {
                runOnUiThread {
                    beginQueue()
                    trainView.setImageBitmap(x)
                }

                sleep(firstStage)

                runOnUiThread {
                    saveAndEndQueue(currentInt)
                    currentInt++

                    rateBut.visibility = View.VISIBLE
                    d("TRACKING1", "1")
                }

                afterRateChange()

                runOnUiThread {
                    d("TRACKING1", "2")
                }

                var tem = rateBut.rating.toInt()
                if (tem > 0)
                    ratings.add(tem)

                runOnUiThread{
                    rateBut.visibility = View.INVISIBLE
                    rateBut.rating = 0.0f
                }

                d("TRACKING1", "3")
            }

            trainView.visibility = View.INVISIBLE
            rateBut.visibility = View.INVISIBLE

            thread {
                easyPrefrences.putListInt("trainRating", ratings)
                d("RATINGSLIST", Arrays.toString(ratings.toArray()))
            }

            runOnUiThread {
                if (nameCount < 30) {
                    return@runOnUiThread (timerFunction(nameCount + 1, ratings))
                }
            }
        }
    }

}