package ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

import model.AuthData;

public class ServerFacade {

  private static final String SERVER_URL = "http://localhost:8080";
  private String authToken = null; // Store auth token after login
  private final Gson gson = new Gson();

  // Registers a new user
  public String register(String username, String password, String email) {
    String jsonInputString = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\"}",
            username, password, email);
    String response = sendPostRequest("/user", jsonInputString);

    if (response.startsWith("Error:")) {
      return response; // Return the error if registration failed
    }

    // Parse the JSON response to confirm registration success
    try {
      AuthData authData = gson.fromJson(response, AuthData.class);
      return "Registration successful for user: " + authData.username();
    } catch (Exception e) {
      e.printStackTrace();
      return "Error parsing registration data";
    }
  }

  // Logs in an existing user and stores the auth token on success
  public String login(String username, String password) {
    String jsonInputString = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
    String response = sendPostRequest("/session", jsonInputString);

    if (response.startsWith("Error:")) {
      return response; // Return the error if login failed
    }

    // Parse the JSON response to get the auth token
    try {
      AuthData authData = gson.fromJson(response, AuthData.class);
      this.authToken = authData.authToken();
      return "Login successful";
    } catch (Exception e) {
      return "Error parsing auth data";
    }
  }

  // Logs out the user using the stored auth token
  public String logout() {
    if (authToken == null) {
      return "Error: No user is logged in";
    }

    try {
      URL url = new URL(SERVER_URL + "/session");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("DELETE");
      connection.setRequestProperty("Authorization", authToken);
      connection.setRequestProperty("Content-Type", "application/json");

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        authToken = null; // Clear auth token on successful logout
        return "Logout successful";
      } else {
        return "Error: Server returned HTTP code " + responseCode;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }


  public String createGame(String gameName) {
    String jsonInputString = String.format("{\"gameName\":\"%s\"}", gameName);


    try {
      URL url = new URL(SERVER_URL + "/game");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", authToken);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return "Game created successfully.";
      } else {
        return "Error: Unable to create game. Server returned HTTP code " + responseCode;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }
  public String listGames() {
    try {
      URL url = new URL(SERVER_URL + "/game");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", authToken);
      connection.setRequestProperty("Content-Type", "application/json");

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = in.readLine()) != null) {
          response.append(responseLine.trim());
        }
        in.close();
        return response.toString(); // Return the list of games
      } else {
        return "Error: Unable to fetch games. Server returned HTTP code " + responseCode;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }

  public String playGame(int gameID, String playerColor) {
    String jsonInputString = String.format("{\"gameID\":%d,\"playerColor\":\"%s\"}", gameID, playerColor);

    try {
      URL url = new URL(SERVER_URL + "/game");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Authorization", authToken);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return "Successfully joined the game.";
      } else {
        return "Error: Unable to join the game. Server returned HTTP code " + responseCode;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }
  public String observeGame(int gameID) {
    // Placeholder: You can expand this later when observing is fully implemented
    return "Observing game with ID: " + gameID + " (not yet implemented).";
  }




  // Helper method to send a POST request
  private String sendPostRequest(String endpoint, String jsonInputString) {
    try {
      URL url = new URL(SERVER_URL + endpoint);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      // Send JSON request body
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = in.readLine()) != null) {
          response.append(responseLine.trim());
        }
        in.close();
        return response.toString(); // Return successful response
      } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
        return "Error: Missing or invalid fields";
      } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
        return "Error: Invalid username or password";
      } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
        return "Error: Username already exists";
      } else {
        return "Error: Server returned HTTP code " + responseCode;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }

}
