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

import java.util.Arrays;
import java.util.Calendar;

/**
 * ContentObject holds accelerometer data at specified time.
 * @author Administrator
 *
 */
public class ContentObject {
	
	public static final int CONTENT_TYPE_ACCEL = 1;
	
	public static final int DATA_COUNT = 20;
	
	public int mContentType;			// Content type
	public int mId;						// ID
	public long mTimeInMilli;			// 
	public int mYear;
	public int mMonth;
	public int mDay;
	public int mhour;
	public int mMinute;
	public int mSecond;
	public int[] mAccelData = null;
	public int mAccelIndex = 0;
	public int mCacheIndex = 0;
	
	
	public ContentObject(int type, int id, long timeInMilli) {
		mContentType = type;
		mId = id;
		mTimeInMilli = timeInMilli;
		
		mAccelData = new int[DATA_COUNT*3];		// DATA_COUNT * 3 axis
		Arrays.fill(mAccelData, 0x00000000);
		mAccelIndex = 0;
		mCacheIndex = 0;
		
		// Convert date info in milli-time
		Calendar cal = Calendar.getInstance();
		setTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 
				cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		if(mTimeInMilli < 1)
			mTimeInMilli = cal.getTimeInMillis();
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	
	public void reset() {
		Arrays.fill(mAccelData, 0x00000000);
		mAccelIndex = 0;
		mCacheIndex = 0;
		setTime(0,0,0,0,0,0);
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	public void setTime(int year, int month, int day, int hour, int minute, int second) {
		mYear = year;
		mMonth = month;
		mDay = day;
		mhour = hour;
		mMinute = minute;
		mSecond = second;
	}
	
	public void setAccelData(int x_axis, int y_axis, int z_axis) {
		if(mAccelData != null && mAccelIndex > -1 && mAccelIndex < mAccelData.length / 3) {
			mAccelData[mAccelIndex] = x_axis;
			mAccelData[mAccelIndex+1] = y_axis;
			mAccelData[mAccelIndex+2] = z_axis;
			mAccelIndex++;
		}
	}
	
	public void setAccelData(int data) {
		if(mAccelData != null && mAccelIndex > -1 && mAccelIndex < DATA_COUNT) {
			mAccelData[mAccelIndex*3 + mCacheIndex] = data;
			mCacheIndex++;
			if(mCacheIndex == 3) {
				//Logs.d("# Accel = "+mAccelData[mAccelIndex*3 + mCacheIndex - 3]+", "+mAccelData[mAccelIndex*3 + mCacheIndex - 2]+", "+mAccelData[mAccelIndex*3 + mCacheIndex - 1]);
				mAccelIndex++;
				mCacheIndex = 0;
			}
		}
	}
	
}
