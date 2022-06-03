/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// ����� ������������ ��� ����, ����� ��������������� ������ � ���� ������ SQLite,
// � ������� ����� ��������� �������

package com.razrabotkin.notes;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {
	
	// ����������� ��������, ������� ����� ������������ � ����������, 
	// ����� ������������� ������ �� ��������������� ����� � ���� ������
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    // ������ �������� ���� ������
    // ��� - data, ���� ������� - notes, 4 ����
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "body text not null, date text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * ��������� ���� ������ �������. ���� ����������, �������, ������� ������� �����
     * �������� ���� �������. ���� ���������� �������, ��������� ����������, �����
     * ������������������ � ����
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();	// ����� getWritableDatabase() ��������� ���������/��������� ���� ������
        return this;
    }

    // ��������� ���� ������, ���������� �������
    public void close() {
        mDbHelper.close();
    }


    /**
     * ������� ����� �������, ��������� ��������������� ���������, ����� � ����. ���� �������
     * ������� �������, ���������� ����� rowId ��� ���� �������, ����� ����������
     * -1, ����� ���������� ����.
     * 
     * @param body ����� �������
     * @param date ���� �������
     * @return rowId ��� -1 � ������ �������
     */
    public long createNote(String body, String date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * ������� ������� � ��������� rowId
     * 
     * @param rowId id �������, ������� ���������� �������
     * @return true ���� �������, ����� false
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * ���������� ������ �� ������ ��� ������� � ���� ������
     * 
     * @return ������ �� ���� ��������
     */
    public Cursor fetchAllNotes() {

    	// ���������:
    	// ��� ������� ���� ������
    	// ������ �������, ������� ���������� ��������, � ���� ������� �����
    	// selection
    	// selectionArgs
    	// groupBy
    	// having
    	// orderBy
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, 
                KEY_BODY, KEY_DATE}, null, null, null, null, KEY_DATE + " DESC");
    }

    /**
     * ���������� ������, ����������������� �� �������, ������� ������������� ���������� rowId
     * 
     * @param rowId id �������, ������� ���������� �������
     * @return ������, ����������������� �� ��������������� �������
     * @throws SQLException ���� ������� �� ����� ���� �������/����������
     */
    public Cursor fetchNote(long rowId) throws SQLException {

    	// ���������:
    	// ������ �������� ����������, ��� ��� ���������� ���� ��������� ���������
    	// ��� ������� ���� ������
    	// ������ �������, ������� ���������� ��������, � ���� ������� �����
    	// selection - �������� ����� ���, ����� ����� ������ ������ � ��������� id
    	// selectionArgs
    	// groupBy
    	// having
    	// orderBy
        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_BODY, KEY_DATE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * �������� �������, ��������� ��������� ������. �������, ������� ���������� ��������,
     * �������� � ������� rowId, � ���������� � ������������ � ����������� ����������,
     * ������� � �����. 
     * 
     * @param rowId id �������, ������� ���������� ��������
     * @param body ��������, � ������� ���������� ���������� �����
     * @param date ��������, � ������� ���������� ���������� ����
     * @return true ���� ������� ���� ������� ���������, ����� false
     */
    public boolean updateNote(long rowId, String body, String date) {
        ContentValues args = new ContentValues();
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, date);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
