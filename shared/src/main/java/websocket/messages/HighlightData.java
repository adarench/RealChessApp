package websocket.messages;

import java.util.List;

public class HighlightData {
  private String pieceSquare;
  private List<String> legalMoves;

  // Constructors
  public HighlightData() {}

  public HighlightData(String pieceSquare, List<String> legalMoves) {
    this.pieceSquare = pieceSquare;
    this.legalMoves = legalMoves;
  }

  // Getters and Setters
  public String getPieceSquare() {
    return pieceSquare;
  }

  public void setPieceSquare(String pieceSquare) {
    this.pieceSquare = pieceSquare;
  }

  public List<String> getLegalMoves() {
    return legalMoves;
  }

  public void setLegalMoves(List<String> legalMoves) {
    this.legalMoves = legalMoves;
  }
}
