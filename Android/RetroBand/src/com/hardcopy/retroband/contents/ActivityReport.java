package com.hardcopy.retroband.contents;

public class ActivityReport {
	public int mId = -1;
	public int mType = 0;
	public long mStartTime = 0;			// Start time of this activity report
	
	// Accelerometer processing data
	public int mSumOfDifference = 0;
	public int mCount = 0;
	public int mAverageDifference = 0;
	public int mSamplingInterval = 0;
	public int mTotalTime = 0;
	
	// Result
	public int mShakeActionCount = 0;	// Walk count
	public double mCalorie = 0;			// Calorie consumes for 1 sec.
	public double mSumOfCalorie = 0;	// Total calorie consumed for this session
}
