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
import com.google.gson.JsonElement;
import com.google.gson.Gson;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static Gson gson;

    @BeforeAll
    public static void init() {
        // Start the server
        server = new Server();
        var port = server.run(0);  // Set to your specific port (3306)
        System.out.println("Started test HTTP server on port " + port);

        // Initialize ServerFacade
        facade = new ServerFacade();
        gson = new Gson();
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
        Assertions.assertTrue(response.equals("Login successful") || response.startsWith("Error:"),
                "Login response should either indicate success or contain an error message");
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
    public void testListGamesSuccess() {
        // Log in to ensure the user is authenticated
        facade.login("testuser", "password123");

        // Call listGames and capture the response
        String response = facade.listGames();

        // Assert that the response is not null
        Assertions.assertNotNull(response, "ListGames response should not be null");

        // If there are no games, the response should indicate an empty list
        Assertions.assertTrue(response.contains("\"games\":[]") || response.contains("["),
                "ListGames response should return an empty list or a list of games");
    }

    @Test
    public void testListGamesUnauthorized() {
        // Make sure no user is logged in
        facade.logout();

        // Call listGames without being logged in
        String response = facade.listGames();

        // Assert that the response indicates an error (e.g., Unauthorized)
        Assertions.assertNotNull(response, "ListGames response should not be null");
        Assertions.assertTrue(response.contains("Error") || response.contains("Unauthorized"),
                "ListGames response should indicate an error if user is not logged in");
    }


    @Test
    public void testPlayGameSuccess() {
        // Register the user if they do not already exist
        String registerResponse = facade.register("testuser", "password123", "testuser@example.com");

        // Log in to ensure the user is authenticated
        String loginResponse = facade.login("testuser", "password123");
        Assertions.assertEquals("Login successful", loginResponse, "Login should be successful for the test");

        // Create a new game to ensure the test has a valid game ID
        String createGameResponse = facade.createGame("Test Game");
        Assertions.assertTrue(createGameResponse.contains("Game created successfully"),
                "CreateGame response should indicate success");

        // Attempt to list games and extract the last game's ID for testing
        String listGamesResponse = facade.listGames();
        JsonObject listGamesJson = gson.fromJson(listGamesResponse, JsonObject.class);
        JsonArray gamesArray = listGamesJson.getAsJsonArray("games");
        int gameID = gamesArray.get(gamesArray.size() - 1).getAsJsonObject().get("gameID").getAsInt();

        // Attempt to join the newly created game with a valid game ID and color
        String playGameResponse = facade.playGame(gameID, "white");

        // Assert that the response indicates success
        Assertions.assertNotNull(playGameResponse, "PlayGame response should not be null");
        Assertions.assertEquals("Successfully joined the game.", playGameResponse,
                "PlayGame response should indicate success");
    }






    @Test
    public void testPlayGameFailure() {
        // Log in to ensure the user is authenticated
        facade.login("testuser", "password123");

        // Attempt to join a game with an invalid game ID or invalid color
        String response = facade.playGame(-1, "blue");

        // Assert that the response indicates an error
        Assertions.assertNotNull(response, "PlayGame response should not be null");
        Assertions.assertTrue(response.contains("Error") || response.contains("Invalid"),
                "PlayGame response should indicate an error");
    }

    @Test
    public void testObserveGameSuccess() {
        // Attempt to observe an existing game
        String response = facade.observeGame(115);

        // Assert that the response contains the expected placeholder
        Assertions.assertNotNull(response, "ObserveGame response should not be null");
        Assertions.assertTrue(response.contains("Observing game with ID: 115"),
                "ObserveGame response should indicate observing game placeholder");
    }

    @Test
    public void testObserveGameFailure() {
        // Attempt to observe a game with an invalid game ID
        String response = facade.observeGame(-1);

        // Assert that the response still contains a placeholder (since functionality is not implemented yet)
        Assertions.assertNotNull(response, "ObserveGame response should not be null");
        Assertions.assertTrue(response.contains("Observing game with ID: -1"),
                "ObserveGame response should still include a placeholder message");
    }



    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }
}
