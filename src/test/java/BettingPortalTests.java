import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
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

    @Test(description = "UC-03: Сортировка рейтинга букмекеров")
    public void testRatingSortingAndReset() throws InterruptedException {
        driver.get("https://tiu.ru/");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Букмекеры')]"))).click();

        By ratingLocator = By.xpath("//div[contains(@class, 'broker-card__rating')]//div[contains(@class, 'broker-card__detail-content')]/span");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Все')]"))).click();
        Thread.sleep(2000);
        List<Double> initialRatings = getRatingsSafe(ratingLocator);

        driver.findElement(By.xpath("//button[contains(.,'По рейтингу')]")).click();
        Thread.sleep(2000);
        List<Double> sortedRatings = getRatingsSafe(ratingLocator);
        Assert.assertNotEquals(initialRatings, sortedRatings, "Список не отсортировался!");

        driver.findElement(By.xpath("//button[contains(.,'Все')]")).click();
        Thread.sleep(2000);
        List<Double> finalRatings = getRatingsSafe(ratingLocator);
        Assert.assertEquals(finalRatings, initialRatings, "Рейтинг не вернулся в исходное состояние!");
    }

    private List<Double> getRatingsSafe(By locator) {
        List<Double> ratings = new ArrayList<>();
        List<WebElement> elements = driver.findElements(locator);
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                String text = element.getText().trim();
                if (!text.isEmpty()) {
                    ratings.add(Double.parseDouble(text));
                }
            }
        }
        return ratings;
    }

    @Test(description = "UC-06: Работа с комментариями (Неавторизованный пользователь)")
    public void testLeaveCommentAsGuest() throws InterruptedException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-private");
        options.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");
        WebDriver localDriver = new FirefoxDriver(options);
        WebDriverWait localWait = new WebDriverWait(localDriver, Duration.ofSeconds(10));

        try {
            localDriver.manage().window().maximize();
            localDriver.get("https://tiu.ru/");
            JavascriptExecutor js = (JavascriptExecutor) localDriver;

            WebElement newsMenu = localWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Новости')]")
            ));
            js.executeScript("arguments[0].click();", newsMenu);

            WebElement specificNews = localWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(text(),'Бубнов и Петржела предложили распустить сборную России')]")
            ));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", specificNews);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", specificNews);

            WebElement leaveCommentBtn = localWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(@href, '#review-form')]")
            ));
            js.executeScript("arguments[0].click();", leaveCommentBtn);

            Thread.sleep(2000);

            WebElement commentTextArea = localWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//textarea[@id='comment']")
            ));
            commentTextArea.clear();
            commentTextArea.sendKeys("Очень интересная новость! Согласен с экспертами.");

            localDriver.findElement(By.id("comment-username")).sendKeys("Иван Тестировщик");
            localDriver.findElement(By.id("comment-email")).sendKeys("test.ivan123@example.com");

            WebElement phoneInput = localDriver.findElement(By.id("comment-phone"));
            js.executeScript("arguments[0].click();", phoneInput);
            phoneInput.sendKeys("9991234567");

            WebElement agreeReviewsCheckbox = localDriver.findElement(By.name("acf[agree_reviews]"));
            js.executeScript("arguments[0].click();", agreeReviewsCheckbox);

            WebElement agreePersonalDataCheckbox = localDriver.findElement(By.xpath("//input[@name='acf[agree_personal_data]']"));
            js.executeScript("arguments[0].click();", agreePersonalDataCheckbox);

            WebElement submitBtn = localDriver.findElement(By.xpath("//input[@id='submit' and @value='Оставить комментарий']"));
            js.executeScript("arguments[0].click();", submitBtn);

            localWait.until(d -> {
                String pageText = d.findElement(By.tagName("body")).getText().toLowerCase();
                return pageText.contains("модераци") || pageText.contains("ошибк") || pageText.contains("попробуйт");
            });

            String finalPageText = localDriver.findElement(By.tagName("body")).getText().toLowerCase();

            boolean isSuccess = finalPageText.contains("модераци");
            boolean isAntiSpamError = finalPageText.contains("ошибк") || finalPageText.contains("попробуйт");
            Assert.assertTrue(isSuccess || isAntiSpamError,
                    "Сайт вообще никак не отреагировал на кнопку отправки комментария!");
        } finally {
            localDriver.quit();
        }
    }

    @Test(description = "UC-10: Оценка статьи")
    public void testArticleRating() throws InterruptedException {
        driver.get("https://tiu.ru/");

        WebElement articlesMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Статьи')]")
        ));
        articlesMenu.click();

        WebElement specificArticle = wait.until(ExpectedConditions.elementToBeClickable(

                By.xpath("//a[contains(text(),'Как скачать и установить приложение Винлайн на компьютер (Windows)?')]")
        ));
        specificArticle.click();

        By starLocator = By.cssSelector(".post-rating-footer__star:nth-child(4)");
        WebElement fourthStar = wait.until(ExpectedConditions.presenceOfElementLocated(starLocator));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", fourthStar);

        Thread.sleep(1000);

        Actions actions = new Actions(driver);
        actions.moveToElement(fourthStar).click().perform();

        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Спасибо')]")
        ));

        Assert.assertTrue(notification.isDisplayed(), "Уведомление с благодарностью не появилось на экране!");
        String notificationText = notification.getText();
        Assert.assertTrue(notificationText.contains("Спасибо за Вашу оценку") || notificationText.contains("Спасибо"),
                "Текст уведомления не совпадает! Фактический текст: " + notificationText);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}