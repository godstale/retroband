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

package com.hardcopy.retroband.bluetooth;

import java.util.ArrayList;
import com.hardcopy.retroband.contents.ContentObject;
import android.os.Handler;

/**
 * Parse stream and extract accel data
 * @author Administrator
 */
public class TransactionReceiver {
	private static final String TAG = "TransactionReceiver";
	
	private static final int PARSE_MODE_ERROR = 0;
	private static final int PARSE_MODE_WAIT_START_BYTE = 1;
	private static final int PARSE_MODE_WAIT_COMMAND = 2;
	private static final int PARSE_MODE_WAIT_DATA = 3;
	private static final int PARSE_MODE_WAIT_END_BYTE = 4;
	private static final int PARSE_MODE_COMPLETED = 101;
	
	private Handler mHandler = null;
	private ArrayList<ContentObject> mObjectQueue = new ArrayList<ContentObject>();
	
	private int mParseMode = PARSE_MODE_WAIT_START_BYTE;
	private ContentObject mContentObject = null;
	
	
	
	public TransactionReceiver(Handler h) {
		mHandler = h;
		reset();
	}
	
	
	/**
	 * Reset transaction receiver.
	 */
	public void reset() {
		mParseMode = PARSE_MODE_WAIT_START_BYTE;
		mCacheStart = 0x00;
		mCacheEnd = 0x00;
		mCacheData = 0x00;
		mCached = false;
	}
	
	/**
	 * Set bytes to parse
	 * @param buffer	
	 * @param count
	 */
	public void setByteArray(byte[] buffer, int count) {
		parseStream(buffer, count);
	}
	
	/**
	 * After parsing bytes received, Transaction receiver makes parsed object.
	 * This method returns parsed object
	 * @return	ContentObject		parsed object
	 */
	public ContentObject getObject() {
		ContentObject object = null;
		if(mObjectQueue != null && mObjectQueue.size() > 0)
			object = mObjectQueue.remove(0);
		
		return object;
	}

	// Temporary variables for parsing
	private byte mCacheStart = 0x00;
	private byte mCacheEnd = 0x00;
	private int mCacheData = 0x00;
	private boolean mCached = false;
	
	/**
	 * Caching received stream.
	 * And parse byte array to make content object
	 * @param buffer		byte array to parse
	 * @param count			byte array size
	 */
	public void parseStream(byte[] buffer, int count) {
		if(buffer != null && buffer.length > 0 && count > 0) {
			for(int i=0; i < buffer.length && i < count; i++) {
				
				// Parse received data
				// Protocol description -----------------------------------------------------------
				// [*] Accel data
				// 		[Start byte: 2byte]
				//		[Data: 6byte: 3 integer data]... 
				//		[End byte: 2byte]
				
				switch(mParseMode) {
				
				case PARSE_MODE_WAIT_START_BYTE:
					if(buffer[i] == Transaction.TRANSACTION_START_BYTE_2
							&& mCacheStart == Transaction.TRANSACTION_START_BYTE) {
						//Logs.d("Read data: TRANSACTION_START_BYTE");
						mParseMode = PARSE_MODE_WAIT_DATA;
						if(mContentObject == null) {
							mContentObject = new ContentObject(ContentObject.CONTENT_TYPE_ACCEL, -1, 0);
							mContentObject.mTimeInMilli = System.currentTimeMillis();
						}
					} else {
						mCacheStart = buffer[i];
					}
					break;
				/* Disabled: 
				case PARSE_MODE_WAIT_COMMAND:
					Logs.d("Read data: PARSE_MODE_WAIT_COMMAND = " + String.format("%02X ", buffer[i]));
					switch(buffer[i]) {
					case Transaction.COMMAND_TYPE_PING:
						mParseMode = PARSE_MODE_WAIT_END_BYTE;
						break;
						
					case Transaction.COMMAND_TYPE_ACCEL_DATA:
						mParseMode = PARSE_MODE_WAIT_DATA;
						break;

					default:
						mParseMode = PARSE_MODE_WAIT_START_BYTE;
						break;
					}	// End of switch()
					break;
				*/
				case PARSE_MODE_WAIT_DATA:
					/*
					 * TODO: Check end byte (sometimes data byte is same with end byte)
					 * 
					if(buffer[i] == Transaction.TRANSACTION_END_BYTE
							|| buffer[i] == Transaction.TRANSACTION_END_BYTE_2) {
						if(buffer[i] == Transaction.TRANSACTION_END_BYTE_2
								&& mCacheEnd == Transaction.TRANSACTION_END_BYTE) {
							Logs.d("Read data: TRANSACTION_END_BYTE");
							mParseMode = PARSE_MODE_COMPLETED;
							break;
						} else {
							mCacheEnd = buffer[i];
						}
					}
					*/
					
					// Forced to fill 20 accel data
					if(mContentObject != null && mContentObject.mAccelIndex > ContentObject.DATA_COUNT - 1) {
						//Logs.d("Read data: TRANSACTION_END_BYTE");
						mParseMode = PARSE_MODE_COMPLETED;
						break;
					}
					
					// Remote device(Arduino) uses 2-byte integer.
					// We must cache 2byte to make single value
					if(mCached) {
						int tempData = 0x00000000;
						int tempData2 = 0x00000000;
						boolean isNegative = false;
						
						if(mCacheData == 0x0000007f)	// Recover first byte (To avoid null byte, 0x00 was converted to 0x7f)
							mCacheData = 0x00000000;
						if( (mCacheData & 0x00000080) == 0x00000080 )	// Check first bit which is 'sign' bit
							isNegative = true;
						if(buffer[i] == 0x01) 	// Recover second byte (To avoid null byte, 0x00 was converted to 0x01)
							buffer[i] = 0x00;
						
						tempData2 |= (buffer[i] & 0x000000ff);
						tempData = (((mCacheData << 8) | tempData2) & 0x00007FFF);
						
						//Logs.d(String.format("%02X ", mCacheData) + String.format("%02X ", tempData2) + String.format("%02X ", tempData));
						
						// negative number uses 2's complement math. Set first 9 bits as 1.
						if(isNegative)
							tempData = (tempData | 0xFFFF8000);
						
						// Recovered integer value. Remember this value.
						if(mContentObject != null) {
							mContentObject.setAccelData(tempData);
						}
						mCacheData = 0x00000000;
						mCached = false;
					} else {
						mCacheData |= (buffer[i] & 0x000000ff);		// Remember first byte
						mCached = true;
					}
					break;
					
				}	// End of switch()
				
				if(mParseMode == PARSE_MODE_COMPLETED) {
					pushObject();
					reset();
				}
			}	// End of for loop
		}	// End of if()
	}
	
	/**
	 * Push new object to queue
	 */
	private void pushObject() {
		if(mContentObject != null) {
			//Logs.d("ContentObject created: time="+mContentObject.mTimeInMilli);
			mObjectQueue.add(mContentObject);
			mContentObject = null;
		}
	}
	
	/**
	 * Defines transaction constants
	 */
	public class Transaction {
		private static final byte TRANSACTION_START_BYTE = (byte)0xfe;
		private static final byte TRANSACTION_START_BYTE_2 = (byte)0xfd;
		private static final byte TRANSACTION_END_BYTE = (byte)0xfd;
		private static final byte TRANSACTION_END_BYTE_2 = (byte)0xfe;
		
		public static final int COMMAND_TYPE_NONE = 0x00;
		public static final int COMMAND_TYPE_PING = 0x01;
		public static final int COMMAND_TYPE_ACCEL_DATA = 0x02;
	}
	
}
