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
    public void sampleTest() {
        Assertions.assertTrue(true);
    }
}
