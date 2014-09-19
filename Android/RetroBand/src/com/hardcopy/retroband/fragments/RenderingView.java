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


public class RenderingView extends View {
	
	private static final String tag = "RenderingView";
	
	private static final int TYPE_GREEN = 1;
	private static final int TYPE_BLUE = 2;
	private static final int TYPE_RED = 3;
	
	private Context mContext;
	private boolean mIsInitialized = false;
	
	private int mViewW = 0;
	private int mViewH = 0;

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Paint mPaint;
	/* TODO: Use below for enhanced effect
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
	public RenderingView(Context context) {
		super(context);
		mContext = context;
	}
	
	/*
	 * When you defined rendering view in xml layout file
	 */
	public RenderingView(Context context,AttributeSet attrs) {
		super(context,attrs);
	}

	public RenderingView(Context context,AttributeSet attrs,int defStyle) {
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
	private int mCurrentDrawingX = 1 + POINT_WIDTH_HALF;	// current drawing position
	
	private static final int POINT_WIDTH = 5;		// must be odd number.
	private static final int POINT_THICKNESS = 5;	// must be odd number.
	private static final int POINT_WIDTH_HALF = 2;
	private static final int POINT_THICKNESS_HALF = 2;
	
	private static final int GRID_UNIT_SIZE = 5; 	// how many points are included in one grid unit
	
	private float mMaxValue = 0;
	private float mVerticalScale = 1;		// Scale value to fit screen height
	
	/**
	 * Clear canvas. Initialize drawing point X
	 */
	private void resetGraphics() {
		mCanvas.drawColor(Color.WHITE);
		mCurrentDrawingX = 1 + POINT_WIDTH_HALF;
	}
	
	/**
	 * Use this function when your drawing point X touched right end.
	 * Copies right rect area to left end and recalculates drawing point X
	 * @return	New drawing point X
	 */
	private int moveTimeLine() {
		int howManyPointInScreen = mViewW / POINT_WIDTH;
		int cutPoint = howManyPointInScreen / GRID_UNIT_SIZE / 3;		// Must be multiple of GRID_UNIT_SIZE. Cut 1/n of original image
		int cutPointX = mCurrentDrawingX - POINT_WIDTH - cutPoint*GRID_UNIT_SIZE*POINT_WIDTH;
		int cutWidth = mBitmap.getWidth() - cutPointX;

		if(cutPointX <= 0 || cutPointX + cutWidth >= mBitmap.getWidth()) {
			mCanvas.drawColor(Color.WHITE);
			
			// Draw guide line
			mPaint.setColor(0xFFb1b1b1);
			mCanvas.drawLine(0, mViewH/2, 
					mViewW, mViewH/2, 
					mPaint);
			
			mCurrentDrawingX = 0;
			return 0;
		}
		
		// Cut recent area from canvas
		Bitmap bCut  = Bitmap.createBitmap(mBitmap, 
				cutPointX, 0, 
				cutWidth, mViewH);
		
		mCanvas.drawColor(Color.WHITE);
		mCanvas.drawBitmap(bCut, 0, 0, null);	// Paste image cut to left 
		
		// Draw guide line
		mPaint.setColor(0xFFb1b1b1);
		mCanvas.drawLine(cutPoint*GRID_UNIT_SIZE*POINT_WIDTH, mViewH/2, 
				mViewW, mViewH/2, 
				mPaint);
		
		//kbjung
		mCurrentDrawingX = 1 + cutPoint*GRID_UNIT_SIZE*POINT_WIDTH;
		bCut = null;
		
		return cutPoint*GRID_UNIT_SIZE*POINT_WIDTH;
	}
	
	@Deprecated
	private void drawRawData(double[] rawData) {
		if(rawData == null) 
			return;
		
		mCanvas.drawColor(Color.WHITE);
		
		double scale = 1f / mViewH;
		
		mPaint.setColor(0xFF777777);
		for(int i=2; i < rawData.length && i < mViewW; i+=2) {
			mCanvas.drawLine(i, mViewH, i, mViewH - (int)(rawData[i]/scale), mPaint);
		}
	}
	
	/**
	 * Draw a point on canvas
	 * @param color		color type
	 * @param point_x	point x
	 * @param value		point y
	 */
	private void drawPoint(int color, int point_x, float value) {
		
		switch(color) {
			case TYPE_GREEN:
				mPaint.setColor(0xFFFF0000);	// Green
				break;
			case TYPE_BLUE:
				mPaint.setColor(0xFF0000FF);	// Blue
				break;
			case TYPE_RED:
				mPaint.setColor(0xFF00CC00);	// Red
				break;
		}
		
		float height = mViewH - value*mVerticalScale;
		if(POINT_THICKNESS == 1) {
			mCanvas.drawLine(point_x - POINT_WIDTH_HALF, height, 
					point_x + POINT_WIDTH_HALF, height, 
					mPaint);
		} else {
			mCanvas.drawRect(point_x - POINT_WIDTH_HALF, height + POINT_THICKNESS_HALF, 
					point_x + POINT_WIDTH_HALF, height - POINT_THICKNESS_HALF, 
					mPaint);
		}
	}
	
	/**
	 * Draw line
	 * @param color		color type
	 * @param point_x1	starting point x
	 * @param point_x2	end point x2
	 * @param value1	starting point y
	 * @param value2	end point y2
	 */
	private void drawLine(int color, int point_x1, int point_x2, float value1, float value2) {
		
		switch(color) {
			case TYPE_GREEN:
				mPaint.setColor(0xFFFF0000);	// Green
				break;
			case TYPE_BLUE:
				mPaint.setColor(0xFF0000FF);	// Blue
				break;
			case TYPE_RED:
				mPaint.setColor(0xFF00CC00);	// Red
				break;
		}
		
		float height1 = mViewH - value1*mVerticalScale;
		float height2 = mViewH - value2*mVerticalScale;
		mCanvas.drawLine(point_x1, height1, 
				point_x2, height2, 
				mPaint);
	}
	
	
	/*****************************************************
	*		Public methods
	******************************************************/
	/**
	 * Initialize graphics
	 * @param max_val	Max value range from bottom to top
	 */
	public void initializeGraphics(float max_val) {
		mViewW = this.getWidth();
		mViewH = this.getHeight();
		if(max_val <= 0) {
			mMaxValue = mViewH/2;
			mVerticalScale = 1;
		} else {
			mMaxValue = max_val;
			mVerticalScale = (float)(mViewH)/(2f*max_val);
		}
		
		// Initialize graphics
		mBitmap = Bitmap.createBitmap(mViewW, mViewH, Bitmap.Config.ARGB_8888);
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		// TODO: for enhanced effects
		//mPaint.setDither(true);
		//mPaint.setColor(0xFFFF0000);
		//mPaint.setStyle(Paint.Style.STROKE);
		//mPaint.setStrokeJoin(Paint.Join.ROUND);
		//mPaint.setStrokeCap(Paint.Cap.ROUND);
		//mPaint.setStrokeWidth(12);
		// mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
		// mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
		
		mCanvas = new Canvas(mBitmap);
		
		// Draw guide line
		mPaint.setColor(0xFFb1b1b1);
		mCanvas.drawLine(0, mViewH/2, 
				mViewW, mViewH/2, 
				mPaint);
		
		mIsInitialized = true;
	}
	
	/**
	 * Is this rendering view initialized or not
	 * @return	boolean		initialized or not
	 */
	public boolean getInitializationFlag() {
		return mIsInitialized;
	}
	
	/**
	 * Remember drawing bitmap
	 * @param bitmap	bitmap
	 */
	public void setBitmap(Bitmap bitmap){
		mBitmap = bitmap;
	}
	
	static boolean bStart = true;
	static int PrevDrawingX;
	
	/**
	 * Draw accel data on canvas
	 * @param accel		accel data array
	 */
	public void drawAccelGraph(int[] accel) {
		if(accel == null || accel.length < 3)
			return;
		
		if(bStart == true) {
			PrevDrawingX = mCurrentDrawingX;
			bStart = false;
			return;
		}

		for(int i=3; i<accel.length; i+=3) {
			// x axis value is Red dot
			drawPoint(TYPE_RED, mCurrentDrawingX, accel[i] + mMaxValue);
			drawLine(TYPE_RED, PrevDrawingX, mCurrentDrawingX, accel[i-3] + mMaxValue, accel[i] + mMaxValue);
			
			// y axis value is Blue dot
			drawPoint(TYPE_GREEN, mCurrentDrawingX, accel[i+1] + mMaxValue);
			drawLine(TYPE_GREEN, PrevDrawingX, mCurrentDrawingX, accel[i-2] + mMaxValue, accel[i+1] + mMaxValue);
			
			// z axis value is Green dot
			drawPoint(TYPE_BLUE, mCurrentDrawingX, accel[i+2] + mMaxValue);
			drawLine(TYPE_BLUE, PrevDrawingX, mCurrentDrawingX, accel[i-1] + mMaxValue, accel[i+2] + mMaxValue);
			if( mCurrentDrawingX + POINT_WIDTH_HALF >= mViewW ) {
				PrevDrawingX = moveTimeLine();
			} else {
				PrevDrawingX = mCurrentDrawingX;
				mCurrentDrawingX += POINT_WIDTH*2;
			}
		}
		
	}
	
	
	/*****************************************************
	*		Sub classes, Handler, Listener
	******************************************************/

	
}


