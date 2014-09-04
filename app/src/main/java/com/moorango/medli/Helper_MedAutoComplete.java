package com.moorango.medli;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Colin on 7/3/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class Helper_MedAutoComplete {


    private final String TAG = "MedAutoCompleteHelper.java";


    public Helper_MedAutoComplete(Context con) {
        //TODO
    }

    /*
     * @return ArrayList<String> - populated with returned strings pulled from xml
     * @param String partname - characters to match in API
     * @throws JSONException, IOException, XmlPullParserException
     */
    public ArrayList<String> getMedList(String partName) throws IOException, XmlPullParserException {
        ArrayList<String> list = new ArrayList<String>();

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String apiDomain = "http://rxnav.nlm.nih.gov/REST/";
        String apiCall = "spellingsuggestions?name=";
        HttpGet httpGet = new HttpGet(apiDomain + apiCall + partName);
        HttpResponse response = httpClient.execute(httpGet);
        InputStream is = response.getEntity().getContent();
        XmlPullParser parser = Xml.newPullParser();

        parser.setInput(is, null);

        int eventType = parser.getEventType();

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

        return list;
    }

    class GetSuggestions extends AsyncTask<String, Void, ArrayList<String>> {


        @Override
        protected ArrayList<String> doInBackground(String... partNames) {

            ArrayList<String> list = new ArrayList<String>();
            int eventType = XmlPullParser.END_DOCUMENT;
            XmlPullParser parser = null;

            DefaultHttpClient httpClient = new DefaultHttpClient();
            String apiDomain = "http://rxnav.nlm.nih.gov/REST/";
            String apiCall = "spellingsuggestions?name=";
            HttpGet httpGet = new HttpGet(apiDomain + apiCall + partNames);

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
                Log.d(TAG, ioe.toString());
            } catch (XmlPullParserException xmlp) {
                Log.d(TAG, xmlp.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
        }
    }
}


