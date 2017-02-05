package com.app.reallygoodpie.ledvisualalizer.models;


import android.graphics.Color;

public class ColorGridModel {

    public static final int ROWS = 16;
    public static final int COLUMNS = 16;

    private int[][] colors;

    public ColorGridModel()
    {
        colors = new int[ROWS][COLUMNS];
    }

    /**
     * Set the default color for each index of the array
     */
    public void init()
    {
        for (int y = 0; y < ROWS; y ++)
        {
            for (int x = 0; x < COLUMNS; x ++)
            {
                // Default the colors to blue
                colors[y][x] = Color.BLUE;
            }
        }
    }

    /**
     * Get color at index of 2D array
     *
     * @param row       Row index
     * @param column    Column index
     * @return          @ColorInt color
     */
    public int get(int row, int column)
    {
        return colors[row][column];
    }

    /**
     * Get color at specific index
     *
     * @param index     Index in array
     * @return          @ColorInt color
     */
    public int get(int index)
    {
        int row = index / 16;
        int column = index % 16;
        return get(row, column);
    }

    /**
     * Return the size of the grid
     *
     * @return  size of the grid
     */
    public int getSize()
    {
        return COLUMNS * ROWS;
    }
}
