package Tests;

import base.BaseTest;
import core.listeners.ClassListener;
import org.testng.annotations.*;
import pages.PetStorePage;


@Listeners(ClassListener.class)
public class PetStoreSwagger_Test extends BaseTest {

    private PetStorePage petStorePage;

    @BeforeTest
    public void initStringsAndObjects() {
        petStorePage = new PetStorePage();
    }

    @Test(priority = 1)
    public void addNewPetTest() throws InterruptedException {
        petStorePage
                .openUrl()
                .addTestData()
                .checkId();
    }

    @Test(priority = 2)
    public void petDataUpdate() {
        petStorePage.updatePetData();
    }

    @Test(priority = 3)
    public void checkPetUpdatedData() {
        petStorePage.checkUpdatedPetData();
    }

    @Test(priority = 4)
    public void deletePetData() {
        petStorePage.deletePetDataFromStore();
    }

    @Test(priority = 5)
    public void deleteNonExistentPet() {
        petStorePage.deleteNonExistentPetData();
    }

    @Test(priority = 6)
    public void checkInvalidPID() {
        petStorePage.checkInvalidPetsID();
    }
}
