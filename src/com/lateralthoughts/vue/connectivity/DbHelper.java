package com.lateralthoughts.vue.connectivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lateralthoughts.vue.VueConstants;

public class DbHelper extends SQLiteOpenHelper {
    
    public static final String DATABASE_NAME = "Vue.db";
    public static final String DATABASE_TABLE_AISLES = "aisles";
    public static final String DATABASE_TABLE_AISLES_IMAGES = "aisleImages";
    public static final String DATABASE_TABLE_LOOKINGFOR = "lookingFor";
    public static final String DATABASE_TABLE_OCCASION = "occasion";
    public static final String DATABASE_TABLE_CATEGORY = "category";
    public static final String DATABASE_TABLE_COMMENTS_ON_IMAGES = "commentsOnImages";
    public static final String DATABASE_TABLE_RECENTLY_VIEWED_AISLES = "recentlyViewAisles";
    public static final String DATABASE_TABLE_RATED_IMAGES = "ratedImages";
    public static final String DATABASE_TABLE_BOOKMARKS_AISLES = "bookmarkedAisles";
    public static final String DATABASE_TABLE_MY_BOOKMARKED_AISLES = "myBookmarkedAisles";
    public static final int DATABASE_VERSION = 1;
    
    private String mCreateAislesTable = "create table if not exists "
            + DATABASE_TABLE_AISLES + " (" + VueConstants.AISLE_Id
            + " integer primary key, " + VueConstants.CATEGORY + " text, "
            + VueConstants.FIRST_NAME + " text, " + VueConstants.LAST_NAME
            + " text, " + VueConstants.JOIN_TIME + " text, "
            + VueConstants.LOOKING_FOR + " text, " + VueConstants.OCCASION
            + " text, " + VueConstants.USER_ID + " text, "
            + VueConstants.BOOKMARK_COUNT + " text, "
            + VueConstants.IS_BOOKMARKED + " integer, "
            + VueConstants.DIRTY_FLAG + " integer, " + VueConstants.DELETE_FLAG
            + " integer, " + VueConstants.AISLE_DESCRIPTION + " text, "
            + VueConstants.AISLE_OWNER_IMAGE_URL + " text, " + VueConstants.ID
            + " text);";
    
    private String mCreateAisleImagesTable = "create table if not exists "
            + DATABASE_TABLE_AISLES_IMAGES + " (" + VueConstants.IMAGE_ID
            + " integer primary key, " + VueConstants.AISLE_Id + " text, "
            + VueConstants.TITLE + " text, " + VueConstants.IMAGE_URL
            + " text, " + VueConstants.DETAILS_URL + " text, "
            + VueConstants.STORE + " text, " + VueConstants.USER_ID + " text, "
            + VueConstants.ID + " text, " + VueConstants.LIKE_OR_DISLIKE
            + " integer, " + VueConstants.LIKES_COUNT + " integer, "
            + VueConstants.DELETE_FLAG + " integer, " + VueConstants.DIRTY_FLAG
            + " integer, " + VueConstants.HEIGHT + " text, "
            + VueConstants.WIDTH + " text);";
    
    private String mCreateImageCommentsTable = "create table if not exists "
            + DATABASE_TABLE_COMMENTS_ON_IMAGES + " (" + VueConstants.ID
            + " long primary key, " + VueConstants.IMAGE_ID + " long, "
            + VueConstants.AISLE_Id + " long," + VueConstants.DIRTY_FLAG
            + " integer, " + VueConstants.DELETE_FLAG + " integer, "
            + VueConstants.LAST_MODIFIED_TIME + " long, "
            + VueConstants.COMMENTER_URL + " text, " + VueConstants.COMMENTS
            + " text);";
    
    private String mCreateLookingForTable = "create table if not exists "
            + DATABASE_TABLE_LOOKINGFOR + " (" + VueConstants.ID
            + " integer primary key autoincrement, " + VueConstants.KEYWORD
            + " text, " + VueConstants.LAST_USED_TIME + " text, "
            + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
    
    private String mCreateOccasionTable = "create table if not exists "
            + DATABASE_TABLE_OCCASION + " (" + VueConstants.ID
            + " integer primary key autoincrement, " + VueConstants.KEYWORD
            + " text, " + VueConstants.LAST_USED_TIME + " text, "
            + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
    
    private String mCreateCategoryTable = "create table if not exists "
            + DATABASE_TABLE_CATEGORY + " (" + VueConstants.ID
            + " integer primary key autoincrement, " + VueConstants.KEYWORD
            + " text, " + VueConstants.LAST_USED_TIME + " text, "
            + VueConstants.NUMBER_OF_TIMES_USED + " integer);";
    
    private String mCreateRecentViewTable = "create table if not exists "
            + DATABASE_TABLE_RECENTLY_VIEWED_AISLES + " (" + VueConstants.ID
            + " integer primary key autoincrement, "
            + VueConstants.RECENTLY_VIEWED_AISLE_ID + " text, "
            + VueConstants.VIEW_TIME + " text);";
    
    private String mCreateReatingImagesTable = "create table if not exists "
            + DATABASE_TABLE_RATED_IMAGES + " (" + VueConstants.ID
            + " long primary key, " + VueConstants.IS_LIKED_OR_BOOKMARKED
            + " integer, " + VueConstants.AISLE_ID + " text, "
            + VueConstants.IMAGE_ID + " text);";
    
    private String mCreateBookmarkAislesTable = "create table if not exists "
            + DATABASE_TABLE_BOOKMARKS_AISLES + " (" + VueConstants.ID
            + " long primary key, " + VueConstants.IS_LIKED_OR_BOOKMARKED
            + " integer, " + VueConstants.AISLE_ID + " text not null);";
    
    private String mCreateMyBookmarkedAislesTable = "create table if not exists "
            + DATABASE_TABLE_MY_BOOKMARKED_AISLES
            + " ("
            + VueConstants.AISLE_Id
            + " integer primary key, "
            + VueConstants.CATEGORY
            + " text, "
            + VueConstants.FIRST_NAME
            + " text, "
            + VueConstants.LAST_NAME
            + " text, "
            + VueConstants.JOIN_TIME
            + " text, "
            + VueConstants.LOOKING_FOR
            + " text, "
            + VueConstants.OCCASION
            + " text, "
            + VueConstants.USER_ID
            + " text, "
            + VueConstants.BOOKMARK_COUNT
            + " text, "
            + VueConstants.IS_BOOKMARKED
            + " integer, "
            + VueConstants.DIRTY_FLAG
            + " integer, "
            + VueConstants.DELETE_FLAG
            + " integer, "
            + VueConstants.AISLE_DESCRIPTION
            + " text, "
            + VueConstants.ID + " text);";
    
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mCreateAislesTable);
        db.execSQL(mCreateAisleImagesTable);
        db.execSQL(mCreateImageCommentsTable);
        db.execSQL(mCreateLookingForTable);
        db.execSQL(mCreateOccasionTable);
        db.execSQL(mCreateCategoryTable);
        db.execSQL(mCreateRecentViewTable);
        db.execSQL(mCreateReatingImagesTable);
        db.execSQL(mCreateBookmarkAislesTable);
        db.execSQL(mCreateMyBookmarkedAislesTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
}
