package com.moorango.medli;

import com.moorango.medli.Models.MedDose;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Colin on 8/8/2014.
 * Copyright 2014
 */
@SuppressWarnings("WeakerAccess")
public class Helper_DrugData {

    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "DrugDataHelper.java";

    private String text;

    public Helper_DrugData() {
        // mandatory empty constructor.
    }

    public ArrayList<MedDose> getDrugNUI(String drugName) throws IOException, XmlPullParserException {

        return getDoseListFromNui(URLEncoder.encode(drugName, "utf-8"));

    }

    private ArrayList<MedDose> getDoseListFromNui(String rxcui) throws IOException, XmlPullParserException {
        ArrayList<MedDose> doseList = new ArrayList<MedDose>();


        String apiDomain = "http://rxnav.nlm.nih.gov/REST/drugs?name=" + rxcui;

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(apiDomain);
        HttpResponse response = httpClient.execute(httpGet);
        InputStream is = response.getEntity().getContent();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, null);

        int eventType = parser.getEventType();

        MedDose mdo = new MedDose();


        while (eventType != XmlPullParser.END_DOCUMENT) {

            String tagName = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:

                    if (tagName.equalsIgnoreCase("conceptProperties")) {
                        mdo = new MedDose();
                        mdo.setIsProperty();
                    }
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText();

                    break;

                case XmlPullParser.END_TAG:

                    if (tagName.equalsIgnoreCase("conceptProperties")) {

                        if (mdo.getTty().equalsIgnoreCase("SBD")) {
                            Pattern pattern = Pattern.compile("( / )");
                            Matcher matcher = pattern.matcher(mdo.getFullText());
                            if (!matcher.find()) {
                                mdo.fillObject(mdo.getFullText());
                                doseList.add(mdo);
                            }
                        }
                    } else if (tagName.equalsIgnoreCase("tty")) {
                        if (mdo.getIsProperty())
                            mdo.setTty(text);

                    } else if (tagName.equalsIgnoreCase("synonym")) {

                        mdo.setFullText(text);

                    }

                    break;

            }

            eventType = parser.next();
        }

        ArrayList<MedDose> readyList = new ArrayList<MedDose>();

        for (MedDose medObj : doseList) {
            if (medObj.getTty().equalsIgnoreCase("SBD") && medObj.getDoseDouble() > 0) {
                readyList.add(medObj);
            }
        }

        return readyList;
    }
}




