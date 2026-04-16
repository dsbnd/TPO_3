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

    private By commentForm = By.xpath("//form[contains(@class, 'broker-review-form__comment')]");
    private By commentTextarea = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//textarea[@name='comment']");
    private By commentUsername = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//input[@type='text']");
    private By commentEmail = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//input[@type='email']");
    private By commentPhone = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//input[@type='tel']");
    private By commentCheckboxes = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//input[@type='checkbox']");
    private By commentSubmit = By.xpath("//form[contains(@class, 'broker-review-form__comment')]//button[contains(@class, 'form__btn')]");

    private void scrollToForm() throws InterruptedException {
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(commentForm));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", form);
        Thread.sleep(500);
    }

    private void fillCommentFieldsXPath(String name, String email, String phone, String text) throws InterruptedException {
        scrollToForm();
        WebElement textarea = wait.until(ExpectedConditions.presenceOfElementLocated(commentTextarea));
        if (text != null) {
            textarea.clear();
            textarea.sendKeys(text);
        }
        WebElement usr = driver.findElement(commentUsername);
        usr.clear();
        usr.sendKeys(name);
        WebElement em = driver.findElement(commentEmail);
        em.clear();
        em.sendKeys(email);
        WebElement ph = driver.findElement(commentPhone);
        js.executeScript("arguments[0].click();", ph);
        ph.sendKeys(phone);
    }


    private void clickCheckboxes() {
        java.util.List<WebElement> cbs = driver.findElements(commentCheckboxes);
        for (WebElement cb : cbs) {
            js.executeScript("arguments[0].click();", cb);
        }
    }

    private void clickSubmit() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(commentSubmit));
        js.executeScript("arguments[0].click();", btn);
    }

    public void submitCommentWithoutConsent(String name, String email, String phone, String text) throws InterruptedException {
        fillCommentFieldsXPath(name, email, phone, text);
        clickSubmit();
    }

    public void submitCommentWithBadEmail(String name, String badEmail, String phone, String text) throws InterruptedException {
        fillCommentFieldsXPath(name, badEmail, phone, text);
        clickCheckboxes();
        clickSubmit();
    }

    public void submitCommentEmpty(String name, String email, String phone) throws InterruptedException {
        fillCommentFieldsXPath(name, email, phone, null);
        clickCheckboxes();
        clickSubmit();
    }

    public boolean isCommentSubmittedSuccessfully() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            shortWait.until(d -> d.findElement(By.tagName("body")).getText().toLowerCase().contains("модераци"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getFieldValidationMessage(By fieldLocator) {
        WebElement el = driver.findElement(fieldLocator);
        Object msg = js.executeScript("return arguments[0].validationMessage;", el);
        return msg == null ? "" : msg.toString();
    }

    public String getEmailValidationMessage() {
        return getFieldValidationMessage(commentEmail);
    }

    public String getTextareaValidationMessage() {
        return getFieldValidationMessage(commentTextarea);
    }

    public String getCheckboxValidationMessage() {
        java.util.List<WebElement> cbs = driver.findElements(commentCheckboxes);
        if (cbs.isEmpty()) return "";
        Object msg = js.executeScript("return arguments[0].validationMessage;", cbs.get(0));
        return msg == null ? "" : msg.toString();
    }

    private By calendarInput = By.xpath("//input[contains(@class, 'news-calendar__input')]");
    private By calendarMonthLabel = By.xpath("//div[contains(@class, 'news-calendar__month-year')]");
    private By calendarNextBtn = By.xpath("//button[contains(@class, 'news-calendar__nav-btn--next')]");
    private By calendarClearBtn = By.xpath("//button[contains(@class, 'news-calendar__clear')]");
    private By newsItems = By.xpath("//article[contains(@class, 'news-item')]");

    public void openCalendar() throws InterruptedException {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(calendarInput));
        input.click();
        Thread.sleep(500);
    }

    public String getCurrentCalendarMonthLabel() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(calendarMonthLabel)).getText().trim();
    }

    public void switchCalendarToNextMonth() throws InterruptedException {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(calendarNextBtn));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(500);
    }

    public void resetDateFilter() throws InterruptedException {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(calendarClearBtn));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(1500);
    }

    public int getNewsCardsCount() {
        return driver.findElements(newsItems).size();
    }
}