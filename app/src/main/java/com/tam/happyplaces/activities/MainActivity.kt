package com.tam.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tam.happyplaces.R
import com.tam.happyplaces.adapters.HappyPlacesAdapter
import com.tam.happyplaces.database.DataBaseHelper
import com.tam.happyplaces.models.HappyPlaceModel
import com.tam.happyplaces.utils.Chech
import com.tam.happyplaces.utils.SwipeToEditCallBack

import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddHappyPlaces.setOnClickListener {
            val intent = Intent(this,AddHappyPlaces::class.java)
             startActivityForResult(intent,ADD_PALCE_ACTIVITY_REQUEST_CODE)
        }
        getPlaceRecords()
    }

    private fun recyclerLiseHappyPlaces(happy : ArrayList<HappyPlaceModel>){
        rv_happyPlaces.layoutManager = LinearLayoutManager(this)
        rv_happyPlaces.setHasFixedSize(true)
        val happyAdapter = HappyPlacesAdapter(this,happy)
        rv_happyPlaces.adapter = happyAdapter
        happyAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,HappyPlaceDetailActivities::class.java)
                 intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }

        })
        val editSwipeHandler = object : SwipeToEditCallBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happyPlaces.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,
                    ADD_PALCE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouch = ItemTouchHelper(editSwipeHandler)
        editItemTouch.attachToRecyclerView(rv_happyPlaces)


        val deleteSwipeHandler = object : Chech(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happyPlaces.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getPlaceRecords()
            }
        }

        val deleteItemTouch = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouch.attachToRecyclerView(rv_happyPlaces)

    }



    private fun getPlaceRecords(){
        val dataBaseHandler = DataBaseHelper(this)
        val  happyPlaceList  : ArrayList<HappyPlaceModel> = dataBaseHandler.viewHappyPlaces()
        if(happyPlaceList.size > 0){
           recyclerLiseHappyPlaces(happyPlaceList)
           rv_happyPlaces.visibility = View.VISIBLE
            tv_no_happy_place_found.visibility = View.GONE


            }else {

            rv_happyPlaces.visibility = View.GONE
            tv_no_happy_place_found.visibility = View.VISIBLE

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_PALCE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getPlaceRecords()
            }
        }else{
            Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        val ADD_PALCE_ACTIVITY_REQUEST_CODE = 1
        val EXTRA_PLACE_DETAILS = "extra_place_details"
    }

}