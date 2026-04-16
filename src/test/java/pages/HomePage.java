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

    // Локаторы
    private By bookmakersMenu = By.xpath("//a[contains(text(),'Букмекеры')]");
    
    // БЕРЕМ СТРОГО ПЕРВУЮ СТРОКУ ПОИСКА (в шапке сайта), чтобы не улетать на Y=4001
    private By searchInput = By.xpath("(//input[@name='s'])[1]"); 
    
    private By searchResults = By.xpath("//article | //div[contains(@class, 'item')] | //div[contains(@class, 'card')]");

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
        // 1. Ждем появления элемента в DOM
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
        
        // 2. Принудительно скроллим к нему, чтобы он был по центру экрана
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);
        
        try {
            Thread.sleep(500); // Даем время на плавную прокрутку страницы
        } catch (InterruptedException e) {}

        // 3. Используем JS-клик, чтобы обойти любые баннеры и перекрытия
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        
        // 4. Очищаем и вводим текст
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
}