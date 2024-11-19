package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.ServerFacade;
import server.Server;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static Gson gson;

    @BeforeAll
    public static void init() {
        // Start the server and get the dynamic URL
        server = new Server();
        String serverUrl = startServer();
        facade = new ServerFacade(serverUrl);
        gson = new Gson();
    }

    private static String startServer() {
        int port = server.run(0); // Let Spark dynamically assign an available port
        System.out.println("Started test HTTP server on port " + port);
        return "http://localhost:" + port; // Construct the URL
    }

    @BeforeEach
    public void clearDatabase() {
        String response = facade.clearDatabase();
        System.out.println("Clear Database Response: " + response);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // Register Tests
    @Test
    public void testRegisterSuccess() {
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        String response = facade.register(uniqueUsername, "password123", uniqueUsername + "@example.com");

        Assertions.assertNotNull(response, "Register response should not be null");
        Assertions.assertTrue(response.contains("Registration successful for user: " + uniqueUsername),
                "Register response should indicate success with the correct username");
    }

    @Test
    public void testRegisterFailure() {
        String response = facade.register("", "password123", "newuser@example.com");
        Assertions.assertNotNull(response, "Register response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "Register response should indicate an error for invalid input");
    }

    // Login Tests
    @Test
    public void testLoginSuccess() {
        String username = "loginuser";
        String password = "password123";
        facade.register(username, password, username + "@example.com");

        String response = facade.login(username, password);

        Assertions.assertNotNull(response, "Login response should not be null");
        Assertions.assertTrue(response.equals("Login successful"),
                "Login response should indicate success");
    }

    @Test
    public void testLoginFailure() {
        String response = facade.login("nonexistentuser", "wrongpassword");
        Assertions.assertNotNull(response, "Login response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "Login response should indicate an error for invalid credentials");
    }

    // Logout Tests
    @Test
    public void testLogoutSuccess() {
        String username = "logoutuser";
        String password = "password123";
        facade.register(username, password, username + "@example.com");
        facade.login(username, password);

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


    // Create Game Tests
    @Test
    public void testCreateGameSuccess() {
        String username = "gamecreator";
        String password = "password123";
        facade.register(username, password, username + "@example.com");
        facade.login(username, password);

        String response = facade.createGame("Test Game");
        Assertions.assertNotNull(response, "CreateGame response should not be null");
        Assertions.assertTrue(response.contains("Game created successfully"),
                "CreateGame response should indicate success");
    }

    @Test
    public void testCreateGameFailure() {
        String response = facade.createGame(""); // Missing game name
        Assertions.assertNotNull(response, "CreateGame response should not be null");
        Assertions.assertTrue(response.contains("Error"),
                "CreateGame response should indicate an error for invalid input");
    }

    // List Games Tests
    @Test
    public void testListGamesSuccess() {
        facade.register("listgamesuser", "password123", "listgamesuser@example.com");
        facade.login("listgamesuser", "password123");
        facade.createGame("Test Game 1");
        facade.createGame("Test Game 2");

        String response = facade.listGames();
        Assertions.assertNotNull(response, "ListGames response should not be null");
        Assertions.assertTrue(response.contains("Game Name Test Game 1"),
                "ListGames response should contain the name of the first game");
        Assertions.assertTrue(response.contains("Game Name Test Game 2"),
                "ListGames response should contain the name of the second game");
    }



    @Test
    public void testListGamesUnauthorized() {
        facade.logout();
        String response = facade.listGames();

        Assertions.assertNotNull(response, "ListGames response should not be null");
        Assertions.assertTrue(response.contains("Error") || response.contains("Unauthorized"),
                "ListGames response should indicate an error if user is not logged in");
    }

    // Play Game Tests
    // Play Game Tests
    @Test
    public void testPlayGameSuccess() {
        String username = "playgameuser" + System.currentTimeMillis();
        String password = "password123";
        String email = username + "@example.com";

        // Register the user
        String registerResponse = facade.register(username, password, email);
        System.out.println("Register Response: " + registerResponse);
        Assertions.assertTrue(registerResponse.contains("Registration successful"),
                "Register response should indicate success");

        // Login the user
        String loginResponse = facade.login(username, password);
        System.out.println("Login Response: " + loginResponse);
        Assertions.assertEquals("Login successful", loginResponse,
                "Login response should indicate success");

        // Create a game
        String gameName = "Test Game";
        String createGameResponse = facade.createGame(gameName);
        System.out.println("CreateGame Response: " + createGameResponse);
        Assertions.assertTrue(createGameResponse.contains("Game created successfully."),
                "CreateGame response should indicate success");

        // Join the game
        String playGameResponse = facade.playGame(gameName, "white");
        System.out.println("PlayGame Response: " + playGameResponse);
        Assertions.assertNotNull(playGameResponse, "PlayGame response should not be null");
        Assertions.assertTrue(playGameResponse.startsWith("Successfully joined the game:"),
                "PlayGame response should indicate success");
    }







    @Test
    public void testPlayGameFailure() {
        facade.login("testuser", "password123");

        // Attempt to play a non-existent game by name
        String response = facade.playGame("NonExistentGame", "blue");
        Assertions.assertNotNull(response, "PlayGame response should not be null");
        Assertions.assertTrue(response.contains("Error") || response.contains("Invalid"),
                "PlayGame response should indicate an error for invalid game name");
    }


    // Observe Game Tests
    @Test
    public void testObserveGameSuccess() {
        String response = facade.observeGame(115);

        Assertions.assertNotNull(response, "ObserveGame response should not be null");
        Assertions.assertTrue(response.contains("Observing game with ID: 115"),
                "ObserveGame response should indicate observing game placeholder");
    }



    @Test
    public void testObserveGameFailure() {
        String response = facade.observeGame(-1);

        Assertions.assertNotNull(response, "ObserveGame response should not be null");
        Assertions.assertTrue(response.contains("Observing game with ID: -1"),
                "ObserveGame response should still include a placeholder message");
    }

}
