package com.app.reallygoodpie.ledvisualalizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "MainActivity";

    // Information
    private ColorGridModel currentGrid;
    private int currentGlobalColor;
    private Map<Integer, Integer> colorMap;

    // UI Elements
    private GridView gridView;
    private CheckBox brushChecKBox;
    private Button colorSelectButton, saveButton, fillButton;
    private ColorPicker mColorPicker;

    private Context mContext;
    private ColorGridAdapter mAdapter;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Toast.makeText(mContext, "Connected to: " + mDevice.getName(), Toast.LENGTH_LONG).show();
        mConnectThread = new ConnectThread(mDevice);
        mConnectThread.start();
    }

    private class ConnectThread extends Thread {

        private static final String TAG = "ConnectThread";

        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private final UUID MY_UUID;

        public ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            MY_UUID = UUID.randomUUID();
            mDevice = device;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Failed to create BluetoothSocket");
            }

            mSocket = tmp;
        }

        public void run()
        {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException connectException) {
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "run: Failed to close socket exception");
                }
            }
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "cancel: Failed to close socket exception");
            }
        }

    }


}
