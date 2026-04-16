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

    // --- Для UC-7 ---
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

    // --- Для UC-8 ---
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
        Thread.sleep(2000); // Ждем редирект/новую вкладку
    }
}