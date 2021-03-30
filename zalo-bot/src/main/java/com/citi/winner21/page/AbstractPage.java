package com.citi.winner21.page;


import com.citi.winner21.model.ProviderAccount;
import com.citi.winner21.ultils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPage {
    private static final Logger logger = Logger.getLogger(AbstractPage.class.getName());

    protected static final String BASE_URL = "https://https://chat.zalo.me";
    protected static final String USER_DETAILS_URL = BASE_URL + "/Account/UserDetails";
    protected static final String RACE_MEETING_URL = BASE_URL + "/Race/RaceMeeting";
    protected static final String RACE_RESULT_URL = BASE_URL + "/RaceResult";
    protected static final String HOME_URL = BASE_URL + "/Home/ChangeSelectedCountry?country=";
    protected static final String HOME_INDEX_URL = BASE_URL + "/Home/Index";
    protected static final String SESSION_ID = "ASP.NET_SessionId";
    protected static final String ACCOUNT_NAME = "accountName";
    protected WebDriver driver;
    protected ProviderAccount winner21Account;
    protected String pageUrl;


    public AbstractPage(WebDriver driver, ProviderAccount winner21Account, String pageUrl) {
        this.driver = driver;
        this.pageUrl = pageUrl;
        this.winner21Account = winner21Account;
        navigateToMe();
    }

    private boolean navigateToMe() {
        try {
            this.driver.get(pageUrl);
            Thread.sleep(ThreadLocalRandom.current().nextInt(10_000, 15_000));
            WebDriverWait wait = new WebDriverWait(driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(ACCOUNT_NAME)));
            if (pageUrl.equals(driver.getCurrentUrl())) {
                logger.log(Level.INFO, "AbstractPage -> navigate to {0} is success", pageUrl);
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "LoginService -> navigate to {0} -> Exception: {1}", new Object[] {pageUrl, e});
        }
        logger.log(Level.INFO, "AbstractPage -> navigate to {0} is fail", pageUrl);
        return false;
    }

    public boolean retryKeyAlive() {
        for (int i = 0; i < 10; i++) {
            if (checkKeyAlive()) {
                return true;
            }
            navigateToMe();
        }
        return false;
    }

    private boolean checkKeyAlive() {
        try {
            driver.manage().deleteAllCookies();
            Cookie cookie = new Cookie.Builder(SESSION_ID, winner21Account.getAuthKey())
                    .isHttpOnly(true)
                    .isSecure(false)
                    .path("/")
                    .build();
            driver.manage().addCookie(cookie);
            return checkCookieExpire();
        } catch (Exception e) {
            logger.log(Level.WARNING, "AbstractPage -> checkAlive Fail -> account: {0} -> Exception: {1}", new Object[] {winner21Account.getAccountName(), e});
        }
        return false;
    }

    public boolean checkCookieExpire() {
        try {
            driver.get(USER_DETAILS_URL);
            Thread.sleep(ThreadLocalRandom.current().nextInt(10_000, 15_000));
            WebDriverWait wait = new WebDriverWait(driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(ACCOUNT_NAME)));
            if (USER_DETAILS_URL.equals(driver.getCurrentUrl())) {
                logger.log(Level.INFO, "AbstractPage -> checkAlive Success -> account: {0}", winner21Account.getAccountName());
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "AbstractPage -> checkAlive Fail -> account: {0} -> Exception: {1}", new Object[] {winner21Account.getAccountName(), e});
        }
        return false;
    }

    public void changeCountry(String country) {
        this.pageUrl = HOME_URL  + country;
        navigateToMe();
    }

    protected Object executeJavascript(String javascript) {
        JavascriptExecutor jse = (JavascriptExecutor) this.driver;
        return jse.executeScript(javascript);
    }
}
