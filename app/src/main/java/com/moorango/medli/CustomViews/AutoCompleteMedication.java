package com.moorango.medli.CustomViews;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.moorango.medli.Constants;
import com.moorango.medli.Fragments.Fragment_MedSettings;
import com.moorango.medli.Helper_DrugData;
import com.moorango.medli.Models.MedDose;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Colin on 10/6/2014.
 * Copyright 2014
 */
public class AutoCompleteMedication extends AutoCompleteTextView {

    private final String TAG = "AutoCompleteMedication";
    private final int THRESHOLD = 3;

    private GetSuggestions getSuggestions;
    private Helper_DrugData drugDataHelper;
    private AlertDialog.Builder adb, dialog;
    private AlertDialog adDoseChoices;
    private Fragment_MedSettings fms;
    Activity caller;

    public AutoCompleteMedication(Fragment_MedSettings fms, final Activity caller) {


        super(fms.getActivity().getApplicationContext());

        Log.d(TAG, "test");
        this.fms = fms;
        this.caller = caller;

        setThreshold(THRESHOLD);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (getSuggestions != null && getSuggestions.getStatus() != AsyncTask.Status.RUNNING) { // only start new asynctask if one is no running already.

                    getSuggestions = new GetSuggestions().setParName(Uri.encode(charSequence.toString()));
                    getSuggestions.execute();
                } else {
                    if (getSuggestions != null) {
                        getSuggestions.cancel(true);
                    }
                    getSuggestions = new GetSuggestions().setParName(Uri.encode(charSequence.toString()));
                }

                Log.d(TAG, "textChanged");

                //acMedName.setTextColor(Color.BLACK);
                setTextColor(Color.BLACK);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            });

        setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                try {
                    ArrayList<MedDose> drugDosesArray = drugDataHelper.getDrugNUI(getText().toString().toLowerCase().trim());
                    if (drugDosesArray.size() > 1) {
                        adb = new AlertDialog.Builder(caller);
                        adb.setView(buildDoseChoicesForDialog(drugDosesArray));

                        adDoseChoices = adb.create();
                        adDoseChoices.show();

                    } else {

                        dialog = new AlertDialog.Builder(caller)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("No dose suggestions")
                                .setMessage(Constants.NO_DOSE_SUGGESTIONS)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        dialog.show();

                        //Fragment fms = caller.getFragmentManager().findFragmentByTag("medSettings");

                        /***
                         * TODO will reinstate focus shift after everything else is working.
                         */
                        // med_dose.requestFocus();

                    }

                } catch (XmlPullParserException xmp) {
                    Log.e(TAG, xmp.toString());
                } catch (IOException io) {
                    Log.e(TAG, io.toString());
                }
                InputMethodManager imm = (InputMethodManager) caller.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);

            }
        });
    }

    public AutoCompleteMedication(final Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "defaultConst");



        setThreshold(THRESHOLD);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (getSuggestions != null && getSuggestions.getStatus() != AsyncTask.Status.RUNNING) { // only start new asynctask if one is no running already.

                    getSuggestions = new GetSuggestions().setParName(Uri.encode(charSequence.toString()));
                    getSuggestions.execute();
                } else {
                    if (getSuggestions != null) {
                        getSuggestions.cancel(true);
                    }
                    getSuggestions = new GetSuggestions().setParName(Uri.encode(charSequence.toString()));
                }


                //acMedName.setTextColor(Color.BLACK);
                setTextColor(Color.BLACK);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                drugDataHelper = new Helper_DrugData();
                try {
                    ArrayList<MedDose> drugDosesArray = drugDataHelper.getDrugNUI(getText().toString().toLowerCase().trim());
                    if (drugDosesArray.size() > 1) {
                        adb = new AlertDialog.Builder(context);
                        adb.setView(buildDoseChoicesForDialog(drugDosesArray));

                        adDoseChoices = adb.create();
                        adDoseChoices.show();

                    } else {

                        dialog = new AlertDialog.Builder(caller)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("No dose suggestions")
                                .setMessage(Constants.NO_DOSE_SUGGESTIONS)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        dialog.show();

                        //Fragment fms = caller.getFragmentManager().findFragmentByTag("medSettings");

                        /***
                         * TODO will reinstate focus shift after everything else is working.
                         */
                        // med_dose.requestFocus();

                    }

                } catch (XmlPullParserException xmp) {
                    Log.e(TAG, xmp.toString());
                } catch (IOException io) {
                    Log.e(TAG, io.toString());
                }
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);

            }
        });
    }

    public AutoCompleteMedication(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setThreshold(THRESHOLD);
    }


    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    private ScrollView buildDoseChoicesForDialog(final ArrayList<MedDose> choices) {
        ScrollView checkBox = new ScrollView(getContext());
        RadioGroup radioGroup = new RadioGroup(getContext());
        int objectCount = 0;
        for (MedDose medObject : choices) {

            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(medObject.getGenericName() + " " + medObject.getDoseDouble() + " " + medObject.getDoseMeasure() + " " + medObject.getDoseType());
            radioButton.setId(objectCount);
            objectCount++;
            radioGroup.setBackgroundColor(getResources().getColor(android.R.color.white));
            radioGroup.addView(radioButton);
        }

        RadioButton radioButton = new RadioButton(getContext());
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



                    fms.med_dose.setText(String.valueOf(choices.get(id).getDoseDouble()));
                    fms.med_dose.setError(null);

                    fms.med_measure_spinner.setText(choices.get(id).getDoseMeasure());
                    fms.med_measure_spinner.setError(null);
                    fms.doseFormEntry.requestFocus();

                } else {
                    fms.med_dose.requestFocus();
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

                if (!isCancelled()) {
                    HttpResponse response = httpClient.execute(httpGet);
                    InputStream is = response.getEntity().getContent();
                    parser = Xml.newPullParser();

                    parser.setInput(is, null);

                    eventType = parser.getEventType();

                    String text = "";

                    while (!isCancelled() && eventType != XmlPullParser.END_DOCUMENT) {

                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                // do nothing.
                                break;
                            case XmlPullParser.TEXT:
                                text = parser.getText();
                                break;
                            case XmlPullParser.END_TAG:
                                if (parser.getName().equalsIgnoreCase("suggestion")) {
                                    list.add(text);
                                }
                                break;

                        }
                        eventType = parser.next();
                    }
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.select_dialog_item, list);

                //acMedName.setAdapter(adapter);
                setAdapter(adapter);

            }
            super.onPostExecute(list);
        }
    }
}