package com.moorango.medli.CustomViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Fragments.Fragment_History;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.MedLog;
import com.moorango.medli.R;

import java.util.List;

/**
 * Created by Colin on 9/5/2014.
 * Copyright 2014
 */
public class CustomAdapterHistory extends BaseAdapter {

    private final Context context;
    private final List<MedLog> data;
    private final MedLiDataSource dbHelper;
    private final Fragment_History caller;

    public CustomAdapterHistory(Context context, List<MedLog> rowItem, Fragment_History caller) {
        this.context = context;
        this.data = rowItem;
        this.caller = caller;
        dbHelper = MedLiDataSource.getHelper(this.context);
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        if (data.size() > 0)
            return data.get(position);
        else
            return null;

    }

    @Override
    public long getItemId(int position) {

        //noinspection SuspiciousMethodCalls
        return data.indexOf(getItem(position));
    }


    public void removeItem(int itemPosition) {

        data.remove(itemPosition);
        notifyDataSetChanged();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.history_list_item, null);
        }
        DateTime dt = new DateTime();

        TextView medName = (TextView) convertView.findViewById(R.id.name);
        final TextView doseTime = (TextView) convertView.findViewById(R.id.dose_time);
        final TextView dose = (TextView) convertView.findViewById(R.id.dose);
        final TextView message = (TextView) convertView.findViewById(R.id.message);

        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        ImageView delButton = (ImageView) convertView.findViewById(R.id.button_delete_med_admin);
        ImageView editButton = (ImageView) convertView.findViewById(R.id.button_edit_med_admin);

        final MedLog dataItem = data.get(position);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Toast toast = Toast.makeText(context, "Long press button to Edit Record", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(context, "Long Press button to delete Record", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        editButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                caller.editMedAdmin(dataItem);

                return true;
            }
        });

        delButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                dbHelper.deleteMedEntry(dataItem.getUniqueID());

                removeItem(position);

                Toast.makeText(context, "Entry Deleted", Toast.LENGTH_LONG).show();

                return true;
            }
        });

        if (dataItem.isSubHeading()) {

            medName.setText(dt.getReadableDate(dataItem.getDateOnly()));
            medName.setGravity(Gravity.CENTER);
            boxWrapper.setBackgroundResource(R.drawable.list_bg);

            editButton.setVisibility(View.GONE);
            delButton.setVisibility(View.GONE);

        } else {

            String messageNote = dataItem.timeFrame();
            medName.setText(DataCheck.capitalizeTitles(dataItem.getName()));
            doseTime.setText(DateTime.convertToTime12(dataItem.getTimeOnly()));
            dose.setText(dataItem.getDose());
            message.setText(messageNote);
            boxWrapper.setBackgroundResource(android.R.color.white);
            editButton.setVisibility(View.VISIBLE);
            delButton.setVisibility(View.VISIBLE);

        }
        return convertView;

    }
}
