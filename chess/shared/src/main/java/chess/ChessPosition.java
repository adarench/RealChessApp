package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int row;
    private int column;
    public ChessPosition(int row, int column) {
        this.row = row;
        this.col = column;
    }
    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }
    public int getColumn(){
        return column;
    }
    public void setRow(){
        this.row = row;
    }
    public void setColumn(){
        this.column = column;
    }
    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
//equals method
    @Override
    public boolean equals(Object o){
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChessPosition that = (ChessPosition) o;
    return row == that.row && column == that.column;
    }

    //hash cases
    @Override
    public int hashCode(){
        return Objects.has(row, column);
    }

}

