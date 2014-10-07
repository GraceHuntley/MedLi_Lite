package com.moorango.medli.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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


    public AutoCompleteMedication(Context context) {
        super(context);

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
    }

    public AutoCompleteMedication(Context context, AttributeSet attrs) {
        super(context, attrs);
        setThreshold(THRESHOLD);
    }

    public AutoCompleteMedication(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setThreshold(THRESHOLD);
    }


    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
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