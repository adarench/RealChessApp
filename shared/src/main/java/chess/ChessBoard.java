package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;

    //constructor to initialize chess board
    public ChessBoard() {
        board=new ChessPiece[8][8];
    }

    public ChessBoard deepCopyBoard() {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = this.getPiece(position);
                if (piece != null) {
                    // Create a new instance of ChessPiece
                    ChessPiece newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    newBoard.addPiece(position, newPiece);
                }
            }
        }
        return newBoard;
    }



    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1]=piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i=0; i < 8; i++) {
            for (int j=0; j < 8; j++) {
                board[i][j]=null;
            }
        }
        placeWhitePieces();
        placeBlackPieces();
    }

        private void placeWhitePieces () {

            for (int i=0; i < 8; i++) {
                board[1][i]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            }

            board[0][0]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
            board[0][7]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

            board[0][1]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
            board[0][6]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);

            board[0][2]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
            board[0][5]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);

            board[0][3]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
            board[0][4]=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        }

        private void placeBlackPieces () {
            for (int i=0; i < 8; i++) {
                board[6][i]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            }
            board[7][0]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
            board[7][7]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

            board[7][1]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
            board[7][6]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);

            board[7][2]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
            board[7][5]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);

            board[7][3]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
            board[7][4]=new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = board[i][j];
                sb.append("|");
                if (piece == null) {
                    sb.append(" ");
                } else {
                    if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('p');
                        } else {
                            sb.append('P');
                        }
                    } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('r');
                        } else {
                            sb.append('R');
                        }
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('k');
                        } else {
                            sb.append('K');
                        }
                    } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('q');
                        } else {
                            sb.append('Q');
                        }
                    } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('n');
                        } else {
                            sb.append('N');
                        }
                    } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            sb.append('b');
                        } else {
                            sb.append('B');
                        }
                    }
                }
            }
            sb.append("|\n");
        }
        return sb.toString();


    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessBoard that = (ChessBoard) o;

        // Compare each position on the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece thisPiece = this.board[row][col];
                ChessPiece thatPiece = that.board[row][col];

                if (thisPiece == null && thatPiece == null) {
                    continue; // Both are empty, so move on
                }

                if (thisPiece == null || thatPiece == null) {
                    return false; // One is empty but the other is not
                }

                // Compare both pieces' type and team color
                if (!thisPiece.equals(thatPiece)) {
                    return false; // The pieces are different
                }
            }
        }

        return true;
    }
    @Override
    public int hashCode() {
        int result = 1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                result = 31 * result + (piece != null ? piece.hashCode() : 0);
            }
        }
        return result;
    }


}
