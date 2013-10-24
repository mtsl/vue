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
	 public static final String DATABASE_TABLE_COMMENTS_ON_IMAGES = "commentsOnImages";
	 public static final String DATABASE_TABLE_RECENTLY_VIEWED_AISLES = "recentlyViewAisles";
	 public static final String DATABASE_TABLE_RATED_IMAGES = "ratedImages";
	 public static final String DATABASE_TABLE_BOOKMARKS_AISLES = "bookmarkedAisles";
	 public static final String DATABASE_TABLE_FOR_ORDERING = "aislesDisplayOrderMap";
	 public static final int DATABASE_VERSION = 1;

     private String createAislesTable = "create table if not exists " + DATABASE_TABLE_AISLES
     + " (" + VueConstants.AISLE_Id + " integer primary key, "
     + VueConstants.CATEGORY + " text, "
     + VueConstants.FIRST_NAME + " text, "
     + VueConstants.LAST_NAME + " text, "
     + VueConstants.JOIN_TIME + " text, "
     + VueConstants.LOOKING_FOR + " text, "
     + VueConstants.OCCASION + " text, "
     + VueConstants.USER_ID + " text, "
     + VueConstants.BOOKMARK_COUNT + " text, "
     + VueConstants.IS_BOOKMARKED + " integer, "
     + VueConstants.DIRTY_FLAG + " integer, "
     + VueConstants.DELETE_FLAG + " integer, "
     + VueConstants.ID + " text);";

     private String createAisleImagesTable = "create table if not exists " + DATABASE_TABLE_AISLES_IMAGES
     + " (" + VueConstants.IMAGE_ID + " integer primary key, "
     + VueConstants.AISLE_Id + " text, "
     + VueConstants.TITLE + " text, "
     + VueConstants.IMAGE_URL + " text, "
     + VueConstants.DETAILS_URL + " text, "
     + VueConstants.STORE + " text, "
     + VueConstants.USER_ID + " text, "
     + VueConstants.ID + " text, "
     + VueConstants.LIKE_OR_DISLIKE + " integer, "
     + VueConstants.LIKES_COUNT + " integer, "
     + VueConstants.DELETE_FLAG + " integer, "
     + VueConstants.DIRTY_FLAG + " integer, "
     + VueConstants.HEIGHT + " text, "
     + VueConstants.WIDTH + " text);";

     private String createImageCommentsTable = "create table if not exists " + DATABASE_TABLE_COMMENTS_ON_IMAGES
     + " (" + VueConstants.ID + " long primary key, "
     + VueConstants.IMAGE_ID + " long, "
     + VueConstants.DIRTY_FLAG + " integer, "
     + VueConstants.DELETE_FLAG + " integer, "
     + VueConstants.LAST_MODIFIED_TIME + " long, "
     + VueConstants.COMMENTS + " text);";

    private String createLookingForTable = "create table if not exists " + DATABASE_TABLE_LOOKINGFOR
        + " (" + VueConstants.ID + " integer primary key autoincrement, " 
        + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " text, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";

    private String createOccasionTable = "create table if not exists " + DATABASE_TABLE_OCCASION
        + " (" + VueConstants.ID + " integer primary key autoincrement, "
        + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " text, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";

    private String createCategoryTable = "create table if not exists " + DATABASE_TABLE_CATEGORY
        + " (" + VueConstants.ID + " integer primary key autoincrement, "
        + VueConstants.KEYWORD + " text, "
        + VueConstants.LAST_USED_TIME + " text, "
        + VueConstants.NUMBER_OF_TIMES_USED + " integer);";

    private String createRecentViewTable = "create table if not exists " + DATABASE_TABLE_RECENTLY_VIEWED_AISLES
        + " (" + VueConstants.ID + " integer primary key autoincrement, "
        + VueConstants.RECENTLY_VIEWED_AISLE_ID + " text, "
        + VueConstants.VIEW_TIME + " text);";

    private String createReatingImagesTable = "create table if not exists " + DATABASE_TABLE_RATED_IMAGES
        + " (" + VueConstants.ID + " long primary key, "
        + VueConstants.IS_LIKED_OR_BOOKMARKED + " integer, "
        + VueConstants.AISLE_ID + " text, "
        + VueConstants.IMAGE_ID + " text);";
    
    private String createBookmarkAislesTable = "create table if not exists " + DATABASE_TABLE_BOOKMARKS_AISLES
        + " (" + VueConstants.ID + " long primary key, "
        + VueConstants.IS_LIKED_OR_BOOKMARKED + " integer, "
        + VueConstants.AISLE_ID + " text not null);";
    
    private String createAislesDisplayOrderMap = "create table if not exists " + DATABASE_TABLE_FOR_ORDERING
        + " (" + VueConstants.AISLE_ID + " long primary key, "
        + VueConstants.AISLE_ORDER + " integer);";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(createAislesTable);
    db.execSQL(createAisleImagesTable);
    db.execSQL(createImageCommentsTable);
    db.execSQL(createLookingForTable);
    db.execSQL(createOccasionTable);
    db.execSQL(createCategoryTable);
    db.execSQL(createRecentViewTable);
    db.execSQL(createReatingImagesTable);
    db.execSQL(createBookmarkAislesTable);
    db.execSQL(createAislesDisplayOrderMap);
  }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
