package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BonusesPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    public BonusesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) driver;
    }

    public void scrollToFirstBonus() throws InterruptedException {
        WebElement firstCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[contains(@class, 'bonus-card')])[1]")));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", firstCard);
        Thread.sleep(500);
    }

    public int clickLike() throws InterruptedException {
        WebElement likeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'reaction-btn--like')]")));
        js.executeScript("arguments[0].click();", likeBtn);
        Thread.sleep(1500);
        String text = likeBtn.findElement(By.xpath(".//span[contains(@class, 'reaction-count')]")).getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public int clickDislike() throws InterruptedException {
        WebElement dislikeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'reaction-btn--dislike')]")));
        js.executeScript("arguments[0].click();", dislikeBtn);
        Thread.sleep(1500);
        String text = dislikeBtn.findElement(By.xpath(".//span[contains(@class, 'reaction-count')]")).getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public String getPromocode() {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[contains(@class, 'bonus-card')])[1]//input[contains(@class, 'broker-promocode__input')]")));
        return input.getAttribute("value");
    }

    public void clickCopyPromocode() {
        WebElement copyBtn = driver.findElement(By.xpath("(//div[contains(@class, 'bonus-card')])[1]//button[contains(@class, 'broker-promocode__copy-btn')]"));
        js.executeScript("arguments[0].click();", copyBtn);
    }

    public void clickGetBonus() throws InterruptedException {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(@class, 'bonus-card')])[1]//span[contains(@class, 'bonus-card__btn')]")));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(2000);
    }


    private String bonusCardXPath(int index) {
        return "(//article[contains(@class, 'bonus-card')])[" + index + "]";
    }

    private By bonusCardAt(int index) {
        return By.xpath(bonusCardXPath(index));
    }

    private By likeBtnAt(int index) {
        return By.xpath(bonusCardXPath(index) + "//button[contains(@class, 'reaction-btn--like')]");
    }

    private By likeCountAt(int index) {
        return By.xpath(bonusCardXPath(index) + "//button[contains(@class, 'reaction-btn--like')]//span[contains(@class, 'reaction-count')]");
    }

    public void scrollToBonus(int index) throws InterruptedException {
        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(bonusCardAt(index)));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", card);
        Thread.sleep(500);
    }

    public int clickLikeOnBonus(int index) throws InterruptedException {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(likeBtnAt(index)));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(1500);
        return getLikeCount(index);
    }

    public int getLikeCount(int index) {
        WebElement span = wait.until(ExpectedConditions.presenceOfElementLocated(likeCountAt(index)));
        String text = span.getText().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    public boolean isLikeButtonActive(int index) {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(likeBtnAt(index)));
        String cls = btn.getAttribute("class");
        if (cls != null && cls.toLowerCase().contains("active")) return true;
        String pressed = btn.getAttribute("aria-pressed");
        return "true".equalsIgnoreCase(pressed);
    }
}