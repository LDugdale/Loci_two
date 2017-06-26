package com.lauriedugdale.loci.data.dataobjects;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.SparseBooleanArray;

import com.lauriedugdale.loci.data.DataUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mnt_x on 24/06/2017.
 */

public class FilterOptions {

    private String fromDate;
    private String toDate;
    private SparseBooleanArray checkedTypes;

    public FilterOptions() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);

        Calendar calendar = Calendar.getInstance();
        sdf.applyPattern("dd-MM-yyyy");

        toDate = sdf.format(calendar.getTime());
        calendar.add(Calendar.MONTH, -6);
        fromDate = sdf.format(calendar.getTime());


        checkedTypes = new SparseBooleanArray();
        checkedTypes.append(DataUtils.NO_MEDIA, true);
        checkedTypes.append(DataUtils.IMAGE, true);
        checkedTypes.append(DataUtils.AUDIO, true);
    }

    public long getNumericalFromDate(){

//        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
//        Date d = new Date();
//        try {
//            d = f.parse(fromDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//       return d.getTime();

        Calendar c = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);
        c.add(Calendar.MONTH, -6);
        String currentDateandTime = sdf.format(c.getTime());
        Long dateInLong = 0L;
        try {
            dateInLong = sdf.parse(currentDateandTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateInLong;
    }

    public long getNumericalToDate(){
//        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
//        Date d = new Date();
//        try {
//            d = f.parse(toDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return d.getTime();
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

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public SparseBooleanArray getCheckedTypes() {
        return checkedTypes;
    }

    public void setCheckedTypes(SparseBooleanArray checkedTypes) {
        this.checkedTypes = checkedTypes;
    }
}
