package com.moorango.medli;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends BaseAdapter {

    Context context;
    List<MedLog> rowItem;

    CustomAdapter(Context context, List<MedLog> rowItem) {
        this.context = context;
        this.rowItem = rowItem;

    }

    @Override
    public int getCount() {

        return rowItem.size();
    }

    @Override
    public Object getItem(int position) {

        return rowItem.get(position);
    }

    @Override
    public long getItemId(int position) {

        return rowItem.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item, null);
        }
        MakeDateTimeHelper dt = new MakeDateTimeHelper();

        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        MedLog row_pos = rowItem.get(position);
        // setting the image resource and title
        if (row_pos.isSubHeading()) {

            Date date = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            try {
                date = sdf.parse(row_pos.getTimestamp().split(" ")[0]);
            } catch (ParseException p) {

            }
            String dateSplit[] = date.toString().split(" ");
            txtTitle.setText(dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2]);
            txtTitle.setBackgroundResource(android.R.color.darker_gray);

            ViewGroup.LayoutParams lp = txtTitle.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            txtTitle.setLayoutParams(lp);

        } else {
            txtTitle.setText(row_pos.getName() + " " + dt.convertToTime12(row_pos.getTimestamp().split(" ")[1]));
            txtTitle.setBackgroundResource(android.R.color.white);
            ViewGroup.LayoutParams lp = txtTitle.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            txtTitle.setLayoutParams(lp);
        }
        return convertView;

    }

}
