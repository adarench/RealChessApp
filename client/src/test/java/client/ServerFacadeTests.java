package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ui.ServerFacade;
import server.Server;
import model.AuthData;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static Gson gson;

    @BeforeAll
    public static void init() {
        // Start the server
        server = new Server();
        var port = server.run(8080); // Let Spark choose an available port
        System.out.println("Started test HTTP server on port " + port);

        // Initialize ServerFacade with the dynamic server URL
        String serverUrl = "http://localhost:" + port;
        facade = new ServerFacade();
        gson = new Gson();
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
        facade.createGame("Test Game");

        String response = facade.listGames();
        Assertions.assertNotNull(response, "ListGames response should not be null");
        Assertions.assertTrue(response.contains("\"games\":"),
                "ListGames response should contain a list of games");
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
    @Test
    public void testPlayGameSuccess() {
        String username = "playgameuser";
        String password = "password123";
        facade.register(username, password, username + "@example.com");
        facade.login(username, password);

        String createGameResponse = facade.createGame("Test Game");
        Assertions.assertTrue(createGameResponse.contains("Game created successfully"),
                "CreateGame response should indicate success");

        String listGamesResponse = facade.listGames();
        JsonObject listGamesJson = gson.fromJson(listGamesResponse, JsonObject.class);
        JsonArray gamesArray = listGamesJson.getAsJsonArray("games");
        int gameID = gamesArray.get(gamesArray.size() - 1).getAsJsonObject().get("gameID").getAsInt();

        String playGameResponse = facade.playGame(gameID, "white");
        Assertions.assertNotNull(playGameResponse, "PlayGame response should not be null");
        Assertions.assertEquals("Successfully joined the game.", playGameResponse,
                "PlayGame response should indicate success");
    }

    @Test
    public void testPlayGameFailure() {
        facade.login("testuser", "password123");

        String response = facade.playGame(-1, "blue");
        Assertions.assertNotNull(response, "PlayGame response should not be null");
        Assertions.assertTrue(response.contains("Error") || response.contains("Invalid"),
                "PlayGame response should indicate an error");
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
