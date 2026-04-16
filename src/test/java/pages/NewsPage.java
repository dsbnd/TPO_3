package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NewsPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    public NewsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver;
    }

    // UC-6: Календарь
    public void selectFirstDayOfMonth() {
        WebElement calendarInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@class, 'news-calendar__input')]")));
        calendarInput.click();

        WebElement dayOne = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'news-calendar')]//div[contains(@class, 'news-calendar__day') and text()='1']")));
        dayOne.click();
    }

    public void openFirstNewsArticle() {
        WebElement firstNews = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[contains(@class, 'news-list')]//a | //article//a | //div[@class='news-item'])[1]")));
        new Actions(driver).moveToElement(firstNews).click().perform();
    }

    // UC-5: Бронебойный комментарий
    public void openSpecificNews(String newsTitle) throws InterruptedException {
        WebElement specificNews = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(),'" + newsTitle + "')]")));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", specificNews);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", specificNews);
    }

    public void leaveComment(String name, String email, String phoneText, String commentText) throws InterruptedException {
        WebElement leaveCommentBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href, '#review-form')]")));
        js.executeScript("arguments[0].click();", leaveCommentBtn);
        Thread.sleep(2000);

        // Используем гибридный метод, который мы отладили!
        WebElement commentField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//textarea[@id='comment']")));
        commentField.clear(); commentField.sendKeys(commentText);

        driver.findElement(By.id("comment-username")).sendKeys(name);
        driver.findElement(By.id("comment-email")).sendKeys(email);

        WebElement phoneField = driver.findElement(By.id("comment-phone"));
        js.executeScript("arguments[0].click();", phoneField);
        phoneField.sendKeys(phoneText);

        WebElement cb1 = driver.findElement(By.name("acf[agree_reviews]"));
        WebElement cb2 = driver.findElement(By.xpath("//input[@name='acf[agree_personal_data]']"));
        js.executeScript("arguments[0].click();", cb1);
        js.executeScript("arguments[0].click();", cb2);

        WebElement submitBtn = driver.findElement(By.xpath("//input[@id='submit']"));
        js.executeScript("arguments[0].click();", submitBtn);
    }

    public String getResponseText() {
        wait.until(d -> {
            String text = d.findElement(By.tagName("body")).getText().toLowerCase();
            return text.contains("модераци") || text.contains("ошибк") || text.contains("попробуйт");
        });
        return driver.findElement(By.tagName("body")).getText().toLowerCase();
    }
}