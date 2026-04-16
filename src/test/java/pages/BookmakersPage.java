package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BookmakersPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By reviewButton = By.xpath("//a[contains(@class, 'broker-card__btn') and contains(@class, 'broker-card__btn--review')]");
    private By appInfoBlock = By.xpath("//a[contains(@class, 'broker-tabs__link') and contains(@href, '/apps/')]");
    private By ratingValue = By.xpath("//div[contains(@class, 'broker-card__rating')]//div[contains(@class, 'broker-card__detail-content')]/span");
    private By filterAllBtn = By.xpath("//button[@data-sort='all']");
    private By filterByRatingBtn = By.xpath("//button[@data-sort='rating']");

    public BookmakersPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openFirstBookmakerReview() {
        wait.until(ExpectedConditions.elementToBeClickable(reviewButton)).click();
    }

    public boolean isAppBlockDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(appInfoBlock)).isDisplayed();
    }

    public void clickFilterAll() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(filterAllBtn)).click();
        Thread.sleep(2000);
    }

    public void clickFilterByRating() throws InterruptedException {
        driver.findElement(filterByRatingBtn).click();
        Thread.sleep(2000);
    }

    public List<Double> getRatingsList() {
        List<Double> ratings = new ArrayList<>();
        List<WebElement> elements = driver.findElements(ratingValue);
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
}