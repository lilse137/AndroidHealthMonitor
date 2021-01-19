package com.example.healthmonito;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper{
    public static final String TABLE_NAME = "SYMPTOMDATA";

    // Table columns
    public static final String _ID = "_id";
    public static final String USERID = "userId";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String HEARTRATE= "heartrate" ;
    public static final String RESPRATE= "resprate" ;
    public static final String NAUSEA= "nausea" ;
    public static final String HEADACHE= "headache" ;
    public static final String DIARRHEA= "diarrhea" ;
    public static final String SOARTHROAT= "soarthroat" ;
    public static final String FEVER= "fever" ;
    public static final String MUSCLEACHE= "muscleache" ;
    public static final String SMELLTASTE= "smelltaste" ;
    public static final String COUGH= "cough" ;
    public static final String BREATH= "breath" ;
    public static final String TIRED= "tired" ;

    // Database Information


    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USERID + " TEXT NOT NULL, "
            + LATITUDE + " TEXT NOT NULL, "
            + LONGITUDE + " TEXT NOT NULL, "
            + HEARTRATE + " INT , "
            + RESPRATE + " INT , "
            + NAUSEA + " REAL NOT NULL, "
            + HEADACHE + " REAL NOT NULL, "
            + DIARRHEA + " REAL NOT NULL, "
            + SOARTHROAT + " REAL NOT NULL, "
            + FEVER + " REAL NOT NULL, "
            + MUSCLEACHE + " REAL NOT NULL, "
            + SMELLTASTE + " REAL NOT NULL, "
            + COUGH + " REAL NOT NULL, "
            + BREATH + " REAL NOT NULL, "
            + TIRED + " REAL NOT NULL);";
//    static final String DB_NAME = Environment.getExternalStorageDirectory()


    public DatabaseHelper(Context context) {
        super(context, context.getExternalFilesDir(null).getAbsolutePath()+ File.separator + "databases"+ File.separator +"SYMPMONI.DB", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
