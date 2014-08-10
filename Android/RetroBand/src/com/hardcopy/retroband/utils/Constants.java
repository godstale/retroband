package com.hardcopy.retroband.utils;

public class Constants {

	// Service handler message key
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_NAME = "device_name";
	public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS = "device_address";
	public static final String SERVICE_HANDLER_MSG_KEY_TOAST = "toast";
    
    // Preference
	public static final String PREFERENCE_NAME = "RetroBandPref";
	public static final String PREFERENCE_KEY_BG_SERVICE = "BackgroundService";
	public static final String PREFERENCE_KEY_WEIGHT = "Weight";
	public static final String PREFERENCE_CONN_INFO_ADDRESS = "device_address";
	public static final String PREFERENCE_CONN_INFO_NAME = "device_name";
	
    // Message types sent from Service to Activity
    public static final int MESSAGE_CMD_ERROR_NOT_CONNECTED = -50;
    
    public static final int MESSAGE_BT_STATE_INITIALIZED = 1;
    public static final int MESSAGE_BT_STATE_LISTENING = 2;
    public static final int MESSAGE_BT_STATE_CONNECTING = 3;
    public static final int MESSAGE_BT_STATE_CONNECTED = 4;
    public static final int MESSAGE_BT_STATE_ERROR = 10;
    
    public static final int MESSAGE_ADD_NOTIFICATION = 101;
    public static final int MESSAGE_DELETE_NOTIFICATION = 105;
    public static final int MESSAGE_GMAIL_UPDATED = 111;
    public static final int MESSAGE_SMS_RECEIVED = 121;
    public static final int MESSAGE_CALL_STATE_RECEIVED = 131;
    public static final int MESSAGE_RF_STATE_RECEIVED = 141;
    public static final int MESSAGE_FEED_UPDATED = 151;
    
    public static final int MESSAGE_READ_ACCEL_DATA = 201;
    public static final int MESSAGE_READ_ACCEL_REPORT = 211;
    

	
	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	
}
