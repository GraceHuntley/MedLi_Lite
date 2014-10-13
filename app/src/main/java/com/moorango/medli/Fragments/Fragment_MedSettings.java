package com.moorango.medli.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.moorango.medli.Activity_MedLi_light;
import com.moorango.medli.Constants;
import com.moorango.medli.CustomViews.AutoCompleteMedication;
import com.moorango.medli.CustomViews.TimeDoseList;
import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Helpers.AlarmHelpers;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Models.Medication;
import com.moorango.medli.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Fragment_MedSettings extends Fragment implements View.OnClickListener {

    private final String TAG = "MedSettingsFragment.java";
    private EditText doseFrequency, maxDoses;
    public EditText med_dose, med_measure_spinner, doseFormEntry;
    private LinearLayout adminTimesList, prnFreqBox;
    private Spinner med_type;
    private AutoCompleteTextView acMedName;
    private TextWatcher textWatcher;
    private ScrollView formWrapper;
    private boolean isRoutine = false;
    private Button delete_med, dc_med, clear;
    private MedLiDataSource dataSource;
    private OnFragmentInteractionListener mListener;
    private AlertDialog.Builder dialog;
    private TextView medTypePrompt;
    //private GetSuggestions getSuggestions;
    private Tracker ga;
    private TimeDoseList tdv;

    public Fragment_MedSettings() {
        // Required empty public constructor
    }

    public static Fragment_MedSettings newInstance(String param1, boolean param2, int param3) {
        Fragment_MedSettings fragment = new Fragment_MedSettings();
        Bundle args = new Bundle();
        args.putString("name", param1);
        args.putInt("unique_id", param3);
        args.putBoolean("edit", param2);
        fragment.setArguments(args);

        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = MedLiDataSource.getHelper(getActivity());
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ga = ((Activity_MedLi_light) getActivity()).getTracker(
                Activity_MedLi_light.TrackerName.APP_TRACKER);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_med_settings, container, false);

        doseFormEntry = (EditText) view.findViewById(R.id.dose_form_input);
        doseFrequency = (EditText) view.findViewById(R.id.prn_frequency_input);
        med_dose = (EditText) view.findViewById(R.id.med_dose_input);
        maxDoses = (EditText) view.findViewById(R.id.admin_count_edittext);
        adminTimesList = (LinearLayout) view.findViewById(R.id.admin_times_add_box);
        medTypePrompt = (TextView) view.findViewById(R.id.med_type_label);
        formWrapper = (ScrollView) view.findViewById(R.id.scrollview_wrapper);
        clear = (Button) view.findViewById(R.id.clear_btn);
        tdv = (TimeDoseList) view.findViewById(R.id.time_dose_view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        delete_med = (Button) getActivity().findViewById(R.id.del_med);
        //maxDoses = (TextView) getActivity().findViewById(R.id.admin_count_edittext);
        getActivity().findViewById(R.id.minus_button).setOnClickListener(this);
        getActivity().findViewById(R.id.plus_button).setOnClickListener(this);
        getActivity().findViewById(R.id.btn_add_med).setOnClickListener(this);
        dc_med = (Button) getActivity().findViewById(R.id.dc_med);
        med_type = (Spinner) getActivity().findViewById(R.id.med_type_spinner);
        med_measure_spinner = (EditText) getActivity().findViewById(R.id.med_measure_input);
        prnFreqBox = (LinearLayout) getActivity().findViewById(R.id.prn_frequency_box);
        acMedName = (AutoCompleteTextView) getActivity().findViewById(R.id.ac_Med_name);
        acMedName.setThreshold(2);

        doseFormEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            public void afterTextChanged(Editable edt) {
                if (doseFormEntry.getText().length() > 0) {
                    doseFormEntry.setError(null);
                }
            }
        });

        /***
         * Spinner for setting medication type ie. routine or prn.
         * if routine boolean isRoutine set to true.
         * --> if true form will be setup for routine medication.
         */
        med_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                               @Override
                                               public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                   DataCheck.clearFormErrors(formWrapper);

                                                   if (!adapterView.getSelectedItem().toString().equalsIgnoreCase("select")) {
                                                       formWrapper.setVisibility(View.VISIBLE);
                                                       isRoutine = (adapterView.getSelectedItem().toString().equalsIgnoreCase("routine"));
                                                       if (isRoutine) { // setup form for routine medication.
                                                           medTypePrompt.setText("Setting up a Routine medication");
                                                           setTextChangeListener();
                                                           maxDoses.setText("0");
                                                           prnFreqBox.setVisibility(View.GONE);
                                                           doseFrequency.setVisibility(View.GONE);
                                                       } else { // setup form for prn med.
                                                           maxDoses.removeTextChangedListener(textWatcher);
                                                           medTypePrompt.setText("Setting up a Non-Routine medication");
                                                           adminTimesList.removeAllViews();

                                                           prnFreqBox.setVisibility(View.VISIBLE);
                                                           doseFrequency.setVisibility(View.VISIBLE);
                                                       }
                                                   } else {

                                                       medTypePrompt.setText(getResources().getString(R.string.medtype_label));
                                                       formWrapper.setVisibility(View.GONE);
                                                       isRoutine = false;
                                                   }
                                               }

                                               @Override
                                               public void onNothingSelected(AdapterView<?> adapterView) {

                                               }
                                           }

        );

        if (DataCheck.isNetworkAvailable(getActivity())) {
            /***
             * TODO instantiate autocomplete object here.
             *
             */
            //AutoCompleteMedication acm = new AutoCompleteMedication(this, getActivity());

        } else {
            AlertDialog.Builder adB = new AlertDialog.Builder(getActivity());
            adB.setTitle("No Internet Connection")
                    .setMessage(Constants.NO_INTERNET_CONNECT)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            adB.show();

        }

        if (getArguments() != null && getArguments().getBoolean("edit")) {

            populateForEdit(getArguments().getInt("unique_id"));
        }

        clear.setOnClickListener(new View.OnClickListener() { // clears all entries on form.
            @Override
            public void onClick(View view) {
                DataCheck.clearForm(formWrapper);

            }
        });

    } // end onActivityCreated()

    /**
     * TextChange Listener for  Dose count edittext.
     */

    private void setTextChangeListener() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                maxDoses.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                tdv.setEditTextResId(R.layout.time_dose_list);
                tdv.setDoseCount(Integer.valueOf(editable.toString().trim()));

            }
        };
        maxDoses.addTextChangedListener(textWatcher);
    }

    private Medication prepareMedicationObject() {
        Medication medication = new Medication();
        medication.setMedName(acMedName.getText().toString().toLowerCase().trim());
        medication.setAdminType(med_type.getSelectedItem().toString().toLowerCase().trim());
        medication.setDoseMeasure(Float.valueOf(med_dose.getText().toString().trim()));
        medication.setDoseMeasureType(med_measure_spinner.getText().toString().toLowerCase().trim());
        medication.setDoseForm(doseFormEntry.getText().toString());
        medication.setDoseCount(Integer.valueOf(maxDoses.getText().toString()));
        medication.setDoseForm(doseFormEntry.getText().toString());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        medication.setStartDate(df.format(cal.getTime()));

        if (getArguments() != null && getArguments().getBoolean("edit")) {
            medication.setIdUnique(getArguments().getInt("unique_id"));
        }

        if (medication.getAdminType().equalsIgnoreCase("routine")) {

            medication.setDoseInfo(tdv.getDoseData());

            String compiled = "";
            for (String data : medication.getDoseInfo()) {
                String split[] = data.split(";");
                compiled += split[0] + ";";
            }
            compiled = compiled.substring(0, compiled.length() - 1); // remove trailing semi-colon.

            medication.setDoseTimes(compiled);
        } else {

            medication.setDoseFrequency(Integer.valueOf(doseFrequency.getText().toString()));
        }

        return medication;
    }

    /**
     * Called if instance created for editing a medication.
     * populates form data from Medication Object passed.
     *
     * @param uniqueID
     */
    private void populateForEdit(int uniqueID) {
        // TODO will populate for edits.

        Medication medication = dataSource.getSingleMedByName(uniqueID);

        for (int index = 0; index < med_type.getCount(); index++) {
            if (med_type.getItemAtPosition(index).toString().equalsIgnoreCase(medication.getAdminType())) {

                med_type.setSelection(index);
                break;
            }
        }

        acMedName.setText(medication.getMedName());
        med_dose.setText(Float.toString(medication.getDoseMeasure()));
        med_measure_spinner.setText(medication.getDoseMeasureType());

        if (medication.getAdminType().equalsIgnoreCase("routine")) {
            String splitTimes[] = medication.getDoseTimes().split(";");
            if (splitTimes.length > 0) {
                for (int index = 0; index < medication.getDoseCount(); index++) {
                    setTextChangeListener();
                    maxDoses.setText(String.valueOf(index + 1));
                    //etList.get(index).setText(splitTimes[etList.get(index).getId()]);

                }
            }
        } else {
            doseFrequency.setText(String.valueOf(medication.getDoseFrequency()));
            maxDoses.setText(String.valueOf(medication.getDoseCount()));
        }
        doseFormEntry.setText(medication.getDoseForm());
        dc_med.setVisibility(View.VISIBLE);
        delete_med.setVisibility(View.VISIBLE);
        clear.setVisibility(View.GONE);
        setEditButtonListeners(uniqueID); // turn on listeners for buttons.
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_med:
                List list = tdv.getDoseData();
                for (int i = 0; i < list.size(); i++) {
                    Log.d(TAG, list.get(i).toString());
                }
                String selection = med_type.getSelectedItem().toString().toLowerCase();
                boolean doUpdate = false;
                if (getArguments() != null && getArguments().getBoolean("edit")) {
                    doUpdate = true;
                }

                if (selection.equalsIgnoreCase("select")) {
                    // TODO alert they have not entered a med yet.
                    Toast.makeText(getActivity(), "You have not selected a medication type", Toast.LENGTH_LONG).show();

                } else {

                    if (DataCheck.checkFormData(formWrapper, getActivity())) {

                        dataSource.submitNewMedication(prepareMedicationObject(), doUpdate);

                        if (doUpdate) {
                            ga.send(new HitBuilders.EventBuilder().setAction("NewMedAdded").build());
                        } else {
                            ga.send(new HitBuilders.EventBuilder().setAction("MedicationEdited").build());
                        }
                        hideKeyboard();

                        if (dataSource.getPreferenceBool("show_new_med_info") && !doUpdate) {

                            showFirstMedMessage(); // dialog with directions.
                        } else {
                            mListener.onFragmentInteraction(1, null, 0);
                        }
                    }
                }
                break;
            case R.id.plus_button:

                maxDoses.setText("" + (Integer.valueOf(maxDoses.getText().toString()) + 1));
                break;
            case R.id.minus_button:

                if (Integer.valueOf(maxDoses.getText().toString()) != 0) {
                    maxDoses.setText("" + (Integer.valueOf(maxDoses.getText().toString()) - 1));
                }
                break;

        }
    }

    private void setEditButtonListeners(final int idUnique) {
        delete_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Medication medication = dataSource.getSingleMedByName(idUnique);
                String message = "Delete " + DataCheck.capitalizeTitles(acMedName.getText().toString())
                        + " " + medication.getDoseMeasure() + " " + medication.getDoseMeasureType()
                        + "?\n\n"
                        + "This will delete all records associated with " + DataCheck.capitalizeTitles(acMedName.getText().toString()) + "!\n\n"
                        + "If you would like to just stop taking this medication "
                        + "but keep the records use D/C instead.";
                dialog = getDialog(message, "Delete", 0, idUnique);
                dialog.show();
            }
        });

        dc_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = getDialog("Discontinue " + acMedName.getText().toString() + "?", "Discontinue", 1, idUnique);
                dialog.show();
            }
        });
    }

    private AlertDialog.Builder getDialog(String message, String positiveAction, final int action, final int idUnique) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(message)
                .setTitle("Confirm Action")
                .setPositiveButton(positiveAction, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlarmHelpers ah = new AlarmHelpers(getActivity());

                        switch (action) {
                            case 0:
                                dataSource.changeMedicationStatus(idUnique, Medication.DELETED);

                                ah.clearAlarm(idUnique);
                                mListener.onFragmentInteraction(1, null, 0);
                                break;

                            case 1:
                                dataSource.changeMedicationStatus(idUnique, Medication.DISCONTINUED);

                                ah.clearAlarm(idUnique);
                                mListener.onFragmentInteraction(1, null, 0);
                                break;

                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag, String name, int id);
    }

    @Override
    public void onPause() {
        dataSource.close();

        /***
         * TODO clean up asynctask in autocompleteview eventually.
         */
        /*if (getSuggestions != null && getSuggestions.getStatus() == AsyncTask.Status.RUNNING) {
            getSuggestions.cancel(true);
        } */

        InputMethodManager imm = (InputMethodManager)
                getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(acMedName.getWindowToken(), 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showFirstMedMessage() {
        AlertDialog.Builder adB = new AlertDialog.Builder(getActivity());
        adB.setIcon(android.R.drawable.ic_dialog_info);
        adB.setTitle("New medication added");
        adB.setInverseBackgroundForced(true); // fixed bug in older versions of android.

        View view = getActivity().getLayoutInflater().inflate(R.layout.info_dialog, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.no_show_checkbox);

        String home_welcome_info = acMedName.getText().toString() + " has been added.\n\n" +
                "If this is a routine medication the next due will be set to the next available dose after now " +
                " or complete if there are no doses left for the day after now. All reminders have been set and " +
                "starting tomorrow the routine will start from the first dose.";
        TextView tv1 = (TextView) view.findViewById(R.id.main_text);
        tv1.setText(home_welcome_info);

        ((TextView) view.findViewById(R.id.dont_show_message)).setText(getResources().getString(R.string.do_not_show));

        adB.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (checkBox.isChecked()) {
                            dataSource.addOrUpdatePreference("show_new_med_info", false);

                        }
                        mListener.onFragmentInteraction(1, null, 0);
                    }
                }).show();
    }
}