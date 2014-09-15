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

    private final String PRN_DOSE_TEXT = "Earliest Dose: ";
    private final String ROUTINE_DOSE_TEXT = "Next Due: ";

    private ImageView skipButton, editMed, lateDoseStamp;
    private CheckedTextView txtTitle;
    private TextView headerText, nextDueTime;
    private RelativeLayout boxWrapper;

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

        txtTitle = (CheckedTextView) convertView.findViewById(R.id.title);
        headerText = (TextView) convertView.findViewById(R.id.header_title);
        nextDueTime = (TextView) convertView.findViewById(R.id.next_dose_time);
        skipButton = (ImageView) convertView.findViewById(R.id.skip_dose);
        editMed = (ImageView) convertView.findViewById(R.id.edit_med_button);
        lateDoseStamp = (ImageView) convertView.findViewById(R.id.late_dose_image);
        boxWrapper = (RelativeLayout) convertView.findViewById(R.id.box_wrapper);
        final Medication dataItem = data.get(position);

        if (dataItem.isSubHeading()) {

            prepSubHeading(dataItem);

        } else if (dataItem.getAdminType().equalsIgnoreCase("ROUTINE")) {

            fillAndSetTimers(dataItem); // this eventually will be moved. TODO

            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(DataCheck.capitalizeTitles(dataItem.getMedName() + " " + dataItem.getDoseForm()));
            nextDueTime.setVisibility(View.VISIBLE);
            prepEditMed(true, dataItem);

            txtTitle.setChecked(sparseBooleanArray.get(position));

            if (txtTitle.isChecked()) {

                boxWrapper.setBackgroundResource(R.color.green_selector);
            } else {
                boxWrapper.setBackgroundResource(android.R.color.white);
            }

            if (!DataCheck.isToday(dataItem.getNextDue())) {

                nextDueTime.setText("COMPLETE");
                prepSkipButton(false, null);
            } else {
                nextDueTime.setText(ROUTINE_DOSE_TEXT + DateTime.convertToTime12(dataItem.getNextDue().split(" ")[1]));
                prepSkipButton(true, dataItem);

                if (DataCheck.isDoseLate(dataItem.getNextDue(), false)) {
                    isLate(true);
                } else {
                    isLate(false);
                }
            }

        } else {
            isLate(false);
            prepSkipButton(false, null);
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(DataCheck.capitalizeTitles(dataItem.getMedName() + " " + dataItem.getDoseForm()));
            nextDueTime.setVisibility(View.VISIBLE);
            prepEditMed(true, dataItem);

            txtTitle.setChecked(sparseBooleanArray.get(position));

            if (txtTitle.isChecked()) {

                boxWrapper.setBackgroundResource(R.color.green_selector);
            } else {
                boxWrapper.setBackgroundResource(android.R.color.white);
            }

            if (dataItem.getDoseCount() > dataItem.getActualDoseCount()) {
                String nextDue = dataItem.getNextDue();
                nextDue = nextDue.equalsIgnoreCase("NOW") ? "NOW" : DataCheck.isToday(nextDue) ? DateTime.convertToTime12(nextDue.split(" ")[1]) : "Tomorrow at " + DateTime.convertToTime12(nextDue.split(" ")[1]);
                nextDueTime.setText(PRN_DOSE_TEXT + nextDue);
            } else {
                //nextDueTime.setText(DateTime.getReadableDate(dataItem.getNextDue().split(" ")[0]) + " " + DateTime.convertToTime12(dataItem.getNextDue().split(" ")[1]));
                nextDueTime.setText("MAX DOSES REACHED");
            }

        }


        return convertView;

    }

    private void fillAndSetTimers(Medication dataItem) {


        /*if (dataItem.getAdminType().equalsIgnoreCase("routine")) {

            if (dataItem.getNextDue().compareTo(DateTime.getCurrentTimestamp(false, null)) == 1) {

                AlarmHelpers ah = new AlarmHelpers(context);
                ah.setAlarm(dataItem.getNextDue(), dataItem.getMedName(), dataItem.getIdUnique());
            }
        } */
    }

    private void isLate(boolean isLate) {
        if (isLate) {
            nextDueTime.setTextColor(context.getResources().getColor(R.color.red));
            lateDoseStamp.setVisibility(View.VISIBLE);
        } else {
            lateDoseStamp.setVisibility(View.GONE);
            nextDueTime.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }

    private void prepSubHeading(Medication dataItem) {

        txtTitle.setVisibility(View.GONE);
        nextDueTime.setVisibility(View.GONE);
        prepSkipButton(false, null);
        prepEditMed(false, null);
        isLate(false);

        headerText.setText(dataItem.getMedName());
        headerText.setVisibility(View.VISIBLE);

        boxWrapper.setBackgroundResource(R.drawable.list_bg);
    }

    private void prepEditMed(boolean showButton, Medication dataItem) {

        if (showButton) {
            editMed.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);

            final Medication data = dataItem;

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
                    caller.mListener.onFragmentInteraction(3, data.getMedName(), data.getIdUnique());
                    return true;
                }
            });
        } else {
            editMed.setVisibility(View.GONE);
        }

    }

    private void prepSkipButton(boolean showButton, Medication dataItem) {
        if (showButton) {
            skipButton.setVisibility(View.VISIBLE);

            final Medication data = dataItem;

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
                    MedLiDataSource.getHelper(context).submitSkippedDose(data);
                    caller.mListener.onFragmentInteraction(1, null, 0);
                    Toast toast = Toast.makeText(context, "Skipped Dose", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                    return true;
                }
            });
        } else {
            skipButton.setVisibility(View.GONE);
        }
    }

}
