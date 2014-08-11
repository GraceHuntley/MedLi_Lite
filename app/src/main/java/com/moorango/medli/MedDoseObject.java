package com.moorango.medli;

/**
 * Created by Colin on 8/11/2014.
 * Copyright 2014
 */
class MedDoseObject {

    private String umlscui;
    private String tty;
    private String rxcui;
    private String name;
    private String language;

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    public String getRxcui() {
        return rxcui;
    }

    public void setRxcui(String rxcui) {
        this.rxcui = rxcui;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSuppress() {
        return suppress;
    }

    public void setSuppress(String suppress) {
        this.suppress = suppress;
    }

    private String suppress;

    public String getUmlscui() {
        return umlscui;
    }

    public void setUmlscui(String umlscui) {
        this.umlscui = umlscui;
    }


}
