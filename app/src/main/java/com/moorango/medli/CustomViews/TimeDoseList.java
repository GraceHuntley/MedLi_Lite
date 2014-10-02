package com.moorango.medli.CustomViews;

import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colin on 10/1/2014.
 * Copyright 2014
 */
public class TimeDoseList extends LinearLayout {

    private int mDoseCount;
    private int mEditTextResId;
    private static int itemCount;
    private final String TAG = "TimeDoseList";
    private Context context = getContext();

    private ArrayList<View> itemBox = new ArrayList<View>();

    public TimeDoseList(Context context) {
        super(context);
    }

    public TimeDoseList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

    }

    public int getDoseCount() { return mDoseCount; }

    public void setDoseCount(int doseCount) {

        if (doseCount != mDoseCount && doseCount < mDoseCount) { // removeView.
            removeViewAt(getChildCount() - 1);
            mDoseCount = doseCount;
        } else if (doseCount != mDoseCount) {

            //removeAllViews();

            for (int i = 0; i < (doseCount - mDoseCount); i++) {

                addView(createEditText());
            }
            mDoseCount = doseCount;
        }
    }

    private View createEditText() {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(mEditTextResId, this, false);

        final EditText et = (EditText) v.findViewById(R.id.time_entry);
        et.setFocusable(false);

        et.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view2, int hourOfDay, int minute) {

                        et.setText(DateTime.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));
                        et.setBackgroundColor(getResources().getColor(android.R.color.white));
                        et.setError(null);

                    }
                };

                TimePickerDialog tpd = new TimePickerDialog(context, t, 0, 0, false);
                //EditText et = etList.get(v.getId());
                if (et.length() > 0) {
                    String convertedTime[] = DateTime.convertToTime24(et.getText().toString()).split(":");
                    int hour = Integer.valueOf(convertedTime[0]);
                    int minute = Integer.valueOf(convertedTime[1]);
                    tpd.updateTime(hour, minute);

                }

                tpd.show();

            }
        });
        return v;
    }

    public int getEditTextResId() {
        return mEditTextResId;
    }

    public void setEditTextResId(int editTextResId) {
        mEditTextResId = editTextResId;
    }

    public List<String> getDoseData() {
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < getChildCount(); i++) {

            String data = "";

            ViewGroup v = (ViewGroup) getChildAt(i);

            for (int j = 0; j < v.getChildCount(); j++) {

                View view = v.getChildAt(j);

                if ( view instanceof EditText) {
                    EditText et = (EditText) view;
                    data += et.getText().toString() + ";";
                } else if (view instanceof Spinner) {
                    Spinner sp = (Spinner) view;
                    data += sp.getSelectedItem().toString() + ";";
                }
            }

            if (data.length() > 0) {
                data = data.trim().substring(0, data.trim().length() - 1);
                items.add(data.substring(0, data.trim().length()));

            }
        }

        //recursiveLoopChildren(this);
        return items;
    }
}