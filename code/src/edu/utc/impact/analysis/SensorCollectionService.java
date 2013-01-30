package edu.utc.impact.analysis;

import edu.utc.impact.data.DBHelper;
import edu.utc.impact.utils.KVector;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import java.util.ArrayList;
import android.util.Log;
import android.widget.Toast;

public class SensorCollectionService extends Service implements SensorEventListener
{
	private static final String tag = "FallDetection.SensorCollectionService";
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
	
	public SensorCollectionService() 
	{
		super();
		if (!isRunning) 
			isRunning = true;
		
		if (debug) Log.i(tag, "SensorService() - start");
	}
	
	@Override
	public void onCreate()
	{
		if (debug) Log.i(tag, "onCreate()");
		
		curr_vector = new KVector();
		prev_vector = new KVector();
		
		vectorList = new ArrayList<KVector>();
		
		curr_ori = 0;
		prev_ori = 0;
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		
		super.onCreate();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		//Auto-generated method stub
		if (debug) 
			Log.i(tag, "SensorListener.onAccuracyChanged() -> " + accuracy);
		this.accuracy = accuracy;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if (debug) Log.i(tag, "onStartCommand()");
		Bundle extras = intent.getExtras();
		this.uID = extras.getLong("uID");
		this.deID = extras.getLong("deID");
		
		db = new DBHelper(this);
		db.updateDataEventPositionEnd(this.deID, DBHelper.DATA_EVENT_RUNNING);
		Toast.makeText(this, "Service Collection Service started!", Toast.LENGTH_SHORT).show();
		
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    //return START_NOT_STICKY;
		return START_STICKY;
	}
	
	public void onDestroy()
	{
		if (debug) Log.i(tag, "onDestroy()");
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
	
	@Override
	public void onSensorChanged(final SensorEvent se) 
	{
		//se.timestamp returns a time in nanoseconds. Why divide it by 1000? The timestamps ought to be in nanoseconds, not microseconds...
		long timestamp = (long) se.timestamp / 1000;
		this.accuracy = se.accuracy;
		
		if (se.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
		{
			if(debug)
				Log.e(tag, "Sensor not accelerometer: " + se.sensor.getType());
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
	
	public long getUID () { return this.uID; }
	public long getDEID () { return this.deID; }
	
	public static String getMsg() { return ""; }
	
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	
	public class MyBinder extends Binder 
	{
		public SensorCollectionService getService() 
		{
			return SensorCollectionService.this;
		}
	}	
}














