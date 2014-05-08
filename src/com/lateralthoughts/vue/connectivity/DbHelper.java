package com.lateralthoughts.vue.connectivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.lateralthoughts.vue.VueApplication;
import com.lateralthoughts.vue.VueConstants;
import com.lateralthoughts.vue.logging.Logger;
import com.lateralthoughts.vue.utils.RecentlyViewedAisle;
import com.lateralthoughts.vue.utils.UsedKeywordsOnUpgrade;

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
    public static final String DATABASE_TABLE_ALL_RATED_IMAGES = "allRatedImages";
    public static final String DATABASE_TABLE_BOOKMARKS_AISLES = "bookmarkedAisles";
    public static final String DATABASE_TABLE_MY_BOOKMARKED_AISLES = "myBookmarkedAisles";
    public static final String DATABASE_TABLE_MY_SHARED_AISLES = "mySharedAisles";
    public static final String DATABASE_TABLE_NOTIFICATION_AISLES = "notificationAisles";
    public static final int DATABASE_VERSION = 17;
    
    private String mCreateShareTable = "create table if not exists "
            + DATABASE_TABLE_MY_SHARED_AISLES + " ("
            + VueConstants.SHARE_AISLE_ID + " long primary key);";
    
    private String mCreateAislesTable = "create table if not exists "
            + DATABASE_TABLE_AISLES + " (" + VueConstants.AISLE_Id
            + " long primary key, " + VueConstants.CATEGORY + " text, "
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
            + " long primary key, " + VueConstants.AISLE_Id + " text, "
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
            + " text, " + VueConstants.AISLE_IMAGE_COMMENTER_FIRST_NAME
            + " text, " + VueConstants.AISLE_IMAGE_COMMENTER_LAST_NAME
            + " text );";
    
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
            + DATABASE_TABLE_RATED_IMAGES + " (" + VueConstants.IMAGE_ID
            + " long primary key, " + VueConstants.LIKE_OR_DISLIKE
            + " integer, " + VueConstants.AISLE_ID + " long, "
            + VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME + " text, "
            + VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME + " text, "
            + VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME + " long, "
            + VueConstants.DIRTY_FLAG + " integer, " + VueConstants.ID
            + " long);";
    
    private String mCreateAllReatingImagesTable = "create table if not exists "
            + DATABASE_TABLE_ALL_RATED_IMAGES + " (" + VueConstants.ID
            + " long primary key, " + VueConstants.LIKE_OR_DISLIKE
            + " integer, " + VueConstants.AISLE_ID + " long, "
            + VueConstants.AISLE_IMAGE_RATING_OWNER_FIRST_NAME + " text, "
            + VueConstants.AISLE_IMAGE_RATING_OWNER_LAST_NAME + " text, "
            + VueConstants.AISLE_IMAGE_RATING_LASTMODIFIED_TIME + " long, "
            + VueConstants.IMAGE_ID + " long);";
    
    private String mCreateBookmarkAislesTable = "create table if not exists "
            + DATABASE_TABLE_BOOKMARKS_AISLES + " (" + VueConstants.AISLE_ID
            + " long primary key, " + VueConstants.IS_LIKED_OR_BOOKMARKED
            + " integer, " + VueConstants.IS_BOOKMARK_DIRTY + " integer, "
            + VueConstants.ID + " long);";
    
    private String mNotificationAislesTable = "create table if not exists "
            + DATABASE_TABLE_NOTIFICATION_AISLES + " (" + VueConstants.ID
            + " integer primary key autoincrement, " + VueConstants.AISLE_Id
            + " text, " + VueConstants.IS_NOTIFICATION_AISLE_READ_OR_UNREAD
            + " integer, " + VueConstants.IMAGE_URL + " text, "
            + VueConstants.NOTIFICATION_AISLE_TITLE + " text, "
            + VueConstants.IMAGE_ID + " text, "
            + VueConstants.NOTIFICATION_TEXT + " text, "
            + VueConstants.LIKES_COUNT + " integer, "
            + VueConstants.BOOKMARK_COUNT + " integer, "
            + VueConstants.NOTIFICATION_AISLE_COMMENTS_COUNT + " integer);";
    
    private String mCreateMyBookmarkedAislesTable = "create table if not exists "
            + DATABASE_TABLE_MY_BOOKMARKED_AISLES
            + " ("
            + VueConstants.AISLE_Id
            + " long primary key, "
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
            + VueConstants.AISLE_OWNER_IMAGE_URL
            + " text, "
            + VueConstants.ID
            + " text);";
    
    private String mDropAisleTable = "DROP TABLE " + DATABASE_TABLE_AISLES;
    private String mDropAisleImagesTable = "DROP TABLE "
            + DATABASE_TABLE_AISLES_IMAGES;
    private String mDropImageCommentsTable = "DROP TABLE "
            + DATABASE_TABLE_COMMENTS_ON_IMAGES;
    private String mDropLookingForTable = "DROP TABLE "
            + DATABASE_TABLE_LOOKINGFOR;
    private String mDropOccasionTable = "DROP TABLE " + DATABASE_TABLE_OCCASION;
    private String mDropCategoryTable = "DROP TABLE " + DATABASE_TABLE_CATEGORY;
    private String mDropRecentlyViewTable = "DROP TABLE "
            + DATABASE_TABLE_RECENTLY_VIEWED_AISLES;
    private String mDropRatingImagesTable = "DROP TABLE "
            + DATABASE_TABLE_RATED_IMAGES;
    private String mDropAllRatingImagesTable = "DROP TABLE "
            + DATABASE_TABLE_ALL_RATED_IMAGES;
    private String mDropBookmarkAislesTable = "DROP TABLE "
            + DATABASE_TABLE_BOOKMARKS_AISLES;
    private String mDropMyBookmarkedAislesTable = "DROP TABLE "
            + DATABASE_TABLE_MY_BOOKMARKED_AISLES;
    private String mDropMySharedAislesTable = "DROP TABLE "
            + DATABASE_TABLE_MY_SHARED_AISLES;
    private String mDropNotificationAislesTable = "DROP TABLE "
            + DATABASE_TABLE_NOTIFICATION_AISLES;
    
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
        db.execSQL(mCreateAllReatingImagesTable);
        db.execSQL(mCreateShareTable);
        db.execSQL(mNotificationAislesTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            ArrayList<RecentlyViewedAisle> recentlyViewedAisles = DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .getRecentlyViewedAislesOnUpgrade(db);
            ArrayList<UsedKeywordsOnUpgrade> lookingForKeywords = DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .getAisleKeywordsOnUpgrade(DATABASE_TABLE_LOOKINGFOR, db);
            ArrayList<UsedKeywordsOnUpgrade> occasionKeywords = DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .getAisleKeywordsOnUpgrade(DATABASE_TABLE_OCCASION, db);
            ArrayList<UsedKeywordsOnUpgrade> categoryKeywords = DataBaseManager
                    .getInstance(VueApplication.getInstance())
                    .getAisleKeywordsOnUpgrade(DATABASE_TABLE_CATEGORY, db);
            writeToSdcard("Upgrade called : " + oldVersion + "???" + newVersion);
            // Droping tables...
            db.execSQL(mDropAisleTable);
            db.execSQL(mDropAisleImagesTable);
            db.execSQL(mDropImageCommentsTable);
            db.execSQL(mDropLookingForTable);
            db.execSQL(mDropOccasionTable);
            db.execSQL(mDropCategoryTable);
            db.execSQL(mDropRecentlyViewTable);
            db.execSQL(mDropRatingImagesTable);
            try {
                db.execSQL(mDropMySharedAislesTable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                db.execSQL(mDropAllRatingImagesTable);
            } catch (Exception e) {
            }
            try {
                db.execSQL(mDropBookmarkAislesTable);
            } catch (SQLException e) {
            }
            try {
                db.execSQL(mDropMyBookmarkedAislesTable);
            } catch (SQLException e) {
            }
            try {
                db.execSQL(mDropNotificationAislesTable);
            } catch (Exception e) {
            }
            // Creating Tables...
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
            db.execSQL(mCreateAllReatingImagesTable);
            db.execSQL(mCreateShareTable);
            db.execSQL(mNotificationAislesTable);
            // Restore Previous version data...
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .insertRecentlyViewedAislesOnUpgrade(recentlyViewedAisles,
                            db);
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .addAisleMetaDataToDBOnUpgrade(DATABASE_TABLE_LOOKINGFOR,
                            lookingForKeywords, db);
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .addAisleMetaDataToDBOnUpgrade(DATABASE_TABLE_OCCASION,
                            occasionKeywords, db);
            DataBaseManager.getInstance(VueApplication.getInstance())
                    .addAisleMetaDataToDBOnUpgrade(DATABASE_TABLE_CATEGORY,
                            categoryKeywords, db);
        }
    }
    
    private void writeToSdcard(String message) {
        if (!Logger.sWrightToSdCard) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/vueDBUpgrade/");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }
        File file = new File(dir, "/" + "vueDBUpgrade_"
                + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "-"
                + Calendar.getInstance().get(Calendar.DATE) + "_"
                + Calendar.getInstance().get(Calendar.YEAR) + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(file, true)));
            out.write("\n" + message + "\n");
            out.flush();
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
