//package com.citi.winner21.service;
//        import com.citi.winner21.config.SeleniumConfig;
//        import com.google.cloud.firestore.DocumentSnapshot;
//        import com.google.cloud.firestore.EventListener;
//        import org.openqa.selenium.By;
//        import org.openqa.selenium.Keys;
//        import org.openqa.selenium.WebDriver;
//        import org.openqa.selenium.WebElement;
//        import org.openqa.selenium.support.ui.ExpectedConditions;
//        import org.openqa.selenium.support.ui.WebDriverWait;
//        import org.springframework.beans.factory.annotation.Autowired;
//        import org.springframework.context.annotation.DependsOn;
//        import org.springframework.stereotype.Service;
//
//        import javax.annotation.PostConstruct;
//        import javax.annotation.PreDestroy;
//        import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//@DependsOn("FirebaseInitialization")
//@Service
//public class FacebooBotService {
//    private static final Logger logger = Logger.getLogger(ChatBotService.class.getName());
//    @Autowired
//    private SeleniumConfig seleniumConfig;
//    @Autowired
//    ZaloService zaloService;
//    WebDriver driver;
//    @PreDestroy
//    public void destroy() {
//        if(this.driver !=  null) {
//            this.driver.quit();
//            logger.log(Level.INFO, "ChatBotService -> Closed Driver!");
//        }
//    }
//    @PostConstruct
//    public void init() {
//        driver = seleniumConfig.getFireFoxWebDriver();
//        this.listenerComamdZalo();
//
//    }
//    public void listenerComamdZalo() {
//        EventListener<DocumentSnapshot> eventListener = (documentSnapshot, e) -> {
//            ZaloCommand temp = documentSnapshot.toObject(ZaloCommand.class);
//            switch (temp.getAction()) {
//                case  "LOGIN":
//                    logger.log(Level.INFO, "ChatBotService -> Change {0}, " , new Object[]{temp.toString()});
//                    doLogin();
//                default:
//                    logger.log(Level.INFO, "ChatBotService -> Change action {0}, " , new Object[]{temp.toString()});
//            }
//        };
//        zaloService.listenerCommand(eventListener);
//    }
//    public void listenerComamdTrade() {
//        EventListener<DocumentSnapshot> eventListener = (documentSnapshot, e) -> {
//            WefinexCommand temp = documentSnapshot.toObject(WefinexCommand.class);
//            logger.log(Level.INFO, "ChatBotService -> listenerComamdTrade {0}, " , new Object[]{temp.toString()});
//            List<WebElement> gEl = driver.findElements(By.cssSelector("input#contact-search-input"));
//            gEl.forEach( z -> {
//                z.clear();
//                z.sendKeys("WEFINEX_BOT");
//                try {
//                    Thread.sleep(200);
//                    z.sendKeys(Keys.ENTER);
//                    // open chat bot
//                    Thread.sleep(500);
//                    WebElement chatEl = driver.findElement(By.cssSelector("#richInput"));
//                    chatEl.clear();
//                    String keysPressed =  Keys.chord(Keys.SHIFT, Keys.ENTER);
//
//                    chatEl.sendKeys("Thời gian: "+temp.getTime()+ "  -> Lệnh:  " +(temp.getType().equalsIgnoreCase("T")? "TĂNG" :"GIẢM" )+ " -> Tiền: " + temp.getPrice() +"\n");
//                    chatEl.sendKeys(keysPressed) ;
//                    chatEl.clear();
//
////                    chatEl.sendKeys("Thời gian: "+temp.getTime());
////                    chatEl.sendKeys(Keys.ENTER);
////                    chatEl.clear();
////                    Thread.sleep(100);
////                    chatEl = driver.findElement(By.cssSelector("#richInput"));
////                    chatEl.sendKeys("Lệnh: "+(temp.getType().equalsIgnoreCase("T")? "TĂNG" :"GIẢM"));
////                    Thread.sleep(100);
////                    chatEl.sendKeys(Keys.ENTER);
////                    chatEl.clear();
////                    chatEl = driver.findElement(By.cssSelector("#richInput"));
////                    chatEl.sendKeys("Tiền đặt: "+(temp.getPrice()));
////                    chatEl.sendKeys(Keys.ENTER);
//
//
//
//                    chatEl.sendKeys(keysPressed) ;
//
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//            } );
//
//            logger.log(Level.INFO, "ChatBotService -> Change {0}, " , new Object[]{temp.toString()});
//
//        };
//        zaloService.listenerCommandTrade(eventListener);
//    }
//    public void doLogOut() {
//
//    }
//    public void doLogin() {
//        try {
//            driver.get("https://chat.zalo.me");
//            WebDriverWait wait = new WebDriverWait(driver, 30);
//            Thread.sleep(1000);
//            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("app")));
//            String url = driver.getCurrentUrl();
//            if(url.startsWith("https://chat.zalo.me")) {
//                logger.log(Level.INFO, "ChatBotService -> is login!");
//            } else {
//                if(url.contains("id.zalo.me")) {
//                    List<WebElement> logoutEl = driver.findElements(By.cssSelector("div.bottom a[target='_blank']")).stream()
//                            .filter( k -> k.getText().equals("Đăng xuất")).collect(Collectors.toList());
//                    if(logoutEl.size() == 1) {
//                        // login with click on avatar
//                        WebElement avatar = driver.findElement(By.cssSelector("#app div.zLogin-layout.parentDisable  span.avatar")) ;
//                        avatar.click();
//                        logger.log(Level.INFO, "ChatBotService ->  login with click avt: {0}" , new Object[]{url});
//                    } else {
//                        // login with qr code
////                        [...document.querySelectorAll("li a[href='#']")].filter( k => k.textContent === 'Với Mã QR')[0].click()
//                        driver.findElements(By.cssSelector("li a[href='#']")).stream().filter( k -> k.getText().equalsIgnoreCase("VỚI MÃ QR")).forEach( z -> z.click());
//                        logger.log(Level.INFO, "ChatBotService -> login by QR Code: {0}" , new Object[]{url});
//                        this.listenerComamdTrade();
//                    }
//                } else {
//                    logger.log(Level.SEVERE, "ChatBotService -> cannot login with url: {0}" , new Object[]{url});
//                }
//            }
//        }
//        catch (Exception ex) {
//            logger.log(Level.SEVERE, "ChatBotService -> Erro Fail {0}, " , new Object[]{ex});
//        }
//    }
//}
