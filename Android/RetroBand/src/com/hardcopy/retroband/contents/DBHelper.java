/*
 * Copyright (C) 2014 The Retro Band - Open source smart band project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hardcopy.retroband.contents;

import com.hardcopy.retroband.utils.Logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper {
	
	private static final String TAG  ="DBHelper";
	
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "retroband";

	//----------- Accel data table parameters
	public static final String TABLE_NAME_ACCEL_REPORT = "accel";
	
	public static final String KEY_ACCEL_ID = "_id";			// int		primary key, auto increment
	public static final String KEY_ACCEL_TYPE = "type";			// type
	public static final String KEY_ACCEL_TIME = "time";			// time
	public static final String KEY_ACCEL_YEAR = "year";			// year
	public static final String KEY_ACCEL_MONTH = "month";		// month
	public static final String KEY_ACCEL_DAY = "day";			// day
	public static final String KEY_ACCEL_HOUR = "hour";			// hour
	public static final String KEY_ACCEL_MINUTE = "minute";		// minute
	public static final String KEY_ACCEL_SECOND = "second";		// second
	public static final String KEY_ACCEL_DATA1 = "data1";		// mSumOfDifference
	public static final String KEY_ACCEL_DATA2 = "data2";		// mCount
	public static final String KEY_ACCEL_DATA3 = "data3";		// mAverageDifference
	public static final String KEY_ACCEL_DATA4 = "data4";		// mSamplingInterval
	public static final String KEY_ACCEL_DATA5 = "data5";		// mTotalTime
	public static final String KEY_ACCEL_ARG0 = "arg0";		// int		// count of walk
	public static final String KEY_ACCEL_ARG1 = "arg1";		// int 		// calorie
	public static final String KEY_ACCEL_ARG2 = "arg2";		// string
	public static final String KEY_ACCEL_ARG3 = "arg3";		// string
	
	public static final int INDEX_ACCEL_ID = 0;
	public static final int INDEX_ACCEL_TYPE = 1;
	public static final int INDEX_ACCEL_TIME = 2;
	public static final int INDEX_ACCEL_YEAR = 3;
	public static final int INDEX_ACCEL_MONTH = 4;
	public static final int INDEX_ACCEL_DAY = 5;
	public static final int INDEX_ACCEL_HOUR = 6;
	public static final int INDEX_ACCEL_MINUTE = 7;
	public static final int INDEX_ACCEL_SECOND = 8;
	public static final int INDEX_ACCEL_DATA1 = 9;
	public static final int INDEX_ACCEL_DATA2 = 10;
	public static final int INDEX_ACCEL_DATA3 = 11;
	public static final int INDEX_ACCEL_DATA4 = 12;
	public static final int INDEX_ACCEL_DATA5 = 13;
	public static final int INDEX_ACCEL_ARG0 = 14;
	public static final int INDEX_ACCEL_ARG1 = 15;
	public static final int INDEX_ACCEL_ARG2 = 16;
	public static final int INDEX_ACCEL_ARG3 = 17;
	
	private static final String DATABASE_CREATE_ACCEL_TABLE = "CREATE TABLE " +TABLE_NAME_ACCEL_REPORT+ "("
													+ KEY_ACCEL_ID +" Integer primary key autoincrement, "
													+ KEY_ACCEL_TYPE + " Integer not null, "
													+ KEY_ACCEL_TIME + " Integer not null, "
													+ KEY_ACCEL_YEAR + " Integer, "
													+ KEY_ACCEL_MONTH + " Integer, "
													+ KEY_ACCEL_DAY + " Integer, "
													+ KEY_ACCEL_HOUR + " Integer, "
													+ KEY_ACCEL_MINUTE + " Integer, "
													+ KEY_ACCEL_SECOND + " integer, "
													+ KEY_ACCEL_DATA1 + " integer, "
													+ KEY_ACCEL_DATA2 + " integer, "
													+ KEY_ACCEL_DATA3 + " integer, "
													+ KEY_ACCEL_DATA4 + " integer, "
													+ KEY_ACCEL_DATA5 + " integer, "
													+ KEY_ACCEL_ARG0 + " integer, "
													+ KEY_ACCEL_ARG1 + " integer, "
													+ KEY_ACCEL_ARG2 + " Text, "
													+ KEY_ACCEL_ARG3 + " Text"
													+ ")";
	private static final String DATABASE_DROP_ACCEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_ACCEL_REPORT;
	//----------- End of Accel data table parameters
	
	// Context, System
	private final Context mContext;
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	// Constructor
	public DBHelper(Context context) {
		this.mContext = context;
	}
	
	
	//----------------------------------------------------------------------------------
	// Public classes
	//----------------------------------------------------------------------------------
	// DB open (Writable)
	public DBHelper openWritable() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	// DB open (Readable)
	public DBHelper openReadable() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}
	
	// Terminate DB
	public void close() {
		if(mDb != null) {
			mDb.close();
			mDb = null;
		}
		if(mDbHelper != null) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}
	
	//----------------------------------------------------------------------------------
	// INSERT
	//----------------------------------------------------------------------------------
	public long insertActivityReport(int type, long time, int year, int month, int day, int hour, int[] dataArray, String subData) throws SQLiteConstraintException {
		if(time < 1 || dataArray == null || dataArray.length < 5)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_ACCEL_TYPE, type);
		
		insertValues.put(KEY_ACCEL_TIME, time);
		insertValues.put(KEY_ACCEL_YEAR, year);
		insertValues.put(KEY_ACCEL_MONTH, month);
		insertValues.put(KEY_ACCEL_DAY, day);
		insertValues.put(KEY_ACCEL_HOUR, hour);
		
		insertValues.put(KEY_ACCEL_DATA1, dataArray[0]);	// Sum of calorie
		insertValues.put(KEY_ACCEL_DATA2, dataArray[1]);	// Sum of walk count
		insertValues.put(KEY_ACCEL_DATA3, dataArray[2]);
		insertValues.put(KEY_ACCEL_DATA4, dataArray[3]);
		insertValues.put(KEY_ACCEL_DATA5, dataArray[4]);
		insertValues.put(KEY_ACCEL_ARG0, 0);
		insertValues.put(KEY_ACCEL_ARG1, 0);
		insertValues.put(KEY_ACCEL_ARG2, subData);
		
		Logs.d(TAG, "+ Insert activity report : mStartTime="+time+", Year="+year+", Month="+month+", Day="+day+", Hour="+hour);
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.insertOrThrow(TABLE_NAME_ACCEL_REPORT, null, insertValues);
		}
	}

	
	//----------------------------------------------------------------------------------
	// SELECT methods
	//----------------------------------------------------------------------------------
	public Cursor selectReportAll() {
		synchronized (mDb) {
			if(mDb == null) return null;
			return mDb.query(
					TABLE_NAME_ACCEL_REPORT,	// Table : String
					null,		// Columns : String[]
					null,		// Selection : String
					null,		// Selection arguments: String[]
					null,		// Group by : String
					null,		// Having : String
					null,		// Order by : String
					null );		// Limit : String
		}
	}
	
	public Cursor selectReportWithType(int type, int count) {
		synchronized (mDb) {
			if(mDb == null) return null;
			String countString = null;
			if(count > 0)
				countString = Integer.toString(count);
			return mDb.query(
					TABLE_NAME_ACCEL_REPORT,		// Table : String
					null,						// Columns : String[]
					KEY_ACCEL_TYPE + "=" + Integer.toString(type),		// Selection 	: String
					null,			// Selection arguments: String[]
					null,			// Group by 	: String
					null,			// Having 		: String
					KEY_ACCEL_ID+" DESC",			// Order by 	: String
					countString );		// Limit		: String
		}
	}
	
	public Cursor selectReportWithTime(int type, long timeBiggerThan, long timeSmallerThan) {
		synchronized (mDb) {
			if(mDb == null) return null;
			return mDb.query(
					TABLE_NAME_ACCEL_REPORT,		// Table : String
					null,							// Columns : String[]
					KEY_ACCEL_TYPE + "=" + Integer.toString(type)
					+ " AND " + KEY_ACCEL_TIME + ">" + Long.toString(timeBiggerThan) 
					+ " AND " + KEY_ACCEL_TIME + "<" + Long.toString(timeSmallerThan),		// Selection 	: String
					null,			// Selection arguments: String[]
					null,			// Group by 	: String
					null,			// Having 		: String
					KEY_ACCEL_ID+" DESC",			// Order by 	: String
					null );		// Limit		: String
		}
	}
	
	public Cursor selectReportWithDate(int type, int year, int month, int day, int hour) {
		synchronized (mDb) {
			if(mDb == null) return null;
			
			StringBuilder sb = new StringBuilder();
			sb.append(KEY_ACCEL_TYPE).append("=").append(type);
			sb.append(" AND ").append(KEY_ACCEL_YEAR).append("=").append(year);
			
			if(month > -1 && month < 12) {
				sb.append(" AND ").append(KEY_ACCEL_MONTH).append("=").append(month);
			}
			if(day > -1 && day < 31) {
				sb.append(" AND ").append(KEY_ACCEL_DAY).append("=").append(day);
			}
			if(hour > -1 && hour < 24) {
				sb.append(" AND ").append(KEY_ACCEL_HOUR).append("=").append(hour);
			}
			return mDb.query(
					TABLE_NAME_ACCEL_REPORT,		// Table : String
					null,							// Columns : String[]
					sb.toString(),		// Selection 	: String
					null,			// Selection arguments: String[]
					null,			// Group by 	: String
					null,			// Having 		: String
					KEY_ACCEL_ID+" DESC",			// Order by 	: String
					null );		// Limit		: String
		}
	}
	
	//----------------------------------------------------------------------------------
	// Update methods
	//----------------------------------------------------------------------------------
/*
	public int updateFilter(FilterObject filter) 
	{
		if(filter.mType < 0 || filter.mCompareType < 0 
				|| filter.mOriginalString == null || filter.mOriginalString.length() < 1)
			return -1;
		
		ContentValues insertValues = new ContentValues();
		insertValues.put(KEY_FILTER_TYPE, filter.mType);
		insertValues.put(KEY_FILTER_ICON_TYPE, filter.mIconType);
		insertValues.put(KEY_FILTER_MATCHING, filter.mCompareType);
		insertValues.put(KEY_FILTER_REPLACE_TYPE, filter.mReplaceType);
		insertValues.put(KEY_FILTER_ORIGINAL, filter.mOriginalString);
		insertValues.put(KEY_FILTER_REPLACE, filter.mReplaceString);
//		insertValues.put(KEY_FILTER_ARG0, 0);		// for future use
//		insertValues.put(KEY_FILTER_ARG1, 0);
//		insertValues.put(KEY_FILTER_ARG2, "");
//		insertValues.put(KEY_FILTER_ARG3, "");
		
		synchronized (mDb) {
			if(mDb == null) 
				return -1;
			return mDb.update( TABLE_NAME_FILTERS,		// table
					insertValues, 		// values
					KEY_FILTER_ID + "='" + filter.mId + "'", // whereClause
					null ); 			// whereArgs
		}
	}
*/
	
	//----------------------------------------------------------------------------------
	// Delete methods
	//----------------------------------------------------------------------------------

	public void deleteReportWithID(int id) {
		if(mDb == null) return;
		
		synchronized (mDb) {
			int count = mDb.delete(TABLE_NAME_ACCEL_REPORT, 
					KEY_ACCEL_ID + "=" + id, // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete record : id="+id+", count="+count);
		}
	}
	
	public void deleteReportWithType(int type) {
		if(mDb == null) return;
		
		synchronized (mDb) {
			int count = mDb.delete(TABLE_NAME_ACCEL_REPORT, 
					KEY_ACCEL_TYPE + "=" + type, // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete record : type="+type+", deleted count="+count);
		}
	}
	
	public void deleteReportWithTime(int type, long timeBiggerThan, long timeSmallerThan) {
		if(mDb == null) return;
		
		synchronized (mDb) {
			int count = mDb.delete(TABLE_NAME_ACCEL_REPORT, 
					KEY_ACCEL_TYPE + "=" + type 
					+ " AND " + KEY_ACCEL_TIME + ">" + Long.toString(timeBiggerThan) 
					+ " AND " + KEY_ACCEL_TIME + "<" + Long.toString(timeSmallerThan), // whereClause
					null); 			// whereArgs
			Logs.d(TAG, "- Delete record : type="+type+", "+timeBiggerThan+" < time < "+timeSmallerThan+", deleted count="+count);
		}
	}
	
	public void deleteReportWithDate(int type, int year, int month, int day, int hour) {
		if(mDb == null) return;
		
		synchronized (mDb) {
			int count = mDb.delete(TABLE_NAME_ACCEL_REPORT, 
					KEY_ACCEL_TYPE + "=" + type 
					+ " AND " + KEY_ACCEL_YEAR + "=" + Integer.toString(year) 
					+ " AND " + KEY_ACCEL_MONTH + "=" + Integer.toString(month)
					+ " AND " + KEY_ACCEL_DAY + "=" + Integer.toString(day)
					+ " AND " + KEY_ACCEL_HOUR + "=" + Integer.toString(hour), // whereClause
					null); 			// whereArgs
		}
	}
	
	//----------------------------------------------------------------------------------
	// Count methods
	//----------------------------------------------------------------------------------
	public int getReportCount() {
		String query = "select count(*) from " + TABLE_NAME_ACCEL_REPORT;
		Cursor c = mDb.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getReportCountWithType(int type) {
		String query = "select count(*) from " + TABLE_NAME_ACCEL_REPORT + " where " + KEY_ACCEL_TYPE + "=" + Integer.toString(type);
		Cursor c = mDb.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	
	public int getReportCountWithTime(int type, long timeBiggerThan, long timeSmallerThan) {
		String query = "select count(*) from " + TABLE_NAME_ACCEL_REPORT + " where " 
				+ KEY_ACCEL_TYPE + "=" + Integer.toString(type)
				+ " AND " + KEY_ACCEL_TIME + ">" + Long.toString(timeBiggerThan) 
				+ " AND " + KEY_ACCEL_TIME + "<" + Long.toString(timeSmallerThan);
		Cursor c = mDb.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	

	//----------------------------------------------------------------------------------
	// SQLiteOpenHelper
	//----------------------------------------------------------------------------------
	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		// Constructor
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		// Will be called one time at first access
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_ACCEL_TABLE);
		}

		// Will be called when the version is increased
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO: Keep previous data
			db.execSQL(DATABASE_DROP_ACCEL_TABLE);
			
			db.execSQL(DATABASE_CREATE_ACCEL_TABLE);
		}
		
	}	// End of class DatabaseHelper
	
}
