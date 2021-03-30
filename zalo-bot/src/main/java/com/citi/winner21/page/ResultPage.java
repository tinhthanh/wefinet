package com.citi.winner21.page;

import com.citi.winner21.model.HorseResult;
import com.citi.winner21.model.ProviderAccount;
import com.citi.winner21.model.RaceInfo;
import com.citi.winner21.page.component.RaceHeaderComponent;
import com.citi.winner21.ultils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ResultPage extends AbstractPage {

    private static final Logger logger = Logger.getLogger(ResultPage.class.getName());

    private RaceHeaderComponent raceHeaderComponent;

    public ResultPage(WebDriver driver, ProviderAccount account) {
        super(driver, account, RACE_RESULT_URL);
        this.raceHeaderComponent = new RaceHeaderComponent(this.driver);
    }

    public List<HorseResult> crawlHorseResults(RaceInfo raceInfo) {
        List<HorseResult> horseResults = new ArrayList<>();
        if (!selectCountry(raceInfo.getCountryCode()) || !selectRaceDate(raceInfo.covertSelectDate()) || raceHeaderComponent.checkRaceNoResult(raceInfo.getRaceNo())) {
            return new ArrayList<>();
        }
        raceHeaderComponent.crawlRaceNoTabs().get(raceInfo.getRaceNo() - 1).click();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10_000, 20_000));
            WebDriverWait wait = new WebDriverWait(this.driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pnlRaceResult")));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Crawl Horse Results Time Out on raceID {0}, Exception: {1}",
                    new Object[]{raceInfo.getRaceID(), e});
            return new ArrayList<>();
        }
        WebElement raceCardComponent = this.driver.findElement(By.id("pnlRaceResult"));
        String innerHTML = raceCardComponent.getAttribute("innerHTML");
        Document document = Jsoup.parse(innerHTML);
        Elements horseInfoTableE = document.select("div.panel-table-result > div.table-head > div > table > thead:not(.BgBlueGray) > tr");
        Elements horseResultTableE = document.select("div.panel-table-result > div.table-body > div > div > table > tbody > tr");
        for (int rowIndex = 0; rowIndex < horseInfoTableE.size(); rowIndex++) {
            HorseResult horseResult = new HorseResult();
            horseResult.setRaceID(raceInfo.getRaceID());
            horseResult.setRaceDate(raceInfo.getRaceDate());
            horseResult.setRaceNo(raceInfo.getRaceNo());
            horseResult.setCountryCode(raceInfo.getCountryCode());
            horseResult.setVenueCode(raceInfo.getVenueCode());
            List<String> listHorseInfo = horseInfoTableE.get(rowIndex).select("td").eachText().stream().map(String::trim).collect(Collectors.toList());
            horseResult.setHorseNo(Integer.parseInt(listHorseInfo.get(0)));
            horseResult.setHorseName(listHorseInfo.get(1));
            List<Element> listResultInfoE = horseResultTableE.get(rowIndex).select("td");
            horseResult.setHorseWeight(StringUtils.trim(listResultInfoE.get(0).text()));
            horseResult.setHorseRating(StringUtils.trim(listResultInfoE.get(2).text()));
            horseResult.setHandicapWeight(StringUtils.trim(listResultInfoE.get(3).text().contains("c") ? listResultInfoE.get(3).text().split("c")[1] : listResultInfoE.get(3).text()));
            horseResult.setBarrier(StringUtils.trim(listResultInfoE.get(5).text()).replaceAll("[^\\d]", ""));
            horseResult.setRunningPosition(StringUtils.trim(listResultInfoE.get(7).text().replace(" ", "-")));
            horseResult.setRacePosition(NumberUtils.toInt(horseResult.getRunningPosition().split("-").length > 2 ? horseResult.getRunningPosition().split("-")[2] : horseResult.getRunningPosition(), 0));
            horseResult.setDividend(StringUtils.trim(listResultInfoE.get(8).text()));
            horseResult.setFinishedTime(StringUtils.trim(listResultInfoE.get(9).text().replaceAll("[^\\d]", "")));
            horseResult.setRaceReport(StringUtils.trim(listResultInfoE.get(11).text()));
            horseResults.add(horseResult);
        }
        return horseResults;
    }


    private boolean selectCountry(String countryCode) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5_000, 10_000));
            WebDriverWait wait = new WebDriverWait(this.driver, Constants.TIME_OUT);
            wait.until(visibilityOfElement(By.id("select2-ddlCountry-container"), By.cssSelector("input.select2-search__field")));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Crawl Horse Results Time Out on countryCode {0}, Exception: {1}",
                    new Object[]{countryCode, e});
            return false;
        }
        List<String> listCountryCode = driver.findElements(By.cssSelector("select#ddlCountry > option")).stream().map(e -> e.getAttribute("value")).collect(Collectors.toList());
        if (!listCountryCode.contains(countryCode)) {
            logger.log(Level.INFO, "ResultPage -> crawlHorseResults select country Fail on countryCode {0}", new Object[]{countryCode});
            return false;
        }
        WebElement searchInput = driver.findElement(By.cssSelector("input.select2-search__field"));
        Optional<Map.Entry<String, String>> option = Constants.MAP_COUNTRY.entrySet().stream().filter(entry -> countryCode.equals(entry.getValue())).findFirst();
        if (!option.isPresent()) {
            logger.log(Level.INFO, "ResultPage -> crawlHorseResults select country Fail on countryCode {0}", new Object[]{countryCode});
            return false;
        }
        searchInput.sendKeys(option.get().getKey());
        searchInput.sendKeys(Keys.ENTER);
        return true;
    }

    private boolean selectRaceDate(String raceDate) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5_000, 10_000));
            WebDriverWait wait = new WebDriverWait(this.driver, Constants.TIME_OUT);
            wait.until(visibilityOfElement(By.id("select2-ddlDate-container"), By.id("select2-ddlDate-results")));
            List<String> listRaceDate = driver.findElements(By.cssSelector("select#ddlDate > option")).stream().map(e -> e.getAttribute("value")).collect(Collectors.toList());
            if (!listRaceDate.contains(raceDate)) {
                return false;
            }
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li[id$='" + raceDate + "']"))).click();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Crawl Horse Results Time Out on raceDate {0}, Exception: {1}",
                    new Object[]{raceDate, e});
            return false;
        }
        return true;
    }

    public String getRaceVideo() {
        return this.driver.findElement(By.cssSelector("a[id^=raceVideo]")).getAttribute("id");
    }

    private ExpectedCondition<Boolean> visibilityOfElement(By locatorClick, By locatorVisible) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    WebElement elementClick = driver.findElement(locatorClick);
                    if (elementClick == null || !elementClick.isDisplayed()) {
                        return false;
                    }
                    elementClick.click();
                    WebElement elementVisible = driver.findElement(locatorVisible);
                    return elementVisible != null && elementVisible.isDisplayed();
                } catch (WebDriverException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "visibility of " + locatorVisible;
            }
        };
    }

}
