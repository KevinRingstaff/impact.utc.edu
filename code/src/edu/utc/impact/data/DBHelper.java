package edu.utc.impact.data;

import edu.utc.impact.utils.KVector;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.util.Log;
import android.os.Build;
import android.os.Environment;



/**
 * This database helper class sets up all the methods required to create, populate, query, and modify the database.
 * 
 * Modifications:
 * 
 * 		7/24/2012	Kevin Ringstaff
 * 					DB_VERSION = 1
 * 					Initial Version
 *
 */
public class DBHelper extends SQLiteOpenHelper
{
	private static final boolean debug = true;
	private static final String TAG = "FallDetection.DBHelper";
	private static final String CREATE_TAG = "FallDetection.DBHelper.Create";
	private static final String DATA_TAG = "FallDetection.DBHelper.Data";
	
	/**
	 * This variable holds the current version of the database. If this is different than the installed DB, onUpgrade() is called.
	 * 
	 * WARNING: Updating the DB version will erase all the data in the current DB. 
	 */
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "falldectection_";
	public static final String DB_PATH = "/data/falldetection.hig.no/databases/" + DB_NAME;
	public static final String DB_SD_DIR = "/FallDetection";
	public static final String DB_SD_DB_FILE_NAME = "falldetection_data.db";
	
	//user table data
	private static final String USER_TABLE_NAME = DB_NAME + "user";							//Name of the User Table
	private static final String USER_ID = "uID";											//The is the User Row ID index 
	private static final String USER_NAME = "uName";										//This is the name of the user
	private static final String USER_AGE = "uAge";											//This is the age of the user
	private static final String USER_HEIGHT = "uHeight";									//This is the height of the user, measured in decimal format
	private static final String USER_WEIGHT = "uWeight";									//This is the weight of the user
	private static final String USER_SEX = "uSex";											//This is the sex of the user. M or F
	private static final String USER_ACTIVITY_LEVEL = "uActivityLevel";						//This is the average activity level of the user. Values are: L, M, H for low, medium or high activity levels respectfully
	private static final String USER_EMAIL = "uEmail";										//RESERVED FOR FUTURE USE. This is the user's email address
	private static final String USER_ALARM_NUMBER = "uAlarmNumber";							//RESERVED FOR FUTURE USE. This is the number that the phone will call when a fall is detected
	//position table data
	private static final String POSITION_TABLE_NAME = DB_NAME + "position_code";			//Name of the Position Table
	private static final String POSITION_ID = "pID";
	private static final String POSITION_NAME = "pName";
	private static final String POSITION_DESCRIPTION = "pDescription";
	//adl table data
	private static final String ADL_TABLE_NAME = DB_NAME + "adl_code";						//Name of the ADL Table
	private static final String ADL_ID = "adlID";
	private static final String ADL_TYPE = "adlType";
	private static final String ADL_NAME = "adlName";
	private static final String ADL_DESCRIPTION = "adlDescription";
	//data event table data
	private static final String DATA_EVENT_TABLE_NAME = DB_NAME + "data_event";				//Name of the Data Event Table
	private static final String DATA_EVENT_ID = "deID";
	private static final String DATA_EVENT_PSTART = "dePosStart";
	private static final String DATA_EVENT_PEND = "dePosEnd";
	private static final String DATA_EVENT_ADL_CLASSIFY = "deClassify";
	private static final String DATA_EVENT_ADL_ACTUAL = "deActual";
	private static final String DATA_EVENT_TYPE_A_DETECTED = "de_type_a_detected";
	private static final String DATA_EVENT_TYPE_B_DETECTED = "de_type_b_detected";
	private static final String DATA_EVENT_TYPE_AB_DETECTED = "de_type_ab_detected";
	private static final String DATA_EVENT_TYPE_C_DETECTED = "de_type_c_detected";
	private static final String DATA_EVENT_FALL_DETECTED = "de_fall_detected";
	private static final String DATA_EVENT_INACTIVITY_DETECTED = "de_inactivity_detected";
	private static final String DATA_EVENT_UFT_EXCEEDED = "de_uft_exceeded";
	private static final String DATA_EVENT_LFT_EXCEEDED = "de_lft_exceeded";
	//data event results table data
	private static final String DATA_EVENT_RESULTS_TABLE_NAME = "de_data_event_results";
	private static final String DATA_EVENT_RESULTS_ID = "de_data_event_results_id";
	//data item table data
	private static final String DATA_ITEM_TABLE_NAME = DB_NAME + "data_item";				//Name of the Data Item Table
	private static final String DATA_ITEM_ID = "diID";
	private static final String DATA_ITEM_TIMESTAMP = "diTimestamp";
	private static final String DATA_ITEM_ACCURACY = "diAccuracy";
	private static final String DATA_ITEM_X = "diX";
	private static final String DATA_ITEM_Y = "diY";
	private static final String DATA_ITEM_Z = "diZ";
	private static final String DATA_ITEM_RSS = "diRSS";
	//data event features table data
	private static final String DATA_EVENT_FEATURES_TABLE_NAME = DB_NAME + "de_features";	//Name of the Data Event Features Table
	private static final String DATA_EVENT_FEATURES_ID = "defID";
	private static final String DATA_EVENT_FEATURES_VALUE = "defValue";
	private static final String DATA_EVENT_FEATURES_TIMESTAMP = "defTimestamp";
	//feature table data
	private static final String FEATURE_TABLE_NAME = DB_NAME + "feature";
	private static final String FEATURE_ID = "fID"; 
	private static final String FEATURE_NAME = "fName";
	private static final String FEATURE_VERSION = "fVersion";
	private static final String FEATURE_DESCRIPTION = "fDescription";
	private static final String FEATURE_VALUE = "fValue";
	private static final String FEATURE_DURATION = "fDuration";
	private static final String FEATURE_LINK = "fLink";
	//device table data
	private static final String DEVICE_TABLE_NAME = DB_NAME + "device";						//Name of the Device Table
	private static final String DEVICE_ID = "dID";
	private static final String DEVICE_NAME = "dName"; //DEVICE_PRODUCT
	private static final String DEVICE_MODEL = "dModel";
	private static final String DEVICE_SERIAL = "dSerial";
	private static final String DEVICE_FINGERPRINT = "dFingerprint";
	private static final String DEVICE_TYPE = "dType";
	private static final String DEVICE_BOARD = "dBoard";
	private static final String DEVICE_BOOTLOADER = "dBootLoader";
	private static final String DEVICE_BRAND = "dBrand";
	private static final String DEVICE_DEVICE = "dDevice";
	private static final String DEVICE_DISPLAY = "dDisplay";
	private static final String DEVICE_HARDWARE = "dHardware";
	//sensor table data
	private static final String SENSOR_TABLE_NAME = DB_NAME + "sensor";						//Name of the Sensor Table
	private static final String SENSOR_ID = "mID";
	private static final String SENSOR_NAME = "mName";
	private static final String SENSOR_TYPE = "mType";
	private static final String SENSOR_VENDOR = "mVendor";
	private static final String SENSOR_VERSION = "mVersion";
	private static final String SENSOR_MAX_RANGE = "mMaxRange";
	private static final String SENSOR_MIN_DELAY = "mMinDelay";
	private static final String SENSOR_POWER = "mPower";
	
	//these variables are used with the position table to determine if the service is still running / ended correctly 
	public static final String DATA_EVENT_STOPPED = "DE0";									//The data event is stopped, but no ending data has been recorded.
	public static final String DATA_EVENT_RUNNING = "DE1";									//The data event is running.
	public static final String DATA_EVENT_PAUSED = "DE2";									//The data event is paused.
	public static final String DATA_EVENT_ERROR = "DEE";									//The data event exited with an error code.
	public static final String DATA_EVENT_ADL_NAN = "NAN";									//This is when the ADL of the data event is already known or not in use.
	
	public DBHelper (Context context) 
	{
	    super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 *  Called when the database is created for the first time. 
	 *  This is where the creation of tables and the initial population of the tables should happen.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		//TODO: Create indexes on each table for searching
		Log.i(CREATE_TAG, "onCreate() -> START");
		
		//
		//Create the User Table
		String createUserTable = "CREATE TABLE " + USER_TABLE_NAME + " (" + 
				USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DEVICE_ID + " INTEGER NOT NULL, " +
				POSITION_ID + " VARCHAR(3) NULL, " + 
				USER_NAME + " VARCHAR(45), " + 
				USER_AGE + " INTEGER NULL, " +
				USER_HEIGHT + " FLOAT NULL, " +
				USER_WEIGHT + " FLOAT NULL, " +
				USER_SEX + " CHAR NULL, " + 
				USER_ACTIVITY_LEVEL + " CHAR NULL, " +
				USER_EMAIL + " VARCHAR(45) NULL, " + 
				USER_ALARM_NUMBER + " INTEGER NULL, " + 
				"FOREIGN KEY (" + DEVICE_ID + ") REFERENCES " + DEVICE_TABLE_NAME + "(" + DEVICE_ID + "), " + 
				"FOREIGN KEY (" + POSITION_ID + ") REFERENCES " + POSITION_TABLE_NAME + "(" + POSITION_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createUserTable);
		db.execSQL(createUserTable);
		
		//
		//Create the PositionCode Table 
		String createPositionTable = "CREATE TABLE " + POSITION_TABLE_NAME + " (" +
				POSITION_ID + " VARCHAR(3) PRIMARY KEY NOT NULL UNIQUE, " +
				POSITION_NAME + " VARCHAR(45) NOT NULL, " + 
				POSITION_DESCRIPTION + " VARCHAR(45) NULL)"; 
		Log.i(CREATE_TAG, "onCreate() -> " + createPositionTable);
		db.execSQL(createPositionTable);
		
		//
		//Create the ADL Code Table
		String createADLTable = "CREATE TABLE " + ADL_TABLE_NAME + " (" +
				ADL_ID + " VARCHAR(4) PRIMARY KEY NOT NULL UNIQUE, " +
				ADL_TYPE + " INTEGER NOT NULL, " + 
				ADL_NAME + " VARCHAR(45) NOT NULL, " + 
				ADL_DESCRIPTION + " VARCHAR(100) NULL)"; 
		Log.i(CREATE_TAG, "onCreate() -> " + createADLTable);
		db.execSQL(createADLTable);
		
		//
		//Create the Data Event Table
		String createDataEventTable = "CREATE TABLE " + DATA_EVENT_TABLE_NAME + " (" +
				DATA_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				USER_ID + " INTEGER NOT NULL, " +
				DATA_EVENT_PSTART + " VARCHAR(3) NULL, " +
				DATA_EVENT_PEND + " VARCHAR(3) NULL, " +
				DATA_EVENT_ADL_ACTUAL + " VARCHAR(3) NULL, " +
				DATA_EVENT_ADL_CLASSIFY + " VARCHAR(3) NULL, " +
				DATA_EVENT_TYPE_A_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_B_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_AB_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_C_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_FALL_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_INACTIVITY_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_UFT_EXCEEDED + " BOOLEAN NOT NULL DEFAULT FALSE, " +
				DATA_EVENT_LFT_EXCEEDED + " BOOLEAN NOT NULL DEFAULT FALSE, " +
				"FOREIGN KEY (" + USER_ID + ") REFERENCES " + USER_TABLE_NAME + "(" + USER_ID + "), " +
				"FOREIGN KEY (" + DATA_EVENT_PSTART + ") REFERENCES " + POSITION_TABLE_NAME + "(" + POSITION_ID + "), " +
				"FOREIGN KEY (" + DATA_EVENT_PEND + ") REFERENCES " + POSITION_TABLE_NAME + "(" + POSITION_ID + "), " +
				"FOREIGN KEY (" + DATA_EVENT_ADL_ACTUAL + ") REFERENCES " + ADL_TABLE_NAME + "(" + ADL_ID + "), " +
				"FOREIGN KEY (" + DATA_EVENT_ADL_CLASSIFY + ") REFERENCES " + ADL_TABLE_NAME + "(" + ADL_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createDataEventTable);
		db.execSQL(createDataEventTable);
		
		//Create the Data Event Table
		String createDataEventResultsTable = "CREATE TABLE " + DATA_EVENT_RESULTS_TABLE_NAME + " (" +
				DATA_EVENT_RESULTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DATA_EVENT_ID + " INTEGER NOT NULL, " +
				DATA_EVENT_TYPE_A_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_B_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_AB_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_TYPE_C_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_FALL_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_INACTIVITY_DETECTED + " BOOLEAN NULL, " +
				DATA_EVENT_UFT_EXCEEDED + " BOOLEAN NOT NULL DEFAULT FALSE, " +
				DATA_EVENT_LFT_EXCEEDED + " BOOLEAN NOT NULL DEFAULT FALSE, " +
				"FOREIGN KEY (" + DATA_EVENT_ID + ") REFERENCES " + DATA_EVENT_TABLE_NAME + "(" + DATA_EVENT_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createDataEventResultsTable);
		db.execSQL(createDataEventResultsTable);
		
		//
		//Create the Data Item Table
		String createDataItemTable = "CREATE TABLE " + DATA_ITEM_TABLE_NAME + " (" +
				DATA_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DATA_EVENT_ID + " INTEGER NOT NULL, " + 
				DATA_ITEM_TIMESTAMP + " INTEGER NOT NULL, " + 
				DATA_ITEM_ACCURACY + " INTEGER NULL, " + 
				DATA_ITEM_X + " FLOAT NOT NULL DEFAULT 0.0, " +
				DATA_ITEM_Y + " FLOAT NOT NULL DEFAULT 0.0, " + 
				DATA_ITEM_Z + " FLOAT NOT NULL DEFAULT 0.0, " +
				DATA_ITEM_RSS + " DOUBLE NOT NULL DEFAULT 0.0, " +
				"FOREIGN KEY (" + DATA_EVENT_ID + ") REFERENCES " + DATA_EVENT_TABLE_NAME + "(" + DATA_EVENT_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createDataItemTable);
		db.execSQL(createDataItemTable);
		
		//
		//Create the Data Event Features Table
		String createDataEventFeaturesTable = "CREATE TABLE " + DATA_EVENT_FEATURES_TABLE_NAME + " (" +
				DATA_EVENT_FEATURES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DATA_EVENT_ID + " INTEGER NOT NULL, " +
				FEATURE_ID + " INTEGER NOT NULL, " + 
				DATA_EVENT_FEATURES_VALUE + " FLOAT NOT NULL, " + 
				DATA_EVENT_FEATURES_TIMESTAMP + " INTEGER NOT NULL, " + 
				"FOREIGN KEY (" + DATA_EVENT_ID + ") REFERENCES " + DATA_EVENT_TABLE_NAME + "(" + DATA_EVENT_ID + ")" + 
				"FOREIGN KEY (" + FEATURE_ID + ") REFERENCES " + FEATURE_TABLE_NAME + "(" + FEATURE_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createDataEventFeaturesTable);
		db.execSQL(createDataEventFeaturesTable);
		
		//
		//Create the Feature Table
		String createFeatureTable = "CREATE TABLE " + FEATURE_TABLE_NAME + " (" +
				FEATURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				FEATURE_NAME + " VARCHAR(30) NULL, " +
				FEATURE_VERSION + " FLOAT NOT NULL, " +
				FEATURE_DESCRIPTION + " VARCHAR(100) NULL, " + 
				FEATURE_DURATION + " INTEGER NOT NULL DEFAULT 0, " + 
				FEATURE_LINK + " BLOB NULL)";
		Log.i(CREATE_TAG, "onCreate() -> " + createFeatureTable);
		db.execSQL(createFeatureTable);

		//
		//Create the Device Table
		String createDeviceTable = "CREATE TABLE " + DEVICE_TABLE_NAME + " (" +
				DEVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DEVICE_NAME + " VARCHAR(45) NULL, " +
				DEVICE_MODEL + " VARCHAR(45) NULL, " + 
				DEVICE_SERIAL + " VARCHAR(45) NULL, " +
				DEVICE_FINGERPRINT + " VARCHAR(45) NULL, " +
				DEVICE_TYPE + " VARCHAR(45) NULL, " +
				DEVICE_BOARD + " VARCHAR(45) NULL, " +
				DEVICE_BOOTLOADER + " VARCHAR(45) NULL, " +
				DEVICE_BRAND + " VARCHAR(45) NULL, " +
				DEVICE_DEVICE + " VARCHAR(45) NULL, " +
				DEVICE_DISPLAY + " VARCHAR(45) NULL, " +
				DEVICE_HARDWARE + " VARCHAR(45) NULL)";
		Log.i(CREATE_TAG, "onCreate() -> " + createDeviceTable);
		db.execSQL(createDeviceTable);
		
		//
		//Create the Sensor Table
		String createSensorTable = "CREATE TABLE " + SENSOR_TABLE_NAME + " (" +
				SENSOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
				DEVICE_ID + " INTEGER NOT NULL, " + 
				SENSOR_NAME + " VARCHAR(45) NULL, " +
				SENSOR_TYPE + " INTEGER NULL, " + 
				SENSOR_VENDOR + " VARCHAR(45) NULL, " + 
				SENSOR_VERSION + " VARCHAR(45) NULL, " +
				SENSOR_MAX_RANGE + " FLOAT NULL, " + 
				SENSOR_MIN_DELAY + " INT NULL, " + 
				SENSOR_POWER + " FLOAT NULL, " + 
				"FOREIGN KEY (" + DEVICE_ID + ") REFERENCES " + DEVICE_TABLE_NAME + "(" + DEVICE_ID + "))";
		Log.i(CREATE_TAG, "onCreate() -> " + createSensorTable);
		db.execSQL(createSensorTable);
		
		//put some dummy testing values into the user table
		populatePositionTable(db);
		populateADLTable(db);
		
		Log.i(CREATE_TAG, "onCreate() -> END");
	}
	
	/**
	 * Called when the database needs to be upgraded. The implementation should use this method to 
	 * drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		//TODO: Change to be able to save existing data before DROP TABLE
		Log.i(CREATE_TAG, "onUpgrade()");
		db.execSQL("DROP TABLE IF EXISTS "+ USER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ POSITION_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ ADL_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ DATA_EVENT_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ DATA_EVENT_RESULTS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ DATA_ITEM_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ DATA_EVENT_FEATURES_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ FEATURE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ DEVICE_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+ SENSOR_TABLE_NAME);
		onCreate(db);
	}
	
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		INSERT METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
		
	/**
	 * This method takes the required parameters and inserts them into the user table. 
	 * 
	 * Note: USER_ID is not required because it will be automatically assigned a value by the engine upon insert.
	 * 
	 * Note: By default now, the initial phone position is always "U00" (upright, front-left pocket)
	 * 
	 * @param USER_NAME
	 * @param USER_AGE
	 * @param USER_HEIGHT
	 * @param USER_WEIGHT
	 * @param USER_SEX
	 * @param USER_ACTIVITY_LEVEL
	 * @return USER_ID
	 */
	public long addUser(long rID, String name, int age, float height, float weight, String sex, String activityLevel, String email, String alarmNumber)
	{
		return addUser(rID, name, age, height, weight, sex, activityLevel, email, alarmNumber, "U00");
    }
	/**
	 * This method takes the required parameters and inserts them into the user table.
	 * Note: This method does not check for duplicate entries in the user table.
	 * 
	 * @param USER_NAME
	 * @param USER_AGE
	 * @param USER_HEIGHT
	 * @param USER_WEIGHT
	 * @param USER_SEX
	 * @param USER_ACTIVITY_LEVEL
	 * @param USER_EMAIL
	 * @param USER_ALARM_NUMBER
	 * @param POSITION_ID
	 * @return USER_ID
	 */
	public long addUser(long rID, String name, int age, float height, float weight, String sex, String activityLevel, String email, String alarmNumber, String pCode)
	{
		Log.i(CREATE_TAG, "addUser() -> rID:" + rID + "\tname: " + name);
		long uID;
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DEVICE_ID, rID);
        values.put(USER_NAME, name);
        values.put(USER_AGE, age);
        values.put(USER_HEIGHT, height);
        values.put(USER_WEIGHT, weight);
        values.put(USER_SEX, sex);
        values.put(USER_ACTIVITY_LEVEL, activityLevel);
        values.put(USER_EMAIL, email);
        values.put(USER_ALARM_NUMBER, alarmNumber);
        values.put(POSITION_ID, pCode);
        
        // Inserting Row
        uID = db.insert(USER_TABLE_NAME, null, values);
        db.close();
        return uID;
	}
	
	public long addPosition(String pCode, String pName, String pDescription)
	{
		Log.i(CREATE_TAG, "addPosition() -> " + pCode);
		
		long pID;
		if(checkPositionCode(pCode))
		{
			Log.w(CREATE_TAG, "Warning: Position Code is already in use: " + pCode);
			return -1;
		}
		
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(POSITION_ID, pCode);
        values.put(POSITION_NAME, pName);
        values.put(POSITION_DESCRIPTION, pDescription);
        
        // Inserting Row
        pID = db.insert(POSITION_TABLE_NAME, null, values);
        db.close();
        return pID;
    }
	
	public long addADL(String adlCode, int adlType, String adlDescription)
	{
		Log.i(CREATE_TAG, "addADL() -> " + adlCode);
		
		long adlID;
		if(checkADLCode(adlCode))
		{
			Log.w(CREATE_TAG, "Warning: ADL Code is already in use: " + adlCode);
			return -1;
		}
		
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(ADL_ID, adlCode);
        values.put(ADL_TYPE, adlType);
        values.put(ADL_DESCRIPTION, adlDescription);
        
        // Inserting Row
        adlID = db.insert(ADL_TABLE_NAME, null, values);
        db.close();
        return adlID;
    }
	
	public long addDevice()
	{
		//TODO: Implement code to check to see if the FingerPrint has changed for the user, and if so, add another device to the table.
		long rID;		
		rID = getDeviceIDFromFingerprint(Build.FINGERPRINT);
		//if the FingerPrint isn't in the DB, add it
		if(rID == -1)
			rID = addDevice(Build.PRODUCT, Build.MODEL, "0", 
	    			Build.FINGERPRINT, Build.TYPE, Build.BOARD, Build.BOOTLOADER, 
	    			Build.BRAND, Build.DEVICE, Build.DISPLAY, Build.HARDWARE);
		return rID;
	}
	private long addDevice(String name, String model, String serial, String fingerprint, String type, String board, String bootloader, String brand, String device, String display, String hardware)
	{
		Log.i(CREATE_TAG, "addDevice() -> " + type);
		long rID;
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DEVICE_NAME, name);
        values.put(DEVICE_MODEL, model);
        values.put(DEVICE_SERIAL, serial);
        values.put(DEVICE_FINGERPRINT, fingerprint);
        values.put(DEVICE_TYPE, type);
        values.put(DEVICE_BOARD, board);
        values.put(DEVICE_BOOTLOADER, bootloader);
        values.put(DEVICE_BRAND, brand);
        values.put(DEVICE_DEVICE, device);
        values.put(DEVICE_DISPLAY, display);
        values.put(DEVICE_HARDWARE, hardware);
        
        // Inserting Row
        rID = db.insert(DEVICE_TABLE_NAME, null, values);
        db.close();
        return rID;
	}
	
	public void addSensors(long dID, List<Sensor> sensors)
	{
		for(Sensor s : sensors)
        {
        	//TODO: Find out why getMinDelay isn't working for this api level (which is 9). 
        	
        	//Get Min Delay was not available until Android 2.3 (API Level 9)
        	//s.getMinDelay();        	
        	addSensor(dID, s.getName(), s.getType(), s.getVendor(), s.getVersion(), s.getMaximumRange(), 0, s.getPower());
        }
	}
	private long addSensor(long dID, String sName, int sType, String sVendor, int sVersion, float sRange, int sDelay, float sPower)
	{
		Log.i(CREATE_TAG, "addSensor() -> " + sName);
		long sID;
		
		sID = getSensorIDFromType(dID, sType);
		if(sID != -1)
		{
			Log.w(CREATE_TAG, "addSensor() -> Warning: Sensor type (" + sType + ") is already associated with device: " + dID);
			return sID;
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DEVICE_ID, dID);
        values.put(SENSOR_NAME, sName);
        values.put(SENSOR_TYPE, sType);
        values.put(SENSOR_VENDOR, sVendor);
        values.put(SENSOR_VERSION, sVersion);
        values.put(SENSOR_MAX_RANGE, sRange);
        values.put(SENSOR_MIN_DELAY, sDelay);
        values.put(SENSOR_POWER, sPower);
                
        // Inserting Row
        sID = db.insert(SENSOR_TABLE_NAME, null, values);
        db.close();
        return sID;
	}
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		DATA EVENT METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	public void createDataEvent(LinkedList <String> vList)
	{
		//This linked list is used to store the vectors needed to check for ADLs.
		//private static LinkedList <KVector> vectorList=new LinkedList<KVector>();
		
		
	}
	
	
	/**
	 * Create a blank Data Detect Event. This is primarily used for collecting data about an unknown ADL activity.  
	 * Note: This method does not check for valid uID values.
	 * 
	 * @param USER_ID
	 * @return DATA_EVENT_ID
	 */
	public long addDetectDataEvent(long uID)
	{
		Log.i(DATA_TAG, "addDataEvent()" );
		long deID;
		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, uID);
        values.putNull(DATA_EVENT_PSTART);
        values.putNull(DATA_EVENT_PEND);
        values.putNull(DATA_EVENT_ADL_CLASSIFY);
        values.putNull(DATA_EVENT_ADL_ACTUAL);
        
        // Inserting Row
        deID = db.insert(DATA_EVENT_TABLE_NAME, null, values);
        db.close();
        return deID;
	}
	/**
	 * Create a blank ADL Data Event. This is primarily used for collecting data about a known ADL activity. 
	 * Note: This method does not check for valid uID or adlID values.
	 * 
	 * @param USER_ID
	 * @param ADL_ID
	 * @return DATA_EVENT_ID
	 */
	public long addADLDataEvent(long uID, long adlID)
	{
		Log.i(DATA_TAG, "addADLDataEvent()" );
		long deID;
		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, uID);
        values.putNull(DATA_EVENT_PSTART);
        values.putNull(DATA_EVENT_PEND);
        values.put(DATA_EVENT_ADL_CLASSIFY, DATA_EVENT_ADL_NAN);
        values.put(DATA_EVENT_ADL_ACTUAL, adlID);
        
        // Inserting Row
        deID = db.insert(DATA_EVENT_TABLE_NAME, null, values);
        db.close();
        return deID;
	}
	
	/**
	 * Data Event is a little different. There is no name to query by, so when the deID is returned it cannot be lost.
	 * Note: This method does not check for valid position or adl values.
	 * @param DATA_EVENT_PSTART
	 * @param DATA_EVENT_PEND
	 * @param DATA_EVENT_ADL_CLASSIFY
	 * @param DATA_EVENT_ADL_ACTUAL
	 * @return DATA_EVENT_ID
	 */
	public long addDataEvent(String pStart, String pEnd, String adlClassify, String adlActual)
	{
		Log.i(DATA_TAG, "addDataEvent() -> Classify: " + adlClassify + "\tActual: " + adlActual);
		long deID;
		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_PSTART, pStart);
        values.put(DATA_EVENT_PEND, pEnd);
        values.put(DATA_EVENT_ADL_CLASSIFY, adlClassify);
        values.put(DATA_EVENT_ADL_ACTUAL, adlActual);
        
        // Inserting Row
        deID = db.insert(DATA_EVENT_TABLE_NAME, null, values);
        db.close();
        return deID;
	}
	
	public int updateDataEventPositionStart(long deID, String pStart)
	{
		//Log.i(DATA_TAG, "updateDataEventPositionStart() -> deID: " + deID + "\tpStart: " + pStart);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		if(!checkPositionCode(pStart))
		{
			Log.w(DATA_TAG, "Warning - Position is not in database: " + pStart);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_PSTART, pStart);        
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Long.toString(deID)});
        db.close();
        return rows;
	}
	public int updateDataEventPositionEnd(long deID, String pEnd)
	{
		//Log.i(DATA_TAG, "udpateDataEventPositionEnd() -> deID: " + deID + "\tpEnd: " + pEnd);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		if(!checkPositionCode(pEnd))
		{
			Log.w(DATA_TAG, "Warning - Position is not in database: " + pEnd);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_PEND, pEnd);        
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Long.toString(deID)});
        db.close();
        return rows;
	}
	public int updateDataEventADLClassify(long deID, String adlClassify)
	{
		//Log.i(DATA_TAG, "udpateDataEventADLClassify() -> deID: " + deID + "\tadlClassify: " + adlClassify);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		if(!checkADLCode(adlClassify))
		{
			Log.w(DATA_TAG, "Warning - ADL is not in database: " + adlClassify);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_ADL_CLASSIFY, adlClassify);        
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Long.toString(deID)});
        db.close();
        return rows;
	}
	public int updateDataEventADLActual(long deID, String adlActual)
	{
		//Log.i(DATA_TAG, "udpateDataEventADLActual() -> deID: " + deID + "\tadlActual: " + adlActual);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		if(!checkADLCode(adlActual))
		{
			Log.w(DATA_TAG, "Warning - ADL is not in database: " + adlActual);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_ADL_ACTUAL, adlActual);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Long.toString(deID)});
        db.close();
        return rows;
	}
	public int updateDataEventTypeA(long deID, boolean value)
	{
		Log.i(DATA_TAG, "updateDataEventTypeA() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_TYPE_A_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventTypeB(long deID, boolean value)
	{
		Log.i(DATA_TAG, "updateDataEventTypeB() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_TYPE_B_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventTypeC(long deID, boolean value)
	{
		Log.i(DATA_TAG, "updateDataEventTypeC() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_TYPE_C_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventTypeAB(long deID, boolean value)
	{
		Log.i(DATA_TAG, "updateDataEventTypeAB() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_TYPE_AB_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventInactivity(long deID, boolean value)
	{
		Log.i(DATA_TAG, "updateDataEventInactivity() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_INACTIVITY_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventFall(long deID, boolean value)
	{
		//Log.i(DATA_TAG, "updateDataEventFall() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_FALL_DETECTED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventUFT(long deID, boolean value)
	{
		//Log.i(DATA_TAG, "updateDataEventUFT() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_UFT_EXCEEDED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	public int updateDataEventLFT(long deID, boolean value)
	{
		//Log.i(DATA_TAG, "updateDataEventLFT() -> deID: " + deID + "\tvalue: " + value);
		
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Event ID not in database: " + deID);
			return -1;
		}
		
		int rows;
		String whereClause = DATA_EVENT_ID + "=?";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_LFT_EXCEEDED, value);
        rows = db.update(DATA_EVENT_TABLE_NAME, values, whereClause, new String[]{Boolean.toString(value)});
        db.close();
        return rows;
	}
	
 	
	public long addDataItem(long deID, long ts, int accuracy, float gX, float gY, float gZ, double rss)
	{
		//Log.i(DATA_TAG, "addDataItem()" );
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Data Event ID not in database: " + deID);
			return -1;
		}
		if(checkDataItemTimeStamp(deID, ts))
		{
			Log.w(DATA_TAG, "Warning - This Data Item Timestamp (" + ts + ") is already associated with the data event id: " + deID);
			return -1;
		}
		
		long diID;		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_ID, deID);
        values.put(DATA_ITEM_TIMESTAMP, ts);
        values.put(DATA_ITEM_ACCURACY, accuracy);
        values.put(DATA_ITEM_X, gX);
        values.put(DATA_ITEM_Y, gY);
        values.put(DATA_ITEM_Z, gZ);
        values.put(DATA_ITEM_RSS, rss);
        
        // Inserting Row
        diID = db.insert(DATA_ITEM_TABLE_NAME, null, values);
        db.close();
        return diID;
	}
	
	public boolean addDataItems(ArrayList<KVector> list, long deID, int accuracy)
	{
		Log.i(DATA_TAG, "addDataItems() -> " + list.size());
		if(!checkDataEventID(deID))
		{
			Log.w(DATA_TAG, "Warning - Data Event ID not in database: " + deID);
			return false;
		}
		
		
		long diID;		
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        
        ContentValues values;
        try
        {
	        for (KVector item : list)
	        {
	        	if(checkDataItemTimeStamp(deID, item.getTimeStamp()))
	    		{
	    			Log.w(DATA_TAG, "Warning - This Data Item Timestamp (" + item.getTimeStamp() + ") is already associated with the data event id: " + deID);
	    			return false;
	    		}
	        	
		        values = new ContentValues();
		        values.put(DATA_EVENT_ID, deID);
		        values.put(DATA_ITEM_TIMESTAMP, item.getTimeStamp());
		        values.put(DATA_ITEM_ACCURACY, accuracy);
		        values.put(DATA_ITEM_X, item.getX());
		        values.put(DATA_ITEM_Y, item.getY());
		        values.put(DATA_ITEM_Z, item.getZ());
		        values.put(DATA_ITEM_RSS, item.getRSS());
		        
		        // Inserting Row
		        diID = db.insert(DATA_ITEM_TABLE_NAME, null, values);
		        
		        if(diID == -1)
		        {
		        	Log.e(DATA_TAG, "Error Inserting Data Item (" + item.getTimeStamp() + ") into the database.");
	    			return false;
		        }
	        }
	        db.setTransactionSuccessful();
        }
        finally
        {
	        db.endTransaction();
	        db.close();
        }
        return true;
	}
	
	
	/**
	 * This method adds a new event feature to the DataEventFeatures table.
	 * 
	 * @param DATA_EVENT_ID
	 * @param FEATURE_ID
	 * @param DATA_EVENT_FEATURE_VALUE
	 * @param DATA_EVENT_FEATURE_TIMESTAMP
	 * @return DATA_EVENT_FEATURE_ID
	 */
	public long addDataEventFeature(long deID, long fID, float defValue, long defTS)
	{
		Log.i(DATA_TAG, "addDataEventFeature() -> desValue: " + defValue + "\tdefTS: " + defTS);
		
		if(checkDataEventFeature(deID, fID, defTS))
		{
			Log.w(DATA_TAG, "Warning - This Data Item Timestamp (" + defTS + ", " + defValue + ") is already associated with the data event deID: " + deID + "\tand fID: " + fID);
			return -1;
		}
		
		long defID;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATA_EVENT_ID, deID);
        values.put(FEATURE_ID, fID);
        values.put(DATA_EVENT_FEATURES_VALUE, defValue);
        values.put(DATA_EVENT_FEATURES_TIMESTAMP, defTS);
        
        // Inserting Row
        defID = db.insert(DATA_EVENT_FEATURES_TABLE_NAME, null, values);
        db.close();
        return defID;
	}
	
	/**
	 * This method takes the parameters and adds a new feature to the Feature table.
	 * Note: This does not check for duplicates.
	 * 
	 * @param FEATURE_NAME
	 * @param FEATURE_DESCRIPTION
	 * @param FEATURE_DURATION
	 * @param FEATURE_VALUE
	 * @return FEATURE_ID
	 */
	public long addFeature(String fName, String fDescription, int fDuration, float fValue)
	{
		Log.i(DATA_TAG, "addFeature() -> fName: " + fName + "\tfValue: " + fValue);
				
		long fID;		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FEATURE_NAME, fName);
        values.put(FEATURE_DESCRIPTION, fDescription);
        values.put(FEATURE_DURATION, fDuration);
        values.put(FEATURE_VALUE, fValue);
        
        // Inserting Row
        fID = db.insert(FEATURE_TABLE_NAME, null, values);
        db.close();
        return fID;
	}
	
	
	public long addAlgorithm(long fID, String aName, String aVersion, String aDescription, String aLink)
	{
		/*
		Log.i(DATA_TAG, "addAlgorithm() -> aName: " + aName + "\taVersion: " + aVersion);
		
		if(checkAlgorithm(fID, aName, aVersion))
		{
			Log.w(DATA_TAG, "Warning - This Algorithm (" + aName + ", " + aVersion + ") is already associated with fID: " + fID);
			return -1;
		}
		
		long aID;		
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FEATURE_ID, fID);
        values.put(ALGORITHM_NAME, aName);
        values.put(ALGORITHM_VERSION, aVersion);
        values.put(ALGORITHM_DESCRIPTION, aDescription);
        values.put(ALGORITHM_LINK, aLink);
        
        // Inserting Row
        aID = db.insert(ALGORITHM_TABLE_NAME, null, values);
        db.close();
        return aID;
        */
        return -1;
	}
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		GET METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	
	public long getDeviceIDFromFingerprint(String fingerprint)
	{
		Log.i(TAG, "getDeviceIDFromFingerprint() -> " + fingerprint);
		SQLiteDatabase db = this.getReadableDatabase();
		
		long rID = -1;
		String selection = "SELECT " + DEVICE_ID + " FROM " + DEVICE_TABLE_NAME + " WHERE " + DEVICE_FINGERPRINT + " = '" + fingerprint + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			rID = cursor.getLong(0);
		cursor.close();
		return rID;
	}
	
	public String getADLDescriptionFromID(long adlID)
	{
		Log.i(TAG, "getADLDescriptionFromID() -> " + adlID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		String adlDescription = "";
		String selection = "SELECT " + ADL_DESCRIPTION + " FROM " + ADL_TABLE_NAME + " WHERE " + ADL_ID + " = '" + adlID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			adlDescription = cursor.getString(0);
		cursor.close();
		return adlDescription;
	}
	public int getADLTypeFromID(long adlID)
	{
		Log.i(TAG, "getADLDescriptionFromID() -> " + adlID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		int adlType = -1;
		String selection = "SELECT " + ADL_TYPE + " FROM " + ADL_TABLE_NAME + " WHERE " + ADL_ID + " = '" + adlID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			adlType = cursor.getInt(0);
		cursor.close();
		return adlType;
	}
	
	//TODO: Make one method that will return any column value based on the ID
	public String getStringTableData(String tableName, int colID) { return ""; }	
	public String getADLStringData(long adlID) { return ""; }
	public int getADLIntData(long adlID) { return -1; }
	
	/**
	 * 
	 * Note: The names are not required to be unique in the table, so it is possible that a name
	 * could have more than one match.
	 * @param adlName
	 * @return
	 */
	public long getADLIDFromName(String adlName)
	{
		Log.i(TAG, "getADLDescriptionFromID() -> " + adlName);
		SQLiteDatabase db = this.getReadableDatabase();
		
		long adlID = -1;
		String selection = "SELECT " + ADL_ID + " FROM " + ADL_TABLE_NAME + " WHERE " + ADL_NAME + " = '" + adlName + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			adlID = cursor.getInt(0);
		cursor.close();
		return adlID;
	}
	
	public String getADLNameFromID(long adlID)
	{
		Log.i(TAG, "getADLDescriptionFromID() -> " + adlID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		String adlName = "";
		String selection = "SELECT " + ADL_NAME + " FROM " + ADL_TABLE_NAME + " WHERE " + ADL_ID + " = '" + adlID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			adlName = cursor.getString(0);
		cursor.close();
		return adlName;
	}
	public String[] getADLNames()
	{
		Log.i(TAG, "getADLNames()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] adls;
		String selection = "SELECT " + ADL_NAME + " FROM " + ADL_TABLE_NAME;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new String[0];
		}
		
		adls = new String[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			adls[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		cursor.close();
		return adls;
	}
	
	public String getPositionNameFromID(long pID)
	{
		Log.i(TAG, "getPositionNameFromID() -> " + pID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		String pName = "";
		String selection = "SELECT " + POSITION_NAME + " FROM " + POSITION_TABLE_NAME + " WHERE " + POSITION_ID + " = '" + pID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			pName = cursor.getString(0);
		cursor.close();
		return pName;
	}
	public String getPositionDescriptionFromID(long pID)
	{
		Log.i(TAG, "getPositionDescriptionFromID() -> " + pID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		String pDescription = "";
		String selection = "SELECT " + POSITION_DESCRIPTION + " FROM " + POSITION_TABLE_NAME + " WHERE " + POSITION_ID + " = '" + pID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			pDescription = cursor.getString(0);
		cursor.close();
		return pDescription;
	}
	
	/**
	 * This method returns the sensor type from the provided sensor id.
	 *  
	 * @param SENSOR_ID
	 * @return SENSOR_TYPE
	 */
	public int getSensorTypeFromID(long sID)
	{
		Log.i(TAG, "getSensorTypeFromID() -> " + sID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		int sType = -1;
		String selection = "SELECT " + SENSOR_ID + " FROM " + SENSOR_TABLE_NAME + " WHERE " + SENSOR_ID + " = '" + sID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			sType = cursor.getInt(0);
		cursor.close();
		return sType;
	}
	/**
	 * This method returns the sensor id from the provided sensor type and device id.
	 *  
	 * @param SENSOR_TYPE
	 * @param DEVICE_ID
	 * @return SENSOR_ID
	 */
	public long getSensorIDFromType(long dID, long sType)
	{
		Log.i(TAG, "getSensorIDFromType() -> dID: " + dID + "\tsType: " + sType);
		SQLiteDatabase db = this.getReadableDatabase();
		
		long sID = -1;
		String selection = "SELECT " + SENSOR_ID + " FROM " + SENSOR_TABLE_NAME + " WHERE " + SENSOR_TYPE + " = '" + sType + "' AND " + DEVICE_ID + " = '" + dID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			sID = cursor.getLong(0);
		cursor.close();
		return sID;
	}
	
	/**
	 * This method returns the data item id from the DataItem table that matches the provided data event id and 
	 * the provided timestamp, -1 otherwise.
	 * 
	 * @param DATA_EVENT_ID
	 * @param DATA_ITEM_TIMESTAMP
	 * @return DATA_ITEM_ID
	 */
	public long getDataItemIDFromTimeStamp(long deID, long ts)
	{
		Log.i(TAG, "getDataItemIDFromTimeStamp() -> " + ts);
		SQLiteDatabase db = this.getReadableDatabase();
		
		long rID = -1;
		String selection = "SELECT " + DATA_ITEM_ID + " FROM " + DATA_ITEM_TABLE_NAME + " WHERE " + DATA_ITEM_TIMESTAMP + " = '" + ts + "' AND " + DATA_EVENT_ID + " = '" + deID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			rID = cursor.getLong(0);
		cursor.close();
		return rID;
	}
	
	/**
	 * This method takes the provided user name and returns the ID associated with it. 
	 * 
	 * Note: If multiple users exist with the same name, it will return the first name in the list. 
	 * @param USER_NAME
	 * @return USER_ID
	 */
	public long getUserIDFromName(String uName)
	{
		Log.i(TAG, "getUserIDFromName() -> uName: " + uName);
		SQLiteDatabase db = this.getReadableDatabase();
		
		long uID = -1;
		String selection = "SELECT " + USER_ID + " FROM " + USER_TABLE_NAME + " WHERE " + USER_NAME + " = '" + uName + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			uID = cursor.getLong(0);
		cursor.close();
		return uID;
	}
	
	public String getUserNameFromID(long uID)
	{
		Log.i(TAG, "getUserNameFromID() -> uID: " + uID);
		SQLiteDatabase db = this.getReadableDatabase();
		
		String uName = "";
		String selection = "SELECT " + USER_ID + " FROM " + USER_TABLE_NAME + " WHERE " + USER_NAME + " = '" + uID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			uName = cursor.getString(0);
		cursor.close();
		return uName;
	}
	
	public String[] getUserNames()
	{
		Log.i(TAG, "getUserNames()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] userNames;
		String selection = "SELECT " + USER_NAME + " FROM " + USER_TABLE_NAME;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new String[0];
		}
		
		userNames = new String[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			userNames[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		cursor.close();
		return userNames;
	}
	public long[] getUserIDs()
	{
		Log.i(TAG, "getUserIDs()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		long[] userIDs;
		String selection = "SELECT " + USER_ID + " FROM " + USER_TABLE_NAME;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new long[0];
		}
		
		userIDs = new long[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			userIDs[i] = cursor.getLong(0);
			cursor.moveToNext();
		}
		cursor.close();
		return userIDs;
	}
	
	public String[] getDataEvents()
	{
		//Log.i(TAG, "getDataEvents()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] dataEvents;
		String selection = "SELECT " + DATA_EVENT_ID + " FROM " + DATA_EVENT_TABLE_NAME;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new String[0];
		}
		
		dataEvents = new String[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			dataEvents[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		cursor.close();
		return dataEvents;
	}
	
	public String[] getDataEventsFromUserId(long uID)
	{
		Log.i(TAG, "getDataEventsFromUserId()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] dataEvents;
		String selection = "SELECT " + DATA_EVENT_ID + " FROM " + DATA_EVENT_TABLE_NAME + " WHERE " + USER_ID + " = " + uID;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new String[0];
		}
		
		dataEvents = new String[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			dataEvents[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		cursor.close();
		return dataEvents;
	}
	
	public String[] getDataEventsFromADLCode(String adlCode)
	{
		Log.i(TAG, "getDataEventsFromADLCode()");
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] dataEvents;
		String selection = "SELECT " + DATA_EVENT_ID + " FROM " + DATA_EVENT_TABLE_NAME + " WHERE " + ADL_ID + " = " + adlCode;
		Cursor cursor = db.rawQuery(selection, null);
		
		if(cursor.getColumnCount() == 0)
		{
			cursor.close();
			return new String[0];
		}
		
		dataEvents = new String[cursor.getCount()];
		
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++)
		{
			dataEvents[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		cursor.close();
		return dataEvents;
	}
	
	public String[] getDataItemsFromDataEvent(long dataEventID)
	{
		String[] dataEvents;
		
		dataEvents = new String[0];
		return dataEvents;
	}
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		CHECK METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	/**
	 * This method returns true if the data event ID is found in the data event table, false otherwise.
	 * 
	 * @param DATA_EVENT_ID
	 * @return boolean
	 */
	public boolean checkDataEventID(long deID)
	{
		//Log.i(TAG, "getDataEventID() -> " + deID);
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + DATA_EVENT_ID + " FROM " + DATA_EVENT_TABLE_NAME + " WHERE " + DATA_EVENT_ID + " = '" + deID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.moveToFirst())
			deID = cursor.getLong(0);
		cursor.close();
		
		if(deID == -1)
			return false;
		else 
			return true;
	}
		
	/**
	 * This method returns true if a data item with the provided timestamp is found with the current data event id, false otherwise.
	 * Note: This does not check for duplicate timestamp values.
	 * 
	 * @param DATA_EVENT_ID
	 * @param DATA_ITEM_TIMESTAMP
	 * @return boolean
	 */
	public boolean checkDataItemTimeStamp(long deID, long ts)
	{
		//Log.i(TAG, "checkDataItemTimeStamp() -> " + ts);
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + DATA_ITEM_TIMESTAMP + " FROM " + DATA_ITEM_TABLE_NAME + " WHERE " + DATA_ITEM_TIMESTAMP + " = '" + ts + "' AND " + DATA_EVENT_ID + " + '" + deID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		else
		{
			cursor.close();
			return true;
		}
	}
	
	/**
	 * This method returns true if the provided ADL code is in the ADLCode table, false otherwise.
	 *  
	 * @param ADL_ID
	 * @return boolean
	 */
	public boolean checkADLCode(String adlCode)
	{
		//Log.i(TAG, "checkADLCode() -> " + adlCode);
		
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + ADL_ID + " FROM " + ADL_TABLE_NAME+ " WHERE " + ADL_ID + " = '" + adlCode + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		else
		{
			cursor.close();
			return true;
		}
	}
	/**
	 * This method returns true if the provided Position code is in the PositionCode table, false otherwise.
	 *  
	 * @param PositionID
	 * @return boolean
	 */
	public boolean checkPositionCode(String pCode)
	{
		//Log.i(TAG, "checkPositionCode() -> " + pCode);
		
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + POSITION_ID + " FROM " + POSITION_TABLE_NAME + " WHERE " + POSITION_ID + " = '" + pCode + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		else 
		{
			cursor.close();
			return true;
		}
	}
	
	/**
	 * This method returns true if the provided values match anything in the Algorithm table, false otherwise.
	 *   
	 * @param FEATURE_ID
	 * @param ALGORITHM_NAME
	 * @param ALGORITHM_VERSION
	 * @return boolean
	 */
	public boolean checkAlgorithm(long fID, String aName, String aVersion)
	{
		/*
		Log.i(TAG, "checkAlgorithm() -> aName: " + aName + "\taVersion" + aVersion);
		
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + ALGORITHM_ID + " FROM " + ALGORITHM_TABLE_NAME + " WHERE " + ALGORITHM_NAME + " = '" + aName + "' AND " +  
																								    ALGORITHM_VERSION + " = '" + aVersion + "' AND" +
																								    FEATURE_ID + " = '" + fID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		else 
		{
			cursor.close();
			return true;
		}
		*/
		return false;
	}
	/**
	 * This method returns true if the provided values match anything in the DataEventFeatures table, false otherwise.
	 * 
	 * @param DATA_EVENT_ID
	 * @param FEATURE_ID
	 * @param DATA_EVENT_FEATURE_TIMESTAMP
	 * @return boolean
	 */
	public boolean checkDataEventFeature(long deID, long fID, long defTS)
	{
		Log.i(TAG, "checkDataEventFeature() -> deID: " + deID + "\tfID" + fID + "\tdefTS" + defTS);
		
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = "SELECT " + DATA_EVENT_FEATURES_ID + " FROM " + DATA_EVENT_FEATURES_ID + " WHERE " + DATA_EVENT_FEATURES_TIMESTAMP + " = '" + defTS + "' AND " +  
																								    			DATA_EVENT_ID + " = '" + deID + "' AND" +
																							    				FEATURE_ID + " = '" + fID + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return false;
		}
		else
		{
			cursor.close();
			return true;
		}
	}
	
	
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		INIT METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	/**
	 * This method populates the PositionCode table with some default values.
	 */
	private void populatePositionTable(SQLiteDatabase db)
	{
		initPosition(db, "000", "Default", "Default");
		
		//These are reserved positions used in the Data Event methods to check the status of the service. 
		initPosition(db, "DE0", "Data Event 0", "The data event is stopped, but no ending data has been recorded.");
		initPosition(db, "DE1", "Data Event 1", "The data event is running.");
		initPosition(db, "DE2", "Data Event 2", "The data event is paused.");
		initPosition(db, "DEE", "Data Event Error", "The data event exited with an error code.");
		
		
		//phone positions
		initPosition(db, "U00", "User Position 00", "The phone is oriented upright in the users front-right pocket with the the top of the phone facing upward.");
		initPosition(db, "U01", "User Position 01", "The phone is oriented upright in the users front-right pocket with the the top of the phone facing downward.");
		initPosition(db, "U02", "User Position 02", "The phone is oriented sideways in the users front-right pocket with the the top of the phone facing to the left.");
		initPosition(db, "U03", "User Position 03", "The phone is oriented sideways in the users front-right pocket with the the top of the phone facing to the right.");

		initPosition(db, "U10", "User Position 10", "The phone is oriented upright in the users front-left pocket with the the top of the phone facing upward.");
		initPosition(db, "U11", "User Position 11", "The phone is oriented upright in the users front-left pocket with the the top of the phone facing downward.");
		initPosition(db, "U12", "User Position 12", "The phone is oriented sideways in the users front-left pocket with the the top of the phone facing to the left.");
		initPosition(db, "U13", "User Position 13", "The phone is oriented sideways in the users front-left pocket with the the top of the phone facing to the right.");
		
		initPosition(db, "U20", "User Position 20", "The phone is oriented upright in the users back-left pocket with the the top of the phone facing upward.");
		initPosition(db, "U21", "User Position 21", "The phone is oriented upright in the users back-left pocket with the the top of the phone facing downward.");
		initPosition(db, "U22", "User Position 22", "The phone is oriented sideways in the users back-left pocket with the the top of the phone facing to the left.");
		initPosition(db, "U23", "User Position 23", "The phone is oriented sideways in the users back-left pocket with the the top of the phone facing to the right.");
		
		initPosition(db, "U30", "User Position 30", "The phone is oriented upright in the users back-right pocket with the the top of the phone facing upward.");
		initPosition(db, "U31", "User Position 31", "The phone is oriented upright in the users back-right pocket with the the top of the phone facing downward.");
		initPosition(db, "U32", "User Position 32", "The phone is oriented sideways in the users back-right pocket with the the top of the phone facing to the left.");
		initPosition(db, "U33", "User Position 33", "The phone is oriented sideways in the users back-right pocket with the the top of the phone facing to the right.");
		
		
		//orientation positions
		initPosition(db, "SIT", "Sitting", "The user is sitting normally.");
		initPosition(db, "STA", "Standing", "The user is standing normally.");
		initPosition(db, "LAY", "Laying Down on Back", "The user is laying on their back.");
		initPosition(db, "LYF", "Laying Down on Front", "The user is laying on their front.");
		initPosition(db, "SDL", "Left Side", "The user is laying horizontally on their left side.");
		initPosition(db, "SDR", "Right Side", "The user is laying horizontally on their left side.");
	}

	/**
	 * Populate the ADL Code table with some sample values taken from 
	 * "Recognition of false alarms in fall detection systems" 
	 */
	private void populateADLTable(SQLiteDatabase db)
	{
		initADL(db, "000", 1, "Default", "Default");
		initADL(db, "001", 1, "NAN", "Not an Event. This is used when the ADL is known (i.e.- it is a data collecting event) or when the ADL is not otherwise relevant for the current activity. NOTE: This is normally used for internal use. Select DEFAULT unless you have a specific use for this option.");
		
		/*
		initADL(db, "FBE", 1, "Fall from bed", "ADL Description");
		initADL(db, "FFA", 1, "Fall almost vertically from standing (faint)", "ADL Description");
		initADL(db, "FPG", 1, "Fall after parkinsonian gait", "ADL Description");
		initADL(db, "FHA", 1, "Fall forward landing on hands first", "ADL Description");
		initADL(db, "FJU", 1, "Fall after a small jump", "ADL Description");
		initADL(db, "FKN", 1, "Fall forward landing on knees first", "ADL Description");
		initADL(db, "FRU", 1, "Fall while running", "ADL Description");
		initADL(db, "FSI", 1, "Fall from sitting", "ADL Description");		
		*/
		
		initADL(db, "LYBE", 1, "Lying-bed", "Lying-bed ADL Description");
		initADL(db, "RIBE", 1, "Rising-bed", "Rising-bed ADL Description");
		initADL(db, "SIBE", 1, "Sit-bed", "Sit-bed ADL Description");
		initADL(db, "SCH", 1, "Sit-chair", "Sit-chair ADL Description");
		initADL(db, "SSO", 1, "Sit-sofa", "Sit-sofa ADL Description");
		initADL(db, "SAI", 1, "Sit-air", "Sit-airADL Description");
		initADL(db, "WAF", 1, "Walking(forward)", "Walking(forward) ADL Description");
		initADL(db, "JOF", 1, "Jogging", "Jogging ADL Description");
		initADL(db, "WAB", 1, "Walking(backward)", "Walking(backward) ADL Description");
		initADL(db, "BEX", 1, "Bending", "Bending ADL Description");
		initADL(db, "BEP", 1, "Bending-pick-up", "Bending-pick-up ADL Description");
		initADL(db, "STU", 1, "Stumble", "StumbleADL Description");
		initADL(db, "LIM", 1, "Limp", "LimpADL Description");
		initADL(db, "SQD", 1, "Squatting-down", "Squatting-down ADL Description");
		initADL(db, "TRO", 1, "Trip-over", "Trip-over ADL Description");
		initADL(db, "COSN", 1, "Coughing-sneezing", "Coughing-sneezing ADL Description");
	}
	
	private long initPosition(SQLiteDatabase db, String pCode, String pName, String pDescription)
	{
		Log.i(CREATE_TAG, "initPosition() -> " + pCode);
		
		long pID;
		String selection = "SELECT " + POSITION_ID + " FROM " + POSITION_TABLE_NAME+ " WHERE " + POSITION_ID + " = '" + pCode + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() != 0)
		{
			Log.w(TAG, "Warning: Position Code is already in use: " + pCode);
			return -1;
		}
		
        ContentValues values = new ContentValues();
        values.put(POSITION_ID, pCode);
        values.put(POSITION_NAME, pName);
        values.put(POSITION_DESCRIPTION, pDescription);
        
        // Inserting Row
        pID = db.insert(POSITION_TABLE_NAME, null, values);
        return pID;
    }
	
	private long initADL(SQLiteDatabase db, String adlCode, int adlType, String adlName, String adlDescription)
	{
		Log.i(CREATE_TAG, "initADL() -> " + adlCode);
		
		long adlID;
		String selection = "SELECT " + ADL_ID + " FROM " + ADL_TABLE_NAME+ " WHERE " + ADL_ID + " = '" + adlCode + "'";
		Cursor cursor = db.rawQuery(selection, null);
		if(cursor.getCount() != 0)
		{
			Log.w(TAG, "Warning: ADL Code is already in use: " + adlCode);
			return -1;
		}
		
        ContentValues values = new ContentValues();
        values.put(ADL_ID, adlCode);
        values.put(ADL_TYPE, adlType);
        values.put(ADL_NAME, adlName);
        values.put(ADL_DESCRIPTION, adlDescription);
        
        // Inserting Row
        adlID = db.insert(ADL_TABLE_NAME, null, values);
        return adlID;
    }

	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//		EXPORT METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	public boolean copyDatabase() { return copyDataBase(DB_SD_DB_FILE_NAME); }
	public boolean copyDataBase(String fileName)
	{
		if(debug) Log.i(TAG, "copyDataBase -> " + fileName);
		try 
		{ 
			File sdCard = Environment.getExternalStorageDirectory();
			File directory = new File (sdCard.getAbsolutePath() + DB_SD_DIR);
			
        	if(!sdCard.canWrite())
        	{
        		if(debug)
    				Log.e(TAG, "ERROR: SD card is not setup to write.");
        		return false;
        	}        	
        	//create the directory if it doesn't exist already
        	if(!directory.exists())
        		if(!directory.mkdirs())
        		{
        			if(debug)
        				Log.e(TAG, "ERROR: Directory could not be created.");
            		return false;
        		}
        	
        	File currentDB = new File(Environment.getDataDirectory(), DB_PATH);
    		File newDB = new File(directory, fileName);
    		
    		if (!currentDB.exists())
    		{
    			if(debug)
    				Log.e(TAG, "ERROR: No database object present.");
    			return false;
    		}
    		
    		FileChannel src = new FileInputStream(currentDB).getChannel();
    		FileChannel dst = new FileOutputStream(newDB).getChannel();
    		dst.transferFrom(src, 0, src.size());
    		src.close();
    		dst.close();
    		
    		return true;
		}
		catch (IOException ioe) 
		{
			if(debug)
				Log.e(TAG, "ERROR: IOException in save_data_btn_click() -> " + ioe.getMessage());
			
			ioe.printStackTrace();
			return false;
		}
	}
	
	
}

















