package com.moorango.medli;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Colin on 8/4/2014.
 * Copyright 2014
 */
public class Dialog_Fragment extends DialogFragment {

    public static Dialog_Fragment newInstance(int title) {
        Dialog_Fragment frag = new Dialog_Fragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.submit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Home) getActivity().getSupportFragmentManager().findFragmentByTag("home")).doPositiveClick();
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