package com.app.reallygoodpie.ledvisualalizer;

import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.app.reallygoodpie.ledvisualalizer.adapters.ColorGridAdapter;
import com.app.reallygoodpie.ledvisualalizer.models.ColorGridModel;
import com.thebluealliance.spectrum.SpectrumDialog;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private ColorGridModel currentGrid;

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the grid
        currentGrid = new ColorGridModel();
        currentGrid.init(); // Default colors

        // Get UI elements
        gridView = (GridView) findViewById(R.id.color_gridview);

        // Initialize the grid
        final ColorGridAdapter adapter = new ColorGridAdapter(getApplicationContext(), currentGrid);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Log.i(TAG, "Grid view clicked at position " + i);
                new SpectrumDialog.Builder(getApplicationContext())
                        .setColors(R.array.main_colors)
                        .setSelectedColorRes(R.color.md_blue_500)
                        .setDismissOnColorSelected(true)
                        .setOutlineWidth(2)
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                if (positiveResult) {
                                    currentGrid.setColor(color, i);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Dialog cancelled", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).build().show(getSupportFragmentManager(), "color_chooser_fragment");
            }
        });
    }
}
