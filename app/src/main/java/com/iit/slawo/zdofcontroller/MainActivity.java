package com.iit.slawo.zdofcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;


public class MainActivity extends ActionBarActivity {
    private enum ControlType {GYRO, TOUCH}
    private static final String TAG = "2DOFcontroller";
    private TextView StatusDisplay;
    BluetoothAdapter bluetooth;
    private String DEVICE_MAC_ADDR;
    private boolean testing=true;//teszteléshez
    BluetoothDevice TARGET_DEVICE;

    //private SurfaceView Display;
    private ControlType InputMode;
    private int Ts_ms=1000;
    private Handler handler = new Handler();

    final float[] mValuesMagnet      = new float[3];
    final float[] mValuesAccel       = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix    = new float[9];

    //Draw basic layout
    protected void DrawControlLayout(){
        /*TODO: draw basic controll surface on canvas*/
    }

    public void setListners(SensorManager sensorManager, SensorEventListener mEventListener){
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

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(testing){//tesztelés laptop MAC-al
            DEVICE_MAC_ADDR = "50:63:13:8B:47:56";
        }else{//éles MAC
            DEVICE_MAC_ADDR = "";
        }
        InputMode = ControlType.GYRO;

        //Gui elemek initje hogy kódból hívható legyen
        StatusDisplay = (TextView) findViewById(R.id.stats);
        //Display = (SurfaceView) findViewById(R.id.inputField);//unused

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
                //power on/of bluetooth
                if (bluetooth.isEnabled()) {
                    bluetooth.disable();
                }else{
                    bluetooth.enable();
                }
            break;
            case R.id.action_connection :
                //connect to device


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
    private class bluetoothStatus{
        public String power;
        public String connection;
        bluetoothStatus(){power="";connection="";}
        public void powerOn(){ power = "Power On";}
        public void powerOff(){ power = "Power Off";}
        public void connectionNotEstablished(){ connection = "No connection";}
        public void connectionEstablished(){ connection = "Connected to device";}
    }
    bluetoothStatus BTS = new bluetoothStatus();

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

    private void updateDataDisplay(){
        String s;

        s=String.format("Bluetooth Data:\n%s\n%s",BTS.power,BTS.connection);
        s=String.format("%s\nGYRO Data (rad):\ndiff X: %.6f\ndiff Y: %.6f",s,dX,dY);
        StatusDisplay.setText(s);
    }

    //időzített hurok
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* sensor poll */
            switch(InputMode){
                default:
                case GYRO:
                    pollOrientation();
                break;
                case TOUCH:

                break;
            }

            if (bluetooth.isEnabled()) {
                /*adat küldése */
                BTS.powerOn();


                /*adat fogadás */


            }else{
                BTS.powerOff();
                BTS.connectionNotEstablished();
            }
            /*fogadott adat feldolgozása */

            /* feldolgozás újrahívása */
            updateDataDisplay();
            handler.postDelayed(this, Ts_ms);
        }
    };


}
