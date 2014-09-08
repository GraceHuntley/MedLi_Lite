package com.moorango.medli.CustomViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moorango.medli.Fragments.Fragment_Home;
import com.moorango.medli.Helpers.Helper_DataCheck;
import com.moorango.medli.Models.Object_Medication;
import com.moorango.medli.R;

import java.util.List;

/**
 * Created by Colin on 9/5/2014.
 * Copyright 2014
 */
public class HomeCustomAdapter extends ArrayAdapter<Object_Medication> {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "Home/HomeCustomAdapter";
    private final Fragment_Home caller;
    private final Context context;

    private final List<Object_Medication> data;
    private final SparseBooleanArray sparseBooleanArray;

    public HomeCustomAdapter(Context context,
                             List<Object_Medication> rowItem, Fragment_Home caller) {
        super(context, R.layout.home_list_item, R.id.title, rowItem);
        this.context = context;
        this.data = rowItem;
        this.caller = caller;
        sparseBooleanArray = new SparseBooleanArray();

        for (int index = 0; index < rowItem.size(); index++) {
            sparseBooleanArray.put(index, false);
        }
    }


    public SparseBooleanArray getCheckedItemPositions() {

        return sparseBooleanArray;
    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Object_Medication getItem(int position) {

        return data.get(position);

    }

    public void clearChoices() {
        for (int index = 0; index < sparseBooleanArray.size(); index++) {
            sparseBooleanArray.put(index, false);
            notifyDataSetChanged();
        }
    }

    public void setItemChecked(int position, boolean value) {
        sparseBooleanArray.put(position, value);
    }

    public void toggleChecked(int position) {
        if (sparseBooleanArray.get(position)) {
            sparseBooleanArray.put(position, false);
        } else {
            sparseBooleanArray.put(position, true);
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {

        return data.indexOf(getItem(position));
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.home_list_item, null);
        }

        final CheckedTextView txtTitle = (CheckedTextView) convertView.findViewById(R.id.title);
        final TextView headerText = (TextView) convertView.findViewById(R.id.header_title);
        final TextView nextDueTime = (TextView) convertView.findViewById(R.id.next_dose_time);

        final ImageView editMed = (ImageView) convertView.findViewById(R.id.edit_med_button);
        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        final Object_Medication dataItem = data.get(position);

        txtTitle.setChecked(sparseBooleanArray.get(position));

        if (dataItem.isSubHeading()) {
            headerText.setText(dataItem.getMedName());
            txtTitle.setText(dataItem.getMedName());
            txtTitle.setChecked(false);
            txtTitle.setVisibility(View.INVISIBLE);
            nextDueTime.setVisibility(View.GONE);

            headerText.setVisibility(View.VISIBLE);
            editMed.setVisibility(View.GONE);
            // boxWrapper.setBackgroundResource(android.R.color.background_light);
            boxWrapper.setBackgroundResource(R.drawable.list_bg);
        } else {
            txtTitle.setText(Helper_DataCheck.capitalizeTitles(dataItem.getMedName()) + " " + dataItem.getDoseForm());

            /***
             * Fill in missed doses for a new medication.
             */

            if (dataItem.getStatus().equalsIgnoreCase("new")) {
                dataItem.setNextDue(Helper_DataCheck.findNextDoseNewMed(context, dataItem));

            }
            String doseVerbage = (dataItem.getAdminType().equalsIgnoreCase("routine")) ? "Next Due: " : "Next Earliest Dose: ";
            String dueWording = (dataItem.getNextDue().equalsIgnoreCase("prn") ? "Any Time" : dataItem.getNextDue());
            nextDueTime.setText((dataItem.getNextDue().equalsIgnoreCase("complete")) ? dataItem.getNextDue() : doseVerbage + dueWording);
            boxWrapper.setBackgroundResource(android.R.color.white);


            if (!dataItem.getNextDue().equalsIgnoreCase("complete") &&
                    !dataItem.getNextDue().equalsIgnoreCase("prn") &&
                    !dataItem.getAdminType().equalsIgnoreCase("prn") &&
                    dataItem.getStatus().equalsIgnoreCase("active")) {

                if (Helper_DataCheck.isDoseLate(dataItem.getNextDue())) {

                    nextDueTime.setTextColor(context.getResources().getColor(R.color.red));

                    convertView.findViewById(R.id.late_dose_image).setVisibility(View.VISIBLE);
                    boxWrapper.setBackgroundColor(context.getResources().getColor(android.R.color.white));

                }
            }

            txtTitle.setVisibility(View.VISIBLE);
            nextDueTime.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);

            editMed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Long press to Edit Medication", Toast.LENGTH_SHORT).show();
                }
            });

            editMed.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    caller.mListener.onFragmentInteraction(3, dataItem.getMedName(), dataItem.getIdUnique());
                    return true;
                }
            });

        }

        return convertView;

    }

}
