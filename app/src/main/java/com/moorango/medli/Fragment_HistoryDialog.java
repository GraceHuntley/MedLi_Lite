package com.moorango.medli;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.ArrayList;

/**
 * Created by Colin on 8/21/2014.
 * Copyright 2014
 */
public class Fragment_HistoryDialog extends DialogFragment {

    private ArrayList<Object_Medication> choicesList;


    public static Fragment_HistoryDialog newInstance(String medLog) {
        Fragment_HistoryDialog frag = new Fragment_HistoryDialog();

        String medData[] = medLog.split(";");
        Bundle args = new Bundle();
        args.putString("title", medData[0]);
        args.putString("time", medData[1]);
        args.putString("dose", medData[2]);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        //String message = getArguments().getString("text");
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage("Make changes Below")
                .setView(createView())
                .setPositiveButton(R.string.submit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((Fragment_History) getActivity().getSupportFragmentManager().findFragmentByTag("history")).doPositiveClick();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //((Fragment_History) getActivity().getSupportFragmentManager().findFragmentByTag("history")).doNegativeClick();
                            }
                        }
                )
                .create();
    }

    private View createView() {


        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.history_dialog_content, null);
        TimePicker tp = (TimePicker) getActivity().findViewById(R.id.time_picker);
        String timeSplit[] = getArguments().getString("time").split(":");
        Log.d("dialog", getArguments().getString("time"));
        tp.setCurrentHour(Integer.valueOf(timeSplit[0]));
        tp.setCurrentMinute(Integer.valueOf(timeSplit[1]));


        return v;
    }
}
