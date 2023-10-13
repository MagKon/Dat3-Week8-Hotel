package org.example.controller.impl;

import io.javalin.Javalin;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.example.config.ApplicationConfig;
import org.example.config.HibernateConfig;
import org.example.dto.HotelDto;
import org.example.dto.RoomDto;
import org.example.model.Hotel;
import org.example.model.Role;
import org.example.model.Room;
import org.example.model.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HotelControllerTest2
{
    private static Javalin app;
    private static final String BASE_URL = "http://localhost:7777/api/v1";
    private static HotelController hotelController;
    private static EntityManagerFactory emfTest;
    private static Object adminToken;
    private static Object userToken;
    private static Object managerToken;
    private static Hotel h1, h2;
    private static User user, admin, manager;
    private static Role userRole, adminRole, managerRole;

    @BeforeAll
    static void beforeAll()
    {
        HibernateConfig.setTest(true);
        emfTest = HibernateConfig.getEntityManagerFactory();
        hotelController = new HotelController();
        app = Javalin.create();
        ApplicationConfig.startServer(app, 7777);

        // Create users and roles
        user = new User("usertest", "user123");
        admin = new User("admintest", "admin123");
        manager = new User("managertest", "manager123");

        userRole = new Role("user");
        adminRole = new Role("admin");
        managerRole = new Role("manager");

        user.addRole(userRole);
        admin.addRole(adminRole);
        manager.addRole(managerRole);

        try (var em = emfTest.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(userRole);
            em.persist(adminRole);
            em.persist(managerRole);
            em.persist(user);
            em.persist(admin);
            em.persist(manager);
            em.getTransaction().commit();
        }

        // Get tokens
        UserController userController = new UserController();
        adminToken = getToken(admin.getUsername(), "admin123");
        userToken = getToken(user.getUsername(), "user123");
        managerToken = getToken(manager.getUsername(), "manager123");
    }

    @BeforeEach
    void setUp()
    {
        Set<Room> calRooms = getCalRooms();
        Set<Room> hilRooms = getBatesRooms();

        try (var em = emfTest.createEntityManager())
        {
            em.getTransaction().begin();

            // Delete all rows
            em.createQuery("DELETE FROM Room r").executeUpdate();
            em.createQuery("DELETE FROM Hotel h").executeUpdate();

            // Reset sequence
            em.createNativeQuery("ALTER SEQUENCE room_room_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE hotel_hotel_id_seq RESTART WITH 1").executeUpdate();

            // Insert test data for hotels and rooms
            h1 = new Hotel("Hotel California", "California", Hotel.HotelType.LUXURY);
            h2 = new Hotel("Bates Motel", "Lyngby", Hotel.HotelType.STANDARD);
            h1.setRooms(calRooms);
            h2.setRooms(hilRooms);
            em.persist(h1);
            em.persist(h2);

            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDown()
    {
        HibernateConfig.setTest(false);
        ApplicationConfig.stopServer(app);
    }

    @Test
    void read()
    {
        given()
                .header("Authorization", adminToken)
                .contentType("application/json")
                .when()
                .get(BASE_URL + "/hotels/" + h1.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(h1.getId()));
    }

    @Test
    void readAsManager() {
        given()
                .header("Authorization", managerToken)
                .contentType("application/json")
                .when()
                .get(BASE_URL + "/hotels/" + h1.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200)
                .body("id", equalTo(h1.getId()));
    }

    @Test
    void readAll()
    {
        // Given -> When -> Then
        List<HotelDto> hotelDtoList =
                given()
                        .contentType("application/json")
                        .when()
                        .get(BASE_URL + "/hotels")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.OK_200)  // could also just be 200
                        .extract().body().jsonPath().getList("", HotelDto.class);

        HotelDto h1DTO = new HotelDto(h1);
        HotelDto h2DTO = new HotelDto(h2);

        assertEquals(hotelDtoList.size(), 2);
        assertThat(hotelDtoList, containsInAnyOrder(h1DTO, h2DTO));
    }

    @Test
    void readAllWithLowerRange()
    {
        // Given -> When -> Then
        List<RoomDto> roomDtoList =
                given()
                        .contentType("application/json")
                        .queryParam("range", 3000)
                        .when()
                        .get(BASE_URL + "/rooms")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.OK_200)  // could also just be 200
                        .extract().body().jsonPath().getList("", RoomDto.class);

        assertEquals(roomDtoList.size(), 8);
    }

    @Test
    void readAllWithRange()
    {
        // Given -> When -> Then
        List<RoomDto> roomDtoList =
                given()
                        .contentType("application/json")
                        .queryParam("range", "3000-4000")
                        .when()
                        .get(BASE_URL + "/rooms")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.OK_200)  // could also just be 200
                        .extract().body().jsonPath().getList("", RoomDto.class);

        assertEquals(roomDtoList.size(), 2);
    }

    @Test
    void create()
    {
        Hotel h3 = new Hotel("Cab-inn", "Østergade 2", Hotel.HotelType.BUDGET);
        Room r1 = new Room(117, new BigDecimal(4500), Room.RoomType.SINGLE, 20.0);
        Room r2 = new Room(118, new BigDecimal(2300), Room.RoomType.DOUBLE, 30.0);
        h3.addRoom(r1);
        h3.addRoom(r2);
        HotelDto newHotel = new HotelDto(h3);

        List<RoomDto> roomDtos =
                given()
                        .header("Authorization", adminToken)
                        .contentType(ContentType.JSON)
                        .body(newHotel)
                        .when()
                        .post(BASE_URL + "/hotels")
                        .then()
                        .statusCode(201)
                        .body("id", equalTo(3))
                        .body("hotelName", equalTo("Cab-inn"))
                        .body("hotelAddress", equalTo("Østergade 2"))
                        .body("hotelType", equalTo("BUDGET"))
                        .body("rooms", hasSize(2))
                        .extract().body().jsonPath().getList("rooms", RoomDto.class);

        assertThat(roomDtos, containsInAnyOrder(new RoomDto(r1), new RoomDto(r2)));
    }

    @Test
    void update()
    {
        // Update the Bates Motel to luxury

        HotelDto updateHotel = new HotelDto("Bates Motel", "Lyngby", Hotel.HotelType.LUXURY);
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .body(updateHotel)
                .log().all()
                .when()
                .put(BASE_URL + "/hotels/" + h2.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(h2.getId()))
                .body("hotelName", equalTo("Bates Motel"))
                .body("hotelAddress", equalTo("Lyngby"))
                .body("hotelType", equalTo("LUXURY"))
                .body("rooms", hasSize(6));
    }

    @Test
    void delete()
    {
        // Remove hotel California
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .when()
                .delete(BASE_URL + "/hotels/" + h1.getId())
                .then()
                .statusCode(204);

        // Check that it is gone
        given()
                .header("Authorization", adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get(BASE_URL + "/hotels/" + h1.getId())
                .then()
                .statusCode(404);
    }

    @NotNull
    private static Set<Room> getCalRooms()
    {
        Room r100 = new Room(100, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r101 = new Room(101, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r102 = new Room(102, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r103 = new Room(103, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r104 = new Room(104, new BigDecimal(3200), Room.RoomType.DOUBLE, 30.0);
        Room r105 = new Room(105, new BigDecimal(4500), Room.RoomType.SUITE, 40.0);

        Room[] roomArray = {r100, r101, r102, r103, r104, r105};
        return Set.of(roomArray);
    }

    @NotNull
    private static Set<Room> getBatesRooms()
    {
        Room r111 = new Room(111, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r112 = new Room(112, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r113 = new Room(113, new BigDecimal(2520), Room.RoomType.SINGLE, 20.0);
        Room r114 = new Room(114, new BigDecimal(2520), Room.RoomType.DOUBLE, 30.0);
        Room r115 = new Room(115, new BigDecimal(3200), Room.RoomType.DOUBLE, 30.0);
        Room r116 = new Room(116, new BigDecimal(4500), Room.RoomType.SUITE, 40.0);

        Room[] roomArray = {r111, r112, r113, r114, r115, r116};
        return Set.of(roomArray);
    }

    public static Object getToken(String username, String password)
    {
        return login(username, password);
    }

    private static Object login(String username, String password)
    {
        String json = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

        var token = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("http://localhost:7777/api/v1/auth/login")
                .then()
                .extract()
                .response()
                .body()
                .path("token");

        return "Bearer " + token;
    }


}