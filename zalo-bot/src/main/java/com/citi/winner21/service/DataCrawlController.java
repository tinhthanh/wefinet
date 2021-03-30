package com.citi.winner21.service;

import com.citi.winner21.model.HorsePerformance;
import com.citi.winner21.model.RaceDataInternal;
import com.citi.winner21.repository.DataCrawlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DataCrawlController {
    private static final Logger logger = Logger.getLogger(DataCrawlController.class.getName());

    @Autowired
    private DataCrawlRepository dataCrawlRepository;

    @Autowired
    private DataCrawlService dataCrawlService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @GetMapping(value = "/raceCard")
    public ResponseEntity<List<RaceDataInternal>> getRaceCardUpcoming() {
        return ResponseEntity.ok().body(dataCrawlRepository.getRaceInfoUncompleted().stream()
                .map(raceInfo -> new RaceDataInternal(raceInfo, dataCrawlRepository.getHorsePerformance(raceInfo.getRaceID(), false)))
                .collect(Collectors.toList()));
    }

    @GetMapping(value = "/raceResult/{raceId}")
    public ResponseEntity<List<HorsePerformance>> getRaceResultCompleted(@PathVariable String raceId) {
        return ResponseEntity.ok()
                .body(dataCrawlRepository.getHorsePerformance(raceId, true));
    }

    @GetMapping(value = "/raceVideo/{raceId}")
    public ResponseEntity<String> getRaceInfoVideo(@PathVariable String raceId) {
        return ResponseEntity.ok().body(dataCrawlRepository.getRaceInfoVideo(raceId));
    }

    @GetMapping(value = "/crawlData")
    public ResponseEntity<String> crawlDataDate(@RequestParam(value = "raceDate") String raceDate,
                                                @RequestParam(value = "countryCode", required = false, defaultValue = "") String countryCode,
                                                @RequestParam(value = "raceNo", required = false, defaultValue = "0") int raceNo) {
        executorService.submit(() -> dataCrawlService.crawlDataWinner21(false, raceDate, countryCode, raceNo));
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "/crawlResult")
    public ResponseEntity<String> crawlResult() {
        executorService.submit(() -> dataCrawlService.crawlRaceResults());
        return ResponseEntity.ok("OK");
    }

    @PreDestroy
    public void stop() {
        try {
            executorService.shutdown();
        } catch (Throwable ignored) {
        }
        logger.log(Level.INFO, "{0} stopped.", DataCrawlController.class.getName());
    }
}
