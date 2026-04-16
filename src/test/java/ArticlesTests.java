
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ArticlesPage;
import pages.HomePage;

import java.util.List;

public class ArticlesTests extends BaseTest {

    //не проходит
    @Test(description = "UC-9: Оценка статьи")
    public void testArticleRating() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        ArticlesPage articlesPage = new ArticlesPage(driver);

        homePage.open();
        homePage.goToArticles();

        // ВМЕСТО openSpecificArticle вызываем наш новый метод:
        articlesPage.openRandomArticle();

        // Ставим 3 звезды (можно тоже сделать рандом от 1 до 5, если хочешь!)
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
        
        articlesPage.openFirstArticleInList();
        
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

    @Test(description = "UC-24: Профиль автора содержит непустое описание")
    public void testAuthorBioIsNotEmpty() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        ArticlesPage articlesPage = new ArticlesPage(driver);

        homePage.open();
        homePage.goToArticles();

        articlesPage.clickReadMore();
        articlesPage.clickAuthorProfile();

        String bio = articlesPage.getAuthorBioText();
        Assert.assertFalse(bio.isEmpty(), "Описание автора пустое!");
        Assert.assertTrue(bio.length() >= 3, "Описание автора слишком короткое: '" + bio + "'");
    }

    @Test(description = "UC-25: Последовательное переключение категорий меняет список статей")
    public void testSequentialCategorySwitching() throws InterruptedException {
        ArticlesPage articlesPage = new ArticlesPage(driver);

        articlesPage.openWiki();
        int catCount = articlesPage.getCategoryButtonCount();
        Assert.assertTrue(catCount >= 2, "Недостаточно категорий для теста: " + catCount);

        articlesPage.clickCategoryByIndex(0);
        List<String> titlesA = articlesPage.getVisibleArticleTitles();

        articlesPage.openWiki();
        articlesPage.clickCategoryByIndex(1);
        List<String> titlesB = articlesPage.getVisibleArticleTitles();

        Assert.assertFalse(titlesA.isEmpty(), "Список статей категории #0 пуст!");
        Assert.assertFalse(titlesB.isEmpty(), "Список статей категории #1 пуст!");
        Assert.assertNotEquals(titlesB, titlesA,
                "Список статей не поменялся при смене категории!");
    }
}