package pages.base;

import core.TestManager;
import org.apache.logging.log4j.LogManager;

public abstract class BasePage {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(BasePage.class);

    public TestManager manager = TestManager.getActualInstance();

    /**
     * Open by direct url
     * @return Page object
     */
    public abstract BasePage openUrl();

}
