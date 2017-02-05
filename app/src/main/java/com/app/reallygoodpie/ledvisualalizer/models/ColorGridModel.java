package com.app.reallygoodpie.ledvisualalizer.models;


import android.graphics.Color;
import android.support.annotation.ColorInt;

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
    public void init(@ColorInt int color)
    {
        for (int y = 0; y < ROWS; y ++)
        {
            for (int x = 0; x < COLUMNS; x ++)
            {
                // Default the color
                colors[y][x] = color;
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
        int row = index / ROWS;
        int column = index % COLUMNS;
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

    public void setColor(@ColorInt int color, int index)
    {
        int row = index / ROWS;
        int column = index % COLUMNS;
        colors[row][column] = color;
    }
}
