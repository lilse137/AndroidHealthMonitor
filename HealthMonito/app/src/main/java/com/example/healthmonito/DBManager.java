package com.example.healthmonito;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String userId, double lat, double lon, int heartrate,int resprate,float nausea,float headache,float diarrhea,
                       float soarthroat,float fever,float muscleache,float smelltaste,float cough,
                       float breath,float tired) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.USERID, userId);
        contentValue.put(DatabaseHelper.LATITUDE, lat);
        contentValue.put(DatabaseHelper.LONGITUDE,  lon);
        contentValue.put(DatabaseHelper.HEARTRATE, (float)heartrate);
        contentValue.put(DatabaseHelper.RESPRATE, (float) resprate);
        contentValue.put(DatabaseHelper.NAUSEA, nausea);
        contentValue.put(DatabaseHelper.HEADACHE, headache);
        contentValue.put(DatabaseHelper.DIARRHEA, diarrhea);
        contentValue.put(DatabaseHelper.SOARTHROAT, soarthroat);
        contentValue.put(DatabaseHelper.FEVER, fever);
        contentValue.put(DatabaseHelper.MUSCLEACHE, muscleache);
        contentValue.put(DatabaseHelper.SMELLTASTE, smelltaste);
        contentValue.put(DatabaseHelper.COUGH, cough);
        contentValue.put(DatabaseHelper.BREATH, breath);
        contentValue.put(DatabaseHelper.TIRED, tired);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID,DatabaseHelper.USERID,  DatabaseHelper.LATITUDE, DatabaseHelper.LONGITUDE, DatabaseHelper.HEARTRATE,DatabaseHelper.RESPRATE,DatabaseHelper.NAUSEA,DatabaseHelper.HEADACHE,DatabaseHelper.DIARRHEA,DatabaseHelper.SOARTHROAT,DatabaseHelper.FEVER,DatabaseHelper.MUSCLEACHE,DatabaseHelper.SMELLTASTE,DatabaseHelper.COUGH,DatabaseHelper.BREATH,DatabaseHelper.TIRED };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

//    public int update(long _id, String name, String desc) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(DatabaseHelper.SUBJECT, name);
//        contentValues.put(DatabaseHelper.DESC, desc);
//        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
//        return i;
//    }

    public void delete() {
        database.delete(DatabaseHelper.TABLE_NAME, null, null);
    }

}
