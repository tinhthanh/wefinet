package com.citi.winner21.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceNum {
    private String betDataParam = "";
    private String openDate = "";
    private int raceNo = 0;
    private String countryCode = "";
    private String betKey = "";

    public RaceNum(RaceNumMap raceNumMap, int raceNo) {
        this.betDataParam = raceNumMap.getBetDataParam();
        this.openDate = raceNumMap.getOpenDate();
        this.raceNo = raceNo;
        this.countryCode = raceNumMap.getCountryCode();
        this.betKey = raceNumMap.getBetKey();
    }
}
