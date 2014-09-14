package com.moorango.medli.CustomViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Fragments.Fragment_Home;
import com.moorango.medli.Helpers.AlarmHelpers;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.Medication;
import com.moorango.medli.R;

import java.util.List;

/**
 * Created by Colin on 9/5/2014.
 * Copyright 2014
 */
public class HomeCustomAdapter extends ArrayAdapter<Medication> {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "Home/HomeCustomAdapter";
    private final Fragment_Home caller;
    private final Context context;

    private final List<Medication> data;
    private final SparseBooleanArray sparseBooleanArray;

    public HomeCustomAdapter(Context context,
                             List<Medication> rowItem, Fragment_Home caller) {
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
    public Medication getItem(int position) {

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

    public boolean isChecked(int position) {
        return sparseBooleanArray.get(position);
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
        final ImageView skipButton = (ImageView) convertView.findViewById(R.id.skip_dose);

        final ImageView editMed = (ImageView) convertView.findViewById(R.id.edit_med_button);
        RelativeLayout boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);

        final Medication dataItem = data.get(position);

        txtTitle.setChecked(sparseBooleanArray.get(position));

        if (dataItem.isSubHeading()) {
            headerText.setText(dataItem.getMedName());
            txtTitle.setText(dataItem.getMedName());
            txtTitle.setChecked(false);
            txtTitle.setVisibility(View.INVISIBLE);
            nextDueTime.setVisibility(View.GONE);
            skipButton.setVisibility(View.GONE);
            headerText.setVisibility(View.VISIBLE);
            editMed.setVisibility(View.GONE);

            boxWrapper.setBackgroundResource(R.drawable.list_bg);
        } else {
            txtTitle.setText(DataCheck.capitalizeTitles(dataItem.getMedName()) + " " + dataItem.getDoseForm());

            if (txtTitle.isChecked()) {

                boxWrapper.setBackgroundResource(android.R.color.holo_green_light);
            } else {
                boxWrapper.setBackgroundResource(android.R.color.white);
            }


            /***
             * Fill in missed doses for a new medication.
             */

            if (dataItem.getStatus() == Medication.NEW) {
                dataItem.setNextDue(DataCheck.findNextDoseNewMed(context, dataItem));

            }

            if (dataItem.getAdminType().equalsIgnoreCase("routine")) {

                if (dataItem.getNextDue().compareTo(DateTime.getCurrentTimestamp(false, null)) == 1) {

                    AlarmHelpers ah = new AlarmHelpers(context);
                    ah.setAlarm(dataItem.getNextDue(), dataItem.getMedName(), dataItem.getIdUnique());
                }
            }

            String doseVerbage = (dataItem.getAdminType().equalsIgnoreCase("routine")) ? "Next Due: " : "Earliest dose: ";
            String dueWording = (dataItem.getNextDue().equalsIgnoreCase("prn") ? "Any Time" : DateTime.convertToTime12(dataItem.getNextDue().split(" ")[1]));
            nextDueTime.setText(!DataCheck.isToday(dataItem.getNextDue()) && !dataItem.getAdminType().equalsIgnoreCase("prn") ? "COMPLETE" : doseVerbage + dueWording);

            if (DataCheck.isToday(dataItem.getNextDue()) &&
                    !dataItem.getNextDue().equalsIgnoreCase("prn") &&
                    !dataItem.getAdminType().equalsIgnoreCase("prn") &&
                    dataItem.getStatus() == Medication.ACTIVE) {

                if (DataCheck.isDoseLate(dataItem.getNextDue(), false)) {

                    nextDueTime.setTextColor(context.getResources().getColor(R.color.red));
                    convertView.findViewById(R.id.late_dose_image).setVisibility(View.VISIBLE);

                } else {
                    nextDueTime.setTextColor(context.getResources().getColor(android.R.color.black));
                    convertView.findViewById(R.id.late_dose_image).setVisibility(View.GONE);
                }
            }

            txtTitle.setVisibility(View.VISIBLE);
            nextDueTime.setVisibility(View.VISIBLE);
            if (!DataCheck.isToday(dataItem.getNextDue()) || dataItem.getAdminType().equalsIgnoreCase("prn")) {
                skipButton.setVisibility(View.GONE);
            } else {
                skipButton.setVisibility(View.VISIBLE);
            }
            editMed.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);

            editMed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(context, "Long press to Edit Medication", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });

            editMed.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    caller.mListener.onFragmentInteraction(3, dataItem.getMedName(), dataItem.getIdUnique());
                    return true;
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast = Toast.makeText(context, "Long press to Skip Dose", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            });

            skipButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MedLiDataSource.getHelper(context).submitSkippedDose(dataItem);
                    caller.mListener.onFragmentInteraction(1, null, 0);
                    Toast toast = Toast.makeText(context, "Skipped Dose", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return true;
                }
            });

        }

        return convertView;

    }

}
