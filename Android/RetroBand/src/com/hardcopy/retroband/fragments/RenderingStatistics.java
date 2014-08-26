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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.hardcopy.retroband.contents.ContentManager;

public class RenderingStatistics extends View {
	
	private static final String tag = "RenderingStatistics";
	
	private static final int TYPE_GREEN = 1;
	private static final int TYPE_BLUE = 2;
	private static final int TYPE_RED = 3;
	
	private static final int MARGIN_LEFT = 10;
	private static final int MARGIN_RIGHT = 10;
	private static final int MARGIN_TOP = 35;
	private static final int MARGIN_BOTTOM = 35;
	
	
	private Context mContext;
	
	private boolean mIsInitialized = false;
	
	private int mViewW = 0;
	private int mViewH = 0;
	
	private float mMaxValue = 0;
	private float mScale = 0;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mPaint;
	/*
	private Path    mPath;
	private MaskFilter  mEmboss;
	private MaskFilter  mBlur;
	*/
	
	
	
	/*****************************************************
	*		Initialization methods
	******************************************************/
	/*
	 * Use this when you make rendering view from source code
	 */
	public RenderingStatistics(Context context) {
		super(context);
		mContext = context;
	}
	
	/*
	 * When you defined rendering view in xml layout file
	 */
	public RenderingStatistics(Context context,AttributeSet attrs) {
		super(context,attrs);
	}

	public RenderingStatistics(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);
	}

	
	
	/*****************************************************
	*		Override methods
	******************************************************/
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(mBitmap != null)
			canvas.drawBitmap(mBitmap, 0, 0, null);
	}
	
	@Override
	protected void onFinishInflate() {
		setClickable(true);
	}

	
	/*****************************************************
	*		Private methods
	******************************************************/

	
	
	/*****************************************************
	*		Public methods
	******************************************************/
	/**
	 * Initialize rendering view.
	 * @param max_val	Max value range from bottom to top
	 */
	public void initializeGraphics(float max_val) {
		if(mIsInitialized)
			return;
		
		// Get screen features
		mViewW = this.getWidth();
		mViewH = this.getHeight();
		
		// Initialize graphics
		mBitmap = Bitmap.createBitmap(mViewW, mViewH, Bitmap.Config.ARGB_8888);
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		
		// TODO: If you want additional effect, use below code.
		//mPaint.setDither(true);
		//mPaint.setColor(0xFFFF0000);
		//mPaint.setStyle(Paint.Style.STROKE);
		//mPaint.setStrokeJoin(Paint.Join.ROUND);
		//mPaint.setStrokeCap(Paint.Cap.ROUND);
		//mPaint.setStrokeWidth(12);
		// mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
		// mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
		
		mMaxValue = max_val;
		
		// Make canvas
		mCanvas = new Canvas(mBitmap);
		mIsInitialized = true;
	}
	
	/**
	 * Draw graph with accel data array
	 * @param type			use REPORT_TYPE_MONTH or REPORT_TYPE_DAY or REPORT_TYPE_HOUR.
	 * @param dataArray		accel value array
	 */
	public void drawGraph(int type, int[] dataArray) {
		if(dataArray == null || dataArray.length < 1)
			return;
		
		// Calculate drawing parameters
		int columnSize = 0;
		float scaleH = 0f;
		float maxH = 0;
		columnSize = ((mViewW - MARGIN_LEFT - MARGIN_RIGHT) / dataArray.length);
		
		// find max value
		if(mMaxValue > 0) {
			maxH = mMaxValue;
			if(mScale <= 0) {
				mScale = (float)(mViewH - MARGIN_TOP - MARGIN_BOTTOM) / maxH;	// Calculate height scale value
			}
			scaleH = mScale;
		} 
		// Initialize with default settings
		else {
			for(int i=0; i<dataArray.length; i++) {
				if(dataArray[i] > maxH)
					maxH = dataArray[i];
			}
			maxH = (((int)maxH)/1000 + 1)*1000;
			scaleH = (float)(mViewH - MARGIN_TOP - MARGIN_BOTTOM) / maxH;	// Calculate height scale value
		}
		
		// Erase canvas
		if(mBitmap != null && mCanvas != null)
			mCanvas.drawColor(Color.WHITE);
		else
			return;
		
		// Draw guideline
		int startNum = 1000;
		int increaseNum = 1000;
		if(maxH <= 5000) {
			startNum = 1000;
			increaseNum = 1000;
		} else if(maxH <= 10000) {
			startNum = 5000;
			increaseNum = 5000;
		} else if(maxH <= 50000) {
			startNum = 10000;
			increaseNum = 10000;
		} else if(maxH <= 100000) {
			startNum = 50000;
			increaseNum = 50000;
		} else {
			startNum = 100000;
			increaseNum = 100000;
		}
		
		mPaint.setColor(0xFF333333);	// Dark gray
		mPaint.setTextSize(18);
		for(int i=startNum; i<=maxH; i+=increaseNum) {
			float y_pos = mViewH - MARGIN_BOTTOM - i*scaleH;
			// Draw guide line
			mCanvas.drawLine(MARGIN_LEFT, y_pos, mViewW, y_pos, mPaint);
			// Draw unit size string
			mCanvas.drawText(Integer.toString(i/1000)+"k", 1, y_pos, mPaint);
		}
		
		// Draw each value
		int startPointX = MARGIN_LEFT;
		for(int i=0; i<dataArray.length; i++) {
			// Draw rect
			mPaint.setColor(0xFF0000CC);	// Blue
			if(dataArray[i] > 0) {
				mCanvas.drawRect(startPointX + 2,		// Left 
						mViewH - MARGIN_BOTTOM - dataArray[i]*scaleH,	// Top
						startPointX + columnSize - 2, 	// Right
						mViewH - MARGIN_BOTTOM, 	// Bottom
						mPaint);
			}
			
			// Draw column string
			mPaint.setColor(0xFF333333);	// Dark gray
			mPaint.setTextSize(24);
			int guideNum = i;
			if(type != ContentManager.REPORT_TYPE_HOUR)
				guideNum += 1;
			mCanvas.drawText(Integer.toString(guideNum), startPointX + columnSize/5, mViewH - 8, mPaint);
			
			// Increase draw point X
			startPointX += columnSize;
		}
		
	}
	
	
	/*****************************************************
	*		Sub classes, Handler, Listener
	******************************************************/

	
}
