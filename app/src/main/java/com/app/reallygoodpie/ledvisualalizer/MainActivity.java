package com.app.reallygoodpie.ledvisualalizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Toast;

import com.app.reallygoodpie.ledvisualalizer.adapters.ColorGridAdapter;
import com.app.reallygoodpie.ledvisualalizer.models.ColorGridModel;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "MainActivity";
    public static final String APP_NAME = "com.brandyn.LEDVisualizer";
    private static final UUID MY_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Information
    private ColorGridModel currentGrid;
    private int currentGlobalColor;

    // UI Elements
    private GridView gridView;
    private CheckBox brushChecKBox;
    private Button colorSelectButton, saveButton, fillButton;
    private ColorPicker mColorPicker;

    private Context mContext;
    private ColorGridAdapter mAdapter;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private DataThread mConnectThread;
    private DataThread mDataThread;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectThread = null;
        mDataThread = null;

        mContext = getApplicationContext();

        mColorPicker = new ColorPicker(MainActivity.this, 33, 159, 243);

        // Initialize the grid
        currentGrid = new ColorGridModel();
        currentGrid.init(ContextCompat.getColor(getApplicationContext(), R.color.md_blue_500)); // Default color to blue

        // Get UI elements
        gridView = (GridView) findViewById(R.id.color_gridview);
        brushChecKBox = (CheckBox) findViewById(R.id.brush_checkbox);
        brushChecKBox.setChecked(true);

        colorSelectButton = (Button) findViewById(R.id.brush_color_button);
        colorSelectButton.setOnClickListener(this);

        fillButton = (Button) findViewById(R.id.fill_button);
        fillButton.setOnClickListener(this);

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        // Set the default color to green
        currentGlobalColor = ContextCompat.getColor(mContext, R.color.md_green_500);

        // Initialize the grid
        mAdapter = new ColorGridAdapter(getApplicationContext(), currentGrid);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Log.i(TAG, "Grid view clicked at position " + i);

                // Get the current color of the grid element
                int currentColor = currentGrid.get(i);

                // Check if painting
                boolean isPainting = brushChecKBox.isChecked();

                if (!isPainting) {
                    // Onclick listener to change the currently selected block
                    // Set the currently selected color
                    mColorPicker.show();
                    Button okColorSelection = (Button) mColorPicker.findViewById(R.id.okColorButton);
                    okColorSelection.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentGlobalColor = mColorPicker.getColor();
                            currentGrid.setColor(currentGlobalColor, i);
                            mColorPicker.dismiss();
                        }
                    });
                }
                else
                {
                    // Change the color of the selected grid element
                    currentGrid.setColor(currentGlobalColor, i);
                    mAdapter.notifyDataSetInvalidated();
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId)
        {
            // Allow the user to select another color for the global color
            case R.id.brush_color_button:
                mColorPicker.show();
                Button okColorSelection = (Button) mColorPicker.findViewById(R.id.okColorButton);
                okColorSelection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentGlobalColor = mColorPicker.getColor();
                        mColorPicker.dismiss();
                    }
                });
                break;
            // Fill the grid with one color
            case R.id.fill_button:
                currentGrid.init(currentGlobalColor);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.save_button:
                Log.i(TAG, "Saving...");
                Log.i(TAG, "Grid: " + currentGrid.toString());
                connectBluetooth();
                break;
        }
    }

    public void initBluetooth()
    {
        // Check if bluetooth can be enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            // Device is not supported
            Toast.makeText(mContext, "Device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }

        // Check if bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Discover Devices
        // Assume all other devices are disconnected
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                mDevice = device;
            }
        }
    }

    public void connectBluetooth()
    {
        Toast.makeText(mContext, "Attempting to connect to device...", Toast.LENGTH_LONG).show();
        // Ensure we have a device to connect to
        if (mDevice == null)
        {
            initBluetooth();
        }


        ConnectThread connectThread = new ConnectThread(mDevice);
        connectThread.run();

    }

    private class ConnectThread extends Thread
    {

        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            // Use a temp object that is later assigned to mmSocket
            BluetoothSocket tmpSocket = null;
            mmDevice = device;

            try {
                // Get a bluetooth socket to connect with the given BluetoothDevice
                tmpSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }

            mmSocket = tmpSocket;
        }

        public void run()
        {
            // Cancel discovery because it is slow af
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to remote device through the socket. This call will block until it
                // succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; Close the socket and return
                try {
                    Log.e(TAG, "Could not connect the socket. Attempting Fallback", connectException);
                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException fallbackException) {
                    Log.e(TAG, "Failed fallback", fallbackException);
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Failed to close socket", closeException);
                    }
                }
                return;
            }

            Toast.makeText(mContext, "Connected to: " + mDevice.getName(), Toast.LENGTH_LONG).show();
            DataThread dataThread = new DataThread(mmSocket);
            dataThread.run();
            dataThread.write("Hello World".getBytes());
        }

        public void cancel()
        {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

    }

    private class DataThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] writeBuf = (byte[]) msg.obj;
                int begin = (int)msg.arg1;
                int end = (int)msg.arg2;

                switch(msg.what) {
                    case 1:
                        String writeMessage = new String(writeBuf);
                        writeMessage = writeMessage.substring(begin, end);
                        break;
                }
            }
        };


        public DataThread(BluetoothSocket socket)
        {
            mmSocket = socket;

            // Create output stream
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occured when creating output stream", e);
            }

            mmOutStream = tmpOut;
        }

        public void run()
        {
            mmBuffer = new byte[1024];
            while (true)
            {

            }
            // Do nothing as we need no input stream
        }

        public void write(byte[] bytes)
        {
            try {
                mmOutStream.write(bytes);
                Message writtenMsg = mHandler.obtainMessage(0,
                        -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(1);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }


    }



}
