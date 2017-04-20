package com.pablo.test2opencv;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pablo.test2opencv.filters.DetectionActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private static final String MY_SHARED_PREFERENCES = "Test2OpenCVSharedPreferences";

    // A TAG for log output.
    private static final String TAG = MainActivity.class.getSimpleName();

    // The indices of the active filters.
    private int mImageDetectionFilterIndex;

    // Keys for storing the indices of the active filters.
    private static final String STATE_IMAGE_DETECTION_FILTER_INDEX = "imageDetectionFilterIndex";

    //----------BT BEGIN------------------
    private BluetoothAdapter mBluetoothAdapter;
    //final String TAG = "BtTry";
    final String ROBOTNAME = "mastIRA"; //"NXT";//"ORT4";
    private final UUID SP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int REQUEST_ENABLE_BT = 1;

    // BT Variables
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket socket;
    private BluetoothDevice bd;
    private InputStream is = null;
    public static OutputStream os = null;
    private List<BluetoothDevice> deviceNames;

    //----------BT END------------------

    //recorrido
    private int stepsForward;

    private static final String STATE_STEPS_FORWARD = "stateStepsForward";


    private int processIndex;

    private static final String STATE_PROCESS_INDEX = "stateProcessIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mImageDetectionFilterIndex = prefs.getInt(STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
        stepsForward = prefs.getInt(STATE_STEPS_FORWARD, 0);
        processIndex = prefs.getInt(STATE_PROCESS_INDEX, 0);

        /*if (savedInstanceState != null) {
            mImageDetectionFilterIndex = savedInstanceState.getInt(STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
            stepsForward = savedInstanceState.getInt(STATE_STEPS_FORWARD, 0);
            processIndex = savedInstanceState.getInt(STATE_PROCESS_INDEX, 0);
        }else{
            mImageDetectionFilterIndex = 0;
            stepsForward = 0;
            processIndex = 0;
        }*/

        Intent intent1 = getIntent();
        Bundle extras = intent1.getExtras();
        if (extras != null) {

            if (extras.containsKey("templateX")) {
                double templateX = extras.getDouble("templateX");
                double templateY = extras.getDouble("templateY");
                double distanceX = extras.getDouble("distanceX");
                TextView connectedText = (TextView) findViewById(R.id.textView);
                connectedText.setText("Can(" + templateX + "," + templateY + ") DistX: " + distanceX);

                if (distanceX<50){
                    processIndex = 2;
                }
                try {
                    canCarryProcess();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        Button buttonCocaComun = (Button) findViewById(R.id.buttonCocaComun);
        Button buttonCocaLight = (Button) findViewById(R.id.buttonCocaLight);
        Button buttonFantaComun = (Button) findViewById(R.id.buttonFantaComun);
        Button buttonSpriteComun = (Button) findViewById(R.id.buttonSpriteComun);

        final Intent intent = new Intent(this, DetectionActivity.class);
        buttonCocaComun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processIndex = 1;
                mImageDetectionFilterIndex = 1;

                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_PROCESS_INDEX, processIndex);
                editor.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor.commit();

                intent.putExtra(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                MainActivity.this.startActivity(intent);

            }
        });

        buttonCocaLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processIndex = 1;
                mImageDetectionFilterIndex = 2;

                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_PROCESS_INDEX, processIndex);
                editor.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor.commit();

                intent.putExtra(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                MainActivity.this.startActivity(intent);
            }
        });

        buttonFantaComun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processIndex = 1;
                mImageDetectionFilterIndex = 3;

                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_PROCESS_INDEX, processIndex);
                editor.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor.commit();

                intent.putExtra(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                MainActivity.this.startActivity(intent);
            }
        });

        buttonSpriteComun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processIndex = 1;
                mImageDetectionFilterIndex = 4;

                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_PROCESS_INDEX, processIndex);
                editor.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor.commit();

                intent.putExtra(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                MainActivity.this.startActivity(intent);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processIndex = 0;
                mImageDetectionFilterIndex = 0;
                stepsForward = 0;

                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_PROCESS_INDEX, processIndex);
                editor.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor.putInt(STATE_STEPS_FORWARD, stepsForward);
                editor.commit();

            }
        });

        //----------BT BEGIN------------------
        //Create handle to bluetooth and check for device support
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {  //yes, bluetooth is supported
            if (!mBluetoothAdapter.isEnabled()) {
                //bluetooth is off; prompt user to connect
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                //bluetooth is on
            }
        }

        if (os == null) {
            connectNXT();
        }
        //----------BT END------------------
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current filter index, stepsForward and process index
        /*savedInstanceState.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
        savedInstanceState.putInt(STATE_STEPS_FORWARD, stepsForward);
        savedInstanceState.putInt(STATE_PROCESS_INDEX, processIndex);*/

        super.onSaveInstanceState(savedInstanceState);
    }



    //------------BT
    public void connectNXT() {
        try	{
            //On click, get a list of connected devices
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                Iterator<BluetoothDevice> it = pairedDevices.iterator();
                while (it.hasNext()) {
                    bd = it.next();
                    Log.i(TAG,"Name of peer is [" + bd.getName() + "]");
                    try {
                        deviceNames.add(bd);
                        Log.i(TAG,"Item was added to list");
                    }
                    catch (Exception e){
                        Log.i(TAG,"No idea what is wrong with list");
                    }
                    if (bd.getName().equalsIgnoreCase(ROBOTNAME)) {
                        Log.i(TAG, "Found "+ bd.getName() + " with ID " + bd.getAddress());
                        Log.i(TAG,bd.getBluetoothClass().toString());
                        try {
                            socket = bd.createRfcommSocketToServiceRecord(SP_UUID);
                            socket.connect();
                        }
                        catch (Exception e) {
                            Log.e(TAG,"Error interacting with remote device -> " + e.getMessage());
                        }

                        try {
                            is = socket.getInputStream();
                            os = socket.getOutputStream();
                        } catch (Exception e) {
                            is = null;
                            os = null;
                            disconnectNXT(null);
                        }
                    }
                }
                return;
            }
        }
        catch (Exception e) 	{
            Log.e(TAG,"Failed in finding NXT -> " + e.getMessage());
        }
    }

    public void disconnectNXT(View v) {
        try {
            Log.i(TAG,"Attempting to break BT connection of " + bd.getName());
            socket.close();
            is.close();
            os.close();
            Log.i(TAG, "BT connection of " + bd.getName() + " is disconnected");
        }
        catch (Exception e)	{
            Log.e(TAG,"Error in disconnect -> " + e.getMessage());
        }
    }

    public void stopNXT(View v) {
        MoveMotor(0, 0, 0x00);
        MoveMotor(1, 0, 0x00);
        MoveMotor(2, 0, 0x00);
    }

    public void forward() {
        MoveMotor(0, -25, 0x20); //0=A 1=B 2=C
    }

    public void backward() throws InterruptedException {
        for (int i=0;i<(stepsForward+4);i++){
            MoveMotor(0, 50, 0x20); //0x40- RampDown
            sleep(500);
        }
    }

    public void armOut() throws InterruptedException {
        for (int i=0; i<5; i++){
            MoveMotor(1, 50, 0x20);
            sleep(500);
        }
    }

    public void armIn() throws InterruptedException {
        for (int i=0; i<5; i++){
            MoveMotor(1, -50, 0x20);
            sleep(500);
        }
    }

    public void clampClose() {
        MoveMotor(2, -75, 0x20);
    }

    public void clampOpen() {
        MoveMotor(2, 75, 0x20);
    }

    /*
    * Motor A (0) desplazamiento
    * Motor B (1) brazo
    * Motor C (2) pinza
    * */
    private void MoveMotor(int motor,int speed, int state) {
        try {
            Log.i(TAG,"Attempting to move [" + motor + " @ " + speed + "]");

            byte[] buffer = new byte[15];

            buffer[0] = (byte) (15-2);			//length lsb
            buffer[1] = 0;						// length msb
            buffer[2] =  0;						// direct command (with response)
            buffer[3] = 0x04;					// set output state
            buffer[4] = (byte) motor;			// output 1 (motor B)
            buffer[5] = (byte) speed;			// power
            buffer[6] = 0x07;                   //Motor ON with Break and Regulated mode
            buffer[7] = 0;						// regulation
            buffer[8] = 0;						// turn ration??
            buffer[9] = (byte) state; 			// run state 0x20;
            buffer[10] = (byte) 0x2D;           //0 tacho limit 2D = 45 grados
            buffer[11] = (byte) 0x00;           //0 tacho limit
            buffer[12] = (byte) 0x00;           //0 tacho limit
            buffer[13] = (byte) 0x00;           //0 tacho limit
            buffer[14] = 0;

            os.write(buffer);
            os.flush();

        }
        catch (Exception e) {
            Log.e(TAG,"Error in MoveForward(" + e.getMessage() + ")");
        }
    }
    //-----------BT END

    /*
    0 - nada
    1 - se cliqueo un sabor -> mover motor 1 paso y buscar matching
    2 - lata posicionada frente a pinza, dar 1 paso mas, extender brazo, tomar lata, contraer brazo,
    dar pasos atrás, extender brazo soltar lata, contraer brazo

     */
    public void canCarryProcess() throws InterruptedException {
        switch (processIndex){
            case 1:
                forward();

                stepsForward++;
                SharedPreferences prefs = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(STATE_STEPS_FORWARD, stepsForward);
                editor.commit();

                final Intent intent = new Intent(this, DetectionActivity.class);
                intent.putExtra(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                MainActivity.this.startActivity(intent);
                break;
            case 2:
                forward();
                sleep(500);
                armOut();
                sleep(500);
                clampClose();
                sleep(500);
                armIn();
                sleep(500);
                backward();
                sleep(500);
                armOut();
                sleep(500);
                clampOpen();
                sleep(500);
                armIn();
                sleep(500);

                processIndex = 0;
                mImageDetectionFilterIndex = 0;
                stepsForward = 0;

                SharedPreferences prefs2 = getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = prefs2.edit();
                editor2.putInt(STATE_PROCESS_INDEX, processIndex);
                editor2.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
                editor2.putInt(STATE_STEPS_FORWARD, stepsForward);
                editor2.commit();
                break;
        }

    }
}