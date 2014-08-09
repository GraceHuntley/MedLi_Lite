package com.moorango.medli;

import android.util.Xml;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Colin on 8/8/2014.
 * Copyright 2014
 */
public class DrugDataHelper {

    private final String TAG = "DrugDataHelper.java";

    private static final String ns = null;

    private MedLiDataSource dataSource;


    DrugDataHelper() {

    }

    public void getAutoCompleteHelper() {

    }

    public ArrayList<String> getDrugNUI(String drugName) throws IOException, XmlPullParserException {

        ArrayList<String> doseList = new ArrayList<String>();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        //String apiDomain = "http://rxnav.nlm.nih.gov/REST/Ndfrt/";
        //String apiCall = "search?conceptName=" + URLEncoder.encode(drugName, "utf-8") + "&kindName=DRUG_KIND";
        String apiCall = "http://rxnav.nlm.nih.gov/REST/rxcui?name=" + URLEncoder.encode(drugName, "utf-8");

        HttpGet httpGet = new HttpGet(apiCall);


        HttpResponse response = httpClient.execute(httpGet);
        InputStream is = response.getEntity().getContent();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.setInput(is, null);

        String result = null;

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName().toString();
            if (name.equals("rxnormId")) {
                if (parser.next() == XmlPullParser.TEXT) {

                    result = parser.getText().toString();

                    break;
                }
            }
        }

        if (result != null) {
            doseList = getDoseListFromNui(result);
        }
        return doseList;
    }

    private ArrayList<String> getDoseListFromNui(String rxcui) throws IOException, XmlPullParserException {
        ArrayList<String> doseList = new ArrayList<String>();

        DefaultHttpClient httpClient = new DefaultHttpClient();
        // String apiDomain = "http://rxnav.nlm.nih.gov/REST/Ndfrt/childConcepts/nui=" + nui + "&transitive=true";
        String apiDomain = "http://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui + "/related?rela=" + Constants.RX_ATTRIBUTES;

        HttpGet httpGet = new HttpGet(apiDomain);

        HttpResponse response = httpClient.execute(httpGet);
        InputStream is = response.getEntity().getContent();
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.setInput(is, null);

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName().toString();


            if (name.equals("tty")) {

                if (parser.next() == XmlPullParser.TEXT) {

                    doseList.add(parser.getText());

                }
            }
        }
        return doseList;
    }
}
