package com.app.reallygoodpie.ledvisualalizer;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
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

import java.util.Map;

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

        // Set the default color to blue
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
        }
    }


}
