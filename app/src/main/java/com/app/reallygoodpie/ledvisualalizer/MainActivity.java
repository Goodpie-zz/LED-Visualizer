package com.app.reallygoodpie.ledvisualalizer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import com.app.reallygoodpie.ledvisualalizer.adapters.ColorGridAdapter;
import com.app.reallygoodpie.ledvisualalizer.models.ColorGridModel;

public class MainActivity extends AppCompatActivity {

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
        ColorGridAdapter adapter = new ColorGridAdapter(getApplicationContext(), currentGrid);
        gridView.setAdapter(adapter);
    }
}
