package com.moorango.medli.CustomViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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

    private ImageView skipButton, editMed, lateDoseStamp, setPrnAlarm;
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

    public boolean notChecked(int position) {
        return !sparseBooleanArray.get(position);
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
        setPrnAlarm = (ImageView) convertView.findViewById(R.id.prn_alarm);
        final Medication dataItem = data.get(position);

        if (dataItem.isSubHeading()) {

            prepSubHeading(dataItem);

            showPrnDoseAlarm(false, dataItem);
        } else if (dataItem.getAdminType().equalsIgnoreCase("ROUTINE")) {


            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(DataCheck.capitalizeTitles(dataItem.getMedName()) + " " + dataItem.getDoseForm());
            nextDueTime.setVisibility(View.VISIBLE);
            prepEditMed(true, dataItem);
            showPrnDoseAlarm(false, dataItem);

            txtTitle.setChecked(sparseBooleanArray.get(position));

            if (txtTitle.isChecked()) {

                boxWrapper.setBackgroundResource(R.color.green_selector);
            } else {
                boxWrapper.setBackgroundResource(android.R.color.white);
            }

            if (!DataCheck.isToday(dataItem.getNextDue())) {

                nextDueTime.setText("COMPLETE");
                isLate(false);
                prepSkipButton(false, null);
            } else {
                String ROUTINE_DOSE_TEXT = "Next Due: ";
                nextDueTime.setText(ROUTINE_DOSE_TEXT + DateTime.convertToTime12(dataItem.getNextDue().split(" ")[1]));
                prepSkipButton(true, dataItem);

                if (DataCheck.isDoseLate(dataItem.getNextDue())) {
                    isLate(true);
                } else {
                    isLate(false);
                }
            }

        } else {
            isLate(false);
            prepSkipButton(false, null);
            showPrnDoseAlarm(false, dataItem);
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(DataCheck.capitalizeTitles(dataItem.getMedName()) + " " + dataItem.getDoseForm());
            nextDueTime.setVisibility(View.VISIBLE);
            prepEditMed(true, dataItem);

            txtTitle.setChecked(sparseBooleanArray.get(position));

            if (txtTitle.isChecked()) {

                boxWrapper.setBackgroundResource(R.color.green_selector);
            } else {
                boxWrapper.setBackgroundResource(android.R.color.white);
            }

            if (dataItem.getNextDue().compareTo(DateTime.getCurrentTimestamp(false, null)) <= 0) {
                nextDueTime.setText("NOW");
                showPrnDoseAlarm(true, dataItem);
            } else {
                try {
                    String time = dataItem.getNextDue().split(" ")[1];
                    String PRN_DOSE_TEXT = "Earliest Dose: ";
                    nextDueTime.setText(PRN_DOSE_TEXT + (DataCheck.isToday(dataItem.getNextDue()) ? DateTime.convertToTime12(time) : "Tommorow at " + DateTime.convertToTime12(time)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString());

                }
            }

        }

        return convertView;

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

    private void showPrnDoseAlarm(boolean value, final Medication medication) {
        if (value) {
            setPrnAlarm.setVisibility(View.VISIBLE);
            setPrnAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder adB = new AlertDialog.Builder(getContext())
                            .setTitle("Non-Routine Scheduler")
                            .setMessage("This feature allows you to log a non-routine medication, no questions asked. " +
                                    "It also sets an alarm for the next time you are able to take the medication. " +
                                    "This alarm is a one time alarm that will only be reset again when you activate this. \n\n" +
                                    "In order to activate it long press the icon\n\n" +
                                    "You will only be able to use this feature when you have doses available to be taken.")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    adB.show();


                }
            });

            setPrnAlarm.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    MedLiDataSource dataSource = MedLiDataSource.getHelper(context);
                    dataSource.submitMedicationAdmin(medication, null);

                    Log.d(TAG, dataSource.getPrnNextDose(medication.getIdUnique(), medication.getDoseFrequency()));

                    AlarmHelpers ah = new AlarmHelpers(context);

                    ah.setAlarm(medication.getIdUnique(), dataSource.getPrnNextDose(medication.getIdUnique(), medication.getDoseFrequency()));


                    caller.mListener.onFragmentInteraction(5, "setPRNAlarm", 0);

                    caller.mListener.onFragmentInteraction(1, null, 0);
                    Toast.makeText(getContext(), "Medication submitted and alarm has been set for the next dose.", Toast.LENGTH_LONG).show();

                    return true;
                }
            });
        } else {
            setPrnAlarm.setVisibility(View.GONE);
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
