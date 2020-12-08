package com.example.photoproject

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log.d
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zomato.photofilters.SampleFilters
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter
import kotlinx.android.synthetic.main.activity_select.*
import java.io.File
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class Select : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        System.loadLibrary("NativeImageProcessor")

        backA.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        imageSelect.setOnClickListener {
            pickImageFromGallery()
        }
        capImageBut.setOnClickListener {
            captureImage()
        }
        nextBut.setOnClickListener {
            startActivity(Intent(this, Experiment::class.java))
        }

        nextBut.visibility= View.INVISIBLE
    }

    var imageUriCapture: Uri? = null

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intentPick = Intent(Intent.ACTION_PICK)
        intentPick.type = "image/*"
        startActivityForResult(intentPick, IMAGE_PICK_CODE)
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        private const val IMAGE_PICK_CODE = 1000
        private const val CAMERA_REQUEST_CODE = 1888
    }

    private fun captureImage() {
        //Intent to pick image
//        val intentCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        intentCapture.putExtra(MediaStore.EXTRA_OUTPUT, imageUriCapture)
//        intentCapture.type = "image/*"
//        startActivityForResult(intentCapture, IMAGE_PICK_CODE)
//        imageView.setImageURI(imageUriCapture)
//        startActivity(intentCapture)

//        val fileUri = CameraUtils.getOutputMediaFileUri(this);//get fileUri from CameraUtils
//        intentCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
////        intentCapture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);//Send fileUri with intent
//        startActivityForResult(intentCapture, CAMERA_REQUEST_CODE);//start activity for result with CAMERA_REQUEST_CODE

//        var intentCapture = Intent(Intent.ACTION_GET_CONTENT)
//        var intentCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        intentCapture.type = "image/*"
//        intentCapture.addCategory(Intent.CATEGORY_OPENABLE)
//        startActivityForResult(intentCapture, CAMERA_REQUEST_CODE)

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBit = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBit)
            selectProcess()
//            d("DATAFORIMAGE", data.toString())
////            d("DATAFORIMAGE.data", data.data.toString())
//            d("DATAFORIMAGE.extras", data?.extras.toString())
//            d("DATAFORIMAGE.extras.get", data?.extras?.get("data").toString())
//            val imageUri: Uri? = data.data
//            imageView.setImageURI(imageUri)
        }

//        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE && data?.data != null){
//            val imageUri: Uri? = data.data
//            imageView.setImageURI(imageUri)
//            selectProcess()
//        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data?.data != null){
            val imageUri: Uri? = data.data
            imageView.setImageURI(imageUri)
            selectProcess()
        }
    }

    private fun selectProcess(){
//        System.loadLibrary("NativeImageProcessor")
        var imageBitmap = (imageView.drawable as BitmapDrawable).bitmap
        imageBitmap = imageBitmap.copy( Bitmap.Config.ARGB_8888 , true)

        val fil = Filter()
        fil.addSubFilter(ContrastSubfilter(1.9f))
        val fil1 = SampleFilters.getAweStruckVibeFilter() //works
        val fil2 = SampleFilters.getBlueMessFilter()
        val fil3 = SampleFilters.getLimeStutterFilter()
        val fil4 = SampleFilters.getNightWhisperFilter()
        val fil5 = SampleFilters.getStarLitFilter()

        val list = listOf(fil, fil1, fil2, fil3, fil4, fil5)
        val listName = listOf("cont", "fil1", "fil2", "fil3", "fil4", "fil5")
        var count = 0
        var threadCount = 0
        d("donebruh", "START")
        thread{
            threadCount++

            for (item in list) {

                thread {
                    threadCount++
                    val name = listName[count]

                    saveBitmapAsImage(item.processFilter(imageBitmap), "$name.png")


                    runOnUiThread {
                        d("donebruh", "$count")
                    }

                    count++
                }
                sleep(200)
//                if (count == list.size-1){

//                    runOnUiThread{
//                        nextBut.visibility= View.VISIBLE
//                    }
//                }
            }

            while(count< list.size){
                sleep(50)
            }

            runOnUiThread{
                nextBut.visibility= View.VISIBLE
                d("donebruh", "end")

            }

        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun saveBitmapAsImage(bit: Bitmap, nam: String){
        val dir = getDir("imagesFold", 0)
        val filepath = dir.path + "/" + nam
        val fileNew = File(filepath)

        fileNew.writeBitmap(bit, Bitmap.CompressFormat.PNG, 90)
    }

}