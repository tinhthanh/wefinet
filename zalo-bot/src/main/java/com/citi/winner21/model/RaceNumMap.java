package com.citi.winner21.model;

import lombok.*;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceNumMap {
    private String betDataParam = "";
    private String raceNumString = "";
    private String openDate = "";
    private String countryCode = "";
    private String betKey = "";

    public RaceNumMap(ResultSet rs) throws SQLException {
        this.betDataParam = rs.getString("bet_data_param");
        this.raceNumString = rs.getString("race_num");
        this.openDate = rs.getString("open_date");
        this.countryCode = rs.getString("country_name");
        this.betKey = rs.getString("bet_key");
    }
}
