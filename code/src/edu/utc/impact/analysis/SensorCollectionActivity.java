package edu.utc.impact.analysis;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import edu.utc.impact.R;
import edu.utc.impact.data.DBHelper;
import edu.utc.impact.utils.KVector;
//import falldetection.hig.no.FallDetection;
//import falldetection.hig.no.SensorService;

public class SensorCollectionActivity extends Activity implements SensorEventListener
{
	private static final String TAG = "FallDetection.SensorCollectionService";
	private static final boolean debug = true;
	
	private static KVector curr_vector;
	private static KVector prev_vector;
	
	/**
	 * This is the scalar orientation between curr_ori and prev_ori.
	 */
	private static float curr_ori;
	/**
	 * This holds the value of the last calculated orientation.
	 */
	private static float prev_ori;
	
	//set to true when the Sensor Service is currently running.
	private static boolean isRunning = false;
	
	private static SensorManager sensorManager;
	private static Sensor accelerometer;
	
	//1 millisecond=1.0*10^(3) microseconds
	public static final double MICRO_TO_MILLI= 0.001;
	
	private int accuracy;
	private long uID, deID;
	private DBHelper db;
	
	private ArrayList<KVector> vectorList;

	
	public void onCreate(Bundle savedInstanceState) 
	{
		if (debug)
			Log.i(TAG, "oncreate() started");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		curr_ori = 0;
		prev_ori = 0;
		curr_vector = new KVector();
		prev_vector = new KVector();
		vectorList = new ArrayList<KVector>();
		
		isRunning = false;
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	
	
	
	
	
	
	@Override
	public void onSensorChanged(final SensorEvent se) 
	{
		//se.timestamp returns a time in nanoseconds. Why divide it by 1000? The timestamps ought to be in nanoseconds, not microseconds...
		long timestamp = (long) se.timestamp / 1000;
		this.accuracy = se.accuracy;
		
		if (se.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
		{
			if(debug)
				Log.e(TAG, "Sensor not accelerometer: " + se.sensor.getType());
			return;
		}
		
		
		//save the last vector
		prev_vector.set(curr_vector);
		if(prev_vector.getTimeStamp() <= 0) 
			prev_vector.setTimeStamp(timestamp);
		prev_ori = curr_ori;
		
		/**
		 * All values are in SI units (m/s^2)
		 * 	values[0]: Acceleration minus Gx on the x-axis
		 * 	values[1]: Acceleration minus Gy on the y-axis
		 * 	values[2]: Acceleration minus Gz on the z-axis 
		*/
		curr_vector.set(se.values[0], se.values[1], se.values[2]);
		curr_vector.setTimeStamp(timestamp);
		
		//Orientation between position at timestamp x and timestamp (x-1)
		//Note: The first iteration, prev_vector will be 0;
		curr_ori = curr_vector.orientation(prev_vector);
		
		
		vectorList.add(curr_vector.copy());
		
		//db.addDataItem(this.deID, curr_vector.getTimeStamp(), se.accuracy, curr_vector.getX(), curr_vector.getY(), curr_vector.getZ(), curr_vector.getRSS());
		
	}


	public void export_data_btn_click(View v)
	{
		if (debug) 
			Log.i(TAG, "export_data_btn_click()");
		
		if(db.copyDatabase())
			Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show();
		
		
		
		/*
		try 
		{ 
			File sdCard = Environment.getExternalStorageDirectory();
			File directory = new File (sdCard.getAbsolutePath() + DBHelper.DB_SD_DIR);
			
        	if(!sdCard.canWrite())
        	{
        		if(debug)
    				Log.e(TAG, "ERROR: SD card is not setup to write.");
        		return;
        	}        	
        	//create the directory if it doesn't exist already
        	if(!directory.exists())
        		if(!directory.mkdirs())
        		{
        			if(debug)
        				Log.e(TAG, "ERROR: Directory could not be created.");
            		return;
        		}
        	
        	File currentDB = new File(Environment.getDataDirectory(), DBHelper.DB_PATH);
    		File newDB = new File(directory, DBHelper.DB_SD_DB_FILE_NAME);
    		
    		if (!currentDB.exists())
    		{
    			if(debug)
    				Log.e(TAG, "ERROR: No database object present.");
    			return;
    		}
    		
    		FileChannel src = new FileInputStream(currentDB).getChannel();
    		FileChannel dst = new FileOutputStream(newDB).getChannel();
    		dst.transferFrom(src, 0, src.size());
    		src.close();
    		dst.close();
    		
    		Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
		}
		catch (IOException ioe) 
		{
			if(debug)
				Log.e(TAG, "ERROR: IOException in save_data_btn_click() -> " + ioe.getMessage());
			
			ioe.printStackTrace();
		}
		*/
	}




	
	
	
	//
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//	  DEFAULT OVERRIDE METHODS
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		//Auto-generated method stub
		if (debug) 
			Log.i(TAG, "SensorListenerActivity.onAccuracyChanged() -> " + accuracy);
		this.accuracy = accuracy;
	}
	
	@Override
	protected void onResume() { if (debug) Log.i(TAG, "onResume()"); super.onResume(); }

	@Override
	protected void onRestart() { if (debug) Log.i(TAG, "onRestart()"); super.onRestart(); }

	@Override
	protected void onPause() { if (debug) Log.i(TAG, "onPause()"); super.onPause(); }

	@Override
	protected void onStop() { if (debug) Log.i(TAG, "onStop()"); super.onStop(); }

	@Override
	public void onBackPressed() { if (debug) Log.i(TAG, "onBackPressed()"); super.onBackPressed(); }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (debug)
			Log.i(TAG, "onActivityResult() started -> RequestCode: "
					+ requestCode + "\tResultCode: " + resultCode);
	}

	@Override
	protected void onDestroy() 
	{
		if (debug) Log.i(TAG, "onDestroy()");
		isRunning = false;
		
		db.updateDataEventPositionEnd(this.deID, DBHelper.DATA_EVENT_STOPPED);
		sensorManager.unregisterListener(this);
		// Tell the user we stopped.
        Toast.makeText(this, "Sensor Collection Service Stopped", Toast.LENGTH_SHORT).show();
        
        if(!db.addDataItems(vectorList, deID, accuracy))
        	Toast.makeText(this, "Data Was Not Saved Successfully!", Toast.LENGTH_SHORT).show();
        else
        	Toast.makeText(this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
	}

	
	
}
