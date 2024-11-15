package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ui.ServerFacade;
import server.Server;
import model.AuthData;
import com.google.gson.Gson;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        // Start the server
        server = new Server();
        var port = server.run(8080);  // Set to your specific port (3306)
        System.out.println("Started test HTTP server on port " + port);

        // Initialize ServerFacade
        facade = new ServerFacade();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void testRegisterSuccess() {
        // Generate a unique username for testing
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String response = facade.register(uniqueUsername, "password123", uniqueUsername + "@example.com");

        // Assert that the response contains the success message with the username
        Assertions.assertNotNull(response, "Register response should not be null");
        Assertions.assertTrue(response.contains("Registration successful for user: " + uniqueUsername),
                "Register response should indicate success with the correct username");
    }


    @Test
    public void testRegister_Failure() {
        String response = facade.register("", "password123", "newuser@example.com"); // Missing username
        Assertions.assertNotNull(response, "Register response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "Register response should indicate an error for invalid input");
    }

    @Test
    public void testLogin() {
        String response = facade.login("testuser", "password123");
        Assertions.assertNotNull(response, "Login response should not be null");
        Assertions.assertTrue(response.contains("success") || response.contains("token"),
                "Login response should contain a success indication or token");
    }

    @Test
    public void testLogin_Failure() {
        String response = facade.login("nonexistentuser", "wrongpassword");
        Assertions.assertNotNull(response, "Login response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "Login response should indicate an error for invalid credentials");
    }

    @Test
    public void testLogout_Success() {
        facade.register("logoutuser", "password123", "logoutuser@example.com");
        facade.login("logoutuser", "password123");
        String response = facade.logout();
        Assertions.assertNotNull(response, "Logout response should not be null");
        Assertions.assertTrue(response.contains("Logout successful"),
                "Logout response should indicate success");
    }

    @Test
    public void testLogout_Failure() {
        String response = facade.logout(); // No user logged in
        Assertions.assertNotNull(response, "Logout response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "Logout response should indicate an error when no user is logged in");
    }

    @Test
    public void testCreateGame_Success() {
        facade.register("gamecreator", "password123", "gamecreator@example.com");
        facade.login("gamecreator", "password123");
        String response = facade.createGame("Test Game");
        Assertions.assertNotNull(response, "CreateGame response should not be null");
        Assertions.assertTrue(response.contains("Game created successfully"),
                "CreateGame response should indicate success");
    }

    @Test
    public void testCreateGame_Failure() {
        String response = facade.createGame(""); // Missing game name
        Assertions.assertNotNull(response, "CreateGame response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "CreateGame response should indicate an error for invalid input");
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }
}
