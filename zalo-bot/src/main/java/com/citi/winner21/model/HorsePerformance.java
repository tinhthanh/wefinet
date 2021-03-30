package com.citi.winner21.model;

import lombok.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HorsePerformance {
    private String raceID;
    private String horseId;
    private String raceDate;
    private int raceNo;
    private int horseNo;
    private String horseName;
    private String place;
    private Integer racePosition;
    private String horseRating;
    private String raceDist;
    private String track;
    private String going;
    private String barrier;
    private String horseWeight;
    private String handicapWeight;
    private String runningPosition;
    private int finishedTime;
    private String dividend;
    private String riderId;
    private String riderName;
    private String trainerId;
    private String trainer;
    private String fatherName;
    private String motherName;
    private String ownerName;
    private String achievement;
    private boolean isScr;
    private Map<String, String> mapPastRacesGoing = new HashMap<>();

    public HorsePerformance(ResultSet rs, Map<String, String> mapPastRacesGoing) throws SQLException {
        this(rs);
        this.mapPastRacesGoing = mapPastRacesGoing;
    }

    public HorsePerformance(ResultSet rs) throws SQLException {
        this.raceID = rs.getString("race_id");
        this.horseId = rs.getString("horse_id");
        this.raceDate = rs.getString("race_date");
        this.raceNo = rs.getInt("race_no");
        this.horseNo = rs.getInt("horse_no");
        this.horseName = rs.getString("horse_name");
        this.place = rs.getString("place");
        this.racePosition = rs.getObject("race_position", Integer.class);
        this.horseRating = rs.getString("horse_rating");
        this.raceDist = rs.getString("race_dist");
        this.track = rs.getString("track");
        this.going = rs.getString("going");
        this.barrier = rs.getString("barrier");
        this.horseWeight = rs.getString("horse_weight");
        this.handicapWeight = rs.getString("handicap_weight");
        this.runningPosition = rs.getString("running_position");
        this.finishedTime = rs.getInt("finished_time");
        this.dividend = rs.getString("dividend");
        this.riderId = rs.getString("rider_id");
        this.riderName = rs.getString("rider_name");
        this.trainerId = rs.getString("trainer_id");
        this.trainer = rs.getString("trainer");
        this.fatherName = rs.getString("father_name");
        this.motherName = rs.getString("mother_name");
        this.ownerName = rs.getString("owner_name");
        this.achievement = rs.getString("achievement");
        this.isScr = rs.getBoolean("is_scr");
    }
}
