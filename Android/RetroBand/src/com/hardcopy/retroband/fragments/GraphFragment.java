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

import com.hardcopy.retroband.R;
import com.hardcopy.retroband.R.id;
import com.hardcopy.retroband.R.layout;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GraphFragment extends Fragment implements IAdapterListener {

	private static final float MAX_ACCEL_VALUE = 32768;
	private Context mContext = null;
	private IFragmentListener mFragmentListener = null;		// for future use
	
	private RenderingView mRenderAccel;

	
	
	public GraphFragment() {
	}
	
	public GraphFragment(Context c, IFragmentListener l) {
		mContext = c;
		mFragmentListener = l;
	}
	
	/*****************************************************
	 *	Overrided methods
	 ******************************************************/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		
		mRenderAccel = (RenderingView)rootView.findViewById(R.id.render_accel);
		
		return rootView;
	}
	
	@Override
	public void OnAdapterCallback(int msgType, int arg0, int arg1, String arg2, String arg3, Object arg4) {
		switch(msgType) {
		case IAdapterListener.CALLBACK_xxx:
			// TODO: Send data to activity
			//if(arg4 != null)
			//	mFragmentListener.OnFragmentCallback(IFragmentListener.CALLBACK_REQUEST_ADD_FILTER, 0, 0, null, null, arg4);
			break;
		}
	}
	
	/*****************************************************
	 *	Private methods
	 ******************************************************/
	/**
	 * Check if rendering view is initialized.
	 * If not, initialize with max value
	 * @return	boolean		is initialized or already initialized
	 */
	private boolean checkRenderView() {
		// Initialize rendering view
		if(mRenderAccel != null) {
			if(!mRenderAccel.getInitializationFlag()) {
				// Initialize with the value range from bottom to top
				mRenderAccel.initializeGraphics(MAX_ACCEL_VALUE);
			}
			return true;
		}
		
		return false;
	}
	
	
	/*****************************************************
	 *	Public methods
	 ******************************************************/
	/**
	 * Draw graph
	 * @param accelArray	accel data to draw.
	 */
	public void drawAccelData(int[] accelArray) {
		if(!checkRenderView())
			return;
		
		// Draw graph with array of data
		mRenderAccel.drawAccelGraph(accelArray);
		mRenderAccel.invalidate();
	}
	
	
	
}
