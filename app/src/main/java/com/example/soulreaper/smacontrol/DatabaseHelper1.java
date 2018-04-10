package com.example.soulreaper.smacontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper1 extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper1";

    private static final String TABLE_NAME = "Status_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "Status";

    public DatabaseHelper1(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db1) {
        String createTable1 = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 +" TEXT)";
        db1.execSQL(createTable1);
    }

    @Override
        public void onUpgrade(SQLiteDatabase db1, int i, int i1) {
        db1.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db1);
    }

    public boolean addData1(String item1) {
        SQLiteDatabase db1 = this.getWritableDatabase();
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(COL2, item1);

        Log.d(TAG, "addData: Adding " + item1 + " to " + TABLE_NAME);

        long result = db1.insert(TABLE_NAME, null, contentValues1);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData1(){
        SQLiteDatabase db1 = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data1 = db1.rawQuery(query, null);
        return data1;
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param name
     * @return
     */
    public Cursor getItemID(String name){
        SQLiteDatabase db1 = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + name + "'";
        Cursor data1 = db1.rawQuery(query, null);
        return data1;
    }

}