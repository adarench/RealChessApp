
package passoff.service;
import service.GameService;
import model.GameData;
import model.AuthData;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

public class GameServiceTest {

  private GameService gameService;
  private GameDAO mockGameDAO;
  private AuthDAO mockAuthDAO;

  @BeforeEach
  public void setUp() {
    mockGameDAO = mock(GameDAO.class);
    mockAuthDAO = mock(AuthDAO.class);
    gameService = new GameService(mockGameDAO, mockAuthDAO);
  }

  // Positive test case for createGame
  @Test
  public void testCreateGameSuccess() throws DataAccessException {
    // Mock behavior
    AuthData auth = new AuthData("token123", "username");
    when(mockAuthDAO.getAuth("token123")).thenReturn(auth);
    GameData game = new GameData(1, null, null, "Chess");
    when(mockGameDAO.createGame("Chess")).thenReturn(game);

    // Call the service
    GameData createdGame = gameService.createGame("token123", "Chess");

    // Assert the result
    assertNotNull(createdGame);
    assertEquals("Chess", createdGame.gameName());
    verify(mockGameDAO, times(1)).createGame("Chess");
  }

  // Negative test case for createGame
  @Test
  public void testCreateGameUnauthorized() {
    // Mock behavior for an invalid token
    when(mockAuthDAO.getAuth("invalid_token")).thenReturn(null);

    // Call the service and expect an exception
    assertThrows(DataAccessException.class, () -> {
      gameService.createGame("invalid_token", "Chess");
    });
  }

  // Positive test case for listGames
  @Test
  public void testListGamesSuccess() throws DataAccessException {
    // Mock behavior
    AuthData auth = new AuthData("token123", "username");
    when(mockAuthDAO.getAuth("token123")).thenReturn(auth);

    List<GameData> games = new ArrayList<>();
    games.add(new GameData(1, "player1", "player2", "Game1"));
    when(mockGameDAO.listGames()).thenReturn(games);

    // Call the service
    List<GameData> listedGames = gameService.listGames("token123");

    // Assert the result
    assertNotNull(listedGames);
    assertEquals(1, listedGames.size());
    verify(mockGameDAO, times(1)).listGames();
  }

  // Negative test case for listGames
  @Test
  public void testListGamesUnauthorized() {
    // Mock behavior for an invalid token
    when(mockAuthDAO.getAuth("invalid_token")).thenReturn(null);

    // Call the service and expect an exception
    assertThrows(DataAccessException.class, () -> {
      gameService.listGames("invalid_token");
    });
  }

  // Positive test case for joinGame
  @Test
  public void testJoinGameSuccess() throws DataAccessException {
    // Mock behavior
    AuthData auth = new AuthData("token123", "username");
    when(mockAuthDAO.getAuth("token123")).thenReturn(auth);

    GameData game = new GameData(1, null, null, "Chess");
    when(mockGameDAO.getGame(1)).thenReturn(game);

    // Call the service
    gameService.joinGame("token123", 1, "WHITE");

    // Verify the game update
    verify(mockGameDAO, times(1)).updateGame(any(GameData.class));
  }

  // Negative test case for joinGame (invalid player color)
  @Test
  public void testJoinGameInvalidColor() {
    // Mock behavior
    AuthData auth = new AuthData("token123", "username");
    when(mockAuthDAO.getAuth("token123")).thenReturn(auth);

    assertThrows(DataAccessException.class, () -> {
      gameService.joinGame("token123", 1, "RED");
    });
  }
}

