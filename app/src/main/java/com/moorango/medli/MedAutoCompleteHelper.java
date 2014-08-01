package com.moorango.medli;

import android.content.Context;
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
public class MedAutoCompleteHelper {


    private final String TAG = "MedAutoCompleteHelper.java";


    public MedAutoCompleteHelper(Context con) {
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

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.TEXT) {

                list.add(parser.getText());
            }
            eventType = parser.next();
        }

        return list;
    }
}
