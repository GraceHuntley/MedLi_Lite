package com.moorango.medli.lib.network;

import com.moorango.medli.lib.Constants;


public class URL {

    String url;

    private URL(String url) {
        this.url = url;
    }

    public static URL generateUnsecureURL(String relativeUrl) {
        return new URL(Constants.UNSECURED_BASE_URL + relativeUrl);
    }

    /*public static URL generateSecureURL(String relativeUrl) {
        return new URL(Constants.SECURED_BASE_URL + relativeUrl);
    } */


    public static URL generateUrlFromScratch(String url) {
        return new URL(url);
    }

    public String getURL() {
        return url;
    }


}
