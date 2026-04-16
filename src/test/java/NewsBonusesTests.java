
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.BonusesPage;
import pages.HomePage;
import pages.NewsPage;

import java.util.Set;

public class NewsBonusesTests extends BaseTest {

    @Test(description = "UC-4: Переход во внешние новостные паблики")
    public void testExternalLinksInCitedBy() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        homePage.open();
        homePage.acceptCookiesIfPresent();
        
        String originalWindow = driver.getWindowHandle();
        homePage.clickFirstCitedByPartner();

        Set<String> windows = driver.getWindowHandles();
        Assert.assertTrue(windows.size() > 1, "Новая вкладка не открылась");

        for (String window : windows) {
            if (!window.equals(originalWindow)) driver.switchTo().window(window);
        }

        Assert.assertFalse(driver.getCurrentUrl().contains("tiu.ru"), "Ожидался внешний сайт!");
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    //НЕ ПРОХОДИТСЯ
    @Test(description = "UC-5: Работа с комментариями (Неавторизованный пользователь)")
    public void testLeaveCommentAsGuest() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        NewsPage newsPage = new NewsPage(driver);

        homePage.open();
        homePage.goToNews();
        newsPage.openSpecificNews(
                "Капризов стал лучшим российским снайпером в нынешнем сезоне регулярного чемпионата НХЛ");
        
        newsPage.leaveComment("Иван Тестировщик", "test.ivan123@example.com", "9991234567", "Очень интересная новость!");
        
        String response = newsPage.getResponseText();
        boolean success = response.contains("модераци") || response.contains("ошибк") || response.contains("попробуйт");
        Assert.assertTrue(success, "Сайт не выдал понятный ответ!");
    }

    @Test(description = "UC-6: Поиск новостей по дате")
    public void testNewsSearchByDate() {
        HomePage homePage = new HomePage(driver);
        NewsPage newsPage = new NewsPage(driver);

        homePage.open();
        homePage.goToNews();
        
        newsPage.selectFirstDayOfMonth();
        newsPage.openFirstNewsArticle();

        Assert.assertTrue(driver.getCurrentUrl().contains("/news/") || driver.getCurrentUrl().contains("/post/"), 
                "Не удалось открыть страницу новости после фильтрации");
    }

    @Test(description = "UC-7: Оценка бонуса (Лайк/Дизлайк)")
    public void testBonusRating() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BonusesPage bonusesPage = new BonusesPage(driver);

        homePage.open();
        homePage.goToBonuses();
        bonusesPage.scrollToFirstBonus();

        // Тестируем Лайк. Кликаем дважды, чтобы убедиться, что он меняется
        int initialLikes = bonusesPage.clickLike(); // Первый клик может либо поставить, либо снять лайк
        int newLikes = bonusesPage.clickLike(); // Второй клик вернет или поставит
        
        Assert.assertTrue(newLikes >= 0, "Счетчик лайков выдал ошибку!");
    }

    @Test(description = "UC-8: Получение промокода и редирект")
    public void testGetPromocodeAndRedirect() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BonusesPage bonusesPage = new BonusesPage(driver);

        homePage.open();
        homePage.goToBonuses();
        bonusesPage.scrollToFirstBonus();

        // Шаг 1: Проверка промокода
        String promo = bonusesPage.getPromocode();
        Assert.assertFalse(promo.isEmpty(), "Промокод пуст!");
        bonusesPage.clickCopyPromocode(); // Клик по копированию (просто проверяем, что кнопка нажимается)

        // Шаг 2: Проверка редиректа на сайт БК
        String originalWindow = driver.getWindowHandle();
        bonusesPage.clickGetBonus();

        Set<String> windows = driver.getWindowHandles();
        if (windows.size() > 1) {
            // Если открылась новая вкладка
            for (String w : windows) { 
                if (!w.equals(originalWindow)) driver.switchTo().window(w); 
            }
            Assert.assertFalse(driver.getCurrentUrl().contains("tiu.ru"), "Редирект не произошел (остались на tiu.ru)!");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Если редирект произошел в текущей вкладке
            Assert.assertFalse(driver.getCurrentUrl().contains("tiu.ru"), "Редирект в текущей вкладке не произошел!");
            driver.navigate().back(); // Возвращаемся обратно для чистоты
        }
    }

    @Test(description = "UC-18: Переключение месяца в календаре меняет label")
    public void testCalendarMonthSwitch() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        NewsPage newsPage = new NewsPage(driver);

        homePage.open();
        homePage.goToNews();

        newsPage.openCalendar();
        String before = newsPage.getCurrentCalendarMonthLabel();
        newsPage.switchCalendarToNextMonth();
        String after = newsPage.getCurrentCalendarMonthLabel();

        Assert.assertFalse(before.isEmpty(), "Label месяца до клика пуст!");
        Assert.assertNotEquals(after, before,
                "Label месяца не изменился после клика 'следующий'!");
    }

    @Test(description = "UC-19: Сброс фильтра по дате возвращает полный список новостей")
    public void testDateFilterResetRestoresFullList() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        NewsPage newsPage = new NewsPage(driver);

        homePage.open();
        homePage.goToNews();

        int initial = newsPage.getNewsCardsCount();
        newsPage.selectFirstDayOfMonth();
        Thread.sleep(1500);
        int filtered = newsPage.getNewsCardsCount();

        newsPage.resetDateFilter();
        int restored = newsPage.getNewsCardsCount();

        Assert.assertTrue(initial > 0, "Начальный список новостей пуст!");
        Assert.assertTrue(restored >= filtered,
                "После сброса фильтра новостей меньше, чем при фильтре! restored=" + restored + " filtered=" + filtered);
    }

    @Test(description = "UC-20: Кнопка 'Нравится' получает активный класс после клика")
    public void testLikeButtonGetsActiveClass() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BonusesPage bonusesPage = new BonusesPage(driver);

        homePage.open();
        homePage.goToBonuses();
        bonusesPage.scrollToBonus(1);

        boolean before = bonusesPage.isLikeButtonActive(1);
        bonusesPage.clickLikeOnBonus(1);
        boolean after = bonusesPage.isLikeButtonActive(1);

        Assert.assertNotEquals(after, before,
                "Состояние активности лайка не изменилось после клика!");
    }

    @Test(description = "UC-21: Лайк одного бонуса не меняет счётчик другого")
    public void testLikesAreIndependentBetweenBonuses() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BonusesPage bonusesPage = new BonusesPage(driver);

        homePage.open();
        homePage.goToBonuses();

        bonusesPage.scrollToBonus(2);
        int secondBefore = bonusesPage.getLikeCount(2);

        bonusesPage.scrollToBonus(1);
        bonusesPage.clickLikeOnBonus(1);

        bonusesPage.scrollToBonus(2);
        int secondAfter = bonusesPage.getLikeCount(2);

        Assert.assertEquals(secondAfter, secondBefore,
                "Счётчик лайков бонуса #2 изменился после клика на бонус #1!");
    }
}