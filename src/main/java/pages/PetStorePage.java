package pages;

import data.DataUtils;
import data.pojo.Category;
import data.pojo.Pet;
import data.pojo.Tag;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.testng.Assert;
import pages.base.BasePage;

import java.util.Arrays;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;

public class PetStorePage extends BasePage {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(PetStorePage.class);

    private final String url = manager.getStartUrl();

    private int id;
    private int invalid_id;
    private String photoURL;
    private String name;
    private String new_name;
    private String pet_category;
    private String pet_tag;

    private Pet pet = new Pet();

    protected static final SelenideElement ADD_NEW_PET_FIELD = $x("//*[@id='operations-pet-addPet']");
    protected static final SelenideElement TRY_IT_OUT_BUTTON = $x("//button[@class='btn try-out__btn']");
    protected static final SelenideElement EXECUTE_BUTTON = $x("//button[@class='btn execute opblock-control__btn']");
    protected static final SelenideElement TEST_PARAMETERS = $x("//textarea[@class='body-param__text']");

    public PetStorePage() {
        id = Integer.parseInt(RandomStringUtils.randomNumeric(5));
        invalid_id = new DataUtils().getInvalidID();
        photoURL = new DataUtils().getPhotoURL();
        name = new DataUtils().getName();
        new_name = new DataUtils().getNewName();
        pet_category = new DataUtils().getCategory();
        pet_tag = new DataUtils().getTag();
        RestAssured.baseURI = manager.getStartUrl() + "/v2";
    }

    @Override
    public PetStorePage openUrl() {
        log.info("Open pet store page" + url);
        Selenide.open(url);
        return this;
    }

    @Step("Open 'Add new pet to the store' menu")
    public PetStorePage openMenu() {
        log.info("Open 'Add new pet to the store' menu");
        ADD_NEW_PET_FIELD.click();
        ADD_NEW_PET_FIELD.scrollIntoView(true);
        return this;
    }

    @Step("Click 'Try it out' button")
    public PetStorePage clickTryItOutButton() {
        log.info("Click 'Try it out' button");
        TRY_IT_OUT_BUTTON.click();
        return this;
    }

    @Step("Click 'Execute' button")
    public PetStorePage clickExecuteButton() {
        log.info("Click 'Execute' button");
        EXECUTE_BUTTON.click();
        EXECUTE_BUTTON.scrollIntoView(true);
        return this;
    }

    @Step("Click on test parameters area")
    public PetStorePage clearOnTestParametersArea() {
        log.info("Click on test parameters area");
        TEST_PARAMETERS.click();
        TEST_PARAMETERS.setValue("");
        return this;
    }

    @Step("Check that 'Add new pet to the store' element is displayed")
    public PetStorePage checkAddNewPetElement() {
        log.info("Check that 'Add new pet to the store' element is displayed");
        ADD_NEW_PET_FIELD.shouldBe(Condition.visible);
        Assert.assertTrue(ADD_NEW_PET_FIELD.isDisplayed(), " Add new pet to the store element is displayed");
        return this;
    }

    @Step("Check that 'Try it out' button is displayed")
    public PetStorePage checkTryItOutButton() {
        log.info("Check that 'Try it out' button is displayed");
        Assert.assertTrue(TRY_IT_OUT_BUTTON.isDisplayed(), " 'Try it out' button is is displayed");
        return this;
    }

    @Step("Check that test parameters area is displayed")
    public PetStorePage checkTestParametersArea() {
        log.info("Check that test parameters area is displayed");
        Assert.assertTrue(TEST_PARAMETERS.isDisplayed(), " Test parameters area is displayed");
        return this;
    }

    public PetStorePage checkId() {
        checkNewIDExist();
        return this;
    }


    public PetStorePage addTestData() {
        checkAddNewPetElement();
        openMenu();
        checkTryItOutButton();
        clickTryItOutButton();
        checkTestParametersArea();
        clearOnTestParametersArea();
        setNewPetCorrectData();
        clickExecuteButton();
        return this;
    }

    @Step("Set correct data")
    public PetStorePage setNewPetCorrectData() {
        Category category = new Category();
        category.setId(id);
        category.setName(pet_category);

        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(pet_tag);

        pet.setId(id);
        pet.setName(name);
        pet.setStatus("available");
        pet.setCategory(category);
        pet.setTags(Arrays.asList(tag));
        pet.setPhotoUrls(Arrays.asList(photoURL));

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(pet);

        TEST_PARAMETERS.setValue(json);
        return this;
    }

    @Step("Check that new pet ID created")
    public PetStorePage checkNewIDExist() {
        get(baseURI + "/pet/" + pet.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .body()
                .body("id", equalTo(pet.getId()))
                .body("name", equalToCompressingWhiteSpace(pet.getName()))
                .body("status", equalToCompressingWhiteSpace(pet.getStatus()))
                .body("category.id",equalTo(pet.getCategory().getId()))
                .body("category.name",equalTo(pet.getCategory().getName()))
                .body("tags[0].id",equalTo(pet.getTags().get(0).getId()))
                .body("tags[0].name",equalTo(pet.getTags().get(0).getName()))
                .body("photoUrls[0]", containsStringIgnoringCase(photoURL));

        return this;
    }

    @Step("Update pet data")
    public PetStorePage updatePetData() {
        RestAssured.given()
                .when()
                .contentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .formParam("name", new_name)
                .formParam("status", "pending")
                .post("/pet/" + id)
                .then()
                .log()
                .all()
                .assertThat()
                .statusCode(200);
        return this;
    }

    @Step("Check updated data")
    public PetStorePage checkUpdatedPetData() {
        JsonPath jsonPath = RestAssured.given()
                .when()
                .get("/pet/" + id)
                .then()
                .assertThat()
                .statusCode(200)
                .assertThat()
                .extract().body().jsonPath();

        assertEquals(jsonPath.get("name"), new_name);
        assertEquals(jsonPath.get("status"), "pending");

        return this;
    }

    @Step("Delete pet's data from store")
    public PetStorePage deletePetDataFromStore() {
        RestAssured.given()
                .when()
                .header("api_key", "special-key")
                .delete("/pet/" + id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().body().jsonPath();

        return this;
    }

    @Step("Delete non-existent pet data")
    public PetStorePage deleteNonExistentPetData() {
        RestAssured.given()
                .when()
                .header("api_key", "special-key")
                .delete("/pet/" + id)
                .then()
                .assertThat()
                .statusCode(404);
        return this;
    }

    @Step("Check invalid ID not exist")
    public PetStorePage checkInvalidPetsID() {
        Response response =
                get("/pet/" + invalid_id);
        assertEquals(response.getStatusCode(), 200);
        return this;
    }
}
