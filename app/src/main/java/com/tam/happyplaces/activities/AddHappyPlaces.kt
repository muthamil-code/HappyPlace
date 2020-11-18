package com.tam.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.config.GservicesValue.isInitialized
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.Places.isInitialized
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener.Builder.withContext
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tam.happyplaces.R
import com.tam.happyplaces.database.DataBaseHelper
import com.tam.happyplaces.models.HappyPlaceModel
import com.tam.happyplaces.utils.GetAddressFromLatLog
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast.makeText as makeText1


class AddHappyPlaces : AppCompatActivity(),View.OnClickListener {
    private val cal = Calendar.getInstance()
    private lateinit var datePickerDialog: DatePickerDialog.OnDateSetListener
    private var saveSelectedImage : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mHappyPlaceDetails : HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)
        setSupportActionBar(tool_bar_for_happy_places)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tool_bar_for_happy_places.setNavigationOnClickListener {
            onBackPressed()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
if(!Places.isInitialized()){
    Places.initialize(this,resources.getString(R.string.google_maps))

}



        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
               mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

      datePickerDialog = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

          cal.set(Calendar.YEAR,year)
          cal.set(Calendar.MONTH,month)
          cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
          val myFormat = "dd.MM.yyyy"
          val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
          et_date.setText(sdf.format(cal.time).toString())

      }

        if(mHappyPlaceDetails != null){

            supportActionBar?.title = "Edit Place Details"

            et_title.setText(mHappyPlaceDetails!!.title)
            et_description.setText(mHappyPlaceDetails!!.description)
            et_date.setText(mHappyPlaceDetails!!.date)
            et_location.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            saveSelectedImage = Uri.parse(mHappyPlaceDetails!!.image)
            iv_place_image.setImageURI(saveSelectedImage)
            btn_save.text = "UPDATE"



        }

 et_date.setOnClickListener(this)
  tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)


        //et_location.setOnClickListener(this)
    }

    @Suppress("MissingPermission")

    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()

        )

    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            Toast.makeText(this@AddHappyPlaces,"kk",Toast.LENGTH_SHORT).show()
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            val addressTask = GetAddressFromLatLog(this@AddHappyPlaces,mLatitude,mLongitude)

            addressTask.settAddressListner(object : GetAddressFromLatLog.AddressListner{
                override fun onAddressFound(address: String?) {
                   et_location.setText(address)
                }

                override fun onError() {
                   Log.e("Get Address","something wrong")
                }
            })
            addressTask.getAddresss()
        }

    }




    private fun isLocationEnabled():Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {

            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaces,
                    datePickerDialog,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureSelectItem =
                    arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureSelectItem) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhoto()

                    }
                }
                pictureDialog.show()

            }
            R.id.btn_save -> {

                when {
                    et_title.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(this, "please enter the title", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(this, "please enter the description", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_location.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(this, "please enter the location", Toast.LENGTH_SHORT).show()
                    }
                    saveSelectedImage == null -> {
                        Toast.makeText(this, "select the image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else {
                                mHappyPlaceDetails!!.id
                            },

                            et_title.text.toString(),
                            saveSelectedImage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dataBaseHandler = DataBaseHelper(this)

                        if (mHappyPlaceDetails == null) {
                            val status = dataBaseHandler.addPlaces(happyPlaceModel)
                            if (status > -1) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            Log.i("check","hii.j")
                            try {
                                val updatePlace = dataBaseHandler.updatePlace(happyPlaceModel)
                                if (updatePlace > -1) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }catch (e:IOException){
                                e.printStackTrace()
                            }
                        }

                    }


                }
            }
            R.id.tv_select_current_location ->{


                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // This will redirect you to settings from where you need to turn on the location provider.
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    // For Getting current location of user please have a look at below link for better understanding
                    // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                    Dexter.withActivity(this)
                        .withPermissions(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {

                                    // TODO (Step 6: Remove the toast message and Call the new request location function to get the latest location.)
                                    // START
                                    requestNewLocationData()
                                    // END
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogPermission()
                            }
                        }).onSameThread()
                        .check()
                }
            }

//            R.id.et_location ->{
//
//                try{
//                    val fields = listOf( Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
//                    val intent =
//                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
//                            .build(this@AddHappyPlaces)
//                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
//                }catch(e:Exception){
//
//                }
//            }

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
           }//else if(requestCode == AUTOCOMPLETE_REQUEST_CODE){
//                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
//                et_location.setText(place.address)
//                mLatitude = place.latLng!!.latitude
//                mLongitude = place.latLng!!.longitude
//
//            }
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
        private const val AUTOCOMPLETE_REQUEST_CODE = 3
    }
}


