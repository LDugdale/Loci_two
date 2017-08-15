package com.lauriedugdale.loci.utils;

import android.icu.util.Calendar;

import java.text.ParseException;
import java.util.Locale;

/**
 * Contains helper methods and constants for the data classes
 *
 * @author Laurie Dugdale
 */
public class DataUtils {

    // TODO write reject button - remove from database on reject
    // TODO switch to geofire api to query location more effectively
    // TODO IMPROVE SEARCH
    // TODO stop location querying if only moved a little bit and expand the query parameter to just outside the screen view to allow for this


    // int representation of what media the entry contains
    public static final int NO_MEDIA = 100;
    public static final int IMAGE = 101;
    public static final int AUDIO = 102;

    // codes for permission access
    public static final int ANYONE = 200;
    public static final int FRIENDS = 201;
    public static final int NO_ONE = 202;
    public static final int GROUP = 203;

    // codes to display who file came from
    public static final int FROM_SELF = 300;
    public static final int FROM_ANYONE = 301;
    public static final int FROM_GROUP = 302;
    public static final int FROM_FRIEND = 303;
    public static final int FROM_SINGLE_USER = 304;

    /**
     * Get the date used for adding to entries in the database
     *
     * @return time in long format
     */
    public static long getDateTime(){

        Calendar c = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);
        String currentDateandTime = sdf.format(c.getTime());
        Long dateInLong = 0L;
        try {
            dateInLong = sdf.parse(currentDateandTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateInLong;
    }

}
