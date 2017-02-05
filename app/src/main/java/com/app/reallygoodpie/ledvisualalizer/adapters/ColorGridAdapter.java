package com.app.reallygoodpie.ledvisualalizer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.app.reallygoodpie.ledvisualalizer.models.ColorGridModel;
import com.app.reallygoodpie.ledvisualalizer.R;

public class ColorGridAdapter extends BaseAdapter {

    private ColorGridModel colorGrid;
    private Context mContext;
    private LayoutInflater mInflater;

    public ColorGridAdapter(Context context, ColorGridModel colorGrid) {
        super();

        // Set the color grid
        this.colorGrid = colorGrid;

        // Get the inflate from context
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return colorGrid.getSize();
    }

    @Override
    public Object getItem(int i) {
        return colorGrid.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (convertView == null)
        {
            // Create new view
            convertView = mInflater.inflate(R.layout.column_element, null);

            viewHolder = new ViewHolder();
            viewHolder.colorGridElement = (Button) convertView.findViewById(R.id.color_element_button);

            convertView.setTag(viewHolder);
        }
        else
        {
            // Get recycled youtube view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the current color and set the background color
        int color = colorGrid.get(position);
        viewHolder.colorGridElement.setBackgroundColor(color);


        return convertView;
    }

    private class ViewHolder
    {
        Button colorGridElement;
    }
}
