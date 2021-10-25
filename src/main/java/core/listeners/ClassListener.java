package core.listeners;

import com.codeborne.selenide.WebDriverRunner;
import core.TestManager;
import org.apache.logging.log4j.LogManager;
import org.testng.*;
import ru.yandex.qatools.allure.annotations.Attachment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import static com.codeborne.selenide.Selenide.screenshot;

public class ClassListener implements ITestListener {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(ClassListener.class);

    @Override
    public void onTestStart(ITestResult testResult) {
        printHeader("Starting test " + testResult.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        if (org.testng.Reporter.getOutput(testResult).size() == 0) log.warn("The test has succeeded but no output was generated. Please make sure the test outputs some results also when successful.");
        printHeader("Test completed " + testResult.getMethod().getQualifiedName());
        System.out.println();
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        if (WebDriverRunner.hasWebDriverStarted()) takeScreenshot(testResult);
        printHeader("Test failed " + testResult.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        printHeader("Test skipped " + testResult.getMethod().getQualifiedName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
        printHeader("Test completed within % " + testResult.getMethod().getQualifiedName());
    }

    @Override
    public void onStart(ITestContext testContext) {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j2.xml");
    }

    @Override
    public void onFinish(ITestContext testContext) {
        closeAllWebDrivers();
    }

    void closeAllWebDrivers() {
        if (WebDriverRunner.hasWebDriverStarted()) {
            TestManager.getActualInstance().getDriver().close();
            WebDriverRunner.closeWebDriver();
        }
    }

    @Attachment(value = "Screenshot of {0}", type = "image/png")
    private byte[] takeScreenshot(ITestResult result) {
        try {
            String screenshotName = result.getTestContext().getCurrentXmlTest().getName().replaceAll("[^A-Za-z0-9]", "_") + "_" + String.valueOf(System.currentTimeMillis());
            String absPath = screenshot(screenshotName);
            String src = "screenshots/" + screenshotName + ".png";
            // print html to the report only, not to console
            org.testng.Reporter.log("<b>Screenshot</b><p><a target=\"_blank\" href='" + src + "'><img width=500 src='" + src + "' border=1></a></p>");
            return Files.readAllBytes(Paths.get(absPath));
        } catch (Exception e) {
            log.error("Could not take screenshot");
        }
        return null;
    }

    public static void printHeader(String message) {

        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_HEADING = "\u001B[1;47m";
        final String ANSI_BLACK = "\u001B[30m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_PURPLE = "\u001B[35m";
        final String ANSI_CYAN = "\u001B[36m";
        final String ANSI_WHITE = "\u001B[37m";
        final String ANSI_GREY = "\u001B[37m";

        StringBuffer consoleMessage = new StringBuffer();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.mmm");
        consoleMessage.append(dateFormat.format(System.currentTimeMillis())).append(" ");

        // console out
        System.out.println(consoleMessage.append(ANSI_HEADING).append(padMessageToPrettyLength(message)).append(ANSI_RESET));

        // no reportNG out
    }

    private static String padMessageToPrettyLength(String message) {
        final short prettySize = 110;
        int padding = (int) Math.floor((prettySize - message.length()) / 2);
        StringBuffer outputString = new StringBuffer();
        for (int i = 1; i <= padding; i++) {
            outputString.append(" ");
        }
        outputString.append(message);
        for (int i = 1; i <= padding; i++) {
            outputString.append(" ");
        }
        if (outputString.length() < prettySize) outputString.append(" ");
        return outputString.toString();
    }
}
