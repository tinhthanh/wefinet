package com.citi.winner21.repository;

import com.citi.winner21.model.*;
import com.citi.winner21.ultils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository

public class DataCrawlRepository {

    private static final Logger logger = Logger.getLogger(DataCrawlRepository.class.getName());

    @Autowired
    private JdbcTemplate jdbc;

    public ProviderAccount getProviderAccount(String providerName) {
        String sql = "SELECT a.provider_name, " +
                "       a.account_name, " +
                "       a.password, " +
                "       IFNULL(a.auth_key, '') AS auth_key, " +
                "       a.ping_status, " +
                "       p.proxy_host, " +
                "       p.proxy_port " +
                "FROM cp_provider_account a " +
                "         LEFT JOIN cp_account_proxy p ON a.provider_name = p.provider_name " +
                "WHERE a.provider_name = ? " +
                "ORDER BY RAND() LIMIT 1";
        return jdbc.queryForObject(sql, (rs, i) -> new ProviderAccount(rs), providerName);
    }

    public void updateAuthKeyAccount(ProviderAccount account) {
        jdbc.update("UPDATE cp_provider_account SET auth_key = ? WHERE provider_name = 'WINNER21' AND account_name = ?", account.getAuthKey(), account.getAccountName());
    }

    public void upsertRaceInfoByRaceCards(List<RaceCard> raceCards) {
        List<Object[]> param = raceCards.stream()
                .filter(distinctByKey(RaceCard::getRaceID))
                .map(r ->
                        new Object[]{
                                r.getRaceID(),
                                r.getRaceDate(),
                                r.getRaceNo(),
                                r.getCountryCode(),
                                r.getStartTime(),
                                r.getRaceDistFormat(),
                                r.getTrackType(),
                                r.getTrackTypeCode(),
                                r.getCourse(),
                                r.getPrize(),
                                r.getRaceName(),
                                false,
                                r.getRaceCourse(),
                                r.getVenueCode(),
                                r.getHorseRecommend(),
                                r.getRaceDiv(),
                                r.getRaceClass(),
                                r.getRaceClassName(),
                                false,
                                r.getStartTime(),
                                r.getRaceDistFormat(),
                                r.getTrackType(),
                                r.getTrackTypeCode(),
                                r.getCourse(),
                                r.getPrize(),
                                r.getRaceName(),
                                r.getRaceCourse(),
                                r.getHorseRecommend(),
                                r.getRaceDiv(),
                                r.getRaceClass(),
                                r.getRaceClassName()
                        }
                ).collect(Collectors.toList());
        String sql = "INSERT INTO tc_race_info (race_id,race_date,race_no,country_code,start_time,race_dist,track_type,track_type_code,course,prize,race_name,is_completed,race_course,venue_code,horse_recommend,race_division,race_class,race_class_name,is_crawled) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE start_time = ?, race_dist = ?, track_type = ?, track_type_code = ?, course = ?, prize = ?, race_name = ?, race_course =? , horse_recommend = ?, race_division = ?, race_class = ?, race_class_name = ?";
        jdbc.batchUpdate(sql, param);
    }

    public void upsertHorsePerformanceByRaceCards(List<RaceCard> raceCards) {
        String sqlInsertHorseOwnerId = "INSERT INTO tc_horse_owner (owner_name) " +
                "SELECT ? as owner_name " +
                "WHERE NOT EXISTS (SELECT * FROM tc_horse_owner WHERE owner_name = ?)";
        String sqlSelectHorseOwnerId = "SELECT owner_id from tc_horse_owner WHERE owner_name = ? limit 1";
        List<Object[]> param = raceCards.stream().map(r -> {
            Integer ownerId = null;
            if (StringUtils.isNotEmpty(r.getOwnerName())) {
                jdbc.update(sqlInsertHorseOwnerId, r.getOwnerName(), r.getOwnerName());
                ownerId = jdbc.queryForObject(sqlSelectHorseOwnerId, Integer.class, r.getOwnerName());
            }
            if (StringUtils.isNotEmpty(r.getJockeyID()) && StringUtils.isNotEmpty(r.getJockeyName())) {
                updateRiderInfo(r.getJockeyID(), r.getJockeyName());
            }
            if (StringUtils.isNotEmpty(r.getTrainerID()) && StringUtils.isNotEmpty(r.getTrainerName())) {
                updateTrainerInfo(r.getTrainerID(), r.getTrainerName());
            }
            return new Object[]{
                    r.getRaceID(),
                    r.getHorseId(),
                    r.getRaceDate(),
                    r.getRaceNo(),
                    r.getHorseName(),
                    r.getHorseRating(),
                    r.getRaceDist(),
                    r.getTrackTypeCode(),
                    r.getBarrier(),
                    r.getHorseWeightKg(),
                    r.getJockeyID(),
                    r.getJockeyName(),
                    r.getTrainerID(),
                    r.getTrainerName(),
                    r.getHorseNo(),
                    r.getHandicapWeightKg(),
                    r.getFatherName(),
                    r.getMotherName(),
                    ownerId,
                    r.getOwnerName(),
                    r.getAchievement(),
                    r.getHorseNo(),
                    r.getHorseName(),
                    r.getHorseRating(),
                    r.getHorseWeightKg(),
                    r.getJockeyID(),
                    r.getJockeyName(),
                    r.getTrainerID(),
                    r.getTrainerName(),
                    r.getHandicapWeightKg(),
                    r.getFatherName(),
                    r.getMotherName(),
                    ownerId,
                    r.getOwnerName(),
                    r.getAchievement()
            };
        }).collect(Collectors.toList());
        String sql = "INSERT INTO tc_horse_performance (race_id,horse_id,race_date,race_no,horse_name,horse_rating,race_dist,track,barrier,horse_weight,rider_id,rider_name,trainer_id,trainer,horse_no,handicap_weight,father_name,mother_name,owner_id,owner_name,achievement) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE horse_no = ?, horse_name = ?, horse_rating = ?, horse_weight = ?, rider_id = ?, " +
                "                        rider_name = ?, trainer_id = ?, trainer = ?, handicap_weight = ?," +
                "                        father_name = ?, mother_name = ?, owner_id = ?, owner_name = ?, achievement = ?";
        jdbc.batchUpdate(sql, param);
    }

    public void updateGoingByRaceCards(List<RaceCard> raceCards) {
        List<Object[]> param = raceCards.stream()
                .map(r -> r.getMapPastRacesGoing().entrySet().stream()
                        .map(entry -> new Object[]{entry.getValue(), entry.getKey(), r.getHorseId()})
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        String sql = "UPDATE tc_horse_performance SET going =? WHERE race_date = ? AND horse_id = ?";
        jdbc.batchUpdate(sql, param);
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public int[] upsertSCRHorsePerformance(Stream<RaceCard> raceCards) {
        List<Object[]> param = raceCards
                .map(h -> new Object[]{h.isSCR(), h.getRaceID(), h.getHorseNo(), h.isSCR()}).collect(Collectors.toList());
        return jdbc.batchUpdate("UPDATE tc_horse_performance set is_scr = ? where race_id = ? and horse_no = ? and is_scr <> ?", param);
    }

    public int[] deleteReplaceSCRHorsePerformance(Stream<RaceCard> raceCards) {
        List<Object[]> param = raceCards
                .map(h -> new Object[]{ h.getRaceID(), h.getHorseNo(), h.getHorseName()}).collect(Collectors.toList());
        return jdbc.batchUpdate("DELETE FROM tc_horse_performance WHERE race_id = ? AND horse_no = ? AND horse_name = ?", param);
    }

    public void deleteHorseRanking(String raceId) {
        jdbc.update("DELETE FROM tc_horse_ranking WHERE race_id = ?", raceId);
    }

    public void batchUpdateHorsePerformance(List<HorseResult> horseResults) {
        List<Object[]> param = horseResults.stream()
                .map(h -> new Object[]{
                        h.getBarrier(),
                        NumberUtils.toInt(h.getFinishedTime(), 0),
                        h.getRunningPosition().split("-").length > 2 ? h.getRunningPosition().split("-")[2] + "/" + horseResults.size() : h.getRunningPosition(),
                        h.getRacePosition(),
                        h.getHorseRating(),
                        h.getHorseWeightKg(),
                        h.getHandicapWeightKg(),
                        h.getRunningPosition(),
                        h.getDividend(),
                        h.getRaceID(),
                        h.getHorseNo()})
                .collect(Collectors.toList());
        this.jdbc.batchUpdate(
                "UPDATE tc_horse_performance " +
                        "SET barrier = ? , finished_time = ? , place = ?, race_position = ?, horse_rating = ?, horse_weight = ?, handicap_weight = ?, running_position = ?, dividend = ? " +
                        "WHERE race_id = ? AND horse_no = ?", param);
    }

    public boolean checkUpsertRaceResult(String raceID) {
        String sql = "select is_completed from tc_race_info where race_id = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, raceID) > 0;
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > Need insert raceID: {0}", new Object[]{raceID});
        }
        return false;
    }

    public List<RaceInfo> getRaceInfoUncompleted() {
        return this.jdbc.query("select * from tc_race_info where is_completed = 0 and country_code in ('SG','HK','MY') and is_trial = 0", (rs, i) -> new RaceInfo(rs));
    }

    public void updateStausRaceInfo(String raceCode, int status) {
        List<Object[]> param = new ArrayList<>();
        param.add(new Object[]{status, raceCode});
        this.jdbc.batchUpdate(
                "update tc_race_info set is_completed = ? where race_id = ?;", param);
    }

    public void updateRaceVideo(String raceCode, String raceVideo) {
        List<Object[]> param = new ArrayList<>();
        param.add(new Object[]{raceVideo, raceCode});
        this.jdbc.batchUpdate(
                "update tc_race_info set race_video = ? where race_id = ?;", param);
    }

    public int[] batchUpdateSCRHorseResult(Stream<HorseResult> horseResults) {
        List<Object[]> param = horseResults
                .map(h -> new Object[]{h.getRaceID(), h.getHorseNo()}).collect(Collectors.toList());
        return jdbc.batchUpdate("UPDATE tc_horse_performance set is_scr = 1 where race_id = ? and horse_no = ? and is_scr = 0", param);
    }

    public List<RaceInfo> getRaceInfoByRaceDate(String raceDate) {
        try {
            return this.jdbc.query("SELECT * FROM tc_race_info WHERE race_date = ? AND country_code IN ('SG','HK','MY')", (rs, i) -> new RaceInfo(rs), raceDate);
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > RaceInfo is not found raceDate: {0}", new Object[]{raceDate});
        }
        return new ArrayList<>();
    }

    public List<HorsePerformance> getHorsePerformance(String raceId, boolean isCompleted) {
        String sql = "SELECT t1.* FROM tc_horse_performance t1 " +
                "INNER JOIN tc_race_info t2 ON t2.race_id = t1.race_id " +
                "WHERE t1.race_id = ? AND t2.is_completed = ?";
        try {
            return this.jdbc.query(sql, (rs, i) -> new HorsePerformance(rs, getPastRacesGoing(rs.getString("horse_id"))), raceId, isCompleted);
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > Horse Performance is not found raceId: {0}", new Object[]{raceId});
        }
        return new ArrayList<>();
    }

    private Map<String, String> getPastRacesGoing(String horseId) {
        String sql = "select * from tc_horse_performance where horse_id = ? order by race_date desc limit 4";
        try {
            return this.jdbc.query(sql, (rs, i) -> new HorsePerformance(rs), horseId).stream()
                    .filter(horsePerformance -> StringUtils.isNotEmpty(horsePerformance.getGoing()))
                    .collect(Collectors.toMap(HorsePerformance::getRaceID, HorsePerformance::getGoing));
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > Past Races Going not found horseId: {0}", new Object[]{horseId});
        }
        return new HashMap<>();
    }

    public String getRaceInfoVideo(String raceId) {
        try {
            return this.jdbc.queryForObject("select race_video from tc_race_info where race_id = ?", String.class, raceId);
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > RaceInfo is not found raceId: {0}", new Object[]{raceId});
        }
        return Constants.EMPTY_STRING;
    }

    public void batchUpdateHorsePerformanceInternal(List<HorsePerformance> horsePerformances) {
        List<Object[]> param = horsePerformances.stream()
                .map(h -> new Object[]{
                        h.getBarrier(),
                        h.getFinishedTime(),
                        h.getPlace(),
                        h.getRacePosition(),
                        h.getHorseRating(),
                        h.getHorseWeight(),
                        h.getHandicapWeight(),
                        h.getRunningPosition(),
                        h.getDividend(),
                        h.getRaceID(),
                        h.getHorseNo()})
                .collect(Collectors.toList());
        this.jdbc.batchUpdate(
                "UPDATE tc_horse_performance " +
                        "SET barrier = ? , finished_time = ? , place = ?, race_position = ?, horse_rating = ?, horse_weight = ?, handicap_weight = ?, running_position = ?, dividend = ? " +
                        "WHERE race_id = ? AND horse_no = ?", param);
    }

    public int[] batchUpdateSCRHorsePerformance(List<HorsePerformance> horsePerformances) {
        List<Object[]> param = horsePerformances.stream()
                .map(h -> new Object[]{h.isScr(), h.getRaceID(), h.getHorseNo()}).collect(Collectors.toList());
        return jdbc.batchUpdate("UPDATE tc_horse_performance set is_scr = ? where race_id = ? and horse_no = ?", param);
    }

    public void upsertRaceInfo(List<RaceInfo> raceInfos) {
        List<Object[]> param = raceInfos.stream()
                .filter(distinctByKey(RaceInfo::getRaceID))
                .map(r ->
                        new Object[]{
                                r.getRaceID(),
                                r.getRaceDate(),
                                r.getRaceNo(),
                                r.getCountryCode(),
                                r.getStartTime(),
                                r.getRaceDist(),
                                r.getTrackType(),
                                r.getTrackTypeCode(),
                                r.getCourse(),
                                r.getPrize(),
                                r.getRaceName(),
                                false,
                                r.getRaceCourse(),
                                r.getVenueCode(),
                                r.getHorseRecommend(),
                                r.getRaceDivision(),
                                r.getRaceClass(),
                                r.getRaceClassName(),
                                r.getStartTime(),
                                r.getRaceDist(),
                                r.getTrackType(),
                                r.getTrackTypeCode(),
                                r.getCourse(),
                                r.getPrize(),
                                r.getRaceName(),
                                r.getRaceCourse(),
                                r.getHorseRecommend(),
                                r.getRaceDivision(),
                                r.getRaceClass(),
                                r.getRaceClassName()
                        }
                ).collect(Collectors.toList());
        String sql = "INSERT INTO tc_race_info (race_id,race_date,race_no,country_code,start_time,race_dist,track_type,track_type_code,course,prize,race_name,is_completed,race_course,venue_code,horse_recommend,race_division,race_class,race_class_name) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE start_time = ?, race_dist = ?, track_type = ?, track_type_code = ?, course = ?, prize = ?, race_name = ?, race_course =? , horse_recommend = ?, race_division = ?, race_class = ?, race_class_name = ?";
        jdbc.batchUpdate(sql, param);
    }

    public void upsertHorsePerformance(List<HorsePerformance> horsePerformances) {
        String sqlInsertHorseOwnerId = "INSERT INTO tc_horse_owner (owner_name) " +
                "SELECT ? as owner_name " +
                "WHERE NOT EXISTS (SELECT * FROM tc_horse_owner WHERE owner_name = ?)";
        String sqlSelectHorseOwnerId = "SELECT owner_id from tc_horse_owner WHERE owner_name = ? limit 1";
        List<Object[]> param = horsePerformances.stream().map(r -> {
            Integer ownerId = null;
            if (StringUtils.isNotEmpty(r.getOwnerName())) {
                jdbc.update(sqlInsertHorseOwnerId, r.getOwnerName(), r.getOwnerName());
                ownerId = jdbc.queryForObject(sqlSelectHorseOwnerId, Integer.class, r.getOwnerName());
            }
            if (StringUtils.isNotEmpty(r.getRiderId()) && StringUtils.isNotEmpty(r.getRiderName())) {
                updateRiderInfo(r.getRiderId(), r.getRiderName());
            }
            if (StringUtils.isNotEmpty(r.getTrainerId()) && StringUtils.isNotEmpty(r.getTrainer())) {
                updateTrainerInfo(r.getTrainerId(), r.getTrainer());
            }
            return new Object[]{
                    r.getRaceID(),
                    r.getHorseId(),
                    r.getRaceDate(),
                    r.getRaceNo(),
                    r.getHorseName(),
                    r.getHorseRating(),
                    r.getRaceDist(),
                    r.getTrack(),
                    r.getBarrier(),
                    r.getHorseWeight(),
                    r.getRiderId(),
                    r.getRiderName(),
                    r.getTrainerId(),
                    r.getTrainer(),
                    r.getHorseNo(),
                    r.getHandicapWeight(),
                    r.getFatherName(),
                    r.getMotherName(),
                    ownerId,
                    r.getOwnerName(),
                    r.getAchievement(),
                    r.getHorseNo(),
                    r.getHorseName(),
                    r.getHorseRating(),
                    r.getHorseWeight(),
                    r.getRiderId(),
                    r.getRiderName(),
                    r.getTrainerId(),
                    r.getTrainer(),
                    r.getHandicapWeight(),
                    r.getFatherName(),
                    r.getMotherName(),
                    ownerId,
                    r.getOwnerName(),
                    r.getAchievement()
            };
        }).collect(Collectors.toList());
        String sql = "INSERT INTO tc_horse_performance (race_id,horse_id,race_date,race_no,horse_name,horse_rating,race_dist,track,barrier,horse_weight,rider_id,rider_name,trainer_id,trainer,horse_no,handicap_weight,father_name,mother_name,owner_id,owner_name,achievement) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE horse_no = ?, horse_name = ?, horse_rating = ?, horse_weight = ?, rider_id = ?, " +
                "                        rider_name = ?, trainer_id = ?, trainer = ?, handicap_weight = ?," +
                "                        father_name = ?, mother_name = ?, owner_id = ?, owner_name = ?, achievement = ?";
        jdbc.batchUpdate(sql, param);
    }

    public void updateGoingByHorsePerformances(List<HorsePerformance> horsePerformances) {
        jdbc.batchUpdate("UPDATE tc_horse_performance SET going = ? WHERE race_id = ? AND horse_id = ?", horsePerformances.stream()
                .map(r -> r.getMapPastRacesGoing().entrySet().stream()
                        .map(entry -> new Object[]{entry.getValue(), entry.getKey(), r.getHorseId()})
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    public int[] upsertSCRHorsePerformance(List<HorsePerformance> horsePerformances) {
        List<Object[]> param = horsePerformances.stream()
                .map(h -> new Object[]{h.isScr(), h.getRaceID(), h.getHorseId(), h.isScr()}).collect(Collectors.toList());
        return jdbc.batchUpdate("UPDATE tc_horse_performance set is_scr = ? where race_id = ? and horse_id = ? and is_scr <> ?", param);
    }

    public int deleteReplaceSCRHorsePerformance(String raceId, List<HorsePerformance> horsePerformances) {
        List<String> horseIds = horsePerformances.stream()
                .map(HorsePerformance::getHorseId)
                .collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(horseIds.size(), "?"));
        return jdbc.update(String.format("DELETE FROM tc_horse_performance WHERE race_id = '%s' AND horse_id NOT IN (%s)", raceId, inSql), horseIds.toArray());
    }

    public boolean checkCrawlRaceCards(RaceMeetingItem raceMeetingItem) {
        return jdbc.queryForObject("SELECT COUNT(2) < 2  FROM tc_race_info WHERE race_date = ? AND country_code = ? AND venue_code = ? ",
                Boolean.class, raceMeetingItem.getRaceDate(), raceMeetingItem.getCountryCode(), raceMeetingItem.getVenueCode());
    }

    public List<RaceNum> getAllRaceNumInPlay() {
        String sql = "SELECT d.bet_data_param, d.open_date, r.race_num, d.country_name, d.bet_key " +
                "FROM cp_dividend d " +
                "         INNER JOIN cp_race_num r ON d.bet_key = r.bet_key AND d.open_date = r.open_date " +
                "WHERE d.is_open " +
                "  AND d.race_time != '' " +
                "  AND NOT d.is_over " +
                "  AND d.open_date >= CURRENT_DATE()";
        return this.jdbc.query(sql, (rs, i) -> new RaceNumMap(rs))
                .stream()
                .map(raceNumMap -> Arrays.stream(raceNumMap.getRaceNumString().split("/"))
                        .map(s -> s.replace("Race", "").trim())
                        .filter(NumberUtils::isCreatable)
                        .map(s -> new RaceNum(raceNumMap, NumberUtils.toInt(s)))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public boolean checkCrawlDataRaceInPlay(RaceNum raceNum) {
        String sql = "SELECT COUNT(1) > 0  FROM tc_race_info WHERE race_date = ? AND race_no = ? AND country_code = ? AND is_crawled = 0";
        return jdbc.queryForObject(sql, Boolean.class, raceNum.getOpenDate(), raceNum.getRaceNo(), raceNum.getCountryCode());
    }

    public void updateCrawledRaceInfo(RaceNum raceNum) {
        jdbc.update("UPDATE tc_race_info SET is_crawled = 1 WHERE race_date = ? AND race_no = ? AND country_code = ?", raceNum.getOpenDate(), raceNum.getRaceNo(), raceNum.getCountryCode());
    }

    public int getRaceTime(String betDataParam, int raceNo) {
        try {
            KeyCode keyCode = getKeyCode(betDataParam);
            String sql = "SELECT race_time FROM cp_race_time WHERE bet_key = ? AND open_date = ?  AND race_num = ? AND is_race_finished = 0";
            return jdbc.queryForObject(sql, Integer.class, keyCode.getBetKey(), keyCode.getOpenDate(), raceNo);
        } catch (Exception ex) {
            logger.log(Level.INFO, "RaceResultRepository > Race Time is not found betDataParam: {0}, raceNo: {1}", new Object[]{betDataParam, raceNo});
            return -1;
        }
    }

    private KeyCode getKeyCode(String betDataParam) throws ParseException {
        if ((betDataParam.contains("player.jsp?race_type=") || betDataParam.contains("Q.jsp?race_type="))
                && betDataParam.contains("race_date=")) {
            String[] param = betDataParam.replace("player.jsp?race_type=", Constants.EMPTY_STRING)
                    .replace("Q.jsp?race_type=", Constants.EMPTY_STRING)
                    .replace("race_date=", Constants.EMPTY_STRING).split("&");
            if (param.length == 2) {
                SimpleDateFormat from = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd");
                String reformattedStr = to.format(from.parse(param[1]));
                return new KeyCode(param[0], reformattedStr);
            }
        }
        return new KeyCode(Constants.EMPTY_STRING, "1000-01-01");
    }

    private void updateRiderInfo(String riderId, String riderName) {
        jdbc.update("INSERT INTO tc_horse_rider(rider_id, rider_name) " +
                "SELECT ? AS rider_id, ? AS rider_name " +
                "WHERE NOT EXISTS (SELECT * FROM tc_horse_rider WHERE rider_id = ? OR rider_id = ?)",
                riderId, riderName, riderId, riderName);
        int result = jdbc.update("UPDATE tc_horse_rider SET rider_id = ? WHERE rider_id = ? AND rider_name = ?", riderId, riderName, riderName);
        if (result > 0) {
            jdbc.update("UPDATE tc_horse_performance t1 " +
                    "INNER JOIN tc_race_info t2 ON t2.race_id = t1.race_id " +
                    "SET t1.rider_id = ? " +
                    "WHERE t1.rider_id = ? AND t2.is_trial = 1", riderId, riderName);
        }
    }

    private void updateTrainerInfo(String trainerId, String trainerName) {
        jdbc.update("INSERT INTO tc_horse_trainer(trainer_id, trainer_name) " +
                        "SELECT ? AS trainer_id, ? AS trainer_name " +
                        "WHERE NOT EXISTS (SELECT * FROM tc_horse_trainer WHERE trainer_id = ? OR trainer_id = ?)",
                trainerId, trainerName, trainerId, trainerName);
        int result = jdbc.update("UPDATE tc_horse_trainer SET trainer_id = ? WHERE trainer_id = ? AND trainer_name = ?", trainerId, trainerName, trainerName);
        if (result > 0) {
            jdbc.update("UPDATE tc_horse_performance t1 " +
                    "INNER JOIN tc_race_info t2 ON t2.race_id = t1.race_id " +
                    "SET t1.trainer_id = ? " +
                    "WHERE t1.trainer_id = ? AND t2.is_trial = 1", trainerId, trainerName);
        }
    }
}
