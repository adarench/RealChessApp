// File: shared/src/main/java/chess/websocket/dto/LegalMovesDTO.java
package websocket.dto;

import chess.ChessPosition;
import java.util.List;

/**
 * Data Transfer Object for sending legal moves to the client.
 */
public class LegalMovesDTO {
  private ChessPosition position; // The position of the selected piece
  private List<ChessPosition> legalMoves; // List of legal move positions

  // Constructor
  public LegalMovesDTO(ChessPosition position, List<ChessPosition> legalMoves) {
    this.position = position;
    this.legalMoves = legalMoves;
  }

  // Getters
  public ChessPosition getPosition() {
    return position;
  }

  public List<ChessPosition> getLegalMoves() {
    return legalMoves;
  }

  // Setters (optional)
  public void setPosition(ChessPosition position) {
    this.position = position;
  }

  public void setLegalMoves(List<ChessPosition> legalMoves) {
    this.legalMoves = legalMoves;
  }
}
