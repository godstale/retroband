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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.hardcopy.retroband.R;
import com.hardcopy.retroband.R.color;
import com.hardcopy.retroband.R.id;
import com.hardcopy.retroband.R.layout;
import com.hardcopy.retroband.contents.ActivityReport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TimelineAdapter extends ArrayAdapter<ActivityReport> implements IDialogListener {
	public static final String TAG = "MessageListAdapter";
	
	private Context mContext = null;
	private ArrayList<ActivityReport> mReportList = null;
	private IAdapterListener mAdapterListener = null;
	
	public TimelineAdapter(Context c, int resId, ArrayList<ActivityReport> itemList) {
		super(c, resId, itemList);
		mContext = c;
		if(itemList == null)
			mReportList = new ArrayList<ActivityReport>();
		else
			mReportList = itemList;
	}
	
	
	
	public void setAdapterParams(IAdapterListener l) {
		mAdapterListener = l;
	}
	
	public void addObject(ActivityReport ar) {
		mReportList.add(ar);
	}
	
	public void addObjectOnTop(ActivityReport ar) {
		mReportList.add(0, ar);
	}
	
	public void addObjectAll(ArrayList<ActivityReport> itemList) {
		if(itemList == null)
			return;
		for(int i=0; i<itemList.size(); i++)
			addObject(itemList.get(i));
	}
	
	public void deleteObject(int id) {
		for(int i = mReportList.size() - 1; -1 < i; i--) {
			ActivityReport ar = mReportList.get(i);
			if(ar.mId == id) {
				mReportList.remove(i);
			}
		}
	}
	
	public void deleteObjectByType(int type) {
		for(int i = mReportList.size() - 1; -1 < i; i--) {
			ActivityReport ar = mReportList.get(i);
			if(ar.mType == type) {
				mReportList.remove(i);
			}
		}
	}
	
	public void deleteObjectAll() {
		mReportList.clear();
	}
	
	@Override
	public int getCount() {
		return mReportList.size();
	}
	@Override
	public ActivityReport getItem(int position) { 
		return mReportList.get(position); 
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View v = convertView;
		ActivityReport ar = getItem(position);
		
		if(v == null) {
			LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.list_item_timeline, null);
			holder = new ViewHolder();
			
			holder.mItemContainer = (LinearLayout) v.findViewById(R.id.timeline_item_container);
			holder.mItemContainer.setOnTouchListener(mListItemTouchListener);
			holder.mTextInfo1 = (TextView) v.findViewById(R.id.timeline_info1);
			holder.mTextInfo2 = (TextView) v.findViewById(R.id.timeline_info2);
			holder.mTextInfo3 = (TextView) v.findViewById(R.id.timeline_info3);
			
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		holder.mActivityReport = ar;
		
		if (ar != null && holder != null) {
			holder.mItemContainer.setBackgroundColor(mContext.getResources().getColor(R.color.lightblue1));
			if(ar.mSumOfDifference > 0 && ar.mCount > 0) {
				holder.mTextInfo1.setText("Sum of diff = "+Integer.toString(ar.mSumOfDifference) 
						+ ", Count = " + Integer.toString(ar.mCount)
						+ ", Average = " + Integer.toString(ar.mSumOfDifference / ar.mCount));
			} else {
				holder.mTextInfo1.setText("No data recorded...");
			}
			if(ar.mStartTime > 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(ar.mStartTime);
				
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd, hh:mm a");
				String timeField = formatter.format(cal.getTime());
				holder.mTextInfo2.setText(timeField);
			} else {
				holder.mTextInfo2.setText("Time is not valid");
			}
		}
		
		return v;
	}	// End of getView()
	
	@Override
	public void OnDialogCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IDialogListener.CALLBACK_CLOSE:
			break;
		}
	}
	
	/**
	 * Sometimes onClick listener misses event.
	 * Uses touch listener instead.
	 */
	private OnTouchListener mListItemTouchListener = new OnTouchListener() {
		private float startx = 0;
		private float starty = 0;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				startx = event.getX();
				starty = event.getY();
			}
			if(event.getAction()==MotionEvent.ACTION_UP){
				// if action-up occurred within 30px from start, process as click event. 
				if( (startx - event.getX())*(startx - event.getX()) + (starty - event.getY())*(starty - event.getY()) < 900 ) {
					processOnClickEvent(v);
				}
			}
			return true;
		}
	};	// End of new OnTouchListener
	
	/**
	 * Custom click event process
	 * @param v		target view
	 */
	private void processOnClickEvent(View v) {
		switch(v.getId())
		{
			case R.id.timeline_item_container:
				if(v.getTag() == null)
					break;
				ActivityReport ar = ((ViewHolder)v.getTag()).mActivityReport;
				if(ar != null) {
				}
				break;
		}	// End of switch()
	}
	
	// Holds layout information
	public class ViewHolder {
		public LinearLayout mItemContainer = null;
		public TextView mTextInfo1 = null;
		public TextView mTextInfo2 = null;
		public TextView mTextInfo3 = null;
		
		public ActivityReport mActivityReport = null;
	}
}
