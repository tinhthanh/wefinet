package com.citi.winner21.model;

import lombok.*;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RaceInfo {
    private String raceID = "";
    private String raceDate = "";
    private int raceNo = 0;
    private String countryCode = "";
    private String venueCode  = "";
    private String startTime  = "";
    private String prize  = "";
    private String raceDivision  = "";
    private String trackForm  = "";
    private String raceDist  = "";
    private String trackType  = "";
    private String trackTypeCode  = "";
    private String course  = "";
    private String raceType  = "";
    private String raceClass  = "";
    private String raceClassName  = "";
    private String raceCourse  = "";
    private String raceName  = "";
    private String horseRecommend  = "";
    private boolean isCompleted  = false;
    private String raceVideo  = "";


    public RaceInfo(ResultSet rs) throws SQLException {
        this.raceID = rs.getString("race_id");
        this.raceDate = rs.getString("race_date");
        this.raceNo = rs.getInt("race_no");
        this.countryCode = rs.getString("country_code");
        this.venueCode = rs.getString("venue_code");
        this.startTime = rs.getString("start_time");
        this.prize = rs.getString("prize");
        this.raceDivision = rs.getString("race_division");
        this.trackForm = rs.getString("track_form");
        this.raceDist = rs.getString("race_dist");
        this.trackType = rs.getString("track_type");
        this.trackTypeCode = rs.getString("track_type_code");
        this.raceType = rs.getString("race_type");
        this.raceClass = rs.getString("race_class");
        this.raceClassName = rs.getString("race_class_name");
        this.raceCourse = rs.getString("race_course");
        this.raceName = rs.getString("race_name");
        this.horseRecommend = rs.getString("horse_recommend");
        this.isCompleted = rs.getBoolean("is_completed");
        this.raceVideo = rs.getString("race_video");
    }

    public String covertDate() {
        String[] temp = this.raceDate.split("-");
        return temp[2] + "/" + temp[1] + "/" + temp[0];
    }

    public String covertSelectDate() {
        String[] temp = this.raceDate.split("-");
        return temp[2] + "/" + temp[1] + "/" + temp[0] + "," + this.venueCode;
    }

}
