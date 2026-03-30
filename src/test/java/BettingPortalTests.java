import org.openqa.selenium.By;
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

        // Указываем путь к реальному бинарнику внутри Snap (стандартный путь для Ubuntu/Kubuntu)
        // Если вдруг не сработает, попробуй поменять на "/usr/bin/firefox" или "/snap/bin/firefox"
        options.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");

        // Передаем опции в драйвер при создании
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();

        wait=new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test(description="UC-1: Навигация по многоуровневому каталогу к приложению БК")
    public void testCatalog(){
        driver.get("https://tiu.ru/");

        WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Букмекеры')]")));
        menuLink.click();

        WebElement bookmakerCard=wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Обзор')]")));
        bookmakerCard.click();

        WebElement companyInfoBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(text(),'Приложение')]")
        ));
        Assert.assertTrue(companyInfoBlock.isDisplayed(), "Блок с информацией о приложении конторы не найден на странице профиля");
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

        // 2. ИСПОЛЬЗУЕМ visibilityOfAllElementsLocatedBy вместо presence!
        // Это отсечет все скрытые мобильные дубликаты
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