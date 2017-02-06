package com.app.reallygoodpie.ledvisualalizer;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.app.reallygoodpie.ledvisualalizer.adapters.ColorGridAdapter;
import com.app.reallygoodpie.ledvisualalizer.models.ColorGridModel;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "MainActivity";

    public static final int REQUEST_BT_ENABLE = 1;

    // Information
    private ColorGridModel currentGrid;
    private int currentGlobalColor;
    private Map<Integer, Integer> colorMap;
    private String connectedDevice;

    // UI Elements
    private GridView gridView;
    private CheckBox brushChecKBox;
    private Button colorSelectButton, saveButton, fillButton;

    private Context mContext;
    private SpectrumDialog.Builder spectrumBuilder;
    private ColorGridAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        // Initialize bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedDevice = null;

        // Initialize the grid
        currentGrid = new ColorGridModel();
        currentGrid.init(ContextCompat.getColor(getApplicationContext(), R.color.md_blue_500)); // Default color to blue

        // Get UI elements
        gridView = (GridView) findViewById(R.id.color_gridview);
        brushChecKBox = (CheckBox) findViewById(R.id.brush_checkbox);

        colorSelectButton = (Button) findViewById(R.id.brush_color_button);
        colorSelectButton.setOnClickListener(this);

        fillButton = (Button) findViewById(R.id.fill_button);
        fillButton.setOnClickListener(this);

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        // Map the colors to their values
        colorMap = initializeColorMap();

        // Set the default color to blue
        currentGlobalColor = ContextCompat.getColor(mContext, R.color.md_green_500);

        // Initialize the spectrum color dialog
        spectrumBuilder = new SpectrumDialog.Builder(getApplicationContext())
                .setColors(R.array.main_colors)
                .setDismissOnColorSelected(true)
                .setOutlineWidth(2);


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
                    spectrumBuilder
                        .setSelectedColorRes(colorMap.get(currentColor))
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(boolean colorSelected, @ColorInt int color) {
                                if (colorSelected) {
                                    currentGrid.setColor(color, i);
                                    mAdapter.notifyDataSetInvalidated();
                                }
                            }
                         }).build().show(getSupportFragmentManager(), "color_chooser_fragment");
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
                spectrumBuilder
                    .setSelectedColorRes(colorMap.get(currentGlobalColor))
                    .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                            currentGlobalColor = color;
                        }
                    }).build().show(getSupportFragmentManager(), "color_chooser_fragment");
                break;
            // Fill the grid with one color
            case R.id.fill_button:
                currentGrid.init(currentGlobalColor);
                mAdapter.notifyDataSetChanged();
                break;
            // Send to bluetooth device
            case R.id.save_button:
                checkBluetoothEnabled();
                break;
        }
    }

    /**
     * Checks if the bluetooth on the device is enabled
     * If not enabled, create request to enable bluetooth
     */
    private void checkBluetoothEnabled()
    {

        // Check that device supports bluetooth
        if (mBluetoothAdapter != null)
        {
            if (!mBluetoothAdapter.isEnabled())
            {
                // Create request to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            }
            else
            {
                // Allow the user to select a paired device
                selectPairedDevice();
            }
        }
    }

    /**
     * Allow the user to select a paired device
     */
    private void selectPairedDevice()
    {
        if (connectedDevice == null)
        {
            // Get all paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // Map to handle name and mac address
                final Map<String, String> deviceMap = new ArrayMap<>();

                // Get all currently paired devices
                for (BluetoothDevice device : pairedDevices)
                {
                    // Add device to map
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); // MAC Address
                    deviceMap.put(deviceName, deviceAddress);
                }

                // Build alert dialog
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();

                // Load the alert view
                View convertView = (View) inflater.inflate(R.layout.device_list, null);
                ListView deviceListView = (ListView) convertView.findViewById(R.id.bluetooth_device_listview);

                // Set alert dialog properties
                alertDialogBuilder.setTitle("Select Paired Device");
                alertDialogBuilder.setView(convertView);

                // Create the adapter
                List<String> deviceNameList = new ArrayList<>();
                for (String key : deviceMap.keySet())
                {
                    deviceNameList.add(key);
                }
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNameList);
                deviceListView.setAdapter(adapter);

                // Create AlertDialog to allow for dismissible window (not available on builder)
                final AlertDialog alertDialog = alertDialogBuilder.show();

                alertDialogBuilder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        // Handled on list view on click listener
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // Dismiss the dialog
                        alertDialog.dismiss();
                    }
                });

                deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        // Extract information about the selected device
                        String selectedDevice = adapter.getItem(i);
                        String selectedDeviceMACAddress = deviceMap.get(selectedDevice);
                        Log.i(TAG, "Selected device " + selectedDevice + ":" + selectedDeviceMACAddress);

                        // Set the globally connected device
                        connectedDevice = selectedDeviceMACAddress;

                        // Dismiss the dialog
                        alertDialog.dismiss();
                    }
                });
            }
            else
            {
                // Need to pair device
                Toast.makeText(mContext, "No paired devices found", Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle requests by bluetooth
        if (requestCode == REQUEST_BT_ENABLE)
        {
            if (resultCode == RESULT_OK)
            {
                // Request to enable bluetooth successful
                // Allow the user to select a paired device
                selectPairedDevice();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // Request to enable bluetooth canceled by user
                Toast.makeText(mContext, "Failed to enable bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize color map for SpectrumDialog
     * Very dirty way of handling it however there doesn't seem to be any other option for binding
     * the color int to the actual color int (?)
     *
     * @return  Map of colors
     */
    private Map<Integer, Integer> initializeColorMap()
    {
        final Map<Integer, Integer> colorMap = new ArrayMap<>();
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_white), R.color.md_white);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_red_500), R.color.md_red_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_pink_500), R.color.md_pink_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_purple_500), R.color.md_purple_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_deep_purple_500), R.color.md_deep_purple_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_indigo_500), R.color.md_indigo_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_blue_500), R.color.md_blue_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_light_blue_500), R.color.md_light_blue_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_cyan_500), R.color.md_cyan_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_teal_500), R.color.md_teal_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_green_500), R.color.md_green_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_light_green_500), R.color.md_light_green_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_lime_500), R.color.md_lime_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_yellow_500), R.color.md_yellow_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_amber_500), R.color.md_amber_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_orange_500), R.color.md_orange_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_deep_orange_500), R.color.md_deep_orange_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_brown_500), R.color.md_brown_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_grey_500), R.color.md_grey_500);
        colorMap.put(ContextCompat.getColor(mContext, R.color.md_blue_grey_500), R.color.md_blue_grey_500);

        return colorMap;
    }



}
