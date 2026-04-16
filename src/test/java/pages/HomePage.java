package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By bookmakersMenu = By.cssSelector("a.header__nav-link[href*='bookmaker-ratings']");
    private By searchInput = By.xpath("//input[@name='s']");
    private By searchResults = By.xpath("//article | //div[contains(@class, 'item')] | //div[contains(@class, 'card')]");

    private By newsMenu = By.cssSelector("a.header__nav-link[href*='/news/']");
    private By bonusesMenu = By.cssSelector("a.header__nav-link[href*='bookmaker-bonuses']");
    private By articlesMenu = By.cssSelector("a.header__nav-link[href*='/wiki/']");
    private By citedBySection = By.xpath("//section[contains(@class, 'cited-by')]");
    private By externalLinks = By.xpath("//span[contains(@class, 'cited-by__item')]");
    private By acceptCookiesBtn = By.id("acceptCookies");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open() {
        driver.get("https://tiu.ru/");
    }

    public void goToBookmakers() {
        wait.until(ExpectedConditions.elementToBeClickable(bookmakersMenu)).click();
    }

    public void searchFor(String query) {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);

        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.ENTER);
    }

    public void waitForSearchResults() {
        wait.until(ExpectedConditions.urlContains("?s="));
    }

    public int getSearchResultsCount() {
        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(searchResults));
        return items.size();
    }


    public void goToNews() { wait.until(ExpectedConditions.elementToBeClickable(newsMenu)).click(); }
    public void goToBonuses() { wait.until(ExpectedConditions.elementToBeClickable(bonusesMenu)).click(); }
    public void goToArticles() { wait.until(ExpectedConditions.elementToBeClickable(articlesMenu)).click(); }

    public void acceptCookiesIfPresent() {
        try { driver.findElement(acceptCookiesBtn).click(); Thread.sleep(500); } catch (Exception ignored) {}
    }

    public void clickFirstCitedByPartner() throws InterruptedException {
        WebElement section = wait.until(ExpectedConditions.presenceOfElementLocated(citedBySection));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", section);
        Thread.sleep(1000);

        List<WebElement> links = driver.findElements(externalLinks);
        if (!links.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", links.get(0));
        }
    }

    private By noResultsPlaceholder = By.xpath("//*[contains(@class, 'search-page__empty-title') or contains(@class, 'search-page__empty')]");

    public boolean isNoResultsPlaceholderDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(noResultsPlaceholder)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}