import org.testng.Assert;
import org.testng.annotations.Test;
import pages.BookmakersPage;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HomePageTests extends BaseTest { // Наследуем настройки драйвера

    @Test(description = "UC-1: Навигация по многоуровневому каталогу к приложению БК")
    public void testCatalog() {
        // Инициализируем страницы
        HomePage homePage = new HomePage(driver);
        BookmakersPage bookmakersPage = new BookmakersPage(driver);

        // Логика теста читается как книга
        homePage.open();
        homePage.goToBookmakers();
        bookmakersPage.openFirstBookmakerReview();

        // Проверка
        Assert.assertTrue(bookmakersPage.isAppBlockDisplayed(), 
                "Блок с информацией о приложении конторы не найден!");
    }

    @Test(description = "UC-2: Поиск по сайту (поиск конторы Лига ставок)")
    public void testSiteSearch() {
        HomePage homePage = new HomePage(driver);

        homePage.open();
        homePage.searchFor("Лига ставок");
        homePage.waitForSearchResults();

        String decodedUrl = URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8);
        Assert.assertTrue(decodedUrl.contains("s="), "URL не содержит параметров поиска");
        Assert.assertTrue(decodedUrl.contains("Лига"), "URL не содержит искомое слово");

        Assert.assertTrue(homePage.getSearchResultsCount() > 0, 
                "Поиск отработал, но не выдал ни одного результата!");
    }

    @Test(description = "UC-03: Сортировка рейтинга букмекеров")
    public void testRatingSortingAndReset() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BookmakersPage bookmakersPage = new BookmakersPage(driver);

        homePage.open();
        homePage.goToBookmakers();

        bookmakersPage.clickFilterAll();
        List<Double> initialRatings = bookmakersPage.getRatingsList();

        bookmakersPage.clickFilterByRating();
        List<Double> sortedRatings = bookmakersPage.getRatingsList();
        
        Assert.assertNotEquals(initialRatings, sortedRatings, "Список не отсортировался!");

        bookmakersPage.clickFilterAll();
        List<Double> finalRatings = bookmakersPage.getRatingsList();
        
        Assert.assertEquals(finalRatings, initialRatings, "Рейтинг не вернулся в исходное состояние!");
    }
}