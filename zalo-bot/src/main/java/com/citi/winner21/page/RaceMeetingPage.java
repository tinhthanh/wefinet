package com.citi.winner21.page;

import com.citi.winner21.model.ProviderAccount;
import com.citi.winner21.model.RaceMeetingItem;
import com.citi.winner21.ultils.Constants;
import com.citi.winner21.ultils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RaceMeetingPage extends AbstractPage {
    private static final Logger logger = Logger.getLogger(RaceMeetingPage.class.getName());

    public RaceMeetingPage(WebDriver driver, ProviderAccount account) {
        super(driver, account, RACE_MEETING_URL);
    }

    public List<RaceMeetingItem> crawlRaceMeetingItems(String countryCode) {
        final String divRaceMeetingID = "divRaceMeeting";
        try {
            WebDriverWait wait = new WebDriverWait(driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(divRaceMeetingID)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Get Race Meeting Items Time Out on country code {0}, Exception: {1}",
                    new Object[]{countryCode, e});
            return new ArrayList<>();
        }
        SimpleDateFormat meetingDateFormat = new SimpleDateFormat(Constants.MEETING_DATE_FORMAT);
        SimpleDateFormat dbIDDateFormat = new SimpleDateFormat(Constants.DB_ID_DATE_FORMAT);
        String innerHTML = this.driver.findElement(By.id(divRaceMeetingID)).getAttribute("innerHTML");
        return Jsoup.parse(innerHTML).select("div.list-race").stream()
                .filter(element -> Constants.MAP_COUNTRY.getOrDefault(element.select("div.country-name").text(), "").equals(countryCode))
                .map(element -> element.select("li.selected > a").eachAttr("href"))
                .flatMap(Collection::stream)
                .filter(href -> href.startsWith("/Race?"))
                .map(href -> {
                    RaceMeetingItem item = new RaceMeetingItem();
                    try {
                        item.setRaceMeetingID(Utils.getRaceMeetingID(href));
                        item.setHref(href);
                        item.setRaceDate(Utils.getRaceDateMeeting(href));
                        item.setRaceNo(Utils.getRaceNoMeeting(href));
                        item.setCountryCode(countryCode);
                        item.setVenueCode(item.getRaceMeetingID().substring(2,4));
                        item.setRaceID(item.getRaceMeetingID().substring(0,4)
                                + dbIDDateFormat.format(meetingDateFormat.parse(item.getRaceMeetingID().substring(item.getRaceMeetingID().length() - 8)))
                                + "R" + item.getRaceNo());
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Meeting Date getRaceMeetingItems Fail href: {0}, Exception: {1}", new Object[]{href, e});
                        item.setRaceMeetingID("");
                    }
                    return item;
                })
                .filter(item -> StringUtils.isNotEmpty(item.getRaceMeetingID()))
                .collect(Collectors.toList());
    }
}
