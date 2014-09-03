package com.moorango.medli;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.moorango.medli.Models.Object_Medication;

import java.util.ArrayList;

/**
 * Created by Colin on 8/21/2014.
 * Copyright 2014
 */
public class Fragment_HomeDialog extends DialogFragment {

    private ArrayList<Object_Medication> choicesList;


    public static Fragment_HomeDialog newInstance(int title, String medList) {
        Fragment_HomeDialog frag = new Fragment_HomeDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("text", medList);
        frag.setArguments(args);
        return frag;
    }

    public void setChoiceList(ArrayList<Object_Medication> choiceList) {
        this.choicesList = choiceList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        String message = getArguments().getString("text");
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.submit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Fragment_Home) getActivity().getSupportFragmentManager().findFragmentByTag("home")).doPositiveClick();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Fragment_Home) getActivity().getSupportFragmentManager().findFragmentByTag("home")).doNegativeClick();
                            }
                        }
                )
                .create();
    }
}
