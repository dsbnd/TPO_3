
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ArticlesPage;
import pages.HomePage;

public class ArticlesTests extends BaseTest {

    //не проходит
    @Test(description = "UC-9: Оценка статьи")
    public void testArticleRating() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        ArticlesPage articlesPage = new ArticlesPage(driver);

        homePage.open();
        homePage.goToArticles();
        
        articlesPage.openSpecificArticle("Футбольные номера: история, значения и легенды игры");
        
        // Ставим 4 звезды
        articlesPage.rateArticle(3);
        
        String notification = articlesPage.getRatingNotificationText();
        Assert.assertTrue(notification.contains("Спасибо"), 
                "Уведомление об оценке не появилось или текст не совпадает!");
    }

    //не проходит
    @Test(description = "UC-10: Просмотр информации об авторе статьи")
    public void testAuthorProfile() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        ArticlesPage articlesPage = new ArticlesPage(driver);

        homePage.open();
        homePage.goToArticles();
        
        articlesPage.clickReadMore();
        articlesPage.clickAuthorProfile();
        
        // Кликаем по первой статье автора
        articlesPage.openFirstArticleInList();
        
        // Проверяем, что URL изменился и мы внутри раздела статей
        Assert.assertTrue(driver.getCurrentUrl().contains("/wiki/"), 
                "Не удалось открыть статью автора");
    }

    @Test(description = "UC-11: Поиск статей по категориям")
    public void testSearchArticlesByCategory() throws InterruptedException {
        ArticlesPage articlesPage = new ArticlesPage(driver);

        articlesPage.openWiki(); // Прямой переход по аналогии с твоим старым кодом
        String currentUrl = driver.getCurrentUrl();
        
        articlesPage.clickFirstCategoryAndGetName();
        Thread.sleep(2000); // Даем время на загрузку страницы категории
        
        String categoryUrl = driver.getCurrentUrl();
        Assert.assertTrue(categoryUrl.contains("/wiki/") && !categoryUrl.equals(currentUrl),
                "Не удалось перейти на страницу категории");
        
        articlesPage.openFirstArticleInList();
        Thread.sleep(1500); // Даем время на открытие статьи
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/wiki/"), 
                "Не удалось открыть страницу статьи из категории");
    }
}