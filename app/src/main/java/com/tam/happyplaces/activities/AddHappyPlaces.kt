package com.tam.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tam.happyplaces.R
import com.tam.happyplaces.database.DataBaseHelper
import com.tam.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaces : AppCompatActivity(),View.OnClickListener {
    private val cal = Calendar.getInstance()
    private lateinit var datePickerDialog: DatePickerDialog.OnDateSetListener
    private var saveSelectedImage : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)
        setSupportActionBar(tool_bar_for_happy_places)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tool_bar_for_happy_places.setNavigationOnClickListener {
            onBackPressed()
        }

      datePickerDialog = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

          cal.set(Calendar.YEAR,year)
          cal.set(Calendar.MONTH,month)
          cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
          val myFormat = "dd.MM.yyyy"
          val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
          et_date.setText(sdf.format(cal.time).toString())

      }
 et_date.setOnClickListener(this)
  tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
       when(p0!!.id){

       R.id.et_date ->{
           DatePickerDialog(this@AddHappyPlaces,datePickerDialog,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
       }
           R.id.tv_add_image ->{
               val pictureDialog = AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
               val pictureSelectItem = arrayOf("Select photo from gallery","Capture photo from camera")
               pictureDialog.setItems(pictureSelectItem){
                   _,which ->
                   when(which){
                       0-> choosePhotoFromGallery()
                           1->takePhoto()

                   }
               }
               pictureDialog.show()

           }
           R.id.btn_save -> {

               when{
                   et_title.text.toString().isNullOrEmpty() -> {
                       Toast.makeText(this,"please enter the title",Toast.LENGTH_SHORT).show()
                   }
                   et_description.text.toString().isNullOrEmpty() -> {
                       Toast.makeText(this,"please enter the description",Toast.LENGTH_SHORT).show()
                   }
                   et_location.text.toString().isNullOrEmpty() -> {
                       Toast.makeText(this,"please enter the location",Toast.LENGTH_SHORT).show()
                   }
                   saveSelectedImage == null -> {
                       Toast.makeText(this,"select the image",Toast.LENGTH_SHORT).show()
                   }else -> {
                   val happyPlaceModel = HappyPlaceModel(0,
                       et_title.text.toString(),
                       saveSelectedImage.toString(),
                       et_description.text.toString(),
                       et_date.text.toString(),
                       et_location.text.toString(),
                       mLatitude,
                       mLongitude)
                   val dataBaseHandler = DataBaseHelper(this)
                   val status = dataBaseHandler.addPlaces(happyPlaceModel)
                   if(status > -1){
                     setResult(Activity.RESULT_OK)
                       finish()


                   }else{
                       Toast.makeText(this,"Not added",Toast.LENGTH_SHORT).show()
                   }
               }

               }
           }

       }
    }



    private fun takePhoto(){
        Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report : MultiplePermissionsReport?) {

                    if(report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent,
                            CAMERA
                        )
                    }
                }
                override fun  onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest> , token: PermissionToken ) {

                    showRationalDialogPermission()
                }
            }).onSameThread().check()
    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report : MultiplePermissionsReport?) {

                    if(report!!.areAllPermissionsGranted()){
                       val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent,
                            GALLERY
                        )
                    }
                }
                override fun  onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest> , token: PermissionToken ) {

                    showRationalDialogPermission()
                }
            }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentUri = data.data
                    try{
                        val selectedImageBitMap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                         saveSelectedImage = saveImageToInternalStorage(selectedImageBitMap)
                        Log.i("Saved Image","Path :: $saveSelectedImage")
                        iv_place_image.setImageBitmap(selectedImageBitMap)
                    }catch(e:IOException){
                        Toast.makeText(this,"image load error from gallery",Toast.LENGTH_SHORT).show()
                    }

                }
            }else if(requestCode == CAMERA){
                val thumbNail : Bitmap = data!!.extras!!.get("data") as Bitmap
                 saveSelectedImage = saveImageToInternalStorage(thumbNail)
                Log.i("Saved Image","Path :: $saveSelectedImage")
                iv_place_image.setImageBitmap(thumbNail)
            }
        }

    }


    private fun showRationalDialogPermission(){
        AlertDialog.Builder(this).setTitle("it look like turn off")
            .setPositiveButton("GO TO SETTING")
            {_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch(e:ActivityNotFoundException){
                    e.printStackTrace()
                }

            }
            .setNegativeButton("cancel"){dialog,_ ->

                dialog.dismiss()
            }.show()
    }


    private fun saveImageToInternalStorage(bitMap : Bitmap):Uri{

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try{
            val stream : OutputStream = FileOutputStream(file)
            bitMap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch(e:IOException){
               e.printStackTrace()
        }
      return Uri.parse(file.absolutePath)
    }

    companion object{

        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}


