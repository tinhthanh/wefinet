package com.citi.winner21.page;

import com.citi.winner21.model.ProviderAccount;
import com.citi.winner21.model.RaceCard;
import com.citi.winner21.model.RaceMeetingItem;
import com.citi.winner21.page.component.RaceHeaderComponent;
import com.citi.winner21.ultils.Constants;
import com.citi.winner21.ultils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RacePage extends AbstractPage {
    private static final Logger logger = Logger.getLogger(RacePage.class.getName());

    private static final String RACE_CARD_ID = "raceCard";
    private static final String DATA_POPOVER_CONTENT = "data-popover-content";
    private static final String NUMBER_REGEX = "[^0-9.]+";
    private RaceHeaderComponent raceHeaderComponent;
    private RaceMeetingItem raceMeetingItem;

    public RacePage(WebDriver driver, ProviderAccount account, RaceMeetingItem raceMeetingItem) {
        super(driver, account, BASE_URL + raceMeetingItem.getHref());
        this.driver = driver;
        this.raceMeetingItem = raceMeetingItem;
        this.raceHeaderComponent = new RaceHeaderComponent(this.driver);
    }

    public List<RaceCard> crawlRaceCards() {
        List<RaceCard> raceCards = new ArrayList<>();
        WebElement raceCardComponent;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#raceCard > div > div.panel-table-race > div.table-head > div.table-row > div.thead-number")));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Get Race Card Time Out on locationRace {0}, Exception: {1}",
                    new Object[]{this.raceMeetingItem.getHref(), e});
            return new ArrayList<>();
        }
        try {
            String raceDist = this.raceHeaderComponent.getRaceDistTabOpen();
            String raceDistType = this.raceHeaderComponent.getRaceDistTypeTabOpen();
            String textColor = this.driver.findElement(By.cssSelector("#ulClass > li.dropdown.open > ul")).getCssValue("background-color");

            String trackType;
            String trackTypeCode;
            String course = Utils.mapColor(textColor);
            if ("HK".equals(this.raceMeetingItem.getCountryCode())) {
                trackTypeCode = "T";
                trackType = "P".equals(course) ? "ALL WEATHER TRACK" : "TURF TRACK";
                if ("P".equals(course)) {
                    course = "A";
                } else {
                    course = "HV".equals(this.raceMeetingItem.getVenueCode()) ? "H" : "S";
                }
            } else {
                if ("P".equals(course)) {
                    trackType = "POLYTRACK";
                    trackTypeCode = "P";
                    course = null;
                } else {
                    trackType = "TURF TRACK";
                    trackTypeCode = "T";
                }
            }
            String raceStartTime = this.raceHeaderComponent.getRaceStartTimeTabOpen().toUpperCase();
            String raceName = this.raceHeaderComponent.getRaceName();
            String[] listStr = raceName.split(" ");
            String raceClassName = "";
            String pastRaceDate = Utils.getPastRaceDate(raceMeetingItem.getRaceDate());
            String raceDiv = null;
            for (int i = 0; i < listStr.length; i++) {
                if ("CL".equalsIgnoreCase(listStr[i])) {
                    raceClassName = "CL " + listStr[i + 1];
                } else if ("Div".equalsIgnoreCase(listStr[i])) {
                    raceDiv = listStr[i + 1];
                }
            }
            String racePrize = this.raceHeaderComponent.getRacePrize();
            raceCardComponent = this.driver.findElement(By.id(RACE_CARD_ID));
            String innerHTML = raceCardComponent.getAttribute("innerHTML");
            Document document = Jsoup.parse(innerHTML);
            String horseRecommend = document.select("div#w21selection > div > div > div[class^='selection']").eachText().stream().map(text -> text.trim().split(" ")[0]).collect(Collectors.joining("-"));
            Elements horseInfoTableE = document.select("div > div.panel-table-race > div.table-head > div.table-row");
            Elements horseWeightTableE = document.select("div > div.panel-table-race > div.table-body > div.table-fixed > div > table > tbody > tr");
            Elements jockeyTrainerTableE = document.select("div > div.panel-table-race > div.table-body > div.table-expand > div > table > tbody > tr");
            for (int rowIndex = 0; rowIndex < horseInfoTableE.size(); rowIndex++) {
                Element horseInfoRowE = horseInfoTableE.get(rowIndex);
                RaceCard raceCard = new RaceCard();
                String horseNo = StringUtils.trim(horseInfoRowE.select("div.thead-number > strong").text());
                if (StringUtils.isNotEmpty(horseNo)) {
                    raceCard.setHorseNo(Integer.parseInt(horseNo));
                } else if (StringUtils.isNotEmpty(StringUtils.trim(horseInfoRowE.select("div.thead-number > span > strong").text()))) {
                    raceCard.setHorseNo(Integer.parseInt(StringUtils.trim(horseInfoRowE.select("div.thead-number > span > strong").text())));
                } else {
                    continue;
                }
                raceCard.setRaceID(this.raceMeetingItem.getRaceID());
                raceCard.setRaceDate(this.raceMeetingItem.getRaceDate());
                raceCard.setCountryCode(this.raceMeetingItem.getCountryCode());
                raceCard.setVenueCode(this.raceMeetingItem.getVenueCode());
                raceCard.setRaceDist(raceDist);
                raceCard.setRaceNo(this.raceMeetingItem.getRaceNo());
                raceCard.setCourse(course);
                raceCard.setTrackType(trackType);
                raceCard.setTrackTypeCode(trackTypeCode);
                raceCard.setRaceDistType(raceDistType);
                raceCard.setStartTime(raceStartTime);
                raceCard.setRaceName(raceName);
                raceCard.setPrize(racePrize);
                raceCard.setHorseRecommend(horseRecommend);
                raceCard.setRaceClassName(raceClassName);
                raceCard.setRaceDiv(raceDiv);
                // Crawl Horse SCR
                Element strikethroughE = horseInfoRowE.selectFirst("div.thead-name.strikethrough");
                if (strikethroughE == null) {
                    raceCard.setHorseName(StringUtils.trim(horseInfoRowE.select("div.thead-name > a").text()));
                    String horseId = StringUtils.trim(horseInfoRowE.select("div.thead-name > a").attr(DATA_POPOVER_CONTENT));
                    raceCard.setHorseId(StringUtils.isNotEmpty(horseId) ? "W" + horseId.substring(1) : raceCard.getRaceID() + horseNo);
                    raceCard.setBarrier(StringUtils.trim(horseInfoRowE.select("div.thead-blink").text()).replaceAll("[^\\d]", ""));
                } else {
                    raceCard.setHorseName(StringUtils.trim(strikethroughE.text()));
                    raceCard.setSCR(true);
                    raceCards.add(raceCard);
                    continue;
                }

                // Crawl Horse Weight
                Element horseWeightRowE = horseWeightTableE.get(rowIndex);
                final AtomicInteger columnIndex = new AtomicInteger(0);
                horseWeightRowE.select("td").forEach(columnE -> {
                    switch (columnIndex.getAndIncrement()) {
                        case 7:
                            String[] arr = StringUtils.trim(columnE.text()).replace("*", "").split(NUMBER_REGEX);
                            raceCard.setHorseWeight(arr.length > 0 ? arr[0] : "");
                            break;
                        case 8:
                            arr = StringUtils.trim(columnE.text()).replace("*", "").split(NUMBER_REGEX);
                            raceCard.setHorseRating(arr.length > 0 ? arr[0] : "");
                            break;
                        case 9:
                            arr = StringUtils.trim(columnE.text()).replace("*", "").split(NUMBER_REGEX);
                            raceCard.setHandicapWeight(arr.length > 0 ? arr[0] : "");
                            break;
                        default:
                            break;
                    }
                });

                // Crawl Horse Jockey Trainer
                Element jockeyTrainerRowE = jockeyTrainerTableE.get(rowIndex);
                Elements columnElements = jockeyTrainerRowE.select("td").not(".hidden");
                Element jockeyE = columnElements.get(0).selectFirst("div > a[href='#']");
                if (jockeyE != null) {
                    raceCard.setJockeyName(StringUtils.trim(jockeyE.text()));
                    raceCard.setJockeyID(StringUtils.trim(jockeyE.attr(DATA_POPOVER_CONTENT).substring(1)));
                    String subText = jockeyTrainerRowE.select("td.bordershadow_after > div > sup").text();
                    if (StringUtils.isNotBlank(subText)) {
                        raceCard.setSubDigit(Integer.parseInt(subText));
                    }
                }
                Element trainerE = columnElements.get(1).selectFirst("div > a[href='#']");
                if (trainerE != null) {
                    raceCard.setTrainerName(StringUtils.trim(trainerE.text()));
                    raceCard.setTrainerID(StringUtils.trim(trainerE.attr(DATA_POPOVER_CONTENT).substring(1)));
                }
                Element horseStableE = document.selectFirst("div#" + raceCard.getHorseId().substring(1));
                Elements columnStables = horseStableE.select("div > span.text-blueLight");
                if (columnStables.size() >= 3) {
                    String[] arr = StringUtils.trim(columnStables.get(1).text()).split("--");
                    raceCard.setFatherName(arr[0]);
                    raceCard.setMotherName(arr.length >= 2 ? arr[1] : Constants.EMPTY_STRING);
                    raceCard.setOwnerName(StringUtils.trim(columnStables.get(2).text().split("--")[0]));
                }
                Element achievementE = horseStableE.selectFirst("div.record > div > b");
                if (achievementE != null) {
                    raceCard.setAchievement(achievementE.text());
                }
                Elements tableHeadRowE = horseStableE.select("div.table-head > div > table > tbody > tr");
                if (tableHeadRowE.last() != null && tableHeadRowE.last().text().contains(pastRaceDate)) {
                    raceCard.setRaceClass(StringUtils.trim(horseStableE.select("div.table-body > div > table > tbody > tr").last().selectFirst("td").text()));
                }
                for (Element trE : tableHeadRowE) {
                    List<String> list = trE.select("td").eachText().stream().map(String::trim).collect(Collectors.toList());
                    if (list.size() >= 2) {
                        raceCard.addPastRacesGoing(Utils.getDBRaceDate(list.get(0)), Utils.checkStringEndNonDigits(list.get(1)) ? list.get(1).substring(list.get(1).length() - 1).toLowerCase() : null);
                    }
                }
                raceCards.add(raceCard);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "RacePage -> getAllRaceCard on locationRace {0}, Exception: {1}", new Object[]{this.raceMeetingItem.getHref(), ex});
            return new ArrayList<>();
        }
        return raceCards;
    }
}
