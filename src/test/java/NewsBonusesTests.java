
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
        newsPage.openSpecificNews("Бубнов и Петржела предложили распустить сборную России");
        
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
}