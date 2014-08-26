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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import com.hardcopy.retroband.logic.Analyzer;
import com.hardcopy.retroband.utils.Logs;

import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;


public class ContentManager {
	
	private static final String TAG = "ContentManager";
	
	public static final int RESPONSE_INVALID_PARAMETER = -1;
	public static final int RESPONSE_NO_MATCHING_CONTENT = -2;
	
	public static final int RESPONSE_OBJECT_ADDED = 11;
	public static final int RESPONSE_OBJECT_UPDATED = 12;
	public static final int RESPONSE_OBJECT_DELETED = 13;
	
	public static final int REPORT_TYPE_YEAR = 1;
	public static final int REPORT_TYPE_MONTH = 2;
	public static final int REPORT_TYPE_DAY = 3;
	public static final int REPORT_TYPE_HOUR = 4;
	
	
	private static ContentManager mContentManager = null;		// Singleton pattern
	
	private Context mContext;
	private IContentManagerListener mContentManagerListener;
	private DBHelper mDB = null;
	
	private ArrayList<ContentObject> mContentList;		// Cache content objects
	
	// Time parameters
	private static final int REPORT_INTERVAL = 1000;
	private static final int REPORT_SAMPLING_TIME = 50;
	private long mPreviousProcessTime = 0;
	
	// Activity statistics
	// WARNING: Date parameter is the zero-based number
	// because we use parameters as array index
	private int mThisYear = -1;
	
	private int mThisMonth = -1;	// (in the range [0,11])
	private int[] mMonthArray = new int[12];
	
	private int mThisDay = -1;		// (in the range [0,30])
	private int[] mDayArray = new int[31];
	
	private int mThisHour = -1;		// (in the range [0,23])
	private int[] mHourArray = new int[24];
	
	private int mThisMinute = -1;	// (in the range [0,59])
	private int[] mMinuteArray = new int[60]; 
	

	/**
	 * Constructor
	 */
	private ContentManager(Context c, IContentManagerListener l) {
		mContext = c;
		mContentManagerListener = l;
		
		mContentList = new ArrayList<ContentObject>();
		
		//----- Make DB helper
		if(mDB == null) {
			mDB = new DBHelper(mContext).openWritable();
		}
		
		//----- Initialize activity data
		initializeActivityParams();
		getCurrentReportsFromDB();
	}
	
	/**
	 * Singleton pattern
	 */
	public synchronized static ContentManager getInstance(Context c, IContentManagerListener l) {
		if(mContentManager == null)
			mContentManager = new ContentManager(c, l);
		
		return mContentManager;
	}
	
	public synchronized void finalize() {
		if(mDB != null) {
			mDB.close();
			mDB = null;
		}
		if(mContentList != null)
			mContentList.clear();
		mContentManager = null;
	}

	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	private void initializeBuffer() {
		Arrays.fill(mMonthArray, 0x00000000);
		Arrays.fill(mDayArray, 0x00000000);
		Arrays.fill(mHourArray, 0x00000000);
		Arrays.fill(mMinuteArray, 0x00000000);
	}
	
	private void initializeActivityParams() {
		initializeBuffer();
		
		Calendar cal = Calendar.getInstance();
		mThisYear = cal.get(Calendar.YEAR);
		mThisMonth = cal.get(Calendar.MONTH);			// 0~11
		mThisDay = cal.get(Calendar.DAY_OF_MONTH) - 1;	// convert to 0~30
		mThisHour = cal.get(Calendar.HOUR_OF_DAY);		// 0~23
		mThisMinute = cal.get(Calendar.MINUTE);			// 0~59
	}
	
	/**
	 * Make sum of calorie at this month, day, hour.
	 */
	private void getCurrentReportsFromDB() {
		// Get month data in this year
		Cursor c = mDB.selectReportWithDate(REPORT_TYPE_MONTH, mThisYear, -1, -1, -1);
		if(c != null) {
			getDataFromCursor(MODE_CURRENT_TIME, REPORT_TYPE_MONTH, c);
			c.close();
		}
		
		// Get day data in this month
		c = mDB.selectReportWithDate(REPORT_TYPE_DAY, mThisYear, mThisMonth, -1, -1);
		if(c != null) {
			getDataFromCursor(MODE_CURRENT_TIME, REPORT_TYPE_DAY, c);
			c.close();
		}
		
		// Get hour data in this day
		c = mDB.selectReportWithDate(REPORT_TYPE_HOUR, mThisYear, mThisMonth, mThisDay, -1);
		if(c != null) {
			getDataFromCursor(MODE_CURRENT_TIME, REPORT_TYPE_HOUR, c);
			c.close();
		}
	}
	
	/**
	 * Load sum of calorie from DB
	 */
	private int[] getReportsFromDB(int type, int year, int month, int day, int hour) {
		int[] timeArray = null;
		// Get month data in this year
		Cursor c = mDB.selectReportWithDate(type, year, month, day, hour);
		if(c != null) {
			timeArray = getDataFromCursor(MODE_SELECTED_TIME, type, c);
			c.close();
		}
		return timeArray;
	}
	
	public static final int MODE_CURRENT_TIME = 1;
	public static final int MODE_SELECTED_TIME = 2;
	/**
	 * Parse cursor and make cache
	 * @param mode		Make cache based on current time or specified time
	 * @param type		REPORT_TYPE_MONTH or REPORT_TYPE_DAY or REPORT_TYPE_HOUR
	 * @param c			Cursor
	 * @return	int[]	Parsed result array
	 */
	private int[] getDataFromCursor(int mode, int type, Cursor c) {
		int[] timeArray = null;
		int columnIndex = 0;
		
		switch(type) {
		case REPORT_TYPE_MONTH:
			if(mode == MODE_CURRENT_TIME) {
				timeArray = mMonthArray;
			} else {
				timeArray = new int[12];
				Arrays.fill(timeArray, 0x00000000);
			}
			
			columnIndex = DBHelper.INDEX_ACCEL_MONTH;
			break;
			
		case REPORT_TYPE_DAY:
			if(mode == MODE_CURRENT_TIME) {
				timeArray = mDayArray;
			} else {
				timeArray = new int[31];
				Arrays.fill(timeArray, 0x00000000);
			}
			
			columnIndex = DBHelper.INDEX_ACCEL_DAY;
			break;
			
		case REPORT_TYPE_HOUR:
			if(mode == MODE_CURRENT_TIME) {
				timeArray = mHourArray;
			} else {
				timeArray = new int[24];
				Arrays.fill(timeArray, 0x00000000);
			}
			
			columnIndex = DBHelper.INDEX_ACCEL_HOUR;
			break;
		default:
			return null;
		}
		
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				int index = c.getInt(columnIndex);
				int calorie = c.getInt(DBHelper.INDEX_ACCEL_DATA1);
				if(calorie > 0 && index > -1 && index < timeArray.length) {
					timeArray[index] = calorie;
				}
				
				c.moveToNext();
			}
		}
		
		return timeArray;
	}
	
	/**
	 * Add newly received data to cache and write on DB if needed
	 * @param ar	Raw accel data from remote
	 */
	private void addActivityReport(ActivityReport ar) {
		if(ar != null) {
			
			boolean isTimeChanged = false;
			Calendar cal = Calendar.getInstance();
			int prevYear = mThisYear;
			int prevMonth = mThisMonth;
			int prevDay = mThisDay;
			int prevHour = mThisHour;
			
			// Add calorie to buffer
			if(mThisMonth != cal.get(Calendar.MONTH)) {
				// Push monthly report to DB
				Time tempTime = new Time();
				tempTime.set(1, 0, 0, 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
				long millis = tempTime.toMillis(true);
				pushReportToDB(REPORT_TYPE_MONTH, millis, prevYear, prevMonth, 1, 0);
				
				// Set new date
				mThisYear = cal.get(Calendar.YEAR);
				mThisMonth = cal.get(Calendar.MONTH);
				if(mThisMonth == Calendar.JANUARY)
					Arrays.fill(mMonthArray, 0x00000000);
				isTimeChanged = true;
			}
			mMonthArray[mThisMonth] += ar.mCalorie;
			
			if(mThisDay != cal.get(Calendar.DAY_OF_MONTH) - 1 || isTimeChanged) {
				// Push daily report to DB
				Time tempTime = new Time();
				tempTime.set(1, 0, 0, prevDay + 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
				long millis = tempTime.toMillis(true);
				pushReportToDB(REPORT_TYPE_DAY, millis, prevYear, prevMonth, prevDay, 0);
				
				if(isTimeChanged) {
					// Month changed !! 
					Arrays.fill(mDayArray, 0x00000000);					
				} else {
					// Month is not changed but day changed
				}
				
				mThisDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
				isTimeChanged = true;
			}
			mDayArray[mThisDay] += ar.mCalorie;
			
			if(mThisHour != cal.get(Calendar.HOUR_OF_DAY) || isTimeChanged) {
				// Push hourly report to DB
				Time tempTime = new Time();
				tempTime.set(1, 0, prevHour, prevDay + 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
				long millis = tempTime.toMillis(true);
				pushReportToDB(REPORT_TYPE_HOUR, millis, prevYear, prevMonth, prevDay, prevHour);
				
				if(isTimeChanged) {
					// Day changed !! 
					Arrays.fill(mHourArray, 0x00000000);					
				} else {
					// Day is not changed but hour changed
				}
				
				mThisHour = cal.get(Calendar.HOUR_OF_DAY);
				isTimeChanged = true;
			}
			mHourArray[mThisHour] += ar.mCalorie;
			
			if(isTimeChanged || mThisMinute != cal.get(Calendar.MINUTE)) {
				if(isTimeChanged) {
					// Hour changed !! 
					Arrays.fill(mMinuteArray, 0x00000000);					
				} else {
					// Hour is not changed but minute changed
				}
				
				// Add to new minute buffer
				mThisMinute = cal.get(Calendar.MINUTE);
			}
			mMinuteArray[mThisMinute] += ar.mCalorie;
			
		}	// end of if(ar != null)
	}
	
	/**
	 * Write sum of calorie on DB
	 * @param type		REPORT_TYPE_YEAR, REPORT_TYPE_MONTH, REPORT_TYPE_DAY, REPORT_TYPE_HOUR
	 * @param time		time in milli-second
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 */
	private void pushReportToDB(int type, long time, int year, int month, int day, int hour) {
		int calorie = 0;
		
		switch(type) {
		case REPORT_TYPE_YEAR:
			// Not available
			return;
			
		case REPORT_TYPE_MONTH:
			if(month > -1 && month < mMonthArray.length) {
				calorie = mMonthArray[month];
			} else {
				return;
			}
			break;
			
		case REPORT_TYPE_DAY:
			if(day > -1 && day < mDayArray.length) {
				calorie = mDayArray[day];
			} else {
				return;
			}
			break;
			
		case REPORT_TYPE_HOUR:
			if(hour > -1 && hour < mHourArray.length) {
				calorie = mHourArray[hour];
			} else {
				return;
			}
			break;
		}
		
		if(calorie < 1)
			return;
		
		// Make data array to save
		// We use only one information, calorie.
		int[] dataArray = new int[5];
		Arrays.fill(dataArray, 0x00000000);
		dataArray[0] = calorie;
		
		mDB.insertActivityReport(type, time, year, month, day, hour, dataArray, null);
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public void setListener(IContentManagerListener l) {
		mContentManagerListener = l;
	}
	
	/**
	 * Save sum of calorie cache to DB
	 */
	public void saveCurrentActivityReport() {
		int prevYear = mThisYear;
		int prevMonth = mThisMonth;
		int prevDay = mThisDay;
		int prevHour = mThisHour;
		
		// Push monthly report to DB
		Time tempTime1 = new Time();
		tempTime1.set(1, 0, 0, 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
		long millis1 = tempTime1.toMillis(true);
		pushReportToDB(REPORT_TYPE_MONTH, millis1, prevYear, prevMonth, 1, 0);
		
		// Push daily report to DB
		Time tempTime2 = new Time();
		tempTime2.set(1, 0, 0, prevDay + 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
		long millis2 = tempTime2.toMillis(true);
		pushReportToDB(REPORT_TYPE_DAY, millis2, prevYear, prevMonth, prevDay, 0);
		
		// Push hourly report to DB
		Time tempTime3 = new Time();
		tempTime3.set(1, 0, prevHour, prevDay + 1, prevMonth, prevYear);	// convert day: in the range [1,31], month: in the range [0,11]
		long millis3 = tempTime3.toMillis(true);
		pushReportToDB(REPORT_TYPE_HOUR, millis3, prevYear, prevMonth, prevDay, prevHour);
	}
	
	
	/**
	 * After parsing packets from remote, service calls this method with result object.
	 * This method analyze accel raw data and calculate walks, calories.
	 * And makes an activity report instance which has analyzed results.
	 * @param co		content object which has accel raw data array
	 * @return			activity report instance which has analyzed results.
	 */
	public synchronized ActivityReport addContentObject(ContentObject co) {
		if(co == null) {
			return null;
		}
		
		// Caching contents
		mContentList.add(co);
		
		// Get current time
		long currentTime = System.currentTimeMillis();
		if(mPreviousProcessTime < 1)
			mPreviousProcessTime = currentTime;
		
		// Analyze cached contents
		ActivityReport ar = null;
		if(currentTime - mPreviousProcessTime > REPORT_INTERVAL) {
			Logs.d("#");
			Logs.d("# before analyzer");
			// Analyze accelerometer value and make report
			ar = Analyzer.analyzeAccel(mContentList, REPORT_SAMPLING_TIME, REPORT_INTERVAL);
			
			// Remember activity report
			if(ar != null) {
				addActivityReport(ar);
			}
			mPreviousProcessTime = currentTime;
			mContentList.clear();
		}
		
		return ar;
	}
	
	/**
	 * Delete specified content object from cache and DB
	 * @param co_id		content object ID
	 * @return			result code
	 */
	public synchronized int deleteContentObject(int co_id) {
		if(co_id < 0)
			return RESPONSE_INVALID_PARAMETER;
		// Remove from DB
		mDB.deleteReportWithID(co_id);
		// Remove cached
		int count = 0;
		for(int i = mContentList.size() - 1; i > -1; i--) {
			ContentObject temp = mContentList.get(i);
			if(temp.mId == co_id) {
				mContentList.remove(i);
				count++;
			}
		}
		if(count < 1)
			return RESPONSE_NO_MATCHING_CONTENT;
		
		return RESPONSE_OBJECT_DELETED;
	}
	
	/**
	 * Delete all contents
	 * @return		result code
	 */
	public synchronized int deleteContentsAll() {
		// Remove from DB
		mDB.deleteReportWithType(ContentObject.CONTENT_TYPE_ACCEL);
		// Remove cached
		mContentList.clear();
		return RESPONSE_OBJECT_DELETED;
	}
	
	/**
	 * Returns cached activity data
	 * @param type		time period type
	 * @return			array of activity data
	 */
	public int[] getCurrentActivityData(int type) {
		int[] activityData = null;
		
		switch(type) {
		case REPORT_TYPE_MONTH:
			activityData = mMonthArray;
			break;
			
		case REPORT_TYPE_DAY:
			activityData = mDayArray;
			break;
			
		case REPORT_TYPE_HOUR:
			activityData = mHourArray;
			break;
			
		default:
			break;
		}	// End of switch

		return activityData;
	}
	
	/**
	 * Returns activity data from DB
	 * @param type		time period type
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @return			array of activity data
	 */
	public int[] getActivityData(int type, int year, int month, int day, int hour) {
		int[] activityData = null;
		
		activityData = getReportsFromDB(type, year, month, day, hour);
		
		return activityData;
	}
	
	
}
