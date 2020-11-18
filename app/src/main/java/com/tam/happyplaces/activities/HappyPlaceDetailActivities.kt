package com.tam.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tam.happyplaces.MapsFragment
import com.tam.happyplaces.R
import com.tam.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_detail_activities.*

class HappyPlaceDetailActivities : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail_activities)
        var happyPlace : HappyPlaceModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlace = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if(happyPlace != null){
            setSupportActionBar(tool_bar_for_happy_places_Details)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = happyPlace.title
            tool_bar_for_happy_places_Details.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(happyPlace.image))
           tv_description.text = happyPlace.description
            tv_location.text = happyPlace.location

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,happyPlace)
                startActivity(intent)
            }

        }






//        setSupportActionBar(tool_bar_for_happy_places_Details)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        tool_bar_for_happy_places_Details.setNavigationOnClickListener {
//            onBackPressed()
//        }

    }
}