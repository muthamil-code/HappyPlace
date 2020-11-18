package com.tam.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.tam.happyplaces.models.HappyPlaceModel

class DataBaseHelper (context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "HappyPlaceDataBase"
        private val TABLE_CONTACTS = "HappyPlaceTable"
        private val KEY_ID = "id"
        private val KEY_TITLE = "title"
        private val KEY_IMAGE = "image"
        private val KEY_DESCRIPTION = "description"
        private val KEY_DATE = "date"
        private val KEY_LOCATION = "location"
        private val KEY_LATITUDE = "latitude"
        private val KEY_LONGITUDE = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE+" TEXT,"
                + KEY_LOCATION+" TEXT,"
                + KEY_LATITUDE+" TEXT,"
                + KEY_LONGITUDE+" TEXT"+")")
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS)
        onCreate(db)
    }

    fun deleteEmployee(happy: HappyPlaceModel):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, happy.id) // EmpModelClass UserId
        // Deleting Row
        val success = db.delete(TABLE_CONTACTS,"id="+happy.id,null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }



    fun updatePlace(happy: HappyPlaceModel):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_TITLE, happy.title)
        contentValues.put(KEY_IMAGE,happy.image)
        contentValues.put(KEY_DESCRIPTION, happy.description)
        contentValues.put(KEY_DATE,happy.date)
        contentValues.put(KEY_LOCATION, happy.location)
        contentValues.put(KEY_LATITUDE,happy.latitude)
        contentValues.put(KEY_LONGITUDE,happy.longitude)



        // Updating Row
        val success = db.update(TABLE_CONTACTS, contentValues,"id="+happy.id,null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }


    fun addPlaces(happy: HappyPlaceModel):Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, happy.title)
        contentValues.put(KEY_IMAGE,happy.image)
        contentValues.put(KEY_DESCRIPTION, happy.description)
        contentValues.put(KEY_DATE, happy.date)
        contentValues.put(KEY_LOCATION,happy.location)
        contentValues.put(KEY_LATITUDE, happy.latitude)
        contentValues.put(KEY_LONGITUDE,happy.longitude)


        val success = db.insert(TABLE_CONTACTS, null, contentValues)
        db.close()
        return success
    }

    fun viewHappyPlaces():ArrayList<HappyPlaceModel>{
        val happyList:ArrayList<HappyPlaceModel> = ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT  * FROM $TABLE_CONTACTS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        if (cursor.moveToFirst()) {
            do {
                  val place = HappyPlaceModel(
                      cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                      cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                      cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                      cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                      cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                      cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                      cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                      cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                  )
                happyList.add(place)

            } while (cursor.moveToNext())
        }
        return happyList
    }
}