package com.muhammadmehar.mmmgweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.muhammadmehar.mmmgweather.data.WeatherContract;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Muhammad Mehar on 1/15/2017.
 */
public class Utility {

    public static String getFriendlyDayString( Context context, String dateStr){
        String todayStr = WeatherContract.getDBDateString(new Date());
        Date inputDate = WeatherContract.getDateFromDB(dateStr);
        Date todayDate = WeatherContract.getDateFromDB(todayStr);;
        if (todayStr.equals(dateStr)){
            return context.getString(R.string.format_friendly_String, context.getString(R.string.today), getFormattedMonthDay(dateStr));
        }else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            long diff = cal.getTimeInMillis();
            cal.setTime(inputDate);
            diff = cal.getTimeInMillis() - diff;
            if ( diff / (24 * 60 * 60 * 1000) == 1){
                return context.getString(R.string.tomorrow);
            }else if ( (diff / (24 * 60 * 60 * 1000) > 1) && (diff / (24 * 60 * 60 * 1000) < 7)){
                return cal.getDisplayName( Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            }else {
                return context.getString(R.string.format_friendly_String, cal.getTime().toString().split("\\s")[0], getFormattedMonthDay(dateStr));
            }
        }
    }

    public static String getFormattedMonthDay(String dateStr){
        String[] st = formatDateFromDB(dateStr).split("\\s|,");
        return st[0] + " " + st[1];
    }

    public static int getResourseId(int weatherID, int check, String dateStr, Context context){
        switch (weatherID) {
            case 200:
                if (check == 1) {
                    int hr;
                    if (Utility.getFriendlyDayString(context, dateStr).split(",")[0].equals(context.getString(R.string.today))) {
                        Date date = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        hr = calendar.get(Calendar.HOUR_OF_DAY);
                    } else {
                        hr = 8;
                    }
                    if (hr > 7 && hr < 19) {
                        return R.drawable.thunderstrom;
                    } else {
                        return R.drawable.nt_thunderstrom;
                    }
                } else {
                    return R.drawable.dark_thunderstrom;
                }
            case 500:
                if (check == 1) {
                    int hr;
                    if (Utility.getFriendlyDayString(context, dateStr).split(",")[0].equals(context.getString(R.string.today))) {
                        Date date = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        hr = calendar.get(Calendar.HOUR_OF_DAY);
                    } else {
                        hr = 8;
                    }
                    if (hr > 7 && hr < 19) {
                        return R.drawable.rain;
                    } else {
                        return R.drawable.nt_rain;
                    }
                } else {
                    return R.drawable.dark_rain;
                }
            case 600:
                if (check == 1) {
                    return R.drawable.snow;
                } else {
                    return R.drawable.dark_snow;
                }
            case 701:
                if (check == 1) {
                    return R.drawable.fog;
                } else {
                    return R.drawable.dark_fog;
                }
            case 800:
                if (check == 1) {
                    int hr;
                    if (Utility.getFriendlyDayString(context, dateStr).split(",")[0].equals(context.getString(R.string.today))) {
                        Date date = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        hr = calendar.get(Calendar.HOUR_OF_DAY);
                    } else {
                        hr = 8;
                    }
                    if (hr > 7 && hr < 19) {
                        return R.drawable.clear;
                    } else {
                        return R.drawable.nt_clear;
                    }
                } else {
                    return R.drawable.dark_clear;
                }
            case 801:
                if (check == 1) {
                    int hr;
                    if (Utility.getFriendlyDayString(context, dateStr).split(",")[0].equals(context.getString(R.string.today))) {
                        Date date = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        hr = calendar.get(Calendar.HOUR_OF_DAY);
                    } else {
                        hr = 8;
                    }
                    if (hr > 7 && hr < 19) {
                        return R.drawable.fewclouds;
                    } else {
                        return R.drawable.nt_fewclouds;
                    }
                } else {
                    return R.drawable.dark_fewclouds;
                }
            case 900:
                if (check == 1) {
                    return R.drawable.cloudy;
                } else {
                    return R.drawable.dark_cloudy;
                }
            default:
                return 0;
        }
    }

    public static String getDirection(float deg){
        if ((deg >= 349 && deg <= 360) || (deg >= 0 && deg <= 11)) {
            return "N";
        }else if (deg >= 12 && deg <= 33) {
            return "NNE";
        }else if (deg >= 34 && deg <= 57) {
            return "NE";
        }else if (deg >= 58 && deg <= 77) {
            return "ENE";
        }else if (deg >= 78 && deg <= 101) {
            return "E";
        }else if (deg >= 102 && deg <= 123) {
            return "ESE";
        }else if (deg >= 124 && deg <= 146) {
            return "SE";
        }else if (deg >= 147 && deg <= 168) {
            return "SSE";
        }else if (deg >= 169 && deg <= 191) {
            return "S";
        }else if (deg >= 192 && deg <= 213) {
            return "SSW";
        }else if (deg >= 214 && deg <= 236) {
            return "SW";
        }else if (deg >= 237 && deg <= 258) {
            return "WSW";
        }else if (deg >= 259 && deg <= 281) {
            return "W";
        }else if (deg >= 282 && deg <= 303) {
            return "WNW";
        }else if (deg >= 304 && deg <= 326) {
            return "NW";
        }else if (deg >= 327 && deg <= 348) {
            return "NNW";
        }else {
            return "Unknow Direction";
        }
    }

    public static String formatTemperature(double temperature, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String units = pref.getString( context.getString(R.string.pref_temperature_key), context.getString(R.string.pref_default_temperature));
        double temp;
        if (units.equalsIgnoreCase(context.getString(R.string.cstr))){
            temp = 9*temperature/5+32;
        }else {
            temp = temperature;
        }
        return context.getString( R.string.format_temp_String, temp);
    }

    public static String formatDateFromDB(String dateStr){
        Date date = WeatherContract.getDateFromDB(dateStr);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String stringValue = pref.getString(context.getString(R.string.pref_location_key), context.getString(R.string.pref_default_location));
        if (stringValue.isEmpty()){
            stringValue = context.getString(R.string.pref_default_location);
        }
        pref.edit().putString( context.getString(R.string.pref_location_key), stringValue).apply();
        return stringValue;
    }

//    public static boolean getCurrentLocationState(Context context){
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        return pref.getBoolean(context.getString(R.string.pref_enable_currentLocation_key), Boolean.parseBoolean(context.getString(R.string.pref_enable_currentLocation_default)));
//    }

    public static int getWeatherID(String icon){
        switch (icon){
            case "tstorms":
            case "chancetstorms":
                return 200;
            case "sleet":
            case "chancesleet":
            case "rain":
            case "chancerain":
                return 500;
            case "chanceflurries":
            case "flurries":
            case "chancesnow":
            case "snow":
                return 600;
            case "fog":
            case "hazy":
                return 701;
            case "clear":
            case "sunny":
                return 800;
            case "partlycloudy":
            case "mostlysunny":
                return 801;
            case "cloudy":
            case "mostlycloudy":
            case "partlysunny":
                return 900;
            default:
                return 0;
        }
    }
}
