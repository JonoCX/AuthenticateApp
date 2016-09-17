package com.jonathancarlton.authenticateapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <h1>Database Helper</h1>
 * SQLite helper class to perform specific operations
 * on the data that is stored.
 *
 * The data that is going to be stored is related to
 * tracking the last date/time that a user was attempting
 * to authenticate.
 *
 * @author Jonathan Carlton
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DateStorage.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATE_TABLE_NAME = "datesince";
    private static final String LAST_COLUMN_TWITTER_ID = "twitterid";
    private static final String LAST_COLUMN_LAST_CHECKED = "lastchecked";

    private static final String[] COLUMNS = {
            LAST_COLUMN_TWITTER_ID,
            LAST_COLUMN_LAST_CHECKED
    };

    private static final String CREATE_QUERY = "CREATE TABLE " + DATE_TABLE_NAME +
            " ( " + LAST_COLUMN_TWITTER_ID + " INTEGER PRIMARY KEY, " + LAST_COLUMN_LAST_CHECKED +
            " TEXT )";

    private static final String DROP_QUERY = "DROP TABLE IF EXISTS " + DATE_TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_QUERY);
        this.onCreate(sqLiteDatabase);
    }

    /**
     * Insert a record into the database.
     *
     * @param twitterID     the id of the record to insert.
     * @param date          the date value.
     * @return              the success of the insert operation.
     */
    public boolean insertDate(long twitterID, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LAST_COLUMN_TWITTER_ID, twitterID);
        contentValues.put(LAST_COLUMN_LAST_CHECKED, date);
        db.insert(DATE_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    /**
     * Fetch a record stored in the database.
     *
     * @param tID       the id of the record to retrieve.
     * @return          return the date value as a string.
     */
    public String getDate(long tID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                DATE_TABLE_NAME,
                COLUMNS,
                " twitterid = ?",
                new String[] { String.valueOf(tID) },
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0)
            cursor.moveToFirst();
        else
            return "No records stored";


        return cursor.getString(1);
    }

    /**
     * Update a record stored in the database.
     *
     * @param tID       the id of the record to update.
     * @param date      the value which is going to be changed.
     * @return          the number of rows affected.
     */
    public int updateDate(long tID, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LAST_COLUMN_TWITTER_ID, tID);
        values.put(LAST_COLUMN_LAST_CHECKED, date);

        int i = db.update(
                DATE_TABLE_NAME,
                values,
                LAST_COLUMN_TWITTER_ID + " = ?",
                new String[] { String.valueOf(tID) }
        );

        db.close();

        return i;
    }

    /**
     * Delete a record stored in the database.
     *
     * @param tID   the id of the record to delete
     */
    public void delete(long tID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                DATE_TABLE_NAME,
                LAST_COLUMN_TWITTER_ID + " = ?",
                new String[] { String.valueOf(tID) }
        );

        db.close();
    }

}
