import org.testng.Assert;
import org.testng.annotations.Test;
import pages.BookmakersPage;
import pages.HomePage;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HomePageTests extends BaseTest {

    @Test(description = "UC-1: Навигация по многоуровневому каталогу к приложению БК")
    public void testCatalog() {
        HomePage homePage = new HomePage(driver);
        BookmakersPage bookmakersPage = new BookmakersPage(driver);

        homePage.open();
        homePage.goToBookmakers();
        bookmakersPage.openFirstBookmakerReview();

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

    @Test(description = "UC-12: Каталог букмекеров содержит достаточное число контор")
    public void testBookmakersCatalogSize() {
        HomePage homePage = new HomePage(driver);
        BookmakersPage bookmakersPage = new BookmakersPage(driver);

        homePage.open();
        homePage.goToBookmakers();

        int count = bookmakersPage.getBookmakerCount();
        Assert.assertTrue(count >= 10, "В каталоге меньше 10 букмекеров! Найдено: " + count);
    }

    @Test(description = "UC-13: Поиск по несуществующему запросу показывает плашку 'не найдено'")
    public void testSearchNoResults() {
        HomePage homePage = new HomePage(driver);

        homePage.open();
        homePage.searchFor("xyzqq123notfound");
        homePage.waitForSearchResults();

        String decodedUrl = URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8);
        Assert.assertTrue(decodedUrl.contains("s="), "URL не содержит параметров поиска");
        Assert.assertTrue(homePage.isNoResultsPlaceholderDisplayed(),
                "Плашка 'ничего не найдено' не отображена!");
    }

    @Test(description = "UC-14: Сортировка по бонусу отличается от сортировки по рейтингу")
    public void testSortByBonusDiffersFromByRating() throws InterruptedException {
        HomePage homePage = new HomePage(driver);
        BookmakersPage bookmakersPage = new BookmakersPage(driver);

        homePage.open();
        homePage.goToBookmakers();

        bookmakersPage.clickFilterByRating();
        List<String> byRating = bookmakersPage.getBookmakerNamesList();

        bookmakersPage.clickFilterByBonus();
        List<String> byBonus = bookmakersPage.getBookmakerNamesList();

        Assert.assertFalse(byRating.isEmpty(), "Список по рейтингу пуст!");
        Assert.assertFalse(byBonus.isEmpty(), "Список по бонусу пуст!");
        Assert.assertNotEquals(byBonus, byRating,
                "Сортировка по бонусу совпала с сортировкой по рейтингу!");
    }
}