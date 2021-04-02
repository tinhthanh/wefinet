package com.citi.winner21.service;

import com.citi.winner21.model.HorsePerformance;
import com.citi.winner21.model.RaceDataInternal;
import com.citi.winner21.model.RaceInfo;
import com.citi.winner21.repository.DataCrawlRepository;
import com.citi.winner21.ultils.Constants;
import com.citi.winner21.ultils.HttpUtil;
import com.citi.winner21.ultils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class InternalDataCrawlService {

    private static final Logger logger = Logger.getLogger(InternalDataCrawlService.class.getName());

    @Autowired
    private DataCrawlRepository dataCrawlRepository;

    @Autowired
    private HttpUtil httpUtil;

    @Value("${patch-race-chart-url:}")
    private String patchRaceChartUrl;

    @Value("${forward-gateway-url:}")
    private String forwardGatewayUrl;

    @Value("${should-forward-gateway:}")
    private boolean shouldForwardGateway;

    private String raceResultApi = "api/raceResult/";
    private String raceCardApi = "api/raceCard";
    private String raceVideoApi = "api/raceVideo/";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Gson gson = new Gson();

//    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void crawlDataInternalGateway() {
        if (shouldForwardGateway) {
            logger.log(Level.INFO, "DataCrawlService -> Forward Crawl Internal Gateway!");
            crawlInternalRaceCards();
            crawlInternalRaceResult();
        }
    }

    private void crawlInternalRaceCards() {
        try {
            logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceCards request url -> {0}", forwardGatewayUrl + raceCardApi);
            ResponseEntity<String> response = httpUtil.sendRequest(forwardGatewayUrl + raceCardApi, String.class, null, HttpMethod.GET);
            logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceCards response -> {0}", new Object[]{response});
            List<RaceDataInternal> raceDataInternals = Objects.nonNull(response.getBody()) ? objectMapper.readValue(response.getBody(), new TypeReference<List<RaceDataInternal>>() {}) : new ArrayList<>();
            dataCrawlRepository.upsertRaceInfo(raceDataInternals.stream().map(RaceDataInternal::getRaceInfo).collect(Collectors.toList()));
            raceDataInternals.forEach(raceDataInternal -> {
                if (!CollectionUtils.isEmpty(raceDataInternal.getHorsePerformances())) {
                    boolean needPatchRaceChart = false;
                    if (dataCrawlRepository.deleteReplaceSCRHorsePerformance(raceDataInternal.getRaceInfo().getRaceID(), raceDataInternal.getHorsePerformances()) >= 1) {
                        dataCrawlRepository.deleteHorseRanking(raceDataInternal.getRaceInfo().getRaceID());
                        needPatchRaceChart = true;
                    }
                    dataCrawlRepository.upsertHorsePerformance(raceDataInternal.getHorsePerformances());
                    dataCrawlRepository.updateGoingByHorsePerformances(raceDataInternal.getHorsePerformances());
                    if (Arrays.stream(dataCrawlRepository.upsertSCRHorsePerformance(raceDataInternal.getHorsePerformances())).anyMatch(rowAffect -> rowAffect == 1) || needPatchRaceChart) {
                        logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults SCR Race ID request url -> {0}", patchRaceChartUrl + raceDataInternal.getRaceInfo().getRaceID());
                        httpUtil.sendRequest(patchRaceChartUrl + raceDataInternal.getRaceInfo().getRaceID(), String.class, null, HttpMethod.GET);
                    }
                }
            });

        }  catch (Exception ex) {
            logger.log(Level.SEVERE, "InternalDataCrawlService -> crawlRaceCards Fail, Exception: {0}", new Object[]{ex});
        }

    }

    private void crawlInternalRaceResult() {
        List<RaceInfo> raceInfoUncompleted = dataCrawlRepository.getRaceInfoUncompleted();
        if (CollectionUtils.isEmpty(raceInfoUncompleted)) {
            logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceResults -> do not find race info un complete");
            return;
        }
        logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceResults -> raceInfoUncompleted: {0}", gson.toJson(raceInfoUncompleted));
        LocalDateTime now = Instant.now().atZone(ZoneId.of(Constants.SINGAPORE_ZONE_TIME)).toLocalDateTime();
        raceInfoUncompleted.stream().filter(raceInfo -> {
            LocalDateTime endRaceDateTime = Utils.parseLocalDateTime(raceInfo.getRaceDate(), "11:59PM");
            return now.isAfter(endRaceDateTime.plusDays(1));
        }).forEach(raceInfo -> {
            try {
                logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceResults request url -> {0}", forwardGatewayUrl + raceResultApi +  raceInfo.getRaceID());
                ResponseEntity<String> response = httpUtil.sendRequest(forwardGatewayUrl + raceResultApi + raceInfo.getRaceID(), String.class, null, HttpMethod.GET);
                logger.log(Level.INFO, "InternalDataCrawlService -> crawlRaceResults race id: {0}, response -> {1}", new Object[]{raceInfo.getRaceID(), response});
                List<HorsePerformance> horsePerformances = Objects.nonNull(response.getBody()) ? objectMapper.readValue(response.getBody(), new TypeReference<List<HorsePerformance>>() {}) : new ArrayList<>();
                if (!CollectionUtils.isEmpty(horsePerformances)) {
                    dataCrawlRepository.batchUpdateHorsePerformanceInternal(horsePerformances);
                    dataCrawlRepository.batchUpdateSCRHorsePerformance(horsePerformances);
                    // Update race video
                    ResponseEntity<String> responseVideoUrl = httpUtil.sendRequest(forwardGatewayUrl + raceVideoApi + raceInfo.getRaceID(), String.class, null, HttpMethod.GET);
                    logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults SCR Race ID response -> {0}", responseVideoUrl.getBody());
                    dataCrawlRepository.updateRaceVideo(raceInfo.getRaceID(), responseVideoUrl.getBody());
                    logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults -> update race video {0} ", new Object[]{responseVideoUrl.getBody()});
                    // Update status race info
                    dataCrawlRepository.updateStausRaceInfo(raceInfo.getRaceID(), 1);
                    // Send request patch data for Citi gateway
                    logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults SCR Race ID request url -> {0}", patchRaceChartUrl + raceInfo.getRaceID());
                    httpUtil.sendRequest(patchRaceChartUrl + raceInfo.getRaceID(), String.class, null, HttpMethod.GET);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "InternalDataCrawlService -> crawlRaceResults Fail, Exception: {0}", new Object[]{ex});
            }
        });
    }


}
