package com.lateralthoughts.vue.connectivity;

import com.lateralthoughts.vue.VueConstants;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class VueContentProvider extends ContentProvider{

  
  private static final int AISLES_MATCH = 1;
  private static final int AISLE_MATCH = 2;
  private static final int AISLE_IMAGES_MATCH = 3;
  private static final int IMAGE_MATCH = 4;
  private static final int LOOKING_FOR_TABLE_MATCH = 5;
  private static final int LOOKING_FOR_ROW_MATCH = 6;
  private static final int OCCATION_TABLE_MATCH = 7;
  private static final int OCCATION_ROW_MATCH = 8;
  private static final int CATEGORY_TABLE_MATCH = 9;
  private static final int CATEGORY_ROW_MATCH = 10;
  private static final int COMMENTS_TABLE_MATCH = 11;
  private static final int COMMENTS_ROW_MATCH = 12;
  private DbHelper dbHelper;
  private static final UriMatcher URIMATCHER;

  /** uri matchers for articles table and articles fts3 table query method. */
  static {
    URIMATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.AISLES,
        AISLES_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.AISLES
        + "/#", AISLE_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.AISLE_IMAGES,
        AISLE_IMAGES_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.AISLE_IMAGES
        + "/#", IMAGE_MATCH );
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.LOOKING_FOR_TABLE,
        LOOKING_FOR_TABLE_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.LOOKING_FOR_TABLE
        + "/#", LOOKING_FOR_ROW_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.OCCASION_TABLE,
        OCCATION_TABLE_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.OCCASION_TABLE
        + "/#", OCCATION_ROW_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.CATEGORY_TABLE,
        CATEGORY_TABLE_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.CATEGORY_TABLE
        + "/#", CATEGORY_ROW_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.COMMENTS_ON_IMAGES_TABLE,
        COMMENTS_TABLE_MATCH);
    URIMATCHER.addURI(VueConstants.AUTHORITY, VueConstants.COMMENTS_ON_IMAGES_TABLE
        + "/#", COMMENTS_ROW_MATCH);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int rowsDeleted = 0;
    String id;
    SQLiteDatabase aislesDB = dbHelper.getWritableDatabase();
    switch (URIMATCHER.match(uri)) {
      case AISLES_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.AISLES, selection,
            selectionArgs);
        break;
      case AISLE_MATCH:
        id = uri.getLastPathSegment();
        rowsDeleted = aislesDB.delete(VueConstants.AISLES,
            VueConstants.AISLE_ID + "=" + id + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : ""),selectionArgs);
        break;
      case AISLE_IMAGES_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.AISLE_IMAGES, selection,
            selectionArgs);
        break;
      case IMAGE_MATCH:
        id = uri.getLastPathSegment();
        rowsDeleted = aislesDB.delete(VueConstants.AISLE_IMAGES,
            VueConstants.IMAGE_ID + "=" + id + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : ""),selectionArgs);
        break;
      case LOOKING_FOR_TABLE_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.LOOKING_FOR_TABLE, selection,
            selectionArgs);
        break;
      case LOOKING_FOR_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsDeleted = aislesDB.delete(VueConstants.LOOKING_FOR_TABLE,
            VueConstants.ID + "=" + id + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : ""),selectionArgs);
        break;
      case OCCATION_TABLE_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.OCCASION_TABLE, selection,
            selectionArgs);
        break;
      case OCCATION_ROW_MATCH:
      id = uri.getLastPathSegment();
      rowsDeleted = aislesDB.delete(VueConstants.OCCASION_TABLE,
          VueConstants.ID + "=" + id + (!TextUtils.isEmpty(selection) ?
              " AND (" + selection + ')' : ""),selectionArgs);
      case CATEGORY_TABLE_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.CATEGORY_TABLE, selection,
            selectionArgs);
        break;
      case CATEGORY_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsDeleted = aislesDB.delete(VueConstants.CATEGORY_TABLE,
            VueConstants.ID + "=" + id + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : ""),selectionArgs);
        break;
      case COMMENTS_TABLE_MATCH:
        rowsDeleted = aislesDB.delete(VueConstants.COMMENTS_ON_IMAGES_TABLE, selection,
            selectionArgs);
        break;
      case COMMENTS_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsDeleted = aislesDB.delete(VueConstants.COMMENTS_ON_IMAGES_TABLE,
            VueConstants.ID + "=" + id + (!TextUtils.isEmpty(selection) ?
                " AND (" + selection + ')' : ""),selectionArgs);
        break;
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
    return rowsDeleted;
  }

  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase aislesDB = dbHelper.getWritableDatabase();
    Uri rowUri = null;
    long rowId = 0;

    switch (URIMATCHER.match(uri)) {
      case AISLES_MATCH:
        rowId = aislesDB.insert(VueConstants.AISLES, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case AISLE_IMAGES_MATCH:
        rowId = aislesDB.insert(VueConstants.AISLE_IMAGES, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.IMAGES_CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case LOOKING_FOR_TABLE_MATCH:
        rowId = aislesDB.insert(VueConstants.LOOKING_FOR_TABLE, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.LOOKING_FOR_CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case OCCATION_TABLE_MATCH:
        rowId = aislesDB.insert(VueConstants.OCCASION_TABLE, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case CATEGORY_TABLE_MATCH:
        rowId = aislesDB.insert(VueConstants.CATEGORY_TABLE, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case COMMENTS_TABLE_MATCH:
        rowId = aislesDB.insert(VueConstants.COMMENTS_ON_IMAGES_TABLE, null, values);
        if (rowId > 0) {
          rowUri = ContentUris.appendId(VueConstants.CONTENT_URI.buildUpon(),
              rowId).build();
          return rowUri;
        }
        break;
      case COMMENTS_ROW_MATCH:
      case CATEGORY_ROW_MATCH:
      case OCCATION_ROW_MATCH:
      case LOOKING_FOR_ROW_MATCH:
      case IMAGE_MATCH:
      case AISLE_MATCH:
        Log.e("provider", "Must use ARTICLES url for inserting: " + uri);
        throw new IllegalArgumentException("Unsupported URI: " + uri);
      default:
        Log.e("provider", "Unsupported URI for inserting: " + uri);
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
    throw new SQLException("Failed to insert row: " + uri);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    SQLiteDatabase aislesDB = dbHelper.getReadableDatabase();
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    Cursor cursor = null;
    String id;

    switch (URIMATCHER.match(uri)) {
      case AISLES_MATCH:
        qb.setTables(VueConstants.AISLES);
        //cursor = aislesDB.query(VueConstants.AISLES, projection, selection, selectionArgs, null, null, sortOrder);
        cursor = qb.query(aislesDB, projection, selection, selectionArgs,
            null, null, sortOrder);
        break;
     case AISLE_MATCH:
       qb.setTables(VueConstants.AISLES);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.AISLE_ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
        break;
     case AISLE_IMAGES_MATCH:
       qb.setTables(VueConstants.AISLE_IMAGES);
       cursor = qb.query(aislesDB, projection, selection, selectionArgs,
           null, null, sortOrder);
       break;
     case IMAGE_MATCH:
       qb.setTables(VueConstants.AISLE_IMAGES);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.IMAGE_ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
       break;
     case LOOKING_FOR_TABLE_MATCH:
       qb.setTables(VueConstants.LOOKING_FOR_TABLE);
       cursor = qb.query(aislesDB, projection, selection, selectionArgs,
           null, null, sortOrder);
       break;
     case LOOKING_FOR_ROW_MATCH:
       qb.setTables(VueConstants.LOOKING_FOR_TABLE);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
       break;
     case OCCATION_TABLE_MATCH:
       qb.setTables(VueConstants.OCCASION_TABLE);
       cursor = qb.query(aislesDB, projection, selection, selectionArgs,
           null, null, sortOrder);
       break;
     case OCCATION_ROW_MATCH:
       qb.setTables(VueConstants.OCCASION_TABLE);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
     break;
     case CATEGORY_TABLE_MATCH:
       qb.setTables(VueConstants.CATEGORY_TABLE);
       cursor = qb.query(aislesDB, projection, selection, selectionArgs,
           null, null, sortOrder);
       break;
     case CATEGORY_ROW_MATCH:
       qb.setTables(VueConstants.CATEGORY_TABLE);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
       break;
     case COMMENTS_TABLE_MATCH:
       qb.setTables(VueConstants.COMMENTS_ON_IMAGES_TABLE);
       cursor = qb.query(aislesDB, projection, selection, selectionArgs,
           null, null, sortOrder);
       break;
     case COMMENTS_ROW_MATCH:
       qb.setTables(VueConstants.COMMENTS_ON_IMAGES_TABLE);
       id = uri.getLastPathSegment();
       cursor = qb.query(aislesDB, projection,
           VueConstants.ID+ "=" + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                   : ""), selectionArgs, null, null, null);
       break;
       
    }
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    SQLiteDatabase aislesDB = dbHelper.getWritableDatabase();
    int rowsUpdated = 0;
    String id;

    /*
     * Update record in articles table and get the row number of recently
     * updated.
     */
    switch (URIMATCHER.match(uri)) {
      case AISLES_MATCH:
        rowsUpdated = aislesDB.update(VueConstants.AISLES, values,
            selection, selectionArgs);
        break;

      case AISLE_MATCH:
        id = uri.getLastPathSegment();
        rowsUpdated = aislesDB.update(VueConstants.AISLES, values,
            VueConstants.AISLE_ID + "=" + id
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                    : ""), selectionArgs);
        break;
      case AISLE_IMAGES_MATCH:
        rowsUpdated = aislesDB.update(VueConstants.AISLE_IMAGES, values,
            selection, selectionArgs);
        break;
      case IMAGE_MATCH:
        id = uri.getLastPathSegment();
        rowsUpdated = aislesDB.update(VueConstants.AISLE_IMAGES, values,
            VueConstants.IMAGE_ID + "=" + id
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                    : ""), selectionArgs);
        break;
      case LOOKING_FOR_TABLE_MATCH:
        rowsUpdated = aislesDB.update(VueConstants.LOOKING_FOR_TABLE, values,
            selection, selectionArgs);
        break;
      case LOOKING_FOR_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsUpdated = aislesDB.update(VueConstants.LOOKING_FOR_TABLE, values,
            VueConstants.IMAGE_ID + "=" + id
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                    : ""), selectionArgs);
        break;
      case OCCATION_TABLE_MATCH:
        rowsUpdated = aislesDB.update(VueConstants.OCCASION_TABLE, values,
            selection, selectionArgs);
        break;
      case OCCATION_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsUpdated = aislesDB.update(VueConstants.OCCASION_TABLE, values,
            VueConstants.IMAGE_ID + "=" + id
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                    : ""), selectionArgs);
        break;
      case CATEGORY_TABLE_MATCH:
        rowsUpdated = aislesDB.update(VueConstants.CATEGORY_TABLE, values,
            selection, selectionArgs);
        break;
      case CATEGORY_ROW_MATCH:
        id = uri.getLastPathSegment();
        rowsUpdated = aislesDB.update(VueConstants.CATEGORY_TABLE, values,
            VueConstants.IMAGE_ID + "=" + id
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                    : ""), selectionArgs);
        break;
    }
    return rowsUpdated;
  }

  @Override
  public boolean onCreate() {
    dbHelper = new DbHelper(getContext());
    return (dbHelper == null) ? false : true;
  }
}
