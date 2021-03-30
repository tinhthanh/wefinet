package com.citi.winner21.page;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.HttpClient;
import com.citi.winner21.model.ProviderAccount;
import com.citi.winner21.page.component.LoginFormComponent;
import com.citi.winner21.ultils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginPage extends AbstractPage {

    private static final Logger logger = Logger.getLogger(LoginPage.class.getName());
    private ProviderAccount deathByCaptchaAccount;

    public LoginPage(WebDriver driver, ProviderAccount account, ProviderAccount deathByCaptchaAccount) {
        super(driver, account, HOME_INDEX_URL);
        this.deathByCaptchaAccount = deathByCaptchaAccount;
    }

    public boolean doLogin() {
        try {
            driver.manage().deleteCookieNamed(SESSION_ID);
            driver.navigate().refresh();
            Thread.sleep(ThreadLocalRandom.current().nextInt(5_000, 10_000));
            WebDriverWait wait = new WebDriverWait(driver, Constants.TIME_OUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(ACCOUNT_NAME)));
            driver.findElement(By.id("accountName")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId")));
            if (!driver.findElements(By.id("g-recaptcha-response")).isEmpty()) {
                Captcha captCha = new Captcha();
                try {
                    captCha = getCaptCha();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "UserLoginService >> doLogin >> error when get CAPTCHA", e);
                }
                if (!captCha.isUploaded() || !captCha.isSolved()) {
                    logger.log(Level.WARNING, "UserLoginService >> doLogin >> cant not upload CAPTCHA");
                    return false;
                }
                executeJavascript("$('#g-recaptcha-response').val('" + captCha.text + "')");
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            }
            LoginFormComponent loginFormComponent = new LoginFormComponent(driver);
            loginFormComponent.login(this.winner21Account.getAccountName(), this.winner21Account.getPassword(), Constants.TIME_OUT);
            this.winner21Account.setAuthKey(driver.manage().getCookieNamed(SESSION_ID).getValue());
            if (checkCookieExpire() || retryKeyAlive()) {
                logger.log(Level.INFO, "LoginService -> Login Success -> account: {0}", winner21Account.getAccountName());
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "LoginService -> Login Fail account {0} -> Exception {1}", new Object[]{winner21Account.getAccountName(), e});
        }
        return false;
    }

    private Captcha getCaptCha() throws RuntimeException {

        final String CAPTCHA_USER = deathByCaptchaAccount.getAccountName();
        final String CAPTCHA_PASSWORD = deathByCaptchaAccount.getPassword();
        final String CAPTCHA_KEY = deathByCaptchaAccount.getAuthKey();

        Client client = new HttpClient(CAPTCHA_USER, CAPTCHA_PASSWORD);
        client.isVerbose = true;
        try {
            try {
                logger.log(Level.INFO, "LoginPage >> getCaptCha >> Your balance is " + client.getBalance() + " US cents");
            } catch (IOException e) {
                logger.log(Level.WARNING, "LoginPage >> getCaptCha >> Failed fetching balance: ", e);
                throw new RuntimeException(e);
            }
            Captcha captcha;
            try {
                // Upload a reCAPTCHA and poll for its status with 120 seconds timeout.
                // Put your proxy, proxy type, page googlekey, page url and solving timeout (in seconds)
                // 0 or nothing for the default timeout value.
                //client.decode(<proxy>, <proxy type>, <recaptcha site key>, <page url>);

                captcha = client.decode(null,null, CAPTCHA_KEY, HOME_INDEX_URL);
                logger.log(Level.INFO, "LoginPage >> getCaptCha {0}", captcha.text);
            } catch (IOException | InterruptedException e) {
                logger.log(Level.WARNING, "LoginPage >> getCaptCha >> Failed uploading CAPTCHA", e);
                throw new RuntimeException("Failed uploading CAPTCHA");
            }
            if (null != captcha) {
                logger.log(Level.INFO, "LoginPage >> getCaptCha >> CAPTCHA " + captcha.id + " solved: " + captcha.text);
                return captcha;

            } else {
                logger.log(Level.WARNING, "LoginPage >> getCaptCha >> Failed solving CAPTCHA");
                throw new RuntimeException("Failed solving CAPTCHA");
            }
        } catch (com.DeathByCaptcha.Exception e) {
            logger.log(Level.WARNING, "LoginPage >> getCaptCha >> Something bad happen", e);
            throw new RuntimeException(e);
        }
    }
}
