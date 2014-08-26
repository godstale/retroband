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

import java.util.Locale;

import com.hardcopy.retroband.R;
import com.hardcopy.retroband.R.string;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class LLFragmentAdapter extends FragmentPagerAdapter {
	
	public static final String TAG = "RetroWatchFragmentAdapter";
	
	// Total count
	public static final int FRAGMENT_COUNT = 3;
	
    // Fragment position
    public static final int FRAGMENT_POS_TIMELINE = 0;
    public static final int FRAGMENT_POS_GRAPH = 1;
    public static final int FRAGMENT_POS_SETTINGS = 2;
    
	public static final String ARG_SECTION_NUMBER = "section_number";
    
    // System
    private Context mContext = null;
    private Handler mHandler = null;
    private IFragmentListener mFragmentListener = null;
    
    private Fragment mTimelineFragment = null;
    private Fragment mGraphFragment = null;
    private Fragment mLLSettingsFragment = null;
    
    public LLFragmentAdapter(FragmentManager fm, Context c, IFragmentListener l, Handler h) {
		super(fm);
		mContext = c;
		mFragmentListener = l;
		mHandler = h;
	}
    
	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		Fragment fragment;
		//boolean needToSetArguments = false;
		
		if(position == FRAGMENT_POS_TIMELINE) {
			if(mTimelineFragment == null) {
				mTimelineFragment = new TimelineFragment(mContext, mFragmentListener, mHandler);
				//needToSetArguments = true;
			}
			fragment = mTimelineFragment;

		} else if(position == FRAGMENT_POS_GRAPH) {
			if(mGraphFragment == null) {
				mGraphFragment = new GraphFragment(mContext, mFragmentListener);
				//needToSetArguments = true;
			}
			fragment = mGraphFragment;
			
		} else if(position == FRAGMENT_POS_SETTINGS) {
			if(mLLSettingsFragment == null) {
				mLLSettingsFragment = new LLSettingsFragment(mContext, mFragmentListener);
				//needToSetArguments = true;
			}
			fragment = mLLSettingsFragment;
			
		} else {
			fragment = null;
		}
		
		// TODO: If you have something to notify to the fragment.
		/*
		if(needToSetArguments) {
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
		}
		*/
		
		return fragment;
	}

	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case FRAGMENT_POS_TIMELINE:
			return mContext.getString(R.string.title_timeline).toUpperCase(l);
		case FRAGMENT_POS_GRAPH:
			return mContext.getString(R.string.title_graph).toUpperCase(l);
		case FRAGMENT_POS_SETTINGS:
			return mContext.getString(R.string.title_ll_settings).toUpperCase(l);
		}
		return null;
	}
    
    
}
