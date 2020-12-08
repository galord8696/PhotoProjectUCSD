package com.example.photoproject

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.util.Log
import android.util.Log.d
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_main.*
import smile.math.Array2D
import java.io.File
import java.io.IOException
import java.util.*
import java.util.UUID.fromString
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        directionsBut.setOnClickListener {
            startActivity(Intent(this, Directions::class.java))
        }
        selectBut.setOnClickListener {
            startActivity(Intent(this, Select::class.java))
        }
        trainBut.setOnClickListener {
            startActivity(Intent(this, Train::class.java))
        }
        executeBut.setOnClickListener {
            startActivity(Intent(this, Start::class.java))
        }

//        Bluetooth code bellow:
//          Gets bluetooth adapter
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//          If bluetooth is off, asks to turn on.
        if (bluetoothAdapter?.isEnabled == false) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Bluetooth is not enabled.")
            builder.create()
            builder.show()
        }
        d("FailureHere", "proof")

        testAnalyzeData()

//        thread{
////            val drawable: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.s01, null)
//        }


        thread {
            val bUUID = fromString("8c23dd5e-2e58-467e-b3bb-70e78e610ef5")
            val bNAME = "LD_PROJECT"
            val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
                bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(bNAME, bUUID)
            }

            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("BLUETOOTH", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    //                    manageMyConnectedSocket(this)
                    mmServerSocket?.close()
                    shouldLoop = false
                }

                try {
                    mmServerSocket?.close()
                } catch (e: IOException) {
                    Log.e("BLUETOOTH", "Could not close the connect socket", e)
                }
            }
        }
        var arrayHi: Array2D = Array2D(2,2, 2.0)
        d("Didnotfail","hi")
    }

    fun write(): DoubleArray{
        var tempX = DoubleArray(1024)
        for (n in 0..1023){
            tempX[n] = sin(n.toDouble())
        }

//        File("testData.txt").printWriter().use{ out -> tempX.forEach {out.println("$it\n")}}
        return(tempX)
    }

    fun testAnalyzeData(){ //generate sinewave array (size 1000)
        val tempX = write()

        d("FFTFILEINPUTX-1", tempX.copyOfRange(0,190).contentToString())
        d("FFTFILEINPUTX-2", tempX.copyOfRange(190,380).contentToString())
        d("FFTFILEINPUTX-3", tempX.copyOfRange(380,570).contentToString())
        d("FFTFILEINPUTX-4", tempX.copyOfRange(570,760).contentToString())
        d("FFTFILEINPUTX-5", tempX.copyOfRange(760,950).contentToString())
        d("FFTFILEINPUTX-6", tempX.copyOfRange(950,1024).contentToString())

        try {
            var tempClass = FFT(tempX.size)
            var tempY = DoubleArray(1024)
            var out = DoubleArray(1024)
            Arrays.fill(tempY, 0.0)
            Arrays.fill(out, 0.0)

            d("FFTStatus","Started")

            d("FFTFILEINPUTY", tempY.contentToString())

            tempClass.fft(tempX, tempY) // tempY is output

            d("FFTStatus","Ended, started conversion from complex to real")

            for (n in 0..1023){
                out[n] = (tempX[n].pow(2) + tempY[n].pow(2)).pow(0.5)
            }

            d("FFTStatus","Ended Real Output Conversion")

            //return (tempY) //output as a doublearray (save it to a file)
//            for (n in 0..1023){
//                d("FILEOUTPUT", tempY[n].toString())
//            }
//            d("FFTFILEOUTPUT", Arrays.toString(out))
            d("FFTFILEOUTPUT-1", out.copyOfRange(0,190).contentToString())
            d("FFTFILEOUTPUT-2", out.copyOfRange(190,380).contentToString())
            d("FFTFILEOUTPUT-3", out.copyOfRange(380,570).contentToString())
            d("FFTFILEOUTPUT-4", out.copyOfRange(570,760).contentToString())
            d("FFTFILEOUTPUT-5", out.copyOfRange(760,950).contentToString())
            d("FFTFILEOUTPUT-6", out.copyOfRange(950,1024).contentToString())

            d("FFTFILEOUTPUTX", Arrays.toString(tempX))
            d("FFTFILEOUTPUTY", Arrays.toString(tempY))

        } catch(e : java.lang.RuntimeException){
            d("FFTStatus","Error: $e")
        }
    }



}
