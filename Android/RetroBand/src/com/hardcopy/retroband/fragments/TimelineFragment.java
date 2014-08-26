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

package com.hardcopy.retroband.fragments;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.hardcopy.retroband.R;
import com.hardcopy.retroband.R.id;
import com.hardcopy.retroband.R.layout;
import com.hardcopy.retroband.R.string;
import com.hardcopy.retroband.contents.ActivityReport;
import com.hardcopy.retroband.contents.ContentManager;
import com.hardcopy.retroband.logic.Analyzer;
import com.hardcopy.retroband.utils.Logs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineFragment extends Fragment implements IAdapterListener, View.OnClickListener {
	private static final String TAG = "TimelineFragment";
	
	// Constants
	private static final long DRAWING_GRAPH_INTERVAL = 5*1000;
	private static final long DRAWING_GRAPH_DELAY = 3*1000;
	
	// System
	private Context mContext = null;
	private Handler mHandler = null;
	private IFragmentListener mFragmentListener;
	
	// Contents
	private ContentManager mContentManager;
	
	// View
	private RenderingStatistics mRenderStatistics;
	private TextView mStatisticsText = null;
	private TextView mCalorieText = null;
	private TextView mWalksText = null;
	//private ListView mTimelineList = null;
	private TimelineAdapter mTimelineListAdapter = null;
	private Button mButtonTimeInterval = null;
	
	// Parameters
	private int mStatisticsType = ContentManager.REPORT_TYPE_HOUR;
	
	// Auto-refresh timer
	private Timer mRefreshTimer = null;
	
	
	public TimelineFragment() {
	}
	
	public TimelineFragment(Context c, IFragmentListener l, Handler h) {
		mContext = c;
		mFragmentListener = l;
		mHandler = h;
	}
	
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logs.d(TAG, "# MessageListFragment - onCreateView()");
		
		View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);

		mRenderStatistics = (RenderingStatistics)rootView.findViewById(R.id.render_statistics);
		
		mStatisticsText = (TextView) rootView.findViewById(R.id.text_title_statistics);
		mCalorieText = (TextView) rootView.findViewById(R.id.text_content_calorie);
		mCalorieText.setText("0");
		mWalksText = (TextView) rootView.findViewById(R.id.text_content_walks);
		mWalksText.setText("0");
		
		mStatisticsType = ContentManager.REPORT_TYPE_HOUR;
		mButtonTimeInterval = (Button) rootView.findViewById(R.id.button_time_interval);
		mButtonTimeInterval.setOnClickListener(this);
		setTimeIntervalString(mStatisticsType);
		
		// TODO: If you need to show activity data as list, use below code
		/*
		mTimelineList = (ListView) rootView.findViewById(R.id.list_timeline);
		if(mTimelineListAdapter == null)
			mTimelineListAdapter = new TimelineAdapter(mContext, R.layout.list_item_timeline, null);
		mTimelineListAdapter.setAdapterParams(this);
		mTimelineList.setAdapter(mTimelineListAdapter);
		*/
		
		return rootView;
	}
	
	@Override
	public void OnAdapterCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IAdapterListener.CALLBACK_xxx:
			// TODO: 
			//if(arg4 != null)
			//	mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_ADD_FILTER, 0, 0, null, null, arg4);
			break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mRefreshTimer = new Timer();
		mRefreshTimer.schedule(new RefreshTimerTask(), DRAWING_GRAPH_DELAY, DRAWING_GRAPH_INTERVAL);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(mRefreshTimer != null) {
			mRefreshTimer.cancel();
			mRefreshTimer = null;
		}
	}

	@Override 
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_time_interval:
			changeTimeInterval();
			setTimeIntervalString(mStatisticsType);
			drawStatistics();
			break;
		default:
			break;
				
		}
	}
	
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	/**
	 * Initialize rendering view
	 * @return	boolean		is initialized or not
	 */
	private boolean checkRenderView() {
		if(mRenderStatistics != null) {
			mRenderStatistics.initializeGraphics(0);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Draw stacked calorie data
	 */
	private void drawStatistics() {
		if(mRenderStatistics == null)
			return;
		
		// Set data type (month or day or hour)
		setStatisticsString(mStatisticsType);
		// Initialize view
		checkRenderView();
		
		// Get contents manager
		if(mContentManager == null)
			mContentManager = ContentManager.getInstance(mContext, null);
		
		int[] arrays = mContentManager.getCurrentActivityData(mStatisticsType);
		if(arrays != null) {
			mRenderStatistics.drawGraph(mStatisticsType, arrays);
			mRenderStatistics.invalidate();
		}
	}
	
	/**
	 * Show graph title string
	 * @param type	REPORT_TYPE_MONTH or REPORT_TYPE_DAY or REPORT_TYPE_HOUR
	 */
	private void setStatisticsString(int type) {
		mStatisticsText.setText(R.string.title_statistics);

		switch(type) {
		case ContentManager.REPORT_TYPE_MONTH:
			mStatisticsText.append(" (");
			mStatisticsText.append(mContext.getString(R.string.title_month));
			mStatisticsText.append(")");
			break;
		case ContentManager.REPORT_TYPE_DAY:
			mStatisticsText.append(" (");
			mStatisticsText.append(mContext.getString(R.string.title_day));
			mStatisticsText.append(")");
			break;
		case ContentManager.REPORT_TYPE_HOUR:
			mStatisticsText.append(" (");
			mStatisticsText.append(mContext.getString(R.string.title_hour));
			mStatisticsText.append(")");
			break;
		}
	}
	
	/**
	 * Set time interval string. 
	 * @param type		REPORT_TYPE_MONTH or REPORT_TYPE_DAY or REPORT_TYPE_HOUR
	 */
	private void setTimeIntervalString(int type) {
		switch(type) {
		case ContentManager.REPORT_TYPE_MONTH:
			mButtonTimeInterval.setText(mContext.getString(R.string.title_month));
			break;
		case ContentManager.REPORT_TYPE_DAY:
			mButtonTimeInterval.setText(mContext.getString(R.string.title_day));
			break;
		case ContentManager.REPORT_TYPE_HOUR:
			mButtonTimeInterval.setText(mContext.getString(R.string.title_hour));
			break;
		}
	}
	
	/**
	 * Change time interval
	 * Repeats REPORT_TYPE_MONTH => REPORT_TYPE_DAY => REPORT_TYPE_HOUR
	 */
	private void changeTimeInterval() {
		switch(mStatisticsType) {
		case ContentManager.REPORT_TYPE_MONTH:
			mStatisticsType = ContentManager.REPORT_TYPE_DAY;
			break;
		case ContentManager.REPORT_TYPE_DAY:
			mStatisticsType = ContentManager.REPORT_TYPE_HOUR;
			break;
		case ContentManager.REPORT_TYPE_HOUR:
			mStatisticsType = ContentManager.REPORT_TYPE_MONTH;
			break;
		}
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	/**
	 * Show sum of calorie and sum of walk count
	 * Service triggers this at every sync
	 * @param object
	 */
	public void showActivityReport(ActivityReport object) {
		if(object != null) {
			String str = String.format("%,.0f", object.mSumOfCalorie);
			mCalorieText.setText(str);
			mWalksText.setText(Integer.toString(object.mShakeActionCount));
		}
	}
	
	public void addMessage(ActivityReport object) {
		if(object != null && mTimelineListAdapter != null) {
			mTimelineListAdapter.addObject(object);
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	public void addMessageOnTop(ActivityReport object) {
		if(object != null && mTimelineListAdapter != null) {
			mTimelineListAdapter.addObjectOnTop(object);
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	public void addMessageAll(ArrayList<ActivityReport> objList) {
		if(objList != null && mTimelineListAdapter != null) {
			mTimelineListAdapter.addObjectAll(objList);
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	public void deleteMessage(int id) {
		if(mTimelineListAdapter != null) {
			mTimelineListAdapter.deleteObject(id);
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	public void deleteMessageByType(int type) {
		if(mTimelineListAdapter != null) {
			mTimelineListAdapter.deleteObjectByType(type);
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	public void deleteMessageAll() {
		if(mTimelineListAdapter != null) {
			mTimelineListAdapter.deleteObjectAll();
			mTimelineListAdapter.notifyDataSetChanged();
		}
	}
	
	
	/*****************************************************
	 *	Handler, Listener, Timer, Sub classes
	 ******************************************************/
    /**
     * Auto-refresh Timer
     */
	private class RefreshTimerTask extends TimerTask {
		public RefreshTimerTask() {}
		
		@Override
		public void run() {
			if(mHandler != null) {
				mHandler.post(new Runnable() {
					public void run() {
						// Do what you want
						drawStatistics();	// Refresh graph periodically
					}
				});
			}
		}
	}
	
}
