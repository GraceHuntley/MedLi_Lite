package com.moorango.medli;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MedSettings extends Fragment {

    private final String TAG = "MedSettingsFragment.java";
    private final ArrayList<EditText> etList = new ArrayList<EditText>();
    private EditText med_dose, adminTimes;
    private MakeDateTimeHelper dt;
    private LinearLayout adminTimesList;
    private Spinner med_measure_spinner, med_type;
    private AutoCompleteTextView acMedName;
    private TextWatcher textWatcher;
    private LinearLayout prnFreqBox;
    private EditText doseFrequency;
    private int index = 0;
    private boolean isRoutine = false;
    private LinearLayout secondaryForm;
    private Button delete_med;
    private Button dc_med;
    private AlertDialog.Builder adb;
    private AlertDialog adDoseChoices;

    private DrugDataHelper drugDataHelper;

    private MedLiDataSource dataSource;

    private OnFragmentInteractionListener mListener;

    public MedSettings() {
        // Required empty public constructor
    }

    public static MedSettings newInstance(String param1, boolean param2) {
        MedSettings fragment = new MedSettings();
        Bundle args = new Bundle();
        args.putString("name", param1);
        args.putBoolean("edit", param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drugDataHelper = new DrugDataHelper();

        dataSource = MedLiDataSource.getHelper(getActivity());
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_med_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        secondaryForm = (LinearLayout) getActivity().findViewById(R.id.secondary_form_wrapper);
        delete_med = (Button) getActivity().findViewById(R.id.del_med);
        Button plusButton = (Button) getActivity().findViewById(R.id.plus_button);
        adminTimes = (EditText) getActivity().findViewById(R.id.admin_count_edittext);
        Button minusButton = (Button) getActivity().findViewById(R.id.minus_button);
        doseFrequency = (EditText) getActivity().findViewById(R.id.prn_frequency_input);
        dc_med = (Button) getActivity().findViewById(R.id.dc_med);
        Button submitMed = (Button) getActivity().findViewById(R.id.btn_add_med);
        med_type = (Spinner) getActivity().findViewById(R.id.med_type_spinner);
        med_dose = (EditText) getActivity().findViewById(R.id.med_dose_input);

        med_measure_spinner = (Spinner) getActivity().findViewById(R.id.med_measure_spinner);
        adminTimesList = (LinearLayout) getActivity().findViewById(R.id.admin_times_add_box);
        prnFreqBox = (LinearLayout) getActivity().findViewById(R.id.prn_frequency_box);

        acMedName = (AutoCompleteTextView) getActivity().findViewById(R.id.ac_Med_name);
        acMedName.setThreshold(2);

        /***
         * Spinner for setting medication type ie. routine or prn.
         * if routine boolean isRoutine set to true.
         * --> if true form will be setup for routine medication.
         */
        med_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!adapterView.getSelectedItem().toString().toLowerCase().equals("select")) {
                    secondaryForm.setVisibility(View.VISIBLE);
                    isRoutine = (adapterView.getSelectedItem().toString().toLowerCase().equals("routine"));
                    if (isRoutine) { // setup form for routine medication.

                        setTextChangeListener();
                        setRoutineForm();
                    } else { // setup form for prn med.
                        adminTimes.removeTextChangedListener(textWatcher);

                        adminTimesList.removeAllViews();
                        etList.clear();
                        setPRNForm();
                    }
                } else {
                    secondaryForm.setVisibility(View.GONE);
                    isRoutine = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (VerifyHelpers.isNetworkAvailable(getActivity())) {
            acMedName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                    fillList(Uri.encode(charSequence.toString()));

                    acMedName.setTextColor(Color.BLACK);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            acMedName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    try {
                        ArrayList<String> drugDosesArray = drugDataHelper.getDrugNUI(acMedName.getText().toString().toLowerCase().trim());
                        if (drugDosesArray.size() > 0) {
                            adb = new AlertDialog.Builder(getActivity());
                            adb.setView(buildDoseChoicesForDialog(drugDosesArray)).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            adDoseChoices = adb.create();
                            adDoseChoices.show();


                        } else {
                            med_dose.requestFocus();
                        }

                    } catch (XmlPullParserException xmp) {
                        Log.d(TAG, xmp.toString());
                    } catch (IOException io) {
                        Log.d(TAG, io.toString());
                    }
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(acMedName.getWindowToken(), 0);
                }
            });
        }

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Integer.valueOf(adminTimes.getText().toString()) != 0) {
                    adminTimes.setText("" + (Integer.valueOf(adminTimes.getText().toString()) - 1));
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminTimes.setText("" + (Integer.valueOf(adminTimes.getText().toString()) + 1));
            }
        });

        submitMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selection = med_type.getSelectedItem().toString().toLowerCase();

                if (selection.equals("select")) {
                    // TODO alert they have not entered a med yet.
                    Toast.makeText(getActivity(), "You have not selected a medication type", Toast.LENGTH_LONG).show();

                } else if (selection.equals("routine")) { // routine
                    if (isFormComplete("routine")) {
                        submitRoutineToDB();
                        hideKeyboard();
                        mListener.onFragmentInteraction(1);
                    } else {
                        Toast.makeText(getActivity(), "You forgot to enter something", Toast.LENGTH_SHORT).show();
                    }
                } else { // prn med
                    if (isFormComplete("prn")) {
                        submitPrnToDB();
                        hideKeyboard();
                        mListener.onFragmentInteraction(1);
                    } else {
                        Toast.makeText(getActivity(), "You forgot to enter something", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (getArguments() != null && getArguments().getBoolean("edit")) {

            populateForEdit(getArguments().getString("name"));

        }

    } // end onActivityCreated()

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

    private void setRoutineForm() {
        // make any changes for a routine med form here. TODO
        prnFreqBox.setVisibility(View.GONE);
    }

    private void setPRNForm() {
        // make any changes for a prn form here TODO
        prnFreqBox.setVisibility(View.VISIBLE);
    }

    private void setTextChangeListener() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.d(TAG, "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable editable) {

                // populate edittexts.

                Log.d(TAG, "textChanged");

                if (Integer.valueOf(editable.toString()) < (adminTimesList.getChildCount() / 2)) {
                    adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                    adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                    etList.remove(etList.size() - 1);
                } else {
                    createEditTexts(editable);
                }

            }
        };
        adminTimes.addTextChangedListener(textWatcher);
    }

    private void submitRoutineToDB() {
        // TODO grab form data here.
        dataSource.submitNewMedication(prepareMedicationObject());
    }

    private void submitPrnToDB() {
        // TODO grab form data here.
        dataSource.submitNewMedication(prepareMedicationObject());
    }

    private Medication prepareMedicationObject() {
        Medication medication = new Medication();
        String type = med_type.getSelectedItem().toString().toLowerCase().trim();

        medication.setMedName(acMedName.getText().toString().toLowerCase().trim());
        medication.setAdminType(type);
        medication.setDoseMeasure(Integer.valueOf(med_dose.getText().toString().trim()));
        medication.setDoseMeasureType(med_measure_spinner.getSelectedItem().toString().toLowerCase().trim());
        medication.setDoseCount(Integer.valueOf(adminTimes.getText().toString()));
        //medication.setFillDate(lastFilled.getText().toString().toLowerCase().trim()); // Will add to next roll-out.
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        medication.setStartDate(df.format(cal.getTime()));


        if (type.equals("routine")) {
            medication.setDoseTimes(new Object() {
                @Override
                public String toString() {
                    String compiled = "";
                    for (index = etList.size() - 1; index > -1; index--) {
                        compiled += etList.get(index).getText().toString() + ";";
                    }
                    compiled = compiled.substring(0, compiled.length() - 1); // remove trailing semi-colon
                    return compiled;
                }
            }.toString());
        } else {
            //TODO deal with prn.
            medication.setDoseFrequency(Integer.valueOf(doseFrequency.getText().toString()));
        }

        return medication;
    }

    void fillList(String chars) {

        MedAutoCompleteHelper medAutoCompleteHelper = new MedAutoCompleteHelper(getActivity());

        ArrayList<String> medList = new ArrayList<String>();

        try {
            medList = medAutoCompleteHelper.getMedList(chars);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (XmlPullParserException x) {
            Log.e(TAG, x.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_item, medList);

        acMedName.setAdapter(adapter);
    }

    /**
     * Validates that form was filled.
     * <p/>
     * TODO validate data in form modules.
     * TODO Mark parts of form that were invalid.
     *
     * @param type
     * @return
     */
    private boolean isFormComplete(String type) {

        if (type.equals("prn")) {
            if (acMedName.getText().length() == 0 || med_dose.getText().length() == 0 || doseFrequency.getText().length() == 0 || adminTimes.getText().toString().equals("0")) {
                return false;
            }
        }

        if (type.equals("routine")) {
            if (acMedName.getText().length() == 0 || med_dose.getText().length() == 0 || adminTimes.getText().length() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called if instance created for editing a medication.
     * populates form data from Medication Object passed.
     *
     * @param name
     */
    private void populateForEdit(String name) {
        // TODO will populate for edits.

        Medication medication = dataSource.getSingleMedByName(name);

        for (int index = 0; index < med_type.getCount(); index++) {
            if (med_type.getItemAtPosition(index).toString().equalsIgnoreCase(medication.getAdminType())) {

                med_type.setSelection(index);
                break;
            }
        }

        acMedName.setText(medication.getMedName());
        med_dose.setText(String.valueOf(medication.getDoseMeasure()));
        for (int index = 0; index < med_measure_spinner.getCount(); index++) {
            if (med_measure_spinner.getItemAtPosition(index).toString().equalsIgnoreCase(medication.getDoseMeasureType())) {

                med_measure_spinner.setSelection(index);
                break;
            }
        }

        adminTimes.setText(String.valueOf(medication.getDoseCount()));
        if (medication.getAdminType().equalsIgnoreCase("routine")) {
            createEditTexts(adminTimes.getEditableText());

            String splitTimes[] = medication.getDoseTimes().split(";");

            for (int index = 0; index < splitTimes.length; index++) {
                etList.get(index).setText(splitTimes[index]);
            }
        } else {
            doseFrequency.setText(String.valueOf(medication.getDoseFrequency()));
        }

        dc_med.setVisibility(View.VISIBLE);
        delete_med.setVisibility(View.VISIBLE);
        setEditButtonListeners(); // turn on listeners for buttons.
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag);
    }

    @Override
    public void onPause() {
        dataSource.close();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        Log.d(TAG, "onDestroy");
        super.onDestroy();

    }

    private void createEditTexts(Editable editable) {

        int viewCount = Integer.valueOf(editable.toString()) - (adminTimesList.getChildCount() / 2);
        for (index = 0; index < viewCount; index++) {
            TextView tv = new TextView(getActivity());
            tv.setText("Enter " + VerifyHelpers.getCountVerbage(index + 1) + " Medication");
            dt = new MakeDateTimeHelper();

            etList.add(index, new EditText(getActivity()));
            etList.get(index).setId(Integer.valueOf(editable.toString()));
            etList.get(index).setFocusable(false);
            Time now = new Time();
            now.setToNow();
            // etList.get(index).setText(dt.convertToTime12(now.hour + ":" + String.format("%02d", now.minute)));
            etList.get(index).setHint("Enter time");

            etList.get(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View v = view;
                    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view2, int hourOfDay, int minute) {

                            EditText et = (EditText) getActivity().findViewById(v.getId());
                            et.setText(dt.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                            if (imm.isAcceptingText()) {
                                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                            }

                        }
                    };

                    TimePickerDialog tpd = new TimePickerDialog(getActivity(), t, 0, 0, false);

                    tpd.show();
                }
            });

            adminTimesList.addView(tv);
            adminTimesList.addView(etList.get(index));
        }
    }

    private void setEditButtonListeners() {
        delete_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSource.changeMedicationStatus(acMedName.getText().toString().toLowerCase(), "del");
                mListener.onFragmentInteraction(1);
            }
        });

        dc_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSource.changeMedicationStatus(acMedName.getText().toString().toLowerCase(), "dc");
                mListener.onFragmentInteraction(1);
            }
        });
    }

    private ScrollView buildDoseChoicesForDialog(ArrayList<String> choices) {
        ScrollView checkBox = new ScrollView(getActivity());
        RadioGroup radioGroup = new RadioGroup(getActivity());

        for (int index = 0; index < choices.size(); index++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(choices.get(index));

            radioButton.setId(index);
            radioGroup.setBackgroundColor(getResources().getColor(android.R.color.white));
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(i);
                String doseData[] = checkedRadioButton.getText().toString().split(" ");

                med_dose.setText(doseData[1]);
                boolean foundMatch = false;
                for (int index = 0; index < med_measure_spinner.getCount(); index++) {
                    if (med_measure_spinner.getItemAtPosition(index).toString().equalsIgnoreCase(doseData[2])) {

                        med_measure_spinner.setSelection(index);
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) { // dose measure is not listed yet.
                    // TODO need to make a database for storing new dose measures. and populate spinner via new databse.
                }

                adDoseChoices.dismiss();

            }
        });
        checkBox.addView(radioGroup);
        return checkBox;

    }
}
