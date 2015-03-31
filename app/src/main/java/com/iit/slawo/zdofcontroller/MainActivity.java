package com.iit.slawo.zdofcontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private enum ControlType {GYRO, TOUCH}

    private TextView StatusDisplay;
    //private SurfaceView Display;
    private ControlType InputMode;
    private int Ts_ms=100;
    private Handler handler = new Handler();

    final float[] mValuesMagnet      = new float[3];
    final float[] mValuesAccel       = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix    = new float[9];

    //Draw basic layout
    protected void DrawControlLayout(){
        /*TODO: draw basic controll surface on canvas*/
    }

    public void setListners(SensorManager sensorManager, SensorEventListener mEventListener)
    {
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Runs when app starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hide notification area
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //init sensor
        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        final SensorEventListener mEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}

            public void onSensorChanged(SensorEvent event) {
                // Handle the events for which we registered
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                        break;
                }
            }
        };

        setListners(sensorManager, mEventListener);

        DrawControlLayout();
        InputMode = ControlType.GYRO;

        //Gui elemek initje hogy kódból hívható legyen
        StatusDisplay = (TextView) findViewById(R.id.stats);
        Display = (SurfaceView) findViewById(R.id.inputField);

        handler.postDelayed(runnable, Ts_ms);
    }

    //Creates the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Runs when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Menu click events
        switch (id){
            case R.id.action_exit :
                //exit
                handler.removeCallbacks(runnable);
                finish();
            break;
            case R.id.action_bluetooth :
                //open bluetooth menu
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings","com.android.settings.bluetooth.BluetoothSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent);
            break;
            default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private double oldX, oldY;
    private double newX, newY;
    private double dX, dY;
    private boolean flag=false;

    private void pollOrientation(){
        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
        newX = mValuesOrientation[1];
        newY = mValuesOrientation[2];

        if(!flag){//first poll
            dX=0;
            dY=0;
            flag=true;
        }else{
            dX=newX-oldX;
            dY=newY-oldY;
        }
        oldX=newX;
        oldY=newY;
    }

    private String s="";
    //időzített hurok
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* sensor poll */
            switch(InputMode){
            default:
                case GYRO:
                    pollOrientation();
                    s=String.format("diff X: %.6f\ndiff Y: %.6f",dX,dY);
                    StatusDisplay.setText(s);
                break;
                case TOUCH:

                break;
            }

            /*adat küldése */


            /* feldolgozás újrahívása */
            handler.postDelayed(this, Ts_ms);
        }
    };


}
