package com.citi.winner21.page.component;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginFormComponent {
    private static final String USERNAME_INPUT_ID = "userId";
    private static final String PASSWORD_INPUT_ID = "password-field";
    private static final String LOGIN_SUCCESS_ID = "a#accountName > i.loginStatus";
    private WebDriver driver;
    private WebElement userNameInput;
    private WebElement passwordInput;

    public LoginFormComponent(WebDriver driver) {
        this.driver = driver;
        initData();
    }

    private void initData() {
        this.userNameInput = driver.findElement(By.id(USERNAME_INPUT_ID));
        this.passwordInput = driver.findElement(By.id(PASSWORD_INPUT_ID));
    }

    public void login(String user, String password, long timeOut) {
        userNameInput.sendKeys(user);
        passwordInput.sendKeys(password);
        passwordInput.sendKeys(Keys.ENTER);
        WebDriverWait wait = new WebDriverWait(driver, timeOut);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(LOGIN_SUCCESS_ID)));
    }
}
