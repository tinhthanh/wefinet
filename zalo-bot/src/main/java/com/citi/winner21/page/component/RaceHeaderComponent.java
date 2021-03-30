package com.citi.winner21.page.component;

import com.citi.winner21.ultils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class RaceHeaderComponent {

    private WebDriver driver;

    public RaceHeaderComponent(WebDriver driver) {
        this.driver = driver;
    }

    public List<WebElement> crawlRaceNoTabs() {
        return this.driver.findElements(By.cssSelector("a[id^=currentRace]"));
    }

    public String getRaceDistTabOpen() {
        String text = this.driver.findElement(By.xpath("//ul[@id='ulClass']/li[contains(@class,'open')]/ul/div[contains(@class,'header-two')]")).getText();
        if (StringUtils.isBlank(text)) {
            return Constants.EMPTY_STRING;
        }
        String str = text.split(" - ")[1].split("\\[")[0];
        return str.replaceAll("[^\\d]", "");
    }

    public boolean checkRaceNoResult(int raceNo) {
        return this.driver.findElements(By.cssSelector("a[id^=currentRace][id$=_" + raceNo)).isEmpty();
    }

    public String getRaceDistTypeTabOpen() {
        String text = this.driver.findElement(By.xpath("//ul[@id='ulClass']/li[contains(@class,'open')]/ul/div[contains(@class,'header-two')]")).getText();
        if (StringUtils.isBlank(text)) {
            return Constants.EMPTY_STRING;
        }
        String str = text.split(" - ")[1];
        int index = 0;
        while (index < str.length() && !Character.isDigit(str.charAt(index))) index++;
        return str.substring(0, index);
    }

    public String getRaceStartTimeTabOpen() {
        String text = this.driver.findElement(By.xpath("//ul[@id='ulClass']/li[contains(@class,'open')]/ul/div[contains(@class,'header-one')]")).getText();
        if (StringUtils.isBlank(text)) {
            return Constants.EMPTY_STRING;
        }
        return text.split(Constants.SPACE_STRING)[0];
    }

    public String getRaceName() {
        return  this.driver.findElement(By.xpath("//ul[@id='ulClass']/li[contains(@class,'open')]/ul/div[contains(@class,'header-one')]")).getText();
    }

    public String getRacePrize() {
        String text = this.driver.findElement(By.xpath("//ul[@id='ulClass']/li[contains(@class,'open')]/ul/div[contains(@class,'header-two')]")).getText();
        if (StringUtils.isBlank(text)) {
            return Constants.EMPTY_STRING;
        }
        return text.split(" - ")[0];
    }
}
