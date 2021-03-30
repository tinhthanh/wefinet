package com.citi.winner21.model;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RaceDataInternal {
    private RaceInfo raceInfo;
    private List<HorsePerformance> horsePerformances;
}
