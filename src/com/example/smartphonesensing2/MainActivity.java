package com.example.smartphonesensing2;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartphonesensing2.DB.ActivityTable;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
	
	private Sensor accelerometer;
	private SensorManager sm;
//	private EditText acceleration;
	private String activity = "Stil";

	private float mlastX, mlastY, mlastZ;
	
	// ???
	private boolean mInitialized;
	
	// ???
	private final float NOISE = (float)2.0;
	
	private DB activityDB;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInitialized = false;

		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		activityDB = new DB(getApplicationContext());
		//acceleration = (EditText) findViewById(R.id.acceleration);
	}

	
	protected void onResume(){
		
		super.onResume();
		sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
		
	}
	
	protected void onPause(){
		
		super.onPause();
		sm.unregisterListener(this);
		
		
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		activityDB.close();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
/*		acceleration.setHint("X: "+ event.values[0]
				+"\nY: "+ event.values[1]
				+"\nZ:"+ event.values[2]
				);
*/
		
		TextView tvX = (TextView)findViewById(R.id.x_axis);
		TextView tvY = (TextView)findViewById(R.id.y_axis);
		TextView tvZ = (TextView)findViewById(R.id.z_axis);
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		if(!mInitialized)
		{
			mlastX = x ; 
			mlastY = y ;
			mlastZ = z ;
			
			
			tvX.setText(Float.toString(mlastX));
			tvY.setText(Float.toString(mlastY));
			tvZ.setText(Float.toString(mlastZ));
			
//			mInitialized = true;
		} else {	
			
			float deltaX = Math.abs(mlastX - x);
			float deltaY = Math.abs(mlastY - y);
			float deltaZ = Math.abs(mlastZ - z);
	
			if(deltaX < NOISE) deltaX = (float)0.0;
			if(deltaY < NOISE) deltaY = (float)0.0;
			if(deltaZ < NOISE) deltaZ = (float)0.0;
		
			mlastX = x ; 
			mlastY = y ;
			mlastZ = z ;
			
			
			tvX.setText(Float.toString(deltaX));
			tvY.setText(Float.toString(deltaY));
			tvZ.setText(Float.toString(deltaZ));
		}
	}
	
	
	public void trainSitActivity(View view){
		TextView debug = (TextView) findViewById(R.id.debugView);
		debug.setText("a");
		storeCoordinates();
		
//		debug.setText("b");
		
		showCoordinates();
		
//		debug.setText("c");
		
		Button b = (Button) view;
		
		if(b.getText().equals("Start sitting")) {
			// When this button is pressed to start the variable activity
			// is set to "sit" and the text shown in the button is changed to
			// "Stop sitting"

			activity = "sit";
			b.setText("Stop sitting");
		}
		else {
			// When this button is pressed to stop the variable activity
			// is set to "none" and the text shown on the button is changed to
			// "Start sitting"

			activity = "none";
			b.setText("Start sitting");
		}
	}
	
	
	public void trainWalkActivity(View view) {
		Button b = (Button) view;
		
		if(b.getText().equals("Start walking")){
			// When this button is pressed to start the variable activity
			// is set to "walk" and the text shown on the button is changed to
			// "Stop walking"
			
			activity = "walk";
			b.setText("Stop walking");
		}
		else {
			// When this button is pressed to stop the variable activity
			// is set to "none" and the text shown on the button is changed to
			// "Start walking"
			
			activity = "none";
			b.setText("Start walking");
		}
	}
	
	
	public void trainRunActivity(View view) {
		Button b = (Button) view;
		
		if(b.getText().equals("Start running")){
			// When this button is pressed to start the variable activity
			// is set to "run" and the text shown in the button is changed to
			// "Stop running"
			
			activity = "run";
			b.setText("Stop running");
		}
		else {
			// When this button is pressed to stop the variable activity
			// is set to "none" and the text shown on the button is changed to
			// "Start running"
			
			activity = "none";
			b.setText("Start running");
		}
	}
	
	
	public void testActivity(View view) {
		Button b = (Button) view;
		
		if(b.getText().equals("Start testing")){
			// When this button is pressed to start the variable activity
			// is set to "test" and the text shown in the button is changed to
			// "Stop testing"
			
			activity = "test";
			b.setText("Stop testing");
		}
		else {
			// When this button is pressed to stop the variable activity
			// is set to "none" and the text shown on the button is changed to
			// "Start testing"
			
			activity = "none";
			b.setText("Start testing");
		}
	}
	
	
	public void storeCoordinates(){
		// debug view
		TextView debug = (TextView) findViewById(R.id.debugView);
		SQLiteDatabase db = null;
		debug.setText("1");
		
		// Insert the values into the database
		
		try{
			db = activityDB.getWritableDatabase();
		}
		catch(SQLException e){
			debug.setText("\n\nErrror Store: "+ e.getMessage() +"\n\n");
		}
		

		ContentValues values = new ContentValues();

//		values.put(ActivityTable._ID, " ");
		values.put(ActivityTable.FIELD_X, Float.toString(mlastX));
		values.put(ActivityTable.FIELD_Y, Float.toString(mlastY));
		values.put(ActivityTable.FIELD_Z, Float.toString(mlastZ));
		values.put(ActivityTable.FIELD_ACTIVITY, activity);

		long rowId;

		rowId = db.insert(ActivityTable.TABLE_NAME, null, values);
		
		debug.setText("rowId: "+ rowId);
	}
	
	
	public void showCoordinates(){
		// Read values from database
		
		TextView showStoredCoordinates = (TextView) findViewById(R.id.showStoredCoodinates);
		SQLiteDatabase db = null;
		TextView debug = (TextView) findViewById(R.id.debugView);
		
		
		try{
			db = activityDB.getReadableDatabase();
		}
		catch(SQLException e){
			debug.setText("\n\nErrror showCoordiantes: "+ e.getMessage() +"\n\n");
		}
		

		String[] data = {
				ActivityTable.FIELD_ID,
				ActivityTable.FIELD_X,
				ActivityTable.FIELD_Y,
				ActivityTable.FIELD_Z,
				ActivityTable.FIELD_ACTIVITY
		};

		
		String where = ActivityTable.FIELD_X +" = "+ mlastX +" AND "+ 
				ActivityTable.FIELD_Y +" = "+ mlastY +" AND "+ 
				ActivityTable.FIELD_Z +" = "+ mlastZ;
		

		String orderBy = ActivityTable.FIELD_ACTIVITY + " ASC";


		Cursor c = db.query(ActivityTable.TABLE_NAME,		// Name of the table 
				data, 								// Fields to be fetched
				null,								// where-clause
				null, 								// arguments for the where-clause
				null, 								// groupBy
				null, 								// having
				null								// orderBy
				);

		
		// Read the values in each field
		c.moveToFirst();
		String dataX = c.getString(c.getColumnIndex(ActivityTable.FIELD_X));
		String dataY = c.getString(c.getColumnIndex(ActivityTable.FIELD_Y));
		String dataZ = c.getString(c.getColumnIndex(ActivityTable.FIELD_Z));
		String dataActivity = c.getString(c.getColumnIndex(ActivityTable.FIELD_ACTIVITY));
		
		
		// show the stored coordinates in db
		showStoredCoordinates.setText("X: "+ dataX +
				" Y: "+ dataY +
				" Z: "+ dataZ +
				" Activity "+ dataActivity);
		
	}

}
