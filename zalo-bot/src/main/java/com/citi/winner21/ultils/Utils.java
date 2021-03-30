package com.citi.winner21.ultils;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class.getName());
    private static Random random = new Random();
    private Utils() {
        throw new IllegalAccessError("Utility class");
    }

    public static String getRaceDateMeeting(String href) throws ParseException {
        SimpleDateFormat meetingDateFormat = new SimpleDateFormat(Constants.MEETING_DATE_FORMAT);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
        String raceMeetingID = href.substring(0, href.indexOf("&raceNo"));
        return dbDateFormat.format(meetingDateFormat.parse(raceMeetingID.substring(raceMeetingID.length() - 8)));
    }

    public static String getRaceMeetingID(String href) {
        String raceMeetingID = href.replace("/Race?hostMeetingId=", "");
        return raceMeetingID.substring(0, raceMeetingID.indexOf("&raceNo"));
    }

    public static int getRaceNoMeeting(String href) {
        return Integer.parseInt(href.substring(href.indexOf("&raceNo=") + 8));
    }

    public static String getRaceId(String href) {
        String raceMeetingID = href.replace("/Race?hostMeetingId=", "");
        return raceMeetingID.substring(0, 2) + raceMeetingID.substring(2) + "R" + raceMeetingID.substring(href.indexOf("&raceNo=") + 8);
    }

    public static String mapColor(String colorName) {
        switch (colorName) {
            case "rgb(251, 241, 114)":
                return "P";
            case "rgb(177, 199, 97)":
                return "SC";
            case "rgb(84, 180, 156)":
                return "LC";
            default:
                return "";
        }
    }

    public static double getWeightKg(String country, String weight) {
        return getWeightKg(country, weight, 0);
    }

    public static double getWeightKg(String country, String weight, int subDigit) {
        if (StringUtils.isEmpty(weight)) {
            return 0;
        }
        return "HK".equalsIgnoreCase(country) ? Utils.poundToKg(Integer.valueOf(weight) - subDigit) : Double.valueOf(weight) - subDigit;
    }

    private static double poundToKg(int weight) {
        return Math.round(weight * 0.45359237 * 2) / 2d;
    }

    public static boolean checkTimeWinner21Maintenance() {
        int hour = Instant.now().atZone(ZoneId.of(Constants.SINGAPORE_ZONE_TIME)).toLocalDateTime().getHour();
        return hour <= 4 || hour == 23;
    }

    public static String getPastRaceDate(String raceDate) throws ParseException {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
        SimpleDateFormat pastRaceDateFormat = new SimpleDateFormat(Constants.PAST_RACE_DATE_FORMAT);
        return pastRaceDateFormat.format(dbDateFormat.parse(raceDate));
    }

    public static String getDBRaceDate(String raceDate) throws ParseException {
        SimpleDateFormat pastRaceDateFormat = new SimpleDateFormat(Constants.PAST_RACE_DATE_FORMAT);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
        return dbDateFormat.format(pastRaceDateFormat.parse(raceDate));
    }

    public static boolean checkStringEndNonDigits(String s) {
        return Pattern.compile("[a-zA-Z]$").matcher(s).find();
    }

    public static void waitingEndCrawlRaceData() {
        try {
            int n = generatingRandomNumber(Constants.MAX_RANDOM, Constants.MIN_RANDOM);
            logger.log(Level.INFO, "Waiting End Crawl Race Data {0} seconds", new Object[]{n});
            Thread.sleep(TimeUnit.SECONDS.toMillis(n));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Waiting End Crawl Race Data Fail", ex);
        }
    }

    public static int generatingRandomNumber(int max, int min) {
        return random.nextInt(max - min) + min;
    }

    public static LocalDateTime parseLocalDateTime(String raceDate, String startTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);
        return LocalDateTime.parse(raceDate + Constants.SPACE_STRING + startTime, formatter);
    }

    public static String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));
    }

}
