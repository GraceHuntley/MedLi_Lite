package com.moorango.medli;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private EditText med_dose, adminTimes, lastFilled;
    private MakeDateTimeHelper dt;
    private LinearLayout adminTimesList;
    private AlertDialog.Builder ad;
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

    private MedLiDataSource dbHelper;

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

        dbHelper = MedLiDataSource.getHelper(getActivity());

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

        ad = new AlertDialog.Builder(getActivity());


        secondaryForm = (LinearLayout) getActivity().findViewById(R.id.secondary_form_wrapper);
        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.med_edit_layout);
        delete_med = (Button) getActivity().findViewById(R.id.del_med);
        Button plusButton = (Button) getActivity().findViewById(R.id.plus_button);
        adminTimes = (EditText) getActivity().findViewById(R.id.admin_count_edittext);
        Button minusButton = (Button) getActivity().findViewById(R.id.minus_button);
        doseFrequency = (EditText) getActivity().findViewById(R.id.prn_frequency_input);
        dc_med = (Button) getActivity().findViewById(R.id.dc_med);
        Button submitMed = (Button) getActivity().findViewById(R.id.btn_add_med);
        med_type = (Spinner) getActivity().findViewById(R.id.med_type_spinner);
        med_dose = (EditText) getActivity().findViewById(R.id.med_dose_input);
        lastFilled = (EditText) getActivity().findViewById(R.id.last_filled_date);
        med_measure_spinner = (Spinner) getActivity().findViewById(R.id.med_measure_spinner);
        adminTimesList = (LinearLayout) getActivity().findViewById(R.id.admin_times_add_box);
        prnFreqBox = (LinearLayout) getActivity().findViewById(R.id.prn_frequency_box);

        acMedName = (AutoCompleteTextView) getActivity().findViewById(R.id.ac_Med_name);
        acMedName.setThreshold(3);


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
                        // adminTimes.setText("0");
                        setTextChangeListener();
                        setRoutineForm();
                    } else { // setup form for prn med.
                        adminTimes.removeTextChangedListener(textWatcher);
                        // adminTimes.setText("0");
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

                    acMedName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            med_dose.requestFocus();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    acMedName.setTextColor(Color.BLACK);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        acMedName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                med_dose.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(acMedName.getWindowToken(), 0);
            }
        });

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

            }

            @Override
            public void afterTextChanged(Editable editable) {

                // populate edittexts.

                if (Integer.valueOf(editable.toString()) < (adminTimesList.getChildCount() / 2)) {
                    adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                    adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                    etList.remove(etList.size() - 1);
                } else {
                    int viewCount = Integer.valueOf(editable.toString()) - (adminTimesList.getChildCount() / 2);
                    for (index = 0; index < viewCount; index++) {
                        TextView tv = new TextView(getActivity());
                        tv.setText("Enter " + VerifyHelpers.getCountVerbage(Integer.valueOf(editable.toString())) + " Medication");
                        dt = new MakeDateTimeHelper();

                        etList.add(index, new EditText(getActivity()));
                        etList.get(index).setId(Integer.valueOf(editable.toString()));
                        Time now = new Time();
                        now.setToNow();
                        etList.get(index).setText(dt.convertToTime12(now.hour + ":" + String.format("%02d", now.minute)));
                        etList.get(index).setOnTouchListener(new View.OnTouchListener() {


                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {

                                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                                    final View v = view;
                                    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view2, int hourOfDay, int minute) {

                                            //etList2.get(nextIndex - 1).setText(dt.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));
                                            EditText et = (EditText) getActivity().findViewById(v.getId());
                                            et.setText(dt.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));

                                        }
                                    };

                                    String timeSplit[] = dt.convertToTime24(etList.get(index - 1).getText().toString()).split(":");

                                    TimePickerDialog tpd = new TimePickerDialog(getActivity(), t, Integer.valueOf(timeSplit[0]), Integer.valueOf(timeSplit[1]), false);

                                    tpd.show();
                                }
                                return false;
                            }
                        });

                        adminTimesList.addView(tv);
                        adminTimesList.addView(etList.get(index));
                    }
                }

            }
        };
        adminTimes.addTextChangedListener(textWatcher);
    }

    private void submitRoutineToDB() {
        // TODO grab form data here.
        dbHelper.submitNewMedication(prepareMedicationObject());
    }

    private void submitPrnToDB() {
        // TODO grab form data here.
        dbHelper.submitNewMedication(prepareMedicationObject());
    }

    private Medication prepareMedicationObject() {
        Medication medication = new Medication();
        String type = med_type.getSelectedItem().toString().toLowerCase().trim();

        medication.setMedName(acMedName.getText().toString().toLowerCase().trim());
        medication.setAdminType(type);
        medication.setDoseMeasure(Integer.valueOf(med_dose.getText().toString().trim()));
        medication.setDoseMeasureType(med_measure_spinner.getSelectedItem().toString().toLowerCase().trim());
        medication.setDoseCount(Integer.valueOf(adminTimes.getText().toString()));
        medication.setFillDate(lastFilled.getText().toString().toLowerCase().trim());
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

    @Override
    public void onPause() {
        dbHelper.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();

    }

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

    private void populateForEdit(String name) {
        // TODO will populate for edits.

        Medication medication = dbHelper.getSingleMedByName(name);

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
        Log.d(TAG, "" + medication.getDoseCount());
        for (int index = 0; index < medication.getDoseCount(); index++) {
            adminTimes.setText("" + (index + 1));
            // adminTimes.setText("1");

        }

        dc_med.setVisibility(View.VISIBLE);
        delete_med.setVisibility(View.VISIBLE);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag);
    }
}
