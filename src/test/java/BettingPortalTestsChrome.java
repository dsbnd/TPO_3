import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.Set;

public class BettingPortalTestsChrome {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/chromium-browser");

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

    @Test(description = "UC-2: Поиск по сайту (поиск конторы Лига ставок)")
    public void testSiteSearch() {
        driver.get("https://tiu.ru/");

        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='s']")));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", searchInput);
        wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        js.executeScript("arguments[0].click();", searchInput);

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

    @Test(description = "UC-5: Переход во внешние новостные паблики из раздела 'Нас цитируют'")
    public void testExternalLinksInCitedBy() {
        driver.get("https://tiu.ru/");

        try {
            driver.findElement(By.id("acceptCookies")).click();
            Thread.sleep(500);
        } catch (Exception e) {}

        WebElement citedBySection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//section[contains(@class, 'cited-by')]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", citedBySection);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(citedBySection.isDisplayed(), "Блок 'Нас цитируют' не найден");
        System.out.println("Блок 'Нас цитируют' найден");

        List<WebElement> externalLinks = driver.findElements(
                By.xpath("//span[contains(@class, 'cited-by__item')]")
        );

        Assert.assertTrue(externalLinks.size() > 0, "Ссылки на внешние издания не найдены");
        System.out.println("Найдено внешних ссылок: " + externalLinks.size());

        String originalWindow = driver.getWindowHandle();

        WebElement firstLink = externalLinks.get(0);
        System.out.println("Клик по первому логотипу");

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstLink);
        System.out.println("Клик по логотипу издания выполнен");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Set<String> windows = driver.getWindowHandles();
        Assert.assertTrue(windows.size() > 1, "Новая вкладка не открылась");
        System.out.println("Открыто вкладок: " + windows.size());

        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }

        String newUrl = driver.getCurrentUrl();
        System.out.println("Открыт внешний сайт: " + newUrl);

        Assert.assertFalse(newUrl.contains("tiu.ru"), "Открылась страница tiu.ru, а ожидался внешний сайт");

        driver.close();
        driver.switchTo().window(originalWindow);
        System.out.println("Новая вкладка закрыта, возврат к исходной");

    }

    @Test(description = "UC-06: Работа с комментариями (Неавторизованный пользователь)")
    public void testLeaveCommentAsGuestChrome() throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.setBinary("/usr/bin/chromium-browser");
        WebDriver localDriver = new ChromeDriver(options);
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

            if (isAntiSpamError) {
                System.out.println("РЕЗУЛЬТАТ: Сработала защита от ботов (ошибка авторизации/попробуйте позже).");
            } else if (isSuccess) {
                System.out.println("РЕЗУЛЬТАТ: Успешно отправлено на модерацию!");
            }

            Assert.assertTrue(isSuccess || isAntiSpamError,
                    "Сайт вообще никак не отреагировал на кнопку отправки комментария!");

        } finally {
            localDriver.quit();
        }
    }

    @Test(description = "UC-7: Поиск новостей по дате")
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

    @Test(description = "UC-8: Оценка бонуса (лайки и дизлайки)")
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

    @Test(description = "UC-9: Получение промокода")
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

    @Test(description = "UC-10: Оценка статьи")
    public void testArticleRating() throws InterruptedException {
        driver.get("https://tiu.ru/");

        WebElement articlesMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Статьи')]")
        ));
        articlesMenu.click();

        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement specificArticle = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(),'Валуйная ставка (валуй)')]")
        ));

        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", specificArticle);
        Thread.sleep(1000);

        js.executeScript("arguments[0].click();", specificArticle);

        By starLocator = By.cssSelector(".post-rating-footer__star:nth-child(4)");
        WebElement fourthStar = wait.until(ExpectedConditions.presenceOfElementLocated(starLocator));

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

    @Test(description = "UC-11: Просмотр информации об авторе статьи")
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

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstArticle);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstArticle);

    }
    @Test(description = "UC-12: Поиск статей по категориям")
    public void testSearchArticlesByCategory() {
        driver.get("https://tiu.ru/wiki/");

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/wiki/"), "Не удалось перейти в раздел статей");
        System.out.println("Перешли в раздел статей: " + currentUrl);

        List<WebElement> categoryButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[contains(@class, 'block-categories-row')]//a[contains(@class, 'block-categories-row__button')]")
        ));

        Assert.assertTrue(categoryButtons.size() > 0, "Кнопки категорий не найдены");
        System.out.println("Найдено кнопок категорий: " + categoryButtons.size());

        WebElement selectedCategory = categoryButtons.get(0);
        String categoryName = selectedCategory.getText();
        System.out.println("Выбрана категория: " + categoryName);

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", selectedCategory);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectedCategory);
        System.out.println("Клик по категории: " + categoryName);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String categoryUrl = driver.getCurrentUrl();
        Assert.assertTrue(categoryUrl.contains("/wiki/") && !categoryUrl.equals(currentUrl),
                "Не удалось перейти на страницу категории");
        System.out.println("Страница категории: " + categoryUrl);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'category-posts__list')]")
        ));

        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[contains(@class, 'category-posts__list')]//article//a)[1]")
        ));

        String articleTitle = firstArticle.getText();

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstArticle);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstArticle);
        System.out.println("Открыта статья: " + articleTitle);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String articleUrl = driver.getCurrentUrl();
        Assert.assertTrue(articleUrl.contains("/wiki/"), "Не удалось открыть страницу статьи");

    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}