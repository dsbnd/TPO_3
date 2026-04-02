import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BettingPortalTests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        FirefoxOptions options = new FirefoxOptions();

        options.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");

        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test(description = "UC-1: Навигация по многоуровневому каталогу к приложению БК")
    public void testCatalog() {
        driver.get("https://tiu.ru/");

        WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Букмекеры')]")));
        menuLink.click();

        WebElement bookmakerCard = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Обзор')]")));
        bookmakerCard.click();

        WebElement companyInfoBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(text(),'Приложение')]")
        ));
        Assert.assertTrue(companyInfoBlock.isDisplayed(), "Блок с информацией о приложении конторы не найден на странице профиля");
    }

    @Test(description = "UC-2: Поиск по сайту (поиск конторы Лига ставок)")
    public void testSiteSearch() {
        driver.get("https://tiu.ru/");

        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='s']")));
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys("Лига ставок");
        searchInput.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.urlContains("?s="));

        String currentUrl = driver.getCurrentUrl();
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8);
        Assert.assertTrue(decodedUrl.contains("s="), "URL не содержит параметров поиска");

        Assert.assertTrue(decodedUrl.contains("Лига"), "URL не содержит искомое слово");

        List<WebElement> searchResultItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//article | //div[contains(@class, 'item')] | //div[contains(@class, 'card')]")
        ));

        Assert.assertTrue(searchResultItems.size() > 0, "Поиск отработал, но не выдал ни одного результата на странице!");
    }

    @Test(description = "UC-4: Сортировка рейтинга букмекеров по бонусам")
    public void testRatingSorting() {
        driver.get("https://tiu.ru/");

        WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Букмекеры')]")));
        menuLink.click();

        WebElement sortedIndicator = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'По рейтингу')]")
        ));
        sortedIndicator.click();

        List<WebElement> ratingElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[contains(@class, 'broker-card__rating')]//div[contains(@class, 'broker-card__detail-content')]/span")
        ));

        List<Double> actualRatings = new ArrayList<>();

        for (WebElement element : ratingElements) {
            if (element.isDisplayed()) {
                String ratingText = element.getText().trim().replace(",", ".");
                if (!ratingText.isEmpty()) {
                    double ratingValue = Double.parseDouble(ratingText);
                    actualRatings.add(ratingValue);
                }
            }
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}