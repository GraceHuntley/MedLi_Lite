package com.moorango.medli.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colin on 10/1/2014.
 * Copyright 2014
 */
public class TimeDoseList extends LinearLayout {

    private int mDoseCount;
    private int mEditTextResId;
    private int mBoxResId;
    private int mLinearLayoutResId;

    public TimeDoseList(Context context) {
        super(context);
    }

    public TimeDoseList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

    }

    public int getDoseCount() { return mDoseCount; }

    public void setDoseCount(int doseCount) {
        if (doseCount != mDoseCount) {

            mDoseCount = doseCount;

            removeAllViews();

            for (int i = 0; i < mDoseCount; i++) {
                //addView(createEditText());
                addView(createLinearLayout());
            }
        }
    }

    private View createEditText() {
        View v;

        if (mEditTextResId > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(mEditTextResId, this, true);
        } else {
            EditText et = new EditText(getContext());
            et.setHint("Time");
            v = et;
        }
        return v;
    }

    private View createLinearLayout() {

        View v;
        if (mLinearLayoutResId > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(mLinearLayoutResId, this, true);
        } else {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(HORIZONTAL);
            EditText et1 = new EditText(getContext());
            et1.setHint("time");
            EditText et2 = new EditText(getContext());
            et2.setHint("measure");
            EditText et3 = new EditText(getContext());
            et3.setHint("unit");
            ll.addView(et1);
            ll.addView(et2);
            ll.addView(et3);
            v = ll;
        }

        return v;
    }

    public int getLinearLayoutResId() {
        return mLinearLayoutResId;
    }

    public void setLinearLayoutResId(int linearLayoutResId) {
        mEditTextResId = linearLayoutResId;
    }

    public List<String> getDoseData() {
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof EditText) {
                String data = "";
                EditText et = (EditText) v;
                data += et.getText().toString().trim() + ";";
                if ((i % 3) == 0) {
                    data = data.substring(0, data.length());
                    names.add(data);
                    data = "";
                }
            }
        }
        return names;
    }
}
