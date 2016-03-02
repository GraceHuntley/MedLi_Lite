package com.moorango.medli.lib;

/**
 * Created by cmac147 on 7/14/15.
 */
public class Constants {

    public static final boolean ENABLE_LOGGING = true;

    public static final int PASSWORD_MINIMUM = 1;

    public static final String UNSECURED_BASE_URL = "http://ec2-52-2-177-156.compute-1.amazonaws.com/";

    public static final String NO_NETWORK_CONNECT_NEW_MED = "Sorry there is no internet connection which means you will not get any dose suggestions for this medication. " +
            "But that's ok. " +
            "If the drug you are adding is for example Miralax crystals do as follows\n" +
            "ex." +
            "\nNumeric value: 1" +
            "\nMeasure unit: tsp" +
            "\nDose Form: 1 tsp\n" +
            "Otherwise you can just wait till your online to add the new medication.";

    public static final String NO_DOSE_SUGGESTIONS = "Sorry there are no dose suggestions available for this medication. " +
            "But that's ok. " +
            "If the drug you are adding is for example Miralax crystals do as follows\n" +
            "ex." +
            "\nNumeric value: 1" +
            "\nMeasure unit: tsp" +
            "\nDose Form: 1 tsp";
}
