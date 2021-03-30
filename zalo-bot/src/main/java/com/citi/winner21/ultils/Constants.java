package com.citi.winner21.ultils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final Map<String, String> MAP_COUNTRY = new HashMap<>();
    static {
        MAP_COUNTRY.put("Singapore", "SG");
        MAP_COUNTRY.put("Hong Kong", "HK");
    }
    public static final String EMPTY_STRING = "";
    public static final String SPACE_STRING = " ";
    public static final String MEETING_DATE_FORMAT = "ddMMyyyy";
    public static final String DB_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd h:ma";
    public static final String DB_ID_DATE_FORMAT = "yyyyMMdd";
    public static final String PAST_RACE_DATE_FORMAT = "dd.MM.yy";
    public static final long TIME_OUT = 15;
    public static final String WINNER21_ACCOUNT = "WINNER21";
    public static final String DEATHBYCAPTCHA_ACCOUNT = "DEATHBYCAPTCHA";
    public static final String SINGAPORE_ZONE_TIME = "GMT+08:00";
    public static final String DISLODGED_STRING = "dislodged";
    public static final int MIN_RANDOM = 10;
    public static final int MAX_RANDOM = 30;

    private Constants() {
        // This is an Utility Class
    }
}
