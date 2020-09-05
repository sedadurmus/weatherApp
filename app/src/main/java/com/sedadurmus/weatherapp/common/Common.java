package com.sedadurmus.weatherapp.common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    public static final String APP_ID = "f2ba2780a30904b917fc29a7957e24ba";
    public static Location current_locaiton= null;

    public static String  convertUnixToDate(int dt){
        Date date= new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd EEE MM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }
    public static String  convertUnixToHour(long dt){
        Date date= new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
