package com.moorango.medli.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

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

        View v;
        if (mEditTextResId > 0) {
            Log.d(TAG, "test");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(mEditTextResId, this, false);
            //v = inflate(getContext(), R.layout.time_dose_list, this);
        } else {

            LinearLayout ll = new LinearLayout(getContext());

            EditText et1 = new EditText(getContext());

            EditText et2 = new EditText(getContext());
            EditText et3 = new EditText(getContext());
            ll.addView(et1);
            ll.addView(et2);
            ll.addView(et3);

            v = ll;
        }
        //itemBox.add(v);
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

    public void recursiveLoopChildren(ViewGroup parent) {
        List<String> list = new ArrayList<String>();

        String data = "";
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                recursiveLoopChildren((ViewGroup) child);
                // DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
            } else {
                if (child != null) {

                    if (child instanceof EditText) {
                        EditText et = (EditText) child;
                        data += et.getText().toString() + ";";
                    } else if (child instanceof Spinner) {
                        Spinner sp = (Spinner) child;
                        data += sp.getSelectedItem().toString() + ";";
                    }
                }
            }
        }
        if (data.length() > 0) {
            list.add(data);
        }

    }
}