package com.app.reallygoodpie.ledvisualalizer.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import java.util.Arrays;

public class ColorGridModel {

    public static final int ROWS = 16;
    public static final int COLUMNS = 16;

    private int[][] colors;

    /**
     * Converts the integer color to an RGB form of RRRGGGBBB as a String
     * @param color Color to convert
     * @return      Color as RRRGGGBBB string
     */
    public static String getColorString(int color)
    {

        // Get Color RGB codes
        String red =  checkValidDeviceString(Color.red(color));
        String green =  checkValidDeviceString(Color.green(color));
        String blue =  checkValidDeviceString(Color.blue(color));

        return red + green + blue;
    }

    /**
     * Pads a number less than 3 digits with 0's and returns as string
     * @param n         number to convert to string
     * @return          number as string in 3 characters
     */
    public static String checkValidDeviceString(int n)
    {
        String strColor = String.valueOf(n);
        if (strColor.length() < 3)
        {
            int appendZeros = 3 - strColor.length();
            for (int i = 0; i < appendZeros; i++)
            {
                strColor = "0" + strColor;
            }
        }

        return strColor;
    }

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

    public int getPositionOnDevice(int index)
    {
        int row = index / ROWS;
        int column = index % COLUMNS;
        if  (row  % 2 == 0)
        {
            column = (COLUMNS - column) - 1;
        }

        return row + column;
    }

    public void setColor(@ColorInt int color, int index)
    {
        int row = index / ROWS;
        int column = index % COLUMNS;
        colors[row][column] = color;
    }

    @Override
    public String toString() {
        String returnString = "ColorGridModel {\n";
        for (int i = 0; i < COLUMNS; i ++)
        {
            returnString += Arrays.toString(colors[i]) + "\n";
        }
        return returnString;
    }
}

