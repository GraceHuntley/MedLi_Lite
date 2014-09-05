package com.moorango.medli.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.util.Xml;
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

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Helper_DrugData;
import com.moorango.medli.Helpers.Helper_DataCheck;
import com.moorango.medli.Helpers.Helper_DateTime;
import com.moorango.medli.Models.MedDoseObject;
import com.moorango.medli.Models.Object_Medication;
import com.moorango.medli.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Fragment_MedSettings extends Fragment implements View.OnClickListener {

    private final String TAG = "MedSettingsFragment.java";
    private final ArrayList<EditText> etList = new ArrayList<EditText>();
    private EditText med_dose, doseFrequency, doseFormEntry, med_measure_spinner;
    private TextView adminTimes;
    private LinearLayout adminTimesList;
    private Spinner med_type;
    private AutoCompleteTextView acMedName;
    private TextWatcher textWatcher;
    private LinearLayout prnFreqBox, secondaryForm;
    private int index = 0;
    private boolean isRoutine = false;
    private Button delete_med, dc_med;
    private AlertDialog.Builder adb;
    private AlertDialog adDoseChoices;
    private static boolean isRunning = false;
    private Helper_DrugData drugDataHelper;
    private MedLiDataSource dataSource;
    private OnFragmentInteractionListener mListener;

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

        drugDataHelper = new Helper_DrugData();

        dataSource = MedLiDataSource.getHelper(getActivity());
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_med_settings, container, false);

        secondaryForm = (LinearLayout) view.findViewById(R.id.secondary_form_wrapper);
        doseFormEntry = (EditText) view.findViewById(R.id.dose_form_input);
        doseFrequency = (EditText) view.findViewById(R.id.prn_frequency_input);
        med_dose = (EditText) view.findViewById(R.id.med_dose_input);
        adminTimes = (TextView) view.findViewById(R.id.admin_count_edittext);
        adminTimesList = (LinearLayout) view.findViewById(R.id.admin_times_add_box);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        delete_med = (Button) getActivity().findViewById(R.id.del_med);
        //adminTimes = (TextView) getActivity().findViewById(R.id.admin_count_edittext);
        getActivity().findViewById(R.id.minus_button).setOnClickListener(this);
        getActivity().findViewById(R.id.plus_button).setOnClickListener(this);
        getActivity().findViewById(R.id.btn_add_med).setOnClickListener(this);
        dc_med = (Button) getActivity().findViewById(R.id.dc_med);
        med_type = (Spinner) getActivity().findViewById(R.id.med_type_spinner);
        med_measure_spinner = (EditText) getActivity().findViewById(R.id.med_measure_input);
        // adminTimesList = (LinearLayout) getActivity().findViewById(R.id.admin_times_add_box);
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

                        setTextChangeListener();
                        prnFreqBox.setVisibility(View.GONE);
                    } else { // setup form for prn med.
                        adminTimes.removeTextChangedListener(textWatcher);

                        adminTimesList.removeAllViews();
                        etList.clear();
                        prnFreqBox.setVisibility(View.VISIBLE);
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

        if (Helper_DataCheck.isNetworkAvailable(getActivity())) {
            acMedName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                    if (!isRunning) { // only start new asynctask if one is no running already.
                        isRunning = true;
                        new GetSuggestions().setParName(Uri.encode(charSequence.toString())).execute();
                    }

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
                        ArrayList<MedDoseObject> drugDosesArray = drugDataHelper.getDrugNUI(acMedName.getText().toString().toLowerCase().trim());
                        if (drugDosesArray.size() > 1) {
                            adb = new AlertDialog.Builder(getActivity());
                            adb.setView(buildDoseChoicesForDialog(drugDosesArray));

                            adDoseChoices = adb.create();
                            adDoseChoices.show();

                        } else {
                            med_dose.requestFocus();
                            Toast.makeText(getActivity(), "Sorry there are no medication suggestions available.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (XmlPullParserException xmp) {
                        Log.e(TAG, xmp.toString());
                    } catch (IOException io) {
                        Log.e(TAG, io.toString());
                    }
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(acMedName.getWindowToken(), 0);
                }
            });
        }

        if (getArguments() != null && getArguments().getBoolean("edit")) {

            populateForEdit(getArguments().getInt("unique_id"));
        }

    } // end onActivityCreated()

    /**
     * TextChange Listener for  Dose count edittext.
     */
    private void setTextChangeListener() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.i(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.i(TAG, "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    if (Integer.valueOf(editable.toString()) < (adminTimesList.getChildCount() / 2)) {
                        adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                        adminTimesList.removeViewAt(adminTimesList.getChildCount() - 1);
                        etList.remove(etList.size() - 1);
                    } else {
                        createEditTexts(editable);
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, nfe.toString());
                } catch (NullPointerException npe) {
                    Log.e(TAG, npe.toString());
                }

            }
        };
        adminTimes.addTextChangedListener(textWatcher);
    }

    private Object_Medication prepareMedicationObject() {
        Object_Medication medication = new Object_Medication();
        String type = med_type.getSelectedItem().toString().toLowerCase().trim();

        medication.setMedName(acMedName.getText().toString().toLowerCase().trim());

        medication.setAdminType(type);
        medication.setDoseMeasure(Float.valueOf(med_dose.getText().toString().trim()));
        medication.setDoseMeasureType(med_measure_spinner.getText().toString().toLowerCase().trim());
        medication.setDoseForm(doseFormEntry.getText().toString());
        medication.setDoseCount(Integer.valueOf(adminTimes.getText().toString()));
        medication.setDoseForm(doseFormEntry.getText().toString());
        //medication.setFillDate(lastFilled.getText().toString().toLowerCase().trim()); // Will add to next roll-out.
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        medication.setStartDate(df.format(cal.getTime()));

        if (getArguments() != null && getArguments().getBoolean("edit")) {
            medication.setIdUnique(getArguments().getInt("unique_id"));
        }


        if (type.equals("routine")) {
            medication.setDoseTimes(new Object() {
                @Override
                public String toString() {
                    String compiled = "";
                    for (index = 0; index < etList.size(); index++) {
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

        int emptyCount = 0;
        if (type.equalsIgnoreCase("routine")) {

            for (EditText et : etList) { // check that times have been entered.
                if (et.getText().length() == 0) {
                    et.setBackgroundColor(getResources().getColor(R.color.red));
                    emptyCount++;
                }

            }
        }

        if (type.equals("prn")) {
            if (acMedName.getText().length() == 0
                    || med_dose.getText().length() == 0
                    || doseFrequency.getText().length() == 0
                    || adminTimes.getText().toString().equals("0")
                    || doseFormEntry.getText().toString().length() == 0) {
                return false;
            }
        }

        if (type.equals("routine")) {
            if (acMedName.getText().length() == 0
                    || med_dose.getText().length() == 0
                    || adminTimes.getText().length() == 0
                    || doseFormEntry.getText().length() == 0
                    || emptyCount > 0) {
                return false;
            }

        }
        return true;
    }

    /**
     * Called if instance created for editing a medication.
     * populates form data from Medication Object passed.
     *
     * @param uniqueID
     */
    private void populateForEdit(int uniqueID) {
        // TODO will populate for edits.

        Object_Medication medication = dataSource.getSingleMedByName(uniqueID);

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
                    adminTimes.setText(String.valueOf(index + 1));
                    //etList.get(index).setText(splitTimes[etList.get(index).getId()]);

                }
                for (EditText anEtList : etList) {
                    anEtList.setText(splitTimes[anEtList.getId()]);

                }
            }
        } else {
            doseFrequency.setText(String.valueOf(medication.getDoseFrequency()));
        }
        doseFormEntry.setText(medication.getDoseForm());
        dc_med.setVisibility(View.VISIBLE);
        delete_med.setVisibility(View.VISIBLE);
        setEditButtonListeners(); // turn on listeners for buttons.
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_med:

                String selection = med_type.getSelectedItem().toString().toLowerCase();
                boolean doUpdate = false;
                if (getArguments() != null && getArguments().getBoolean("edit")) {
                    doUpdate = true;
                }

                if (selection.equals("select")) {
                    // TODO alert they have not entered a med yet.
                    Toast.makeText(getActivity(), "You have not selected a medication type", Toast.LENGTH_LONG).show();

                } else if (selection.equals("routine")) { // routine
                    if (isFormComplete("routine")) {

                        dataSource.submitNewMedication(prepareMedicationObject(), doUpdate);
                        hideKeyboard();
                        mListener.onFragmentInteraction(1, null, 0);
                    } else {
                        Toast.makeText(getActivity(), "You forgot to enter something", Toast.LENGTH_SHORT).show();
                    }
                } else { // prn med
                    if (isFormComplete("prn")) {
                        dataSource.submitNewMedication(prepareMedicationObject(), doUpdate);
                        hideKeyboard();
                        mListener.onFragmentInteraction(1, null, 0);
                    } else {
                        Toast.makeText(getActivity(), "You forgot to enter something", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.plus_button:

                adminTimes.setText("" + (Integer.valueOf(adminTimes.getText().toString()) + 1));
                break;
            case R.id.minus_button:

                if (Integer.valueOf(adminTimes.getText().toString()) != 0) {
                    adminTimes.setText("" + (Integer.valueOf(adminTimes.getText().toString()) - 1));
                }
                break;

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag, String name, int id);
    }

    @Override
    public void onPause() {
        dataSource.close();

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

    private void createEditTexts(Editable editable) {

        int viewCount = Integer.valueOf(editable.toString()) - (adminTimesList.getChildCount() / 2);

        for (index = 0; index < viewCount; index++) {

            TextView tv = new TextView(getActivity());
            tv.setText("Enter " + Helper_DataCheck.getCountVerbage(Integer.valueOf(editable.toString())) + " Dose");
            int location = Integer.valueOf(editable.toString()) - 1;

            etList.add(location, new EditText(getActivity()));

            etList.get(location).setId(location);
            etList.get(location).setFocusable(false);
            Time now = new Time();
            now.setToNow();

            etList.get(location).setHint("Enter time");

            etList.get(location).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View v = view;
                    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view2, int hourOfDay, int minute) {

                            EditText et = etList.get(v.getId());

                            et.setText(Helper_DateTime.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));
                            et.setBackgroundColor(getResources().getColor(android.R.color.white));

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                            if (imm.isAcceptingText()) {
                                try {
                                    //noinspection ConstantConditions
                                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                                } catch (NullPointerException npe) {
                                    Log.e(TAG, npe.toString());
                                }

                            }
                        }
                    };

                    TimePickerDialog tpd = new TimePickerDialog(getActivity(), t, 0, 0, false);

                    tpd.show();
                }
            });

            adminTimesList.addView(tv);
            adminTimesList.addView(etList.get(location));
        }
    }

    private void setEditButtonListeners() {
        delete_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSource.changeMedicationStatus(acMedName.getText().toString().toLowerCase(), "del");
                mListener.onFragmentInteraction(1, null, 0);
            }
        });

        dc_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSource.changeMedicationStatus(acMedName.getText().toString().toLowerCase(), "dc");
                mListener.onFragmentInteraction(1, null, 0);
            }
        });
    }

    private ScrollView buildDoseChoicesForDialog(final ArrayList<MedDoseObject> choices) {
        ScrollView checkBox = new ScrollView(getActivity());
        RadioGroup radioGroup = new RadioGroup(getActivity());
        int objectCount = 0;
        for (MedDoseObject medObject : choices) {

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(medObject.getGenericName() + " " + medObject.getDoseDouble() + " " + medObject.getDoseMeasure() + " " + medObject.getDoseType());
            radioButton.setId(objectCount);
            objectCount++;
            radioGroup.setBackgroundColor(getResources().getColor(android.R.color.white));
            radioGroup.addView(radioButton);
        }

        RadioButton radioButton = new RadioButton(getActivity());
        radioButton.setText("Other");
        radioButton.setId(choices.size() + 1);
        radioGroup.setBackgroundColor(getResources().getColor(android.R.color.white));
        radioGroup.addView(radioButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(i);
                int id = checkedRadioButton.getId();

                if (!checkedRadioButton.getText().toString().equalsIgnoreCase("Other")) {

                    med_dose.setText(String.valueOf(choices.get(id).getDoseDouble()));

                    med_measure_spinner.setText(choices.get(id).getDoseMeasure());

                }
                adDoseChoices.dismiss();

            }
        });
        checkBox.addView(radioGroup);
        return checkBox;

    }

    class GetSuggestions extends AsyncTask<Void, Void, ArrayList<String>> {

        String partName;

        public GetSuggestions setParName(String partName) {
            this.partName = partName;
            return this;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... unused) {

            ArrayList<String> list = new ArrayList<String>();
            int eventType;
            XmlPullParser parser;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            String apiDomain = "http://rxnav.nlm.nih.gov/REST/";
            String apiCall = "spellingsuggestions?name=";
            HttpGet httpGet = new HttpGet(apiDomain + apiCall + partName);

            try {
                HttpResponse response = httpClient.execute(httpGet);
                InputStream is = response.getEntity().getContent();
                parser = Xml.newPullParser();

                parser.setInput(is, null);

                eventType = parser.getEventType();

                String text = "";

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            // do nothing.
                            break;
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equals("suggestion")) {
                                list.add(text);
                            }
                            break;

                    }
                    eventType = parser.next();
                }

            } catch (IOException ioe) {
                Log.e(TAG, ioe.toString());
            } catch (XmlPullParserException xmlp) {
                Log.e(TAG, xmlp.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.select_dialog_item, list);

            acMedName.setAdapter(adapter);
            isRunning = false;
            super.onPostExecute(list);
        }
    }

}