package com.lateralthoughts.vue.connectivity;

import com.lateralthoughts.vue.VueConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	
	 public static final String DATABASE_NAME = "Vue.db";
	 public static final String DATABASE_TABLE_AISLES = "aisles";
	 public static final String DATABASE_TABLE_AISLES_IMAGES = "aisleImages";
	 public static final String DATABASE_TABLE_DATA_TO_SYNC = "dataToSync";
	 public static final String DATABASE_TABLE_LOOKINGFOR = "lookingFor";
	 public static final String DATABASE_TABLE_OCCASION = "occasion";
	 public static final String DATABASE_TABLE_CATEGORY = "category";
	 public static final int DATABASE_VERSION = 1;
	 
     private String createAislesTable = "create table if not exists " + DATABASE_TABLE_AISLES
     + " (" + VueConstants.AISLE_ID + " integer primary key, "
     + VueConstants.CATEGORY + " text, "
     + VueConstants.FIRST_NAME + " text, "
     + VueConstants.LAST_NAME + " text, "
     + VueConstants.JOIN_TIME + " text, "
     + VueConstants.LOOKING_FOR + " text, "
     + VueConstants.OCCASION + " text, "
     + VueConstants.USER_ID + " text, "
     + VueConstants.ID + " text);";
     
     private String createAisleImagesTable = "create table if not exists " + DATABASE_TABLE_AISLES_IMAGES
     + " (" + VueConstants.IMAGE_ID + " integer primary key, "
     + VueConstants.AISLE_ID + " text, "
     + VueConstants.TITLE + " text, "
     + VueConstants.IMAGE_URL + " text, "
     + VueConstants.DETAILS_URL + " text, "
     + VueConstants.STORE + " text, "
     + VueConstants.USER_ID + " text, "
     + VueConstants.ID + " text, "
     + VueConstants.HEIGHT + " text, "
     + VueConstants.WIDTH + " text);";
     
     private String createQueuedDataToSyncTable = "create table if not exists " + DATABASE_TABLE_DATA_TO_SYNC
     + " ("+ VueConstants.ID + " integer primary key, "
     + VueConstants.COMMENT + " text, "
     + VueConstants.AISLE_ID + " text, "
     + VueConstants.IMAGE_ID + " text);";
     
    private String createLookingForTable = "create table if not exists " + DATABASE_TABLE_LOOKINGFOR
        + " (" + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " Long, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
    
    private String createOccasionTable = "create table if not exists " + DATABASE_TABLE_OCCASION
        + " (" + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " Long, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
    
    private String createCategoryTable = "create table if not exists " + DATABASE_TABLE_CATEGORY
        + " (" + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " Long, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
        

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createAislesTable);
		db.execSQL(createAisleImagesTable);
		db.execSQL(createQueuedDataToSyncTable);
		db.execSQL(createLookingForTable);
		db.execSQL(createOccasionTable);
		db.execSQL(createCategoryTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
