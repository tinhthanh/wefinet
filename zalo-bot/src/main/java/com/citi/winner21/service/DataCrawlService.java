package com.citi.winner21.service;

import com.citi.winner21.config.SeleniumConfig;
import com.citi.winner21.model.*;
import com.citi.winner21.page.LoginPage;
import com.citi.winner21.page.RaceMeetingPage;
import com.citi.winner21.page.RacePage;
import com.citi.winner21.page.ResultPage;
import com.citi.winner21.repository.DataCrawlRepository;
import com.citi.winner21.ultils.Constants;
import com.citi.winner21.ultils.HttpUtil;
import com.citi.winner21.ultils.Utils;
import com.google.gson.Gson;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataCrawlService {
    private static final Logger logger = Logger.getLogger(DataCrawlService.class.getName());

    @Autowired
    private SeleniumConfig seleniumConfig;

    @Autowired
    private DataCrawlRepository dataCrawlRepository;

    @Autowired
    private MattermostService mattermostService;

    @Autowired
    private HttpUtil httpUtil;

    @Value("${patch-race-chart-url:}")
    private String patchRaceChartUrl;

    @Value("${max-count-msg:6}")
    private int maxCountMsg;

    @Value("${should-forward-gateway:}")
    private boolean shouldForwardGateway;

    private int countMessage = 0;

    private int countMessageResult = 0;

    private final Gson gson = new Gson();

    private Set<RaceMeetingItem> raceDateCaches = new HashSet<>();

//    @PostConstruct
    public void init() {
        autoCrawlDataWinner21();
    }

//    @Scheduled(cron = "0 0 10 1/1 * ?", zone = Constants.SINGAPORE_ZONE_TIME)
    public void autoCrawlDataWinner21() {
        if (shouldForwardGateway || Utils.checkTimeWinner21Maintenance()) {
            logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcoming should forward gateway or closed for maintenance from 12AM to 3AM daily!");
            return;
        }
        crawlRaceCardsUpcomingRace1();
        logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcoming raceCrawlCaches {0}", raceDateCaches);
        if (!raceDateCaches.isEmpty()) {
            crawlDataWinner21(true, Constants.EMPTY_STRING, Constants.EMPTY_STRING, 0);
        }
    }

    public void crawlDataWinner21(boolean isAuto, String raceDate, String countryCode, int raceNo) {
        ProviderAccount winner21Account = this.dataCrawlRepository.getProviderAccount(Constants.WINNER21_ACCOUNT);
        logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcoming crawl data by account {0}", winner21Account);
        ProviderAccount deathByCaptchaAccount = this.dataCrawlRepository.getProviderAccount(Constants.DEATHBYCAPTCHA_ACCOUNT);
        WebDriver driver = seleniumConfig.getFireFoxWebDriver(winner21Account);
        try {
            LoginPage loginPage = new LoginPage(driver, winner21Account, deathByCaptchaAccount);
            if (!loginPage.doLogin()) {
                if (countMessage++ >= maxCountMsg) {
                    mattermostService.postMessage("Hi all @here! We've detected that winner21 has login fail, our app cannot crawl data on race upcoming!");
                    countMessage = 0;
                }
                return;
            }
            dataCrawlRepository.updateAuthKeyAccount(winner21Account);
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            if (isAuto) {
                raceDateCaches.forEach(raceDateCache -> {
                    loginPage.changeCountry(raceDateCache.getCountryCode());
                    crawlRaceMeetingItems(driver, winner21Account, raceDateCache.getCountryCode())
                            .stream()
                            .filter(raceMeetingItem -> raceMeetingItem.getRaceDate().equalsIgnoreCase(raceDateCache.getRaceDate())
                                    && raceMeetingItem.getCountryCode().equalsIgnoreCase(raceDateCache.getCountryCode())
                                    && raceMeetingItem.getVenueCode().equalsIgnoreCase(raceDateCache.getVenueCode()))
                            .forEach(raceMeetingItem -> {
                                crawlRaceCards(driver, raceMeetingItem, winner21Account, isSuccess);
                                Utils.waitingEndCrawlRaceData();
                            });
                });
                crawlRaceResults(driver, winner21Account);
            } else {
                Constants.MAP_COUNTRY.values()
                        .stream()
                        .filter(country -> StringUtils.isEmpty(countryCode) || country.equalsIgnoreCase(countryCode))
                        .forEach(country -> {
                            loginPage.changeCountry(country);
                            crawlRaceMeetingItems(driver, winner21Account, country)
                                    .stream()
                                    .filter(raceMeetingItem -> raceMeetingItem.getRaceDate().equalsIgnoreCase(raceDate)
                                            && (raceNo == 0 || raceMeetingItem.getRaceNo() == raceNo))
                                    .forEach(raceMeetingItem -> {
                                        crawlRaceCards(driver, raceMeetingItem, winner21Account, isSuccess);
                                        Utils.waitingEndCrawlRaceData();
                                    });
                        });
            }
            countMessage = isSuccess.get() ? 0 : countMessage;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DataCrawlService -> collectRaceMeetings Fail, countMessage: {0}, Exception: {1}", new Object[]{countMessage, ex});
        } finally {
            logger.log(Level.INFO, "DataCrawlService -> Closed Driver!");
            driver.quit();
        }
    }

    private void crawlRaceCardsUpcomingRace1() {
        logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcomingRace1 Begin");
        raceDateCaches.clear();
        WebDriver driver = seleniumConfig.getFireFoxWebDriver();
        try {
            LocalDateTime now = Instant.now().atZone(ZoneId.of(Constants.SINGAPORE_ZONE_TIME)).toLocalDateTime();
            LoginPage loginPage = new LoginPage(driver, null, null);
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            Constants.MAP_COUNTRY.values().forEach(countryCode -> {
                loginPage.changeCountry(countryCode);
                crawlRaceMeetingItems(driver, null, countryCode)
                        .stream()
                        .filter(raceMeetingItem -> {
                            LocalDateTime endRaceDateTime = Utils.parseLocalDateTime(raceMeetingItem.getRaceDate(), "11:59PM");
                            return endRaceDateTime.isAfter(now)
                                    && !this.dataCrawlRepository.checkUpsertRaceResult(raceMeetingItem.getRaceID())
                                    && raceMeetingItem.getRaceNo() == 1;
                        })
                        .forEach(raceMeetingItem -> {
                            crawlRaceCards(driver, raceMeetingItem, null, isSuccess);
                            if (dataCrawlRepository.checkCrawlRaceCards(raceMeetingItem)) {
                                raceDateCaches.add(raceMeetingItem);
                            }
                            Utils.waitingEndCrawlRaceData();
                        });
            });
            countMessage = isSuccess.get() ? 0 : countMessage;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DataCrawlService -> crawlRaceCardsUpcomingRace1 Fail, countMessage: {0}, Exception: {1}", new Object[]{countMessage, ex});
        } finally {
            logger.log(Level.INFO, "DataCrawlService -> raceDateCaches {0}", raceDateCaches);
            logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcomingRace1 Closed Driver!");
            driver.quit();
        }
    }

    private void crawlRaceCards(WebDriver driver, RaceMeetingItem raceMeetingItem, ProviderAccount winner21Account, AtomicBoolean isSuccess) {
        RacePage racePage = new RacePage(driver, winner21Account, raceMeetingItem);
        List<RaceCard> raceCards = racePage.crawlRaceCards();
        List<RaceCard> raceCardsNoSCR = raceCards.stream().filter(raceCard -> !raceCard.isSCR()).collect(Collectors.toList());
        List<Integer> listHorseNo = raceCardsNoSCR.stream().map(RaceCard::getHorseNo).collect(Collectors.toList());
        List<RaceCard> raceCardsSCR = raceCards.stream().filter(raceCard -> raceCard.isSCR() && !listHorseNo.contains(raceCard.getHorseNo())).collect(Collectors.toList());
        List<RaceCard> raceCardsReplace = raceCards.stream().filter(raceCard -> raceCard.isSCR() && listHorseNo.contains(raceCard.getHorseNo())).collect(Collectors.toList());
        logger.log(Level.INFO, "DataCrawlService -> collectRaceMeetings get no SCR Race Card -> {0}", gson.toJson(raceCardsNoSCR));
        logger.log(Level.INFO, "DataCrawlService -> collectRaceMeetings get SCR Race Card -> {0}", gson.toJson(raceCardsSCR));
        logger.log(Level.INFO, "DataCrawlService -> collectRaceMeetings Replace SCR Race Card -> {0}", gson.toJson(raceCardsReplace));
        if (CollectionUtils.isEmpty(raceCards)) {
            isSuccess.set(false);
            if (countMessage++ >= maxCountMsg) {
                mattermostService.postMessage("Hi all @here! We've detected that winner21 has changed race meetings UI, our app cannot crawl data on race url: " + raceMeetingItem.getHref());
                countMessage = 0;
            }
        } else {
            dataCrawlRepository.upsertRaceInfoByRaceCards(raceCardsNoSCR);
            dataCrawlRepository.upsertHorsePerformanceByRaceCards(raceCardsNoSCR);
            dataCrawlRepository.updateGoingByRaceCards(raceCardsNoSCR);
            boolean needPatchRaceChart = false;
            if (Arrays.stream(dataCrawlRepository.deleteReplaceSCRHorsePerformance(raceCardsReplace.stream())).anyMatch(rowAffect -> rowAffect == 1)) {
                dataCrawlRepository.deleteHorseRanking(raceCards.get(0).getRaceID());
                needPatchRaceChart = true;
            }
            if (Arrays.stream(dataCrawlRepository.upsertSCRHorsePerformance(Stream.concat(raceCardsNoSCR.stream(), raceCardsSCR.stream()))).anyMatch(rowAffect -> rowAffect == 1) || needPatchRaceChart) {
                try {
                    logger.log(Level.INFO, "DataCrawlService -> collectRaceMeetings SCR Race ID request url -> {0}", patchRaceChartUrl + raceCards.get(0).getRaceID());
                    ResponseEntity<String> response = httpUtil.sendRequest(patchRaceChartUrl + raceCardsSCR.get(0).getRaceID(), String.class, null, HttpMethod.GET);
                    logger.log(Level.INFO, "DataCrawlService -> collectRaceMeetings SCR Race ID response -> {0}", response.getBody());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "DataCrawlService -> send patchRaceChartUrl fail, Exception: {0}", new Object[]{ex});
                }
            }
        }
    }

    private List<RaceMeetingItem> crawlRaceMeetingItems(WebDriver driver, ProviderAccount account, String countryCode) {
        RaceMeetingPage raceMeetingPage = new RaceMeetingPage(driver, account);
        return raceMeetingPage.crawlRaceMeetingItems(countryCode);
    }

    public void crawlRaceResults() {
        ProviderAccount winner21Account = this.dataCrawlRepository.getProviderAccount(Constants.WINNER21_ACCOUNT);
        logger.log(Level.INFO, "DataCrawlService -> crawlRaceCardsUpcoming crawl data by account {0}", winner21Account);
        ProviderAccount deathByCaptchaAccount = this.dataCrawlRepository.getProviderAccount(Constants.DEATHBYCAPTCHA_ACCOUNT);
        WebDriver driver = seleniumConfig.getFireFoxWebDriver(winner21Account);
        try {
            LoginPage loginPage = new LoginPage(driver, winner21Account, deathByCaptchaAccount);
            if (!loginPage.doLogin()) {
                return;
            }
            dataCrawlRepository.updateAuthKeyAccount(winner21Account);
            crawlRaceResults(driver, winner21Account);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DataCrawlService -> crawlRaceResults Fail, Exception: {0}", new Object[]{ex});
            driver.quit();
        }
    }

    private void crawlRaceResults(WebDriver driver, ProviderAccount winner21Account) {
        List<RaceInfo> raceInfoUncompleted = dataCrawlRepository.getRaceInfoUncompleted();
        if (CollectionUtils.isEmpty(raceInfoUncompleted)) {
            logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults -> do not find race info un complete");
            return;
        }
        logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults -> raceInfoUncompleted: {0}", gson.toJson(raceInfoUncompleted));
        try {
            LocalDateTime now = Instant.now().atZone(ZoneId.of(Constants.SINGAPORE_ZONE_TIME)).toLocalDateTime();
            ResultPage resultPage = new ResultPage(driver, winner21Account);
            raceInfoUncompleted.stream().filter(raceInfo -> {
                LocalDateTime endRaceDateTime = Utils.parseLocalDateTime(raceInfo.getRaceDate(), "11:59PM");
                return now.isAfter(endRaceDateTime.plusDays(1));
            }).forEach(raceInfo -> {
                List<HorseResult> horseResults = resultPage.crawlHorseResults(raceInfo);
                logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults race id {0}: {1}", new Object[]{raceInfo.getRaceID(), gson.toJson(horseResults)});
                if (!CollectionUtils.isEmpty(horseResults)) {
                    dataCrawlRepository.batchUpdateHorsePerformance(horseResults);
                    dataCrawlRepository.batchUpdateSCRHorseResult(horseResults.stream().filter(h -> Constants.DISLODGED_STRING.equalsIgnoreCase(h.getRaceReport()) || h.getRacePosition() == 0));
                    try {
                        logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults SCR Race ID request url -> {0}", patchRaceChartUrl + raceInfo.getRaceID());
                        ResponseEntity<String> response = httpUtil.sendRequest(patchRaceChartUrl + raceInfo.getRaceID(), String.class, null, HttpMethod.GET);
                        logger.log(Level.INFO, "DataCrawlService -> crawlRaceResults SCR Race ID response -> {0}", response.getBody());
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "DataCrawlService -> send patchRaceChartUrl fail, Exception: {0}", new Object[]{ex});
                    }
                    dataCrawlRepository.updateStausRaceInfo(raceInfo.getRaceID(), 1);
                    try {
                        dataCrawlRepository.updateRaceVideo(raceInfo.getRaceID(), resultPage.getRaceVideo());
                        logger.log(Level.INFO, "DataCrawlService  -->  crawlRaceResults -> update race video {0} ", new Object[]{resultPage.getRaceVideo()});
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, "DataCrawlService  -->  crawlRaceResults -> cannot update race video -> race_id {0}, Exception: {1} {2}", new Object[]{raceInfo.getRaceID(), ex, resultPage.getRaceVideo()});
                    }
                    Utils.waitingEndCrawlRaceData();
                }
            });
            countMessageResult = 0;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "DataCrawlService -> crawlRaceResults Fail, countMessage: {0}, Exception: {1}", new Object[]{countMessage,ex});
            if (countMessageResult++ >= maxCountMsg) {
                mattermostService.postMessage("Hi all @here! We've detected that winner21 has changed race result UI, our app cannot crawl data any more.");
            }
        }
    }
}
