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
public class TimeDoseView extends LinearLayout {

    private int mDoseCount;
    private int mEditTextResId;

    public TimeDoseView(Context context) {
        super(context);
    }

    public TimeDoseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

    }

    public int getDoseCount() { return mDoseCount; }

    public void setDoseCount(int doseCount) {
        if (doseCount != mDoseCount) {

            mDoseCount = doseCount;

            removeAllViews();

            for (int i = 0; i < mDoseCount; i++) {
                addView(createEditText());
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

    public int getEditTextResId() {
        return mEditTextResId;
    }

    public void setEditTextResId(int editTextResId) {
        mEditTextResId = editTextResId;
    }

    public List<String> getDoseTimes() {
        List<String> names = new ArrayList();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof EditText) {
                EditText et = (EditText) v;
                names.add(et.getText().toString());
            }
        }
        return names;
    }
}
