package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ArticlesPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    public ArticlesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) driver;
    }

    // --- Для UC-9 (Оценка) ---
    public void openSpecificArticle(String title) {
        WebElement article = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'" + title + "')]")));
        js.executeScript("arguments[0].click();", article);
    }

    public void rateArticle(int starIndex) throws InterruptedException {
        // Ищем нужную звезду (например, 4-ю)
        By starLocator = By.cssSelector(".post-rating-footer__star:nth-child(" + starIndex + ")");
        WebElement star = wait.until(ExpectedConditions.presenceOfElementLocated(starLocator));
        
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", star);
        Thread.sleep(1000); // Ждем плавную прокрутку
        
        new Actions(driver).moveToElement(star).click().perform();
    }

    public String getRatingNotificationText() {
        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Спасибо')]")));
        return notification.getText();
    }

    // --- Для UC-10 (Автор) ---
    public void clickReadMore() {
        WebElement readMore = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Подробнее')]")));
        js.executeScript("arguments[0].click();", readMore);
    }

    public void clickAuthorProfile() {
        WebElement author = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class, 'hero-news-card__author')]")));
        js.executeScript("arguments[0].click();", author);
    }

    public void openFirstArticleInList() throws InterruptedException {
        // Ждем список статей (универсальный локатор и для авторов, и для категорий)
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'category-posts__list')]")));
                
        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//h3[contains(@class, 'blog-card__title')]/a)[1] | (//div[contains(@class, 'category-posts__list')]//article//a)[1]")));
        
        js.executeScript("arguments[0].scrollIntoView(true);", firstArticle);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", firstArticle);
    }

    // --- Для UC-11 (Категории) ---
    public void openWiki() {
        driver.get("https://tiu.ru/wiki/");
    }

    public String clickFirstCategoryAndGetName() throws InterruptedException {
        List<WebElement> categoryBtns = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//div[contains(@class, 'block-categories-row')]//a[contains(@class, 'block-categories-row__button')]")));
        
        WebElement selectedCategory = categoryBtns.get(0);
        String categoryName = selectedCategory.getText();
        
        js.executeScript("arguments[0].scrollIntoView(true);", selectedCategory);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", selectedCategory);
        
        return categoryName;
    }
}