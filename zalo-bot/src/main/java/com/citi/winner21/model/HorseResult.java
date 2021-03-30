package com.citi.winner21.model;

import com.citi.winner21.ultils.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class HorseResult {
    private String raceID = "";
    private String raceDate = "";
    private int raceNo = 0;
    private String countryCode = "";
    private String venueCode = "";
    private int horseNo = 0;
    private String horseName = "";
    private String horseWeight = "";
    private String horseRating = "";
    private String handicapWeight = "";
    private String barrier = "";
    private String runningPosition = "";
    private int racePosition = 0;
    private String finishedTime = "";
    private String dividend = "";
    private String raceReport = "";

    public double getHorseWeightKg() {
        return Utils.getWeightKg(raceID.substring(0, 2), horseWeight);
    }

    public double getHandicapWeightKg() {
        return Utils.getWeightKg(raceID.substring(0, 2), handicapWeight);
    }

}
