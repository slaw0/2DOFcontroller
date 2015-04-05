package com.iit.slawo.zdofcontroller;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private enum ControlType {GYRO, TOUCH}
    private static final String TAG = "2DOFcontroller";
    private TextView StatusDisplay;

    //Bluetooth variables
    private BluetoothAdapter bluetooth = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private BluetoothDevice TARGET_DEVICE;

    //SPP connection parameters
    private String DEVICE_MAC_ADDR = "00:00:00:00:00:00";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//SPP profile UUID
    private static final boolean testing = true;//testing flag
    //PIN Has to be set in the device code too.

    //private SurfaceView Display;
    private ControlType InputMode;
    //Sampling(update) time
    private int Ts_ms=1000;

    //GYRO Sensor raw data containers
    private Handler handler = new Handler();
    final float[] mValuesMagnet      = new float[3];
    final float[] mValuesAccel       = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix    = new float[9];

    //Graphic feedback drawer
    protected void DrawControlLayout(){
        /*TODO: draw basic controll surface on canvas*/
    }

    //Initiating sensor monitoring
    public void setListners(SensorManager sensorManager, SensorEventListener mEventListener){
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
        System.exit(0);
    }

    //Runs when app starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide notification area
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //init sensors
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
        if(testing){//MAC for testing eg. with laptop
            DEVICE_MAC_ADDR = "50:63:13:8B:47:56";
        }else{//device MAC
            DEVICE_MAC_ADDR = "00:00:00:00:00:00";
        }
        InputMode = ControlType.GYRO;

        //initialization of GUI members
        StatusDisplay = (TextView) findViewById(R.id.stats);
        //Display = (SurfaceView) findViewById(R.id.inputField);//unused

        //First call of the cyclic processing function
        handler.postDelayed(runnable, Ts_ms);
    }

    private void btConnect(String context){
        Log.d(TAG, "..."+context+" - Attempting client connect...");

        // Set up a pointer to the remote node using it's address.
        TARGET_DEVICE = bluetooth.getRemoteDevice(DEVICE_MAC_ADDR);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            //TODO: Figure out how to programatically hide pin request dialog
            btSocket = TARGET_DEVICE.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        bluetooth.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    private void btDisconnect(String context){
        Log.d(TAG, "..."+context+"...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            if( btSocket != null) btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
        btSocket = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bluetooth.isEnabled()) {
            btConnect("In onResume()");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluetooth.isEnabled()){
            btDisconnect("In onPause()");
        }
    }

    //Creating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //sending function
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (DEVICE_MAC_ADDR.equals("00:00:00:00:00:00")) msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + SPP_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    //Menu navigation and interaction
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
                btDisconnect("exit button");
                handler.removeCallbacks(runnable);//stopping the timed cycle
                //finish();
                System.exit(0);
            break;
            case R.id.action_bluetooth :
                //power on/of bluetooth
                if (bluetooth.isEnabled()) {
                    btDisconnect("btPower button");
                    bluetooth.disable();
                }else{
                    bluetooth.enable();
                }
            break;
            case R.id.action_connection :
                //connect to device
                if(btSocket == null){
                    btConnect("Menu button call");
                }else{
                    btDisconnect("Menu button call");
                }

            break;
            default:
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Gyro movement displacement variables
    private double oldX, oldY;
    private double newX, newY;
    private double dX, dY;
    private boolean flag=false;
    //bt status recorder
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

    //Orientation Polling
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

    //Displayed information updater
    private void updateDataDisplay(){
        String s;

        s=String.format("Bluetooth Data:\n%s\n%s",BTS.power,BTS.connection);
        s=String.format("%s\nGYRO Data (rad):\ndiff X: %+.6f\ndiff Y: %+.6f",s,dX,dY);
        s=String.format("%s\nGYRO Data (deg):\ndiff X: %+.6f\ndiff Y: %+.6f",s,Math.toDegrees(dX),Math.toDegrees(dY));
        StatusDisplay.setText(s);
    }

    //Scaling the Gyro measurement to desired input range
    private double incX, incY;
    private void ScaleMeasurements(){
        switch(InputMode){
            default:
            case GYRO:
                incX = dX / 0.01;
                incY = dY / 0.01;
            break;
            case TOUCH:

            break;
        }

    }

    //timed cycle
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* sensor poll */
            switch(InputMode){
                default:
                case GYRO:
                    pollOrientation();
                    ScaleMeasurements();
                break;
                case TOUCH:

                break;
            }

            if (bluetooth.isEnabled()) {
                BTS.powerOn();
                if(btSocket != null) {
                    BTS.connectionEstablished();
                    /*send data */
                        sendData(String.format("%+f%+f",incX,incY));

                    /*receive data */


                    /*process incoming data*/

                }else{
                    BTS.connectionNotEstablished();
                }
            }else{
                BTS.powerOff();
                BTS.connectionNotEstablished();
            }

            updateDataDisplay();
            /* recall the timed cycle */
            handler.postDelayed(this, Ts_ms);
        }
    };


}
