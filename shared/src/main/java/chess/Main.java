package chess;

public class Main {
  public static void main(String[] args) {
    // Create a new game
    ChessGame game = new ChessGame();

    System.out.println("Starting Board:");
    System.out.println(game.getBoard().toString());

    // Example move: White pawn from (1, 0) to (3, 0)
    try {
      ChessPosition startPosition = new ChessPosition(1, 0); // starting position of white pawn
      ChessPosition endPosition = new ChessPosition(3, 0); // move it two squares forward
      ChessMove move = new ChessMove(startPosition, endPosition, null); // No promotion

      // Make the move
      game.makeMove(move);
      System.out.println("After Move:");
      System.out.println(game.getBoard().toString());

    } catch (InvalidMoveException e) {
      System.out.println(e.getMessage());
    }

    boolean whiteInCheck = game.isInCheck(ChessGame.TeamColor.WHITE);
    System.out.println("Is White in check? " + whiteInCheck);

    boolean blackInCheckmate = game.isInCheckmate(ChessGame.TeamColor.BLACK);
    System.out.println("Is Black in checkmate? " + blackInCheckmate);

  }
}
