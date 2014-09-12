package com.moorango.medli.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.moorango.medli.Helpers.AlarmHelpers;
import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.MedDose;
import com.moorango.medli.Models.Medication;
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
    private EditText maxDoses;
    private LinearLayout adminTimesList;
    private Spinner med_type;
    private AutoCompleteTextView acMedName;
    private TextWatcher textWatcher;
    private LinearLayout prnFreqBox;
    private ScrollView secondaryForm;
    private ScrollView formWrapper;
    private int index = 0;
    private boolean isRoutine = false;
    private Button delete_med, dc_med, clear;
    private AlertDialog.Builder adb;
    private AlertDialog adDoseChoices;
    private static boolean isRunning = false;
    private Helper_DrugData drugDataHelper;
    private MedLiDataSource dataSource;
    private OnFragmentInteractionListener mListener;
    private AlertDialog.Builder dialog;
    private TextView medTypePrompt;
    private int errorCount = 0;
    private GetSuggestions getSuggestions;
    private ArrayList<String> errorMessages;

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

        doseFormEntry = (EditText) view.findViewById(R.id.dose_form_input);
        doseFrequency = (EditText) view.findViewById(R.id.prn_frequency_input);
        med_dose = (EditText) view.findViewById(R.id.med_dose_input);
        maxDoses = (EditText) view.findViewById(R.id.admin_count_edittext);
        adminTimesList = (LinearLayout) view.findViewById(R.id.admin_times_add_box);
        medTypePrompt = (TextView) view.findViewById(R.id.med_type_label);
        formWrapper = (ScrollView) view.findViewById(R.id.scrollview_wrapper);
        clear = (Button) view.findViewById(R.id.clear_btn);
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
        // adminTimesList = (LinearLayout) getActivity().findViewById(R.id.admin_times_add_box);
        prnFreqBox = (LinearLayout) getActivity().findViewById(R.id.prn_frequency_box);

        acMedName = (AutoCompleteTextView) getActivity().findViewById(R.id.ac_Med_name);
        acMedName.setThreshold(3);

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
        med_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

                                           {
                                               @Override
                                               public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                   DataCheck.clearFormErrors(formWrapper);
                                                   errorCount = 0;
                                                   if (!adapterView.getSelectedItem().toString().toLowerCase().equals("select")) {
                                                       formWrapper.setVisibility(View.VISIBLE);
                                                       isRoutine = (adapterView.getSelectedItem().toString().toLowerCase().equals("routine"));
                                                       if (isRoutine) { // setup form for routine medication.
                                                           medTypePrompt.setText("Setting up a Routine medication");
                                                           setTextChangeListener();
                                                           prnFreqBox.setVisibility(View.GONE);
                                                           doseFrequency.setVisibility(View.GONE);
                                                       } else { // setup form for prn med.
                                                           maxDoses.removeTextChangedListener(textWatcher);
                                                           medTypePrompt.setText("Setting up a Non-Routine medication");
                                                           adminTimesList.removeAllViews();
                                                           etList.clear();
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

        if (DataCheck.isNetworkAvailable(

                getActivity()

        ))

        {
            acMedName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                    if (!isRunning) { // only start new asynctask if one is no running already.
                        isRunning = true;
                        getSuggestions = new GetSuggestions().setParName(Uri.encode(charSequence.toString()));
                        getSuggestions.execute();
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
                        ArrayList<MedDose> drugDosesArray = drugDataHelper.getDrugNUI(acMedName.getText().toString().toLowerCase().trim());
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

        clear.setOnClickListener(new View.OnClickListener() { // clears all entries on form.
            @Override
            public void onClick(View view) {
                DataCheck.clearForm(formWrapper);
                if (etList != null && adminTimesList != null) {
                    etList.clear();
                    adminTimesList.removeAllViews();
                }

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
        maxDoses.addTextChangedListener(textWatcher);
    }

    private Medication prepareMedicationObject() {
        Medication medication = new Medication();
        String type = med_type.getSelectedItem().toString().toLowerCase().trim();
        medication.setMedName(acMedName.getText().toString().toLowerCase().trim());
        medication.setAdminType(type);
        medication.setDoseMeasure(Float.valueOf(med_dose.getText().toString().trim()));
        medication.setDoseMeasureType(med_measure_spinner.getText().toString().toLowerCase().trim());
        medication.setDoseForm(doseFormEntry.getText().toString());
        medication.setDoseCount(Integer.valueOf(maxDoses.getText().toString()));
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

    private boolean isFormCompleted(ViewGroup group) {

        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText && view.getVisibility() == View.VISIBLE) {

                if (view.getId() == R.id.admin_count_edittext) {
                    if (Integer.valueOf(((EditText) view).getText().toString()) < 1) {
                        ((EditText) view).setError("This cannot be empty");
                        Log.d(TAG, "count");
                        errorCount++;
                    }
                } else if (view instanceof EditText && ((EditText) view).getText().length() == 0) {
                    Log.d(TAG, "empty edittexxt");
                    ((EditText) view).setError("This cannot be empty.");

                    errorCount++;
                }
            }


            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                isFormCompleted((ViewGroup) view);

        }

        return errorCount <= 0;
    }

    private boolean checkFormData(ViewGroup view) {
        errorMessages = new ArrayList<String>();
        errorCount = 0;
        if (acMedName.getText().length() == 0) {
            acMedName.setError("This cannot be empty.");
            errorMessages.add("- The medication name cannot be empty\n");

        }

        if (!isFormCompleted(view)) {
            errorMessages.add("- All medication information should be filled\n");
        }

        if (errorMessages.size() == 0 && !checkDoseTimes(etList)) {
            errorMessages.add("- Each dose time must be later then the previous one\n");
        }

        String toastMessage = "";
        for (String error : errorMessages) {

            toastMessage += error;
        }
        if (toastMessage.length() > 0) {
            Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
        }
        return errorMessages.size() == 0;
    }

    private boolean checkDoseTimes(ArrayList<EditText> etList) {

        if (etList.size() == 1) {
            return true;
        }

        int errors = 0;
        for (int index = etList.size() - 1; index > 0; index--) {
            String fDate = DateTime.convertToTime24(etList.get(index).getText().toString());
            String sDate = DateTime.convertToTime24(etList.get(index - 1).getText().toString());

            if (fDate.compareTo(sDate) <= 0) {
                etList.get(index).setError("Invalid Time");
                errors++;
            }

        }

        return errors == 0;
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
        setEditButtonListeners(uniqueID); // turn on listeners for buttons.
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

                } else {
                    errorCount = 0;
                    if (checkFormData(formWrapper)) {

                        //checkDoseTimes(etList);
                        dataSource.submitNewMedication(prepareMedicationObject(), doUpdate);
                        hideKeyboard();
                        mListener.onFragmentInteraction(1, null, 0);
                    } else {
                        //Toast.makeText(getActivity(), "You forgot to enter something", Toast.LENGTH_SHORT).show();
                    }
                    int recurseCountTest = 0;
                    Log.d(TAG, "ln 447: " + recurseCountTest);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int tag, String name, int id);
    }

    @Override
    public void onPause() {
        dataSource.close();

        if (getSuggestions != null && getSuggestions.getStatus() == AsyncTask.Status.RUNNING) {
            getSuggestions.cancel(true);
        }

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
            tv.setText("Enter " + DataCheck.getCountVerbage(Integer.valueOf(editable.toString())) + " Dose");
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

                            et.setText(DateTime.convertToTime12("" + hourOfDay + ":" + String.format("%02d", minute)));
                            et.setBackgroundColor(getResources().getColor(android.R.color.white));
                            et.setError(null);

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
                    EditText et = etList.get(v.getId());
                    if (et.length() > 0) {
                        String convertedTime[] = DateTime.convertToTime24(et.getText().toString()).split(":");
                        int hour = Integer.valueOf(convertedTime[0]);
                        int minute = Integer.valueOf(convertedTime[1]);
                        tpd.updateTime(hour, minute);


                    }

                    tpd.show();
                }
            });

            adminTimesList.addView(tv);
            adminTimesList.addView(etList.get(location));
        }
    }

    private void setEditButtonListeners(final int idUnique) {
        delete_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Delete " + acMedName.getText().toString() + "?\n"
                        + "This will also delete all associated Medication logs!";
                dialog = getDialog(message + "?", "Delete", 0, idUnique);
                dialog.show();
            }
        });

        dc_med.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = getDialog("Discontinue " + acMedName.getText().toString() + "?", "Discontinue", 0, idUnique);
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

    private ScrollView buildDoseChoicesForDialog(final ArrayList<MedDose> choices) {
        ScrollView checkBox = new ScrollView(getActivity());
        RadioGroup radioGroup = new RadioGroup(getActivity());
        int objectCount = 0;
        for (MedDose medObject : choices) {

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
                    med_dose.setError(null);

                    med_measure_spinner.setText(choices.get(id).getDoseMeasure());
                    med_measure_spinner.setError(null);

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
            if (!isCancelled()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.select_dialog_item, list);

                acMedName.setAdapter(adapter);
                isRunning = false;
            }
            super.onPostExecute(list);
        }
    }

}
