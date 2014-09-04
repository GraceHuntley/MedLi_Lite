package com.moorango.medli;

import android.util.Log;

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

    private final String TAG = "DrugDataHelper.java";

    private String text;

    Helper_DrugData() {
        // mandatory empty constructor.
    }

    public ArrayList<String> getDrugNUI(String drugName) throws IOException, XmlPullParserException {

        ArrayList<String> doseList = getDoseListFromNui(URLEncoder.encode(drugName, "utf-8"));
        doseList.add("Other");
        return doseList;
    }

    private ArrayList<String> getDoseListFromNui(String rxcui) throws IOException, XmlPullParserException {
        ArrayList<MedDoseObject> doseList = new ArrayList<MedDoseObject>();


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

        MedDoseObject mdo = new MedDoseObject();
        String tty = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {

            String tagName = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:

                    if (tagName.equalsIgnoreCase("conceptProperties")) {
                        mdo = new MedDoseObject();
                        mdo.setIsProperty(true);
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

        ArrayList<String> readyList = new ArrayList<String>();

        for (MedDoseObject medObj : doseList) {
            if (medObj.getTty().equalsIgnoreCase("SBD") && medObj.getDoseDouble() > 0) {
                readyList.add(medObj.getObject());
            }
        }

        return readyList;
    }
}

class MedDoseObject {

    private String tty;
    private String genericName;
    private String fullText;
    private double doseDouble;
    private String doseMeasure;
    private String doseType;
    private final String TAG = "MedDoseObject";
    private boolean isEmpty = true;
    private boolean isProperty = false;

    public void fillObject(String drug) {

        String entry = drug;

        int brandIndexStart = entry.indexOf("[");
        int brandIndexEnd = entry.indexOf("]");

        if (brandIndexStart > -1 && brandIndexEnd > -1) {
            setGenericName(entry.substring(brandIndexStart + 1, brandIndexEnd));
            entry = entry.substring(0, brandIndexStart).trim();
        }

        String splitEntry[] = entry.split(" ");

        isEmpty = !(findDoseDouble(splitEntry, splitEntry.length - 1));

        setDoseType(splitEntry[splitEntry.length - 1]);
        //isEmpty = false;
    }

    public boolean findDoseDouble(String val[], int index) {

        if (index == 0) return true; // failsafe.
        double dose = 0.0;
        try {
            dose = Double.valueOf(val[index].replace(",", ""));

            Log.d(TAG, "dose: " + dose);
            String newName = "";

            if (getGenericName() == null) {
                for (int i = 0; i < index; i++) {
                    newName += val[i] + " ";
                }
                setGenericName(newName);
            }
            setDoseDouble(dose);
            setDoseMeasure(val[index + 1]);
            return false;

        } catch (NumberFormatException nfe) {
            findDoseDouble(val, index - 1);
        }


        //Log.d(TAG, "index: " + index + " length: " + val.length);

        return true;
    }

    public String getObject() {
        if (isEmpty)
            return "";
        else
            return getGenericName() + " " + getDoseDouble() + " " + getDoseMeasure() + " " + getDoseType();
    }

    public void setIsEmpty(boolean value) {
        this.isEmpty = value;
    }

    public boolean getIsEmpty() {
        return this.isEmpty;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getFullText() {
        return this.fullText;
    }

    public void setIsProperty(boolean value) {
        this.isProperty = value;
    }

    public boolean getIsProperty() {
        return this.isProperty;
    }

    public void setDoseType(String doseType) {
        this.doseType = doseType;
    }

    public String getDoseType() {
        return this.doseType;
    }

    public void setDoseMeasure(String doseMeasure) {
        this.doseMeasure = doseMeasure;
    }

    public String getDoseMeasure() {
        return this.doseMeasure;
    }

    public void setDoseDouble(double doseDouble) {
        this.doseDouble = doseDouble;
    }

    public double getDoseDouble() {
        return this.doseDouble;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getGenericName() {
        return this.genericName;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

}


