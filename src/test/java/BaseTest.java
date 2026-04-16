import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;
import java.util.List;

public class BaseTest {
    protected WebDriver driver;

    @BeforeClass
    public void setUp() {
        String browser = System.getProperty("browser", "chrome");
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setBinary("/usr/bin/chromium-browser");
                // chromeOptions.addArguments("--incognito"); // Можно добавить для стабильности
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");
                driver = new FirefoxDriver(firefoxOptions);
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}