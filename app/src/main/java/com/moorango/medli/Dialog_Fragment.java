package com.moorango.medli;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;

/**
 * Created by Colin on 8/4/2014.
 * Copyright 2014
 */
public class Dialog_Fragment extends DialogFragment {

    private ArrayList<Medication> choicesList;


    public static Dialog_Fragment newInstance(int title, String medList) {
        Dialog_Fragment frag = new Dialog_Fragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("text", medList);
        frag.setArguments(args);
        return frag;
    }

    public void setChoiceList(ArrayList<Medication> choiceList) {
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
                                ((Home) getActivity().getSupportFragmentManager().findFragmentByTag("home")).doPositiveClick(choicesList);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Home) getActivity().getSupportFragmentManager().findFragmentByTag("home")).doNegativeClick();
                            }
                        }
                )
                .create();
    }
}