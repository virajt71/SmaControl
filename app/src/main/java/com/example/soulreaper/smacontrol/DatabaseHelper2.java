package com.example.soulreaper.smacontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper2 extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper2";

    private static final String TABLE_NAME = "Status1_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "Status1";

    public DatabaseHelper2(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db2) {
        String createTable2 = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 +" TEXT)";
        db2.execSQL(createTable2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db2, int i, int i1) {
        db2.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db2);
    }

    public boolean addData2(String item2) {
        SQLiteDatabase db2 = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COL2, item2);

        Log.d(TAG, "addData: Adding " + item2 + " to " + TABLE_NAME);

        long result = db2.insert(TABLE_NAME, null, contentValues2);

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
    public Cursor getData2(){
        SQLiteDatabase db2 = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data2 = db2.rawQuery(query, null);
        return data2;
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param name
     * @return
     */
    public Cursor getItemID(String name){
        SQLiteDatabase db2 = this.getWritableDatabase();
        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
                " WHERE " + COL2 + " = '" + name + "'";
        Cursor data2 = db2.rawQuery(query, null);
        return data2;
    }

}