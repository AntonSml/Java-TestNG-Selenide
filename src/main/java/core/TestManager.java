package core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.ITestContext;
import org.testng.Reporter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static core.Browser.*;


/**
 * Class TestManager to be instantiated in a test class as TestManager manager = new TestManager(); <br>
 * To use Selenium in tests and launch a browser need to call manager.configure() from inside a method <br>
 * TestManager initializes test parameters from Suite XML, or sets default if any of the parameters is not available <br>
 * Takes care of Selenide configuration <br>
 * Manages URLs based on provided project and language parameters
 */
public class TestManager {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(TestManager.class);

    public static final String RESOURCES_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    private static final String WEBDRIVERS_PATH = RESOURCES_PATH + File.separator + "webdrivers";
    private static ITestContext testContext;


    private String url = "https://petstore.swagger.io";
    private Map<String, WebDriver> webDrivers = new HashMap<>();
    private String currentWebDriver;
    private Browser browser = CHROME;
    public static final boolean HEADLESS = true;

    //region Basics

    /**
     * Instantiates a new TestManager with test parameters
     * Does NOT launch Selenium at this point
     */
    public TestManager() {
        testContext = Reporter.getCurrentTestResult().getTestContext();
        testContext.setAttribute("manager", this);
    }

    /**
     * TestManager configuration with test context and webdriver</br>
     * What this method does:</br>
     * 1. Sets up browser specifics and selects platform-speficic webdriver binary</br>
     * 2. Stores this instance of manager in ITestContext</br>
     * 3. Launches specified or default webdriver</br>
     * 4. Retrieves starting urls for frontend, backend, etc. based on project settings</br>
     *
     * @return - this instance of TestManager
     */
    public TestManager configure() {
        readBrowserOverride();
        setDriversPaths();
        setupLogging();
        updateSelenideConfiguration();
        startDriver();
        return this;
    }

    public TestManager configure(Boolean headless) {
        if (headless) browser = CHROME_HEADLESS;
        return configure();
    }

    /**
     * returns parameter mentioned in suite xml or null
     *
     * @param key - string value for parameter key
     * @return parameter value
     */
    public String getTestParameter(String key) {
        return testContext.getCurrentXmlTest().getParameter(key);
    }

    /**
     * Get instance of test manager from other objects and reporter with no access to testmanager variable.
     *
     * @return the test manager
     */
    public static TestManager getActualInstance() {
        if (testContext == null || testContext.getAttribute("manager") == null) {
            log.error("Calling getActualInstance() on empty object. TestManager is not initialized!");
            return null;
        }
        return (TestManager) testContext.getAttribute("manager");
    }

    /**
     * Get the class name of the test that is running now
     *
     * @return
     */
    public String getActualTestName() {
        for (StackTraceElement o : new Exception().getStackTrace()) {
            if (!o.toString().startsWith("core") && !o.toString().startsWith("pages") && !o.toString().startsWith("data") && !o.toString().startsWith("reporter"))
                return o.getFileName().split("\\.")[0];
        }
        return null;
    }


    private void readBrowserOverride() {
        // Browser override by env var
        String browserOverride = System.getProperty("browser");
        String testSiteAddressOverride = System.getProperty("test.url");
        List<String> neverOverride = Arrays.asList("headless", "htmlunit", "mobile");
        if (browserOverride != null && !browserOverride.isEmpty() && !neverOverride.contains(getBrowser().toString())) {
            browser = Browser.findByString(browserOverride.toLowerCase());
        }
        if (testSiteAddressOverride != null && !testSiteAddressOverride.isEmpty()) {
            url = testSiteAddressOverride;
        }
    }

    private void setDriversPaths() {
        if (System.getProperty("os.name").contains("OS X")) {
            System.setProperty("webdriver.chrome.driver", WEBDRIVERS_PATH + File.separator + "mac/chromedriver");
            System.setProperty("webdriver.gecko.driver", WEBDRIVERS_PATH + File.separator + "mac/geckodriver");
        } else if (System.getProperty("os.name").contains("Linux")) {
            System.setProperty("webdriver.chrome.driver", WEBDRIVERS_PATH + File.separator + "linux/chromedriver");
            System.setProperty("webdriver.gecko.driver", WEBDRIVERS_PATH + File.separator + "linux/geckodriver");
        } else {
            System.setProperty("webdriver.chrome.driver", WEBDRIVERS_PATH + File.separator + "windows/chromedriver.exe");
            System.setProperty("webdriver.gecko.driver", WEBDRIVERS_PATH + File.separator + "windows/geckodriver.exe");
        }
    }

    private void setupLogging() {
        // Disable global Selenide logging
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.OFF);

        // Disable ChromeDriver logging
        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");

        // Disable Gecko logging
        if (Configuration.browser == "firefox")
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
    }

    private void updateSelenideConfiguration() {
        Configuration.savePageSource = false;
        Configuration.startMaximized = false;
        Configuration.screenshots = false;
        Configuration.timeout = 4000;
    }

    private void startDriver() {
        log.debug("Using '" + getBrowser() + "' driver for test");

        switch (getBrowser()) {
            case CHROME:
                Configuration.browser = "chrome";
                initFullBrowser();
                break;
            case CHROME_HEADLESS:
                Configuration.browser = "chrome";
                initHeadlessBrowser();
                break;
            case CHROME_INCOGNITO:
                Configuration.browser = "chrome";
                initIncognitoBrowser();
                break;
            case FIREFOX:
                Configuration.browser = "firefox";
                break;
            case INTERNET_EXPLORER:
                Configuration.browser = "ie";
                break;
            default:
                throw new InvalidArgumentException("TestManager configure(): invalid browser value");
        }

        currentWebDriver = "main";
        webDrivers.put(currentWebDriver, WebDriverRunner.getWebDriver());
        if (browser.equals(CHROME) || browser.equals(FIREFOX) || browser.equals(INTERNET_EXPLORER))
            maximizeBrowserWindow();
    }

    private void initFullBrowser() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.plugins", 1);
        prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
        prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("PluginsAllowedForUrls", this.getStartUrl());
        options.setExperimentalOption("prefs", prefs);
        WebDriverRunner.setWebDriver(new ChromeDriver(options));
    }

    private void initHeadlessBrowser() {
        Configuration.headless = true;
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("headless", "disable-gpu");
        chromeOptions.addArguments("window-size=1100,2200");
        WebDriverRunner.setWebDriver(new ChromeDriver(chromeOptions));
    }

    private void initIncognitoBrowser() {
        Configuration.headless = false;
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("incognito", "disable-gpu");
        chromeOptions.addArguments("window-size=1100,2200");
        WebDriverRunner.setWebDriver(new ChromeDriver(chromeOptions));
    }

    /**
     * Get main web driver.
     *
     * @return instance of main WebDriver initiated in {@literal startDriver()}
     */
    public WebDriver getDriver() {
        return getWebDriver();
    }

    private void maximizeBrowserWindow() {
        if (!System.getProperty("os.name").contains("Linux"))
            if (WebDriverRunner.getWebDriver().manage().window().getPosition().x >= 0)
                WebDriverRunner.getWebDriver().manage().window().maximize();
    }

    /**
     * Fetch start url.
     *
     * @return url
     */
    public String getStartUrl() {
        return url;
    }

    /**
     * Gets url from the browser's address line.
     *
     * @return the current url
     */
    public String getCurrentUrl() {
        return url();
    }

    public Browser getBrowser() {
        return browser;
    }

}