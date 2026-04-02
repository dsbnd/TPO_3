import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
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
import java.util.Set;

public class BettingPortalTestsChrome {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        driver = new ChromeDriver(options);
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

    @Test(description = "UC-4: Сортировка рейтинга букмекеров по бонусам")
    public void testRatingSorting() {
        driver.get("https://tiu.ru/");

        WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Букмекеры')]")));
        menuLink.click();

        WebElement sortedIndicator = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'По рейтингу')]")
        ));
        sortedIndicator.click();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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

        List<Double> sortedRatings = new ArrayList<>(actualRatings);
        sortedRatings.sort(Collections.reverseOrder());

        Assert.assertEquals(actualRatings, sortedRatings, "Рейтинги не отсортированы по убыванию");
    }
    @Test(description = "UC-06: Поиск новостей по дате")
    public void testNewsSearchByDate() {
        driver.get("https://tiu.ru/");

        WebElement newsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@class='header__nav-link' and contains(text(),'Новости')]")
        ));
        newsLink.click();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/news/"), "Не удалось перейти в раздел новостей. Текущий URL: " + currentUrl);

        WebElement calendarInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@class, 'news-calendar__input') or contains(@id, 'calendar') or contains(@placeholder, 'дата')]")
        ));
        calendarInput.click();

        WebElement dateToSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'news-calendar')]//div[contains(@class, 'news-calendar__day') and text()='1']")
        ));
        dateToSelect.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'news-list')]//a | //article//a | //div[@class='news-item']")
        ));

        Actions actions = new Actions(driver);

        WebElement firstNews = driver.findElement(
                By.xpath("(//div[contains(@class, 'news-list')]//a | //article//a | //div[@class='news-item'])[1]")
        );

        String newsTitle = firstNews.getText();

        actions.moveToElement(firstNews).click().perform();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1 | //h2 | //article | //div[contains(@class, 'news-detail')]")
        ));

        String newsPageUrl = driver.getCurrentUrl();
        Assert.assertTrue(newsPageUrl.contains("/news/") || newsPageUrl.contains("/post/"),
                "Не удалось открыть страницу новости");

        System.out.println("Успешно найден и открыта новость: " + newsTitle);
    }

    @Test(description = "UC-07: Оценка бонуса (лайки и дизлайки)")
    public void testBonusRating() {
        driver.get("https://tiu.ru/");

        WebElement bonusLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Бонусы')]")
        ));
        bonusLink.click();

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/bookmaker-bonuses"),
                "Не удалось перейти в раздел бонусов. Текущий URL: " + currentUrl);

        List<WebElement> bonusCards = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[contains(@class, 'bonus-card')]")
        ));
        Assert.assertTrue(bonusCards.size() > 0, "Карточки бонусов не найдены");

        WebElement firstBonus = bonusCards.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstBonus);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement likeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'reaction-btn--like')]")
        ));

        WebElement dislikeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'reaction-btn--dislike')]")
        ));

        WebElement likeCount = likeButton.findElement(By.xpath(".//span[contains(@class, 'reaction-count')]"));
        WebElement dislikeCount = dislikeButton.findElement(By.xpath(".//span[contains(@class, 'reaction-count')]"));

        String initialLikeText = likeCount.getText();
        String initialDislikeText = dislikeCount.getText();
        int initialLike = initialLikeText.isEmpty() ? 0 : Integer.parseInt(initialLikeText);
        int initialDislike = initialDislikeText.isEmpty() ? 0 : Integer.parseInt(initialDislikeText);

        System.out.println("Начальные значения - Лайки: " + initialLike + ", Дизлайки: " + initialDislike);

        boolean isLikeActive = likeButton.getAttribute("class").contains("reaction-btn--active");
        boolean isDislikeActive = dislikeButton.getAttribute("class").contains("reaction-btn--active");

        System.out.println("Состояние лайка (активен): " + isLikeActive);
        System.out.println("Состояние дизлайка (активен): " + isDislikeActive);

        if (isLikeActive) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String resetLikeText = likeCount.getText();
            initialLike = resetLikeText.isEmpty() ? 0 : Integer.parseInt(resetLikeText);
            System.out.println("После снятия лайка: " + initialLike);
        }

        if (isDislikeActive) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dislikeButton);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String resetDislikeText = dislikeCount.getText();
            initialDislike = resetDislikeText.isEmpty() ? 0 : Integer.parseInt(resetDislikeText);
            System.out.println("После снятия дизлайка: " + initialDislike);
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String afterLikeText = likeCount.getText();
        int afterLike = afterLikeText.isEmpty() ? 0 : Integer.parseInt(afterLikeText);
        Assert.assertEquals(afterLike, initialLike + 1,
                "Счетчик лайков не увеличился. Было: " + initialLike + ", стало: " + afterLike);
        System.out.println("После лайка: " + afterLike);

        Assert.assertTrue(likeButton.getAttribute("class").contains("reaction-btn--active"),
                "Кнопка лайка не стала активной после нажатия");

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);
        System.out.println("Повторный клик по лайку (отмена)");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String afterCancelText = likeCount.getText();
        int afterCancel = afterCancelText.isEmpty() ? 0 : Integer.parseInt(afterCancelText);
        Assert.assertEquals(afterCancel, initialLike,
                "Счетчик лайков не вернулся к исходному. Должно быть: " + initialLike + ", стало: " + afterCancel);
        System.out.println("После отмены лайка: " + afterCancel);

        Assert.assertFalse(likeButton.getAttribute("class").contains("reaction-btn--active"),
                "Кнопка лайка осталась активной после отмены");

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dislikeButton);
        System.out.println("Клик по кнопке 'Не нравится' выполнен");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String afterDislikeText = dislikeCount.getText();
        int afterDislike = afterDislikeText.isEmpty() ? 0 : Integer.parseInt(afterDislikeText);
        Assert.assertEquals(afterDislike, initialDislike + 1,
                "Счетчик дизлайков не увеличился. Было: " + initialDislike + ", стало: " + afterDislike);
        System.out.println("После дизлайка: " + afterDislike);

        Assert.assertTrue(dislikeButton.getAttribute("class").contains("reaction-btn--active"),
                "Кнопка дизлайка не стала активной после нажатия");

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dislikeButton);
        System.out.println("Повторный клик по дизлайку (отмена)");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String afterCancelDislikeText = dislikeCount.getText();
        int afterCancelDislike = afterCancelDislikeText.isEmpty() ? 0 : Integer.parseInt(afterCancelDislikeText);
        Assert.assertEquals(afterCancelDislike, initialDislike,
                "Счетчик дизлайков не вернулся к исходному. Должно быть: " + initialDislike + ", стало: " + afterCancelDislike);
        System.out.println("После отмены дизлайка: " + afterCancelDislike);

        Assert.assertFalse(dislikeButton.getAttribute("class").contains("reaction-btn--active"),
                "Кнопка дизлайка осталась активной после отмены");
    }

    @Test(description = "UC-08: Получение промокода")
    public void testGetPromocodeSimple() {
        driver.get("https://tiu.ru/bookmaker-bonuses/");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'bonus-card')]")
        ));

        WebElement firstCard = driver.findElement(By.xpath("(//div[contains(@class, 'bonus-card')])[1]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstCard);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement promocodeInput = driver.findElement(
                By.xpath("(//div[contains(@class, 'bonus-card')])[1]//input[contains(@class, 'broker-promocode__input')]")
        );
        String promocode = promocodeInput.getAttribute("value");
        System.out.println("Промокод: " + promocode);
        Assert.assertFalse(promocode.isEmpty(), "Промокод не найден");

        WebElement copyButton = driver.findElement(
                By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'broker-promocode__copy-btn')]")
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", copyButton);
        System.out.println("Клик по кнопке 'Копировать' выполнен");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            WebElement successIndicator = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'success-message')] | " +
                            "//span[contains(@class, 'copied')] | " +
                            "//div[contains(text(), 'скопирован')] | " +
                            "//*[contains(@class, 'checkmark')] | " +
                            "//*[local-name()='svg' and contains(@class, 'check')]")
            ));

            if (successIndicator.isDisplayed()) {
                System.out.println("Зеленая галочка/уведомление о копировании появилось");
            }
        } catch (Exception e) {
            System.out.println("Визуальное уведомление о копировании не найдено, но это может быть особенностью сайта");
        }

        String originalWindow = driver.getWindowHandle();
        String originalUrl = driver.getCurrentUrl();
        System.out.println("Текущий URL до клика: " + originalUrl);

        WebElement getBonusButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[contains(@class, 'bonus-card')])[1]//span[contains(@class, 'bonus-card__btn')]")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", getBonusButton);
        System.out.println("Кнопка 'Получить бонус' нажата");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<String> windows = driver.getWindowHandles();

        if (windows.size() > 1) {
            for (String window : windows) {
                if (!window.equals(originalWindow)) {
                    driver.switchTo().window(window);
                    break;
                }
            }

            String newUrl = driver.getCurrentUrl();
            System.out.println("Открыта новая вкладка с URL: " + newUrl);

            Assert.assertFalse(newUrl.contains("tiu.ru"),
                    "Открылась страница tiu.ru, а ожидался внешний сайт. URL: " + newUrl);

            Assert.assertTrue(newUrl.startsWith("http"), "Невалидный URL: " + newUrl);

            System.out.println("Успешно открыт внешний сайт: " + newUrl);

            driver.close();
            driver.switchTo().window(originalWindow);
            System.out.println("Новая вкладка закрыта, возврат к исходной");

        } else {
            String newUrl = driver.getCurrentUrl();

            if (!newUrl.equals(originalUrl)) {
                System.out.println("Произошел редирект на: " + newUrl);

                Assert.assertFalse(newUrl.contains("tiu.ru"),
                        "Редирект на страницу tiu.ru, а ожидался внешний сайт. URL: " + newUrl);

                System.out.println("Успешный редирект на внешний сайт: " + newUrl);

                driver.navigate().back();
                System.out.println("Возврат на предыдущую страницу");
            } else {
                Assert.fail("Не открылась новая вкладка и не произошло редиректа на внешний сайт");
            }
        }
    }
    @Test(description = "UC-10: Просмотр информации об авторе статьи")
    public void testAuthorProfile() {
        driver.get("https://tiu.ru/");

        WebElement articlesLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Статьи')]")
        ));
        articlesLink.click();

        WebElement readMore = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Подробнее')]")
        ));
        readMore.click();

        WebElement author = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class, 'hero-news-card__author')]")
        ));
        author.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'category-posts__list')]")
        ));

        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//h3[contains(@class, 'blog-card__title')]/a)[1]")
        ));

        Actions actions = new Actions(driver);
        actions.moveToElement(firstArticle).click().perform();

        System.out.println("Тест UC-10 успешно завершен");
    }
    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}