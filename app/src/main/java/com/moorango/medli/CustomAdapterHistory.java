package com.moorango.medli;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class CustomAdapterHistory extends BaseAdapter {

    Context context;
    List<MedLog> data;

    CustomAdapterHistory(Context context, List<MedLog> rowItem) {
        this.context = context;
        this.data = rowItem;

    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Object getItem(int position) {

        return data.get(position);
    }

    @Override
    public long getItemId(int position) {

        return data.indexOf(getItem(position));
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
        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        MedLog dataItem = data.get(position);

        if (dataItem.isSubHeading()) {

            txtTitle.setText(dt.getReadableDate(dataItem.getDateOnly()));
            boxWrapper.setBackgroundResource(android.R.color.background_light);

        } else {
            txtTitle.setText(VerifyHelpers.capitalizeTitles(dataItem.getName()) + " " + dt.convertToTime12(dataItem.getTimeOnly()));
            boxWrapper.setBackgroundResource(android.R.color.white);

        }
        return convertView;

    }

}
