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

    public void openSpecificArticle(String title) {
        WebElement article = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'" + title + "')]")));
        js.executeScript("arguments[0].click();", article);
    }

    public void rateArticle(int starIndex) throws InterruptedException {
        By starLocator = By.cssSelector(".post-rating-footer__star:nth-child(" + starIndex + ")");
        WebElement star = wait.until(ExpectedConditions.presenceOfElementLocated(starLocator));
        
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", star);
        Thread.sleep(1000);
        
        new Actions(driver).moveToElement(star).click().perform();
    }

    public void openRandomArticle() throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(@class, 'blog-card__title')]/a | //article//a")
        ));

        List<WebElement> articles = driver.findElements(
                By.xpath("//h3[contains(@class, 'blog-card__title')]/a | //article//a")
        );
        int randomIndex = new java.util.Random().nextInt(articles.size());
        WebElement randomArticle = articles.get(randomIndex);

        System.out.println("Выбрана случайная статья: " + randomArticle.getText());

        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", randomArticle);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", randomArticle);
    }

    public String getRatingNotificationText() {
        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='notification notification--success']")));
        return notification.getText();
    }

    public void clickReadMore() {
        WebElement readMore = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class, 'hero-news-card__button')]")));
        js.executeScript("arguments[0].click();", readMore);
    }

    public void clickAuthorProfile() {
        WebElement author = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class, 'hero-news-card__author')]")));
        js.executeScript("arguments[0].click();", author);
    }


    public void openFirstArticleInList() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'category-posts__list')]")));

        WebElement firstArticle = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("(//h3[contains(@class, 'blog-card__title')]/a)[1]")));

        String articleUrl = firstArticle.getAttribute("href");
        driver.get(articleUrl);
    }

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

    private By stars = By.xpath("//span[contains(@class, 'post-rating-footer__star')]");
    private By activeStars = By.xpath("//span[contains(@class, 'post-rating-footer__star') and contains(@class, 'active')]");
    private By authorPosition = By.xpath("//div[contains(@class, 'author-page__position')]");
    private By categoryButtons = By.xpath("//a[contains(@class, 'block-categories-row__button')]");
    private By articleTitles = By.xpath("//h3[contains(@class, 'blog-card__title')]");

    public void clickStar(int index) throws InterruptedException {
        By star = By.xpath("(//span[contains(@class, 'post-rating-footer__star')])[" + index + "]");
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(star));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        Thread.sleep(500);
        new Actions(driver).moveToElement(el).click().perform();
        Thread.sleep(1500);
    }

    public int getActiveStarCount() {
        return driver.findElements(activeStars).size();
    }

    public int getStarCount() {
        return driver.findElements(stars).size();
    }

    public String getAuthorBioText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(authorPosition));
        String text = el.getText();
        return text == null ? "" : text.trim();
    }

    public int getCategoryButtonCount() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(categoryButtons));
        return driver.findElements(categoryButtons).size();
    }

    public void clickCategoryByIndex(int index) throws InterruptedException {
        List<WebElement> btns = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(categoryButtons));
        WebElement target = btns.get(index);
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", target);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", target);
        Thread.sleep(1500);
    }

    public List<String> getVisibleArticleTitles() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(articleTitles));
        List<WebElement> els = driver.findElements(articleTitles);
        List<String> titles = new java.util.ArrayList<>();
        for (WebElement e : els) {
            String t = e.getText();
            if (t != null && !t.trim().isEmpty()) titles.add(t.trim());
        }
        return titles;
    }
}