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

    public static class HomePage {
        private WebDriver driver;
        private WebDriverWait wait;

        // Локаторы (инкапсулированы, скрыты от тестов)
        private By bookmakersMenu = By.xpath("//a[contains(text(),'Букмекеры')]");
        private By searchInput = By.xpath("//input[@name='s']");
        private By searchResults = By.xpath("//article | //div[contains(@class, 'item')] | //div[contains(@class, 'card')]");

        private By newsMenu = By.xpath("//a[contains(text(),'Новости')]");
        private By bonusesMenu = By.xpath("//a[contains(text(),'Бонусы')]");
        private By articlesMenu = By.xpath("//a[contains(text(),'Статьи')]");
        private By citedBySection = By.xpath("//section[contains(@class, 'cited-by')]");
        private By externalLinks = By.xpath("//span[contains(@class, 'cited-by__item')]");
        private By acceptCookiesBtn = By.id("acceptCookies");

        // Конструктор
        public HomePage(WebDriver driver) {
            this.driver = driver;
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }

        // Методы действий
        public void open() {
            driver.get("https://tiu.ru/");
        }

        public void goToBookmakers() {
            wait.until(ExpectedConditions.elementToBeClickable(bookmakersMenu)).click();
        }

        public void searchFor(String query) {
            // 1. Ждем появления элемента в DOM
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));

            // 2. Принудительно скроллим к нему, чтобы он был по центру экрана
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);

            try {
                Thread.sleep(500); // Даем время на плавную прокрутку страницы
            } catch (InterruptedException e) {}

            // 3. Используем JS-клик, чтобы обойти любые баннеры и перекрытия
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);

            // 4. Очищаем и вводим текст
            input.clear();
            input.sendKeys(query);
            input.sendKeys(Keys.ENTER);
        }

        public void waitForSearchResults() {
            wait.until(ExpectedConditions.urlContains("?s="));
        }

        public int getSearchResultsCount() {
            List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(searchResults));
            return items.size();
        }


        public void goToNews() { wait.until(ExpectedConditions.elementToBeClickable(newsMenu)).click(); }
        public void goToBonuses() { wait.until(ExpectedConditions.elementToBeClickable(bonusesMenu)).click(); }
        public void goToArticles() { wait.until(ExpectedConditions.elementToBeClickable(articlesMenu)).click(); }

        public void acceptCookiesIfPresent() {
            try { driver.findElement(acceptCookiesBtn).click(); Thread.sleep(500); } catch (Exception ignored) {}
        }

        public void clickFirstCitedByPartner() throws InterruptedException {
            WebElement section = wait.until(ExpectedConditions.presenceOfElementLocated(citedBySection));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", section);
            Thread.sleep(1000);

            List<WebElement> links = driver.findElements(externalLinks);
            if (!links.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", links.get(0));
            }
        }
    }
}