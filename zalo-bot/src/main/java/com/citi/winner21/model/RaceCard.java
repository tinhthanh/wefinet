package com.citi.winner21.model;

import com.citi.winner21.ultils.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Data
@Setter
@Getter
public class RaceCard {
    private String raceID = "";
    private String raceMeetingID = "";
    private String raceDate = "";
    private int raceNo = 0;
    private String horseId = "";
    private String horseName = "";
    private String countryCode = "";
    private String venueCode = "";
    private String raceDist = "";
    private String trackType = "";
    private String trackTypeCode = "";
    private String course = "";
    private String raceDistType = "";
    private int horseNo = 0;
    private String barrier = "";
    private String horseWeight = "";
    private String horseRating = "";
    private String handicapWeight = "";
    private String jockeyName = "";
    private String jockeyID = "";
    private String trainerName = "";
    private String trainerID = "";
    private String fatherName = "";
    private String motherName = "";
    private String ownerName = "";
    private String achievement = "";
    private String startTime = "";
    private String prize = "";
    private String raceClass = "";
    private String raceClassName = "";
    private String raceDiv = "";
    private String raceName = "";
    private String horseRecommend = "";
    private int subDigit = 0;
    private boolean isSCR = false;
    private Map<String, String> mapPastRacesGoing = new HashMap<>();

    public String getRaceDistFormat() {
        return this.raceDist + "M";
    }

    public double getHorseWeightKg() {
        return Utils.getWeightKg(raceID.substring(0, 2), horseWeight);
    }

    public double getHandicapWeightKg() {
        return Utils.getWeightKg(raceID.substring(0, 2), handicapWeight, subDigit);
    }

    public void addPastRacesGoing(String raceDate, String going) {
        mapPastRacesGoing.put(raceDate, going);
    }

    public String getRaceCourse() {
        String suffix = "";
        if ("T".equals(this.getTrackTypeCode())) {
            if ("SC".equals(this.getCourse())) {
                suffix = "s";
            } else if ("LC".equals(this.getCourse())) {
                suffix = "l";
            } else {
                suffix = this.getCourse();
            }
        } else {
            suffix = "p";
        }
        return this.raceDist + suffix;
    }
}
