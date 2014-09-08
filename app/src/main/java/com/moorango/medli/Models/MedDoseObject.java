package com.moorango.medli.Models;

public class MedDoseObject {

    private String tty;
    private String genericName;
    private String fullText;
    private double doseDouble;
    private String doseMeasure;
    private String doseType;
    @SuppressWarnings("UnusedAssignment")
    private final String TAG = "MedDoseObject";
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

        setDoseType(splitEntry[splitEntry.length - 1]);
        findDoseDouble(splitEntry, splitEntry.length);

    }

    public boolean findDoseDouble(String val[], int index) {

        if (index == 0) return true; // failsafe.
        double dose;
        try {
            dose = Double.valueOf(val[index - 1].replace(",", ""));

            String newName = "";

            if (getGenericName() == null) {
                for (int i = 0; i < index; i++) {
                    newName += val[i] + " ";
                }
                setGenericName(newName);
            }
            setDoseDouble(dose);
            setDoseMeasure(val[index]);
            return false;

        } catch (NumberFormatException nfe) {
            findDoseDouble(val, index - 1);
        }

        return true;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getFullText() {
        return this.fullText;
    }

    public void setIsProperty() {
        this.isProperty = true;
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