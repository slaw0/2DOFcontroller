package com.iit.slawo.zdofcontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class MainActivity extends ActionBarActivity {
    //Draw basic
    protected void DrawControlLayout(){
        /*TODO: draw basic controll surface on canvas*/
    }

    //Runs when app starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hide notification area
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*TODO: Init canvas, input fields and display fields*/
        DrawControlLayout();
    }

    //When the app is paused, by another app
//    @Override
//    protected void onStart(){
//        /*TODO: if connection up (settings) then send message controll active.*/
//          /*TODO: if controll active begin.*/
//      }


    //When the app is stopped
//    @Override
//    protected void onStop(){
//        /*TODO: send message controll inactive.*/
//    }
    //When the app is paused, by another app
//    @Override
//    protected void onRestart(){
//        /*TODO: send message controll active.*/
//    }

    //When the app resumed from another app
//    @Override
//    protected void onDestroy(){
//        /*TODO: if Bluetooth connection alive send message disconnect and disconnect.*/
//    }

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
            case R.id.action_connection :
                /*TODO: Set Up connection*/
                break;
            case R.id.action_controller :
                /*TODO: Set up input method*/
                break;
            case R.id.action_aware :
                /*TODO:Set controller awareness timer*/
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
