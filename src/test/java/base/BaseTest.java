package base;

import core.TestManager;
import core.listeners.ClassListener;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

@Listeners(ClassListener.class)
public abstract class BaseTest {

    protected TestManager manager;

    @BeforeTest(alwaysRun = true)
    public void beforeTest() {
        manager = new TestManager();
        manager.configure();
    }
}
