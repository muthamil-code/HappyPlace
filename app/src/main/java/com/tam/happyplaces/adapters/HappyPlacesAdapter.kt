package com.tam.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tam.happyplaces.R
import com.tam.happyplaces.activities.AddHappyPlaces
import com.tam.happyplaces.activities.HappyPlaceDetailActivities
import com.tam.happyplaces.activities.MainActivity
import com.tam.happyplaces.database.DataBaseHelper
import com.tam.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.item_happy_places.view.*


class HappyPlacesAdapter(val context : Context,val items : ArrayList<HappyPlaceModel>): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>(){

private var onClickListener : OnClickListener? = null
    class ViewHolder(view : View):RecyclerView.ViewHolder(view){

//        val iv_place_imageCircle = view.iv_place_imageCircle
//        val tvTitle = view.tvTitle
//        val tvDescription = view.tvDescription

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_happy_places,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       var getItem = items[position]
        holder.itemView.iv_place_imageCircle.setImageURI(Uri.parse(getItem.image))
        holder.itemView.tvTitle.text = getItem.title
        holder.itemView.tvDescription.text = getItem.description


        holder.itemView.setOnClickListener{
            if(onClickListener != null){
                onClickListener!!.onClick(position,getItem)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position : Int,model : HappyPlaceModel)
    }

    fun removeAt(position: Int){
       val dataBaseHandler = DataBaseHelper(context)
        val success = dataBaseHandler.deleteEmployee(items[position])
        if(success > -1){
            items.removeAt(position)
            notifyItemChanged(position)

        }else{
            Toast.makeText(context,"not deleted",Toast.LENGTH_SHORT).show()
        }


    }

    fun notifyEditItem(activity : Activity,position : Int,requestCode : Int){

        val intent = Intent(context,AddHappyPlaces::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,items[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)


    }

}