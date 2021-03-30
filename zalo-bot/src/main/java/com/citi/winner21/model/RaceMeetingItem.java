package com.citi.winner21.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@EqualsAndHashCode(of = "raceID")
public class RaceMeetingItem {
    private String raceMeetingID = "";
    private String raceID = "";
    private String href = "";
    private String raceDate = "";
    private int raceNo = 0;
    private String countryCode = "";
    private String venueCode = "";
}
